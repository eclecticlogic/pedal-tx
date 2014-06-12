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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import javax.persistence.AttributeConverter;
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
 * 4. No embedded id support in entity or fk in entity.
 * 5. No support for custom types (array, etc.)
 * 6. No specific distinction between Temporal TIMESTAMP and DATE.
 * @author kabram.
 *
 */
public class CopyCommand {

    private ConnectionAccessor connectionAccessor;
    private ProviderAccessSpi providerAccessSpi;

    private ConcurrentHashMap<Class<? extends Serializable>, String> fieldNamesByClass = new ConcurrentHashMap<>();
    @SuppressWarnings("unused")
    private ConcurrentHashMap<Class<? extends Serializable>, CopyExtractor<? extends Serializable>> extractorsByClass = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Class<? extends Serializable>, List<MethodHandle>> methodHandlesByClass = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Class<? extends Serializable>, List<Method>> methodsByClass = new ConcurrentHashMap<>();

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


    private <E extends Serializable> void _insert(EntityManager entityManager, CopyList<E> entityList) {
        Class<? extends Serializable> clz = entityList.get(0).getClass();
        setupFor(clz);
        String fieldNames = fieldNamesByClass.get(clz);
        // CopyExtractor<E> extractor = (CopyExtractor<E>) extractorsByClass.get(clz);
        StringBuilder builder = new StringBuilder(1024 * entityList.size());
        for (E entity : entityList) {
            builder.append(getValueList(methodHandlesByClass.get(clz), methodsByClass.get(clz), entity));
            builder.append("\n");
        }

        StringReader reader = new StringReader(builder.toString());
        providerAccessSpi.run(
                entityManager,
                connection -> {
                    try {
                        CopyManager copyManager = new CopyManager((BaseConnection) connectionAccessor
                                .getRawConnection(connection));
                        copyManager.copyIn("copy " + getEntityName(entityList) + "(" + fieldNames + ") from stdin",
                                reader);
                    } catch (Exception e) {
                        logger.trace("Command passed: copy {} ( {} ) from stdin {}", getEntityName(entityList),
                                fieldNames, builder);
                        throw Throwables.propagate(e);
                    }
                });
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <E extends Serializable> String getValueList(List<MethodHandle> methodHandles, List<Method> methods,
            E entity) {
        try {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < methods.size(); i++) {
                Method method = methods.get(i);
                Object value = methodHandles.get(i).invoke(entity);

                if (method.isAnnotationPresent(Id.class) && method.isAnnotationPresent(GeneratedValue.class)
                        && method.getAnnotation(GeneratedValue.class).strategy() == GenerationType.IDENTITY) {
                    // Ignore identity columns.
                } else if (value == null) {
                    builder.append("\\N");
                } else if (method.isAnnotationPresent(Convert.class)) {
                    Class<? extends AttributeConverter<?, ?>> converterClass = method.getAnnotation(Convert.class)
                            .converter();

                    AttributeConverter converter = converterClass.newInstance();
                    builder.append(converter.convertToDatabaseColumn(value));
                } else if (method.isAnnotationPresent(JoinColumn.class)) {
                    // We need to get the id of the joined object.
                    for (Method method2 : value.getClass().getMethods()) {
                        if (method2.isAnnotationPresent(Id.class)) {
                            builder.append(method2.invoke(value));
                        }
                    }
                } else {
                    builder.append(value);
                }

                if (i != methods.size() - 1) {
                    builder.append("\t");
                }
            }
            return builder.toString();
        } catch (Throwable e) {
            throw Throwables.propagate(e);
        }
    }


    private void setupFor(Class<? extends Serializable> clz) {
        if (fieldNamesByClass.get(clz) == null) {
            List<String> fields = new ArrayList<>();
            List<MethodHandle> methodHandles = new ArrayList<>();
            List<Method> methods = new ArrayList<>();
            for (Method method : clz.getMethods()) {
                String columnName = null;
                if (method.isAnnotationPresent(Id.class) && method.isAnnotationPresent(GeneratedValue.class)
                        && method.getAnnotation(GeneratedValue.class).strategy() == GenerationType.IDENTITY) {
                    // Ignore pk with identity strategy.
                } else if (method.isAnnotationPresent(Column.class)) {
                    columnName = method.getAnnotation(Column.class).name();
                } else if (method.isAnnotationPresent(JoinColumn.class)) {
                    columnName = method.getAnnotation(JoinColumn.class).name();
                }
                if (columnName != null) {
                    // Certain one-to-on join situations can lead to multiple columns with the same column-name.
                    if (!fields.contains(columnName)) {
                        fields.add(columnName);
                        try {
                            methodHandles.add(MethodHandles.lookup().unreflect(method));
                            methods.add(method);
                        } catch (IllegalAccessException e) {
                            throw Throwables.propagate(e);
                        }
                    }
                } // end if annotation present
            }
            // extractorsByClass.put(clz, getExtractor(clz, fieldMethods));
            methodsByClass.put(clz, methods);
            methodHandlesByClass.put(clz, methodHandles);
            fieldNamesByClass.put(clz, String.join(",", fields));
        }
    }


    /**
     * Revisit this after javassist support java 8
     */
    @SuppressWarnings({ "unchecked", "unused" })
    private <E extends Serializable> CopyExtractor<E> getExtractor(Class<E> clz, List<Method> fieldMethods) {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.makeClass("com.eclecticlogic.pedal.dialect.postgresql." + clz.getSimpleName()
                + "$CopyExtractor");
        try {
            cc.addInterface(pool.getCtClass(CopyExtractor.class.getName()));
            StringBuilder methodBody = new StringBuilder();
            methodBody.append("public String getValueList(Object entity) {\n");
            methodBody.append("try {\n");
            methodBody.append("StringBuilder builder = new StringBuilder();\n");
            methodBody.append(clz.getName() + " typed = (" + clz.getName() + ")entity;\n");
            for (int i = 0; i < fieldMethods.size(); i++) {
                Method method = fieldMethods.get(i);
                if (method.getReturnType().isPrimitive()) {
                    methodBody.append("builder.append(typed." + method.getName() + "();\n");
                } else if (method.isAnnotationPresent(Convert.class)) {
                    Class<?> converterClass = method.getAnnotation(Convert.class).converter();
                    methodBody.append(converterClass.getName() + " c" + i + " = " + converterClass.getName()
                            + ".class.newInstance();\n");
                    // methodBody.append("c" + i + ".convertToDatabaseColumn(typed." + method.getName() + "());\n");
                    methodBody.append("System.out.println(c" + i + ".convertToEntityAttribute(new Character('A')));");
                    methodBody.append("builder.append(typed." + method.getName() + "());\n");
                }
                if (i != fieldMethods.size() - 1) {
                    methodBody.append("builder.append(\"\\t\");\n");
                }
            }
            methodBody.append("return builder.toString();\n");
            methodBody.append("} catch (Exception e) { throw new RuntimeException(e); } \n");
            methodBody.append("}\n");
            System.out.println(methodBody);
            cc.addMethod(CtNewMethod.make(methodBody.toString(), cc));
        } catch (NotFoundException | CannotCompileException e) {
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
