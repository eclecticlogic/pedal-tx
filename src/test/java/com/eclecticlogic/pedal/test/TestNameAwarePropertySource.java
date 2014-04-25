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
package com.eclecticlogic.pedal.test;

import org.springframework.core.env.PropertySource;

/**
 * @author kabram.
 *
 */
public class TestNameAwarePropertySource extends PropertySource<String> {

    public TestNameAwarePropertySource(String testName) {
        super(testName);
    }


    @Override
    public Object getProperty(String name) {
        if (name.equals("test.schemaName")) {
            return getName();
        } else if (name.equals("test.createScript")) {
            return "create-schema-" + getName().toLowerCase() + ".sql";
        } else if (name.equals("test.dropScript")) {
            return "drop-schema-" + getName().toLowerCase() + ".sql";
        } else {
            return null;
        }
    }

}
