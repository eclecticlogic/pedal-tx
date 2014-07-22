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

import java.util.Collection;
import java.util.Map;

import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.dm.DAORegistry;
import com.eclecticlogic.pedal.loader.Loader;
import com.eclecticlogic.pedal.loader.LoaderExecutor;
import com.eclecticlogic.pedal.loader.Script;

/**
 * Data loader entry class.
 * 
 * @author kabram.
 *
 */
public class LoaderImpl implements Loader {

    private String scriptDirectory;
    private DAORegistry daoRegistry;
    private Transaction transaction;


    public void setDaoRegistry(DAORegistry daoRegistry) {
        this.daoRegistry = daoRegistry;
    }


    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }


    @Override
    public Loader withScriptDirectory(String directory) {
        scriptDirectory = directory;
        return this;
    }


    private ScriptExecutor createScriptExecutor() {
        ScriptExecutor executor = new ScriptExecutor(daoRegistry, transaction);
        executor.setScriptDirectory(scriptDirectory);
        return executor;
    }


    @Override
    public LoaderExecutor withInputs(Map<String, Object> inputs) {
        ScriptExecutor executor = createScriptExecutor();
        executor.setInputs(inputs);
        return executor;
    }


    @Override
    public Map<String, Object> load(String loadScript, String... additionalScripts) {
        return createScriptExecutor().load(loadScript, additionalScripts);
    }


    @Override
    public Map<String, Object> load(Script script, Script... additionalScripts) {
        return createScriptExecutor().load(script, additionalScripts);
    }


    @Override
    public Map<String, Object> load(Collection<String> loadScripts) {
        return createScriptExecutor().load(loadScripts);
    }


    @Override
    public Map<String, Object> loadNamespaced(Collection<Script> loadScripts) {
        return createScriptExecutor().loadNamespaced(loadScripts);
    }
}
