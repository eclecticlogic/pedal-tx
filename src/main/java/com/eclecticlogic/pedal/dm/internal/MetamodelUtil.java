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
package com.eclecticlogic.pedal.dm.internal;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javax.persistence.metamodel.Attribute;

import org.springframework.beans.BeanUtils;

/**
 * @author kabram.
 *
 */
public class MetamodelUtil {

    /**
     * @param attribute JPA metamodel attribute.
     * @param entity Entity to set the value on.
     * @param value Value to set.
     */
    public static <E extends Serializable, T extends Serializable> void set(Attribute<? super E, T> attribute,
            E entity, T value) {
        Member member = attribute.getJavaMember();
        if (member instanceof Field) {
            Field field = (Field) member;
            field.setAccessible(true);
            try {
                field.set(entity, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (member instanceof Method) {
            PropertyDescriptor pd = BeanUtils.findPropertyForMethod((Method) member);
            if (pd.getWriteMethod() != null) {
                pd.getWriteMethod().setAccessible(true);
                try {
                    pd.getWriteMethod().invoke(entity, value);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("No setter for " + attribute.getName() + " in "
                        + entity.getClass().getName());
            }
        } else {
            throw new RuntimeException("Failed to set " + attribute.getName() + " of type "
                    + member.getClass().getName() + " in entity " + entity.getClass().getName());
        }
    }
}
