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
import java.util.Stack;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.dm.DAORegistry;
import com.eclecticlogic.pedal.loader.LoaderExecutor;
import com.google.common.base.Throwables;

/**
 * @author kabram.
 *
 */
public class ScriptExecutor implements LoaderExecutor {

    private Stack<ScriptContext> scriptContextStack = new Stack<>();

    private Map<String, Object> inputs = new HashMap<>();

    private DAORegistry daoRegistry;
    private Transaction transaction;


    public ScriptExecutor(DAORegistry daoRegistry, Transaction transaction) {
        this.daoRegistry = daoRegistry;
        this.transaction = transaction;
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


    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> load(Collection<String> loadScripts) {
        Binding binding = create();
        // Bind inputs.
        for (String key : inputs.keySet()) {
            binding.setVariable(key, inputs.get(key));
        }

        ResourceLoader loader = new DefaultResourceLoader();

        for (String script : loadScripts) {
            try (InputStream stream = loader.getResource(script).getInputStream()) {
                List<String> lines = IOUtils.readLines(stream);
                String content = String.join("\n", lines);

                transaction.exec(() -> execute(content, binding));
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }

        return binding.getVariables();
    }


    @SuppressWarnings("serial")
    private Binding create() {
        Closure<List<Object>> table = new Closure<List<Object>>(this) {

            @SuppressWarnings("unchecked")
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
                return invokeRowClosure(args);
            };
        };

        Binding binding = new Binding();
        binding.setVariable("table", table);
        binding.setVariable("row", row);
        return binding;
    }


    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(String script, Binding binding) {
        GroovyShell shell = new GroovyShell(getClass().getClassLoader(), binding);
        shell.evaluate(script);
        return binding.getVariables();
    }


    protected <V> List<Object> invokeWithClosure(Class<?> clz, List<String> attributes, Closure<V> callable) {
        ScriptContext context = new ScriptContext();
        context.setEntityClass(clz);
        context.setAttributes(attributes);

        scriptContextStack.push(context);

        callable.call();

        scriptContextStack.pop();
        return context.getCreatedEntities();
    }


    protected Object invokeRowClosure(Object... attributeValues) {
        Serializable instance = instantiate();
        DelegatingGroovyObjectSupport<Serializable> delegate = new DelegatingGroovyObjectSupport<Serializable>(instance);

        for (int i = 0; i < scriptContextStack.peek().getAttributes().size(); i++) {
            delegate.setProperty(scriptContextStack.peek().getAttributes().get(i), attributeValues[i]);
        }
        Object entity = daoRegistry.get(instance).create(instance);
        scriptContextStack.peek().getCreatedEntities().add(entity);
        return entity;
    }


    private Serializable instantiate() {
        try {
            return (Serializable) scriptContextStack.peek().getEntityClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
    }

}
