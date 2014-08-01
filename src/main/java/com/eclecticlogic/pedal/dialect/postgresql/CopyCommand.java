/**
 * Copyright (c) 2014 Eclectic Logic LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.eclecticlogic.pedal.dialect.postgresql;

import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclecticlogic.pedal.provider.ConnectionAccessor;
import com.eclecticlogic.pedal.spi.ProviderAccessSpi;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * Limitation in current implementation:
 * 1. @Column annotation must be present on getters
 * 2. @Column annotation should have column name in it.
 * 3. @Convert annotation should be on getter
 * 4. Array types can only be arrays of primitives. Bit arrays are supported. Annotate with @BitString 
 * 5. No embedded id support in entity or fk in entity.
 * 6. No support for custom types.
 * 7. No specific distinction between Temporal TIMESTAMP and DATE.
 * @author kabram.
 *
 */
public class CopyCommand {

    private ConnectionAccessor connectionAccessor;
    private ProviderAccessSpi providerAccessSpi;

    private ConcurrentHashMap<Class<? extends Serializable>, String> fieldNamesByClass = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Class<? extends Serializable>, CopyExtractor<? extends Serializable>> extractorsByClass = new ConcurrentHashMap<>();

    private static Logger logger = LoggerFactory.getLogger(CopyCommand.class);


    public void setConnectionAccessor(ConnectionAccessor connectionAccessor) {
        this.connectionAccessor = connectionAccessor;
    }


    public void setProviderAccessSpi(ProviderAccessSpi providerAccessSpi) {
        this.providerAccessSpi = providerAccessSpi;
    }


    /**
     * @param lists Entities to be inserted using the Postgres COPY command.
     */
    public <E extends Serializable> void insert(EntityManager entityManager, CopyList<E> entityList) {
        if (entityList.size() > 0) {
            _insert(entityManager, entityList);
        }
    }


    @SuppressWarnings("unchecked")
    private <E extends Serializable> void _insert(EntityManager entityManager, CopyList<E> entityList) {
        Class<? extends Serializable> clz = entityList.get(0).getClass();
        setupFor(clz);
        String fieldNames = fieldNamesByClass.get(clz);
        CopyExtractor<E> extractor = (CopyExtractor<E>) extractorsByClass.get(clz);
        StringBuilder builder = new StringBuilder(1024 * entityList.size());
        for (E entity : entityList) {
            builder.append(extractor.getValueList(entity));
            builder.append("\n");
        }

        StringReader reader = new StringReader(builder.toString());
        providerAccessSpi.run(
                entityManager,
                connection -> {
                    try {
                        CopyManager copyManager = new CopyManager((BaseConnection) connectionAccessor
                                .getRawConnection(connection));
                        long t1 = System.currentTimeMillis();
                        copyManager.copyIn("copy " + getEntityName(entityList) + "(" + fieldNames + ") from stdin",
                                reader);
                        long elapsedTime = System.currentTimeMillis() - t1;
                        logger.debug("Wrote {} inserts in {} seconds", entityList.size(),
                                Math.round(elapsedTime / 10.0) / 100.0);
                    } catch (Exception e) {
                        logger.trace("Command passed: copy {} ( {} ) from stdin {}", getEntityName(entityList),
                                fieldNames, builder);
                        throw Throwables.propagate(e);
                    }
                });
    }


    private void setupFor(Class<? extends Serializable> clz) {
        if (fieldNamesByClass.get(clz) == null) {
            List<String> fields = new ArrayList<>();
            List<Method> methods = new ArrayList<>();
            for (Method method : clz.getMethods()) {
                String columnName = null;
                if (method.isAnnotationPresent(Id.class) && method.isAnnotationPresent(GeneratedValue.class)
                        && method.getAnnotation(GeneratedValue.class).strategy() == GenerationType.IDENTITY) {
                    // Ignore pk with identity strategy.
                } else if (method.isAnnotationPresent(Column.class)) {
                    columnName = method.getAnnotation(Column.class).name();
                } else if (method.isAnnotationPresent(JoinColumn.class)
                        && method.getAnnotation(JoinColumn.class).insertable()) {
                    columnName = method.getAnnotation(JoinColumn.class).name();
                }
                if (columnName != null) {
                    // Certain one-to-on join situations can lead to multiple columns with the same column-name.
                    if (!fields.contains(columnName)) {
                        fields.add(columnName);
                        methods.add(method);
                    }
                } // end if annotation present
            }
            extractorsByClass.put(clz, getExtractor(clz, methods));
            fieldNamesByClass.put(clz, String.join(",", fields));
        }
    }


