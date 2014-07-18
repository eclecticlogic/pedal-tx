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
package com.eclecticlogic.pedal.loader.impl;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.eclecticlogic.pedal.dm.DAORegistry;
import com.eclecticlogic.pedal.loader.LoaderExecutor;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * @author kabram.
 *
 */
public class ScriptExecutor implements LoaderExecutor {

    private Class<?> currentClass;
    private List<String> attributes;

    private Map<String, Object> inputs = new HashMap<>();
    private Binding binding;

    private DAORegistry daoRegistry;


    public ScriptExecutor(DAORegistry daoRegistry) {
        this.daoRegistry = daoRegistry;
    }


    public void setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
    }


    @Override
    public Map<String, Object> load(String loadScript, String... additionalScripts) {
        List<String> scripts = new ArrayList<>();
        scripts.add(loadScript);
        if (additionalScripts != null) {
            for (int i = 0; i < additionalScripts.length; i++) {
                scripts.add(additionalScripts[i]);
            }
        }

        return load(scripts);
    }


    @Override
    public Map<String, Object> load(Collection<String> loadScripts) {
        ResourceLoader loader = new DefaultResourceLoader();
        Map<String, Object> retVal = new HashMap<>();
        for (String script : loadScripts) {
            try (InputStream stream = loader.getResource(script).getInputStream()) {
                List<String> lines = IOUtils.readLines(stream);
                String content = String.join("\n", lines);

                retVal.putAll(execute(content));
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }
        return retVal;
    }


    @SuppressWarnings({ "serial", "unchecked" })
    public Map<String, Object> execute(String script) {
        Closure<List<Object>> table = new Closure<List<Object>>(this) {

            @Override
            public List<Object> call(Object... args) {
                if (args == null || args.length != 3) {
                    throw new RuntimeException("The table method expects JPA entity class reference, "
                            + "list of bean properties and a closure");
                }
                return invokeWithClosure((Class<?>) args[0], (List<String>) args[1], (Closure<Void>) args[2]);
            }
        };

        Closure<Object> row = new Closure<Object>(this) {

            @Override
            public Object call(Object... args) {
                return invokeRowClosure((List<Object>) args[0]);
            };
        };

        binding = new Binding();
        for (String key : inputs.keySet()) {
            binding.setVariable(key, inputs.get(key));
        }

        binding.setVariable("table", table);
        binding.setVariable("row", row);

        GroovyShell shell = new GroovyShell(getClass().getClassLoader(), binding);
        shell.evaluate(script);
        System.out.println("Bindings = " + binding.getVariables());
        return binding.getVariables();
    }


    protected <V> List<Object> invokeWithClosure(Class<?> clz, List<String> attributes, Closure<V> callable) {
        currentClass = clz;
        this.attributes = attributes;
        callable.call();
        return Lists.newArrayList("apple", "banana");
    }


    protected Object invokeRowClosure(List<Object> attributeValues) {
        Serializable instance = instantiate();
        DelegatingGroovyObjectSupport<Serializable> delegate = new DelegatingGroovyObjectSupport<Serializable>(instance);

        for (int i = 0; i < attributes.size(); i++) {
            delegate.setProperty(attributes.get(i), attributeValues.get(i));
        }
        return daoRegistry.get(instance).create(instance);
    }


    private Serializable instantiate() {
        try {
            return (Serializable) currentClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
    }

}
