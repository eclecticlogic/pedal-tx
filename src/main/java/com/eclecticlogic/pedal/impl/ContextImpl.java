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
package com.eclecticlogic.pedal.impl;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.persistence.EntityManager;

import com.eclecticlogic.pedal.Context;
import com.eclecticlogic.pedal.DataContext;
import com.eclecticlogic.pedal.spi.ProviderAccessSpi;

public class ContextImpl implements Context {

    private EntityManager entityManager;
    private ProviderAccessSpi providerAccessSpi;


    public ContextImpl(EntityManager em, ProviderAccessSpi providerAccessSpi) {
        this.entityManager = em;
        this.providerAccessSpi = providerAccessSpi;
    }


    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }


    @Override
    public void put(Object key, Object value) {
        TransactionSynchronizationAdapterData.instance().put(key, value);
    }


    @Override
    public <T> T get(Object key) {
        return TransactionSynchronizationAdapterData.instance().<T> get(key);
    }


    @Override
    public void beforeCommit(Consumer<DataContext> task) {
        TransactionSynchronizationAdapterTask.instance().addBeforeCommit(task);
    }


    @Override
    public void afterCommit(Consumer<DataContext> task) {
        TransactionSynchronizationAdapterTask.instance().addAfterCommit(task);
    }


    // Connection work methods
    @Override
    public void run(Consumer<Connection> work) {
        providerAccessSpi.run(this, work);
    }


    @Override
    public <R> R exec(Function<Connection, R> work) {
        return providerAccessSpi.exec(this, work);
    }

}