    /**
     * Revisit this after javassist support java 8
     */
    @SuppressWarnings({ "unchecked" })
    private <E extends Serializable> CopyExtractor<E> getExtractor(Class<E> clz, List<Method> fieldMethods) {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.makeClass("com.eclecticlogic.pedal.dialect.postgresql." + clz.getSimpleName()
                + "$CopyExtractor");

        StringBuilder methodBody = new StringBuilder();
        try {
            cc.addInterface(pool.getCtClass(CopyExtractor.class.getName()));

            methodBody.append("public String getValueList(Object entity) {\n");
            methodBody.append("try {\n");
            methodBody.append("StringBuilder builder = new StringBuilder();\n");
            methodBody.append(clz.getName() + " typed = (" + clz.getName() + ")entity;\n");
            for (int i = 0; i < fieldMethods.size(); i++) {
                Method method = fieldMethods.get(i);
                if (method.getReturnType().isPrimitive()) {
                    methodBody.append("builder.append(typed." + method.getName() + "());\n");
                } else {
                    methodBody.append(method.getReturnType().getName() + " v" + i + " = typed." + method.getName()
                            + "();\n");
                    methodBody.append("if (v" + i + " == null) {builder.append(\"\\\\N\");}\n");
                    methodBody.append("else {\n");

                    if (method.isAnnotationPresent(BitString.class)) {
                        methodBody.append("java.util.Iterator it" + i + " = typed." + method.getName()
                                + "().iterator();\n");
                        methodBody.append("while (it" + i + ".hasNext()) {\n");
                        methodBody.append("Boolean b = (Boolean)it" + i + ".next();\n");
                        methodBody.append("builder.append(b.booleanValue() ? \"0\" : \"1\");\n");
                        methodBody.append("}\n");
                    } else if (Collection.class.isAssignableFrom(method.getReturnType())
                            && method.isAnnotationPresent(Convert.class) == false) {
                        // Postgreql array type.
                        methodBody.append("java.util.Iterator it" + i + " = typed." + method.getName()
                                + "().iterator();\n");
                        methodBody.append("StringBuilder array" + i + " = new StringBuilder();\n");
                        methodBody.append("while (it" + i + ".hasNext()) {\n");
                        methodBody.append("Object o = it" + i + ".next();\n");
                        methodBody.append("array" + i + ".append(\",\").append(o);\n");
                        methodBody.append("}\n");
                        methodBody.append("String arrayStr" + i + " = array" + i + ".length() == 0 ? \"\" : array" + i
                                + ".substring(1);\n");
                        methodBody.append("builder.append(\"{\").append(arrayStr" + i + ").append(\"}\");\n");
                    } else if (method.isAnnotationPresent(Convert.class)) {
                        Class<?> converterClass = method.getAnnotation(Convert.class).converter();
                        methodBody.append(converterClass.getName() + " c" + i + " = (" + converterClass.getName() + ")"
                                + converterClass.getName() + ".class.newInstance();\n");
                        methodBody.append("builder.append(c" + i + ".convertToDatabaseColumn(v" + i + "));\n");
                    } else if (method.isAnnotationPresent(JoinColumn.class)) {
                        // We need to get the id of the joined object.
                        for (Method method2 : method.getReturnType().getMethods()) {
                            if (method2.isAnnotationPresent(Id.class)) {
                                methodBody.append("builder.append(v" + i + "." + method2.getName() + "());\n");
                            }
                        }
                    } else {
                        methodBody.append("builder.append(v" + i + ");\n");
                    }
                    methodBody.append("}\n");
                }
                if (i != fieldMethods.size() - 1) {
                    methodBody.append("builder.append(\"\\t\");\n");
                }
            }
            methodBody.append("return builder.toString();\n");
            methodBody.append("} catch (Exception e) { throw new RuntimeException(e); } \n");
            methodBody.append("}\n");
            logger.trace(methodBody.toString());
            cc.addMethod(CtNewMethod.make(methodBody.toString(), cc));
        } catch (NotFoundException | CannotCompileException e) {
            logger.error(e.getMessage(), "Compiled body is:\n" + methodBody.toString());
            throw Throwables.propagate(e);
        }

        try {
            return (CopyExtractor<E>) cc.toClass().newInstance();
        } catch (InstantiationException | IllegalAccessException | CannotCompileException e) {
            throw Throwables.propagate(e);
        }
    }


    private <E extends Serializable> String getEntityName(CopyList<E> copyList) {
        if (Strings.isNullOrEmpty(copyList.getAlternateTableName())) {
            return providerAccessSpi.getTableName(copyList.get(0).getClass());
        } else {
            String schemaName = providerAccessSpi.getSchemaName();
            if (Strings.isNullOrEmpty(schemaName)) {
                return copyList.getAlternateTableName();
            } else {
                return schemaName + "." + copyList.getAlternateTableName();
            }
        }
    }

}
