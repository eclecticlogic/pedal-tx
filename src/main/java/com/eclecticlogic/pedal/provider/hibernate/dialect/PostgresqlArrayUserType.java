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
package com.eclecticlogic.pedal.provider.hibernate.dialect;

import java.math.BigDecimal;

import com.eclecticlogic.pedal.provider.hibernate.ArrayUserType;

/**
 * Adapter from https://forum.hibernate.org/viewtopic.php?t=946973 and
 * http://blog.xebia.com/2009/11/09/understanding-and-writing-hibernate-user-types/
 * 
 * @author kabram.
 *
 */
public abstract class PostgresqlArrayUserType<T> extends ArrayUserType<T> {

    public static class BOOLEAN extends PostgresqlArrayUserType<Boolean> {

        @Override
        protected String getDialectPrimitiveName() {
            return "boolean";
        }
    }
    
    public static class INTEGER extends PostgresqlArrayUserType<Integer> {

        @Override
        protected String getDialectPrimitiveName() {
            return "integer";
        }
    }
    
    public static class LONG extends PostgresqlArrayUserType<Long> {

        @Override
        protected String getDialectPrimitiveName() {
            return "bigint";
        }
    }
    
    public static class STRING extends PostgresqlArrayUserType<Long> {

        @Override
        protected String getDialectPrimitiveName() {
            return "varchar";
        }
    }
    
    public static class BIGDECIMAL extends PostgresqlArrayUserType<BigDecimal> {

        @Override
        protected String getDialectPrimitiveName() {
            return "numeric";
        }
    }
        
}
