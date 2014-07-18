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
package com.eclecticlogic.pedal.loader;

import java.util.Collection;
import java.util.Map;

/**
 * Data loader utility.
 * @author kabram.
 *
 */
public interface Loader {

    /**
     * @param inputs Objects that can be referenced (by their keys) in the load script.
     * @return fluent interface to continue loading.
     */
    public LoaderExecutor withInputs(Map<String, Object> inputs);


    /**
     * @param loadScript Script to load.
     * @param additionalScripts Other scripts to load.
     * @return Fluent interface that allows you to define inputs and then load the scripts.
     */
    public Map<String, Object> load(String loadScript, String... additionalScripts);


    /**
     * @param loadScripts Collection of scripts to load.
     * @return Fluent interface that allows you to define inputs and then load the scripts.
     */
    public Map<String, Object> load(Collection<String> loadScripts);
}
