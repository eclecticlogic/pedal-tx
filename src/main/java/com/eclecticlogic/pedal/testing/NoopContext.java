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
package com.eclecticlogic.pedal.testing;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.eclecticlogic.pedal.Context;
import com.eclecticlogic.pedal.DataContext;

/**
 * Mock context that uses a simple thread-local map
 * @author kabram.
 *
 */
public class NoopContext implements Context {

    private ThreadLocal<Map<Object, Object>> localData = new ThreadLocal<Map<Object, Object>>() {

        @Override
        protected Map<Object, Object> initialValue() {
            return new HashMap<>();
        }
    };


    /**
     * @see com.eclecticlogic.pedal.DataContext#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public void put(Object key, Object value) {
        localData.get().put(key, value);
    }


    /**
     * @see com.eclecticlogic.pedal.DataContext#get(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key) {
        return (T) localData.get().get(key);
    }


    /**
     * @see com.eclecticlogic.pedal.Context#beforeCommit(java.util.function.Consumer)
     */
    @Override
    public void beforeCommit(Consumer<DataContext> task) {
        // Noop
    }


    /**
     * @see com.eclecticlogic.pedal.Context#afterCommit(java.util.function.Consumer)
     */
    @Override
    public void afterCommit(Consumer<DataContext> task) {
        // Noop
    }

}
