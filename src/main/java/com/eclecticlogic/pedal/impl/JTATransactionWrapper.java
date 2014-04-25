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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.EntityManagerFactory;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.eclecticlogic.pedal.Context;
import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.TransactionRunner;
import com.eclecticlogic.pedal.dm.internal.TransactionManagerAccessor;
import com.eclecticlogic.pedal.spi.ProviderAccessSpi;

@SuppressWarnings("serial")
public class JTATransactionWrapper extends JtaTransactionManager implements TransactionManagerAccessor, Transaction {

    private EntityManagerFactory entityManagerFactory;
    private TransactionDelegate delegate;
    private ProviderAccessSpi providerAccessSpi;


    public JTATransactionWrapper() {
        super();
        delegate = new TransactionDelegate(this);
    }


    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }


    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }


    @Override
    public ProviderAccessSpi getProviderAccessSpi() {
        return providerAccessSpi;
    }


    public void setProviderAccessSpi(ProviderAccessSpi providerAccessSpi) {
        this.providerAccessSpi = providerAccessSpi;
    }


    @Override
    public void run(Consumer<Context> block) {
        delegate.run(block);
    }


    @Override
    public void run(Runnable block) {
        delegate.run(block);
    }


    @Override
    public <R> R exec(Supplier<R> block) {
        return delegate.exec(block);
    }


    @Override
    public <R> R exec(Function<Context, R> block) {
        return delegate.exec(block);
    }


    @Override
    public TransactionRunner with(Propagation propagation) {
        return delegate.with(propagation);
    }


    @Override
    protected void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
        super.prepareSynchronization(status, definition);
        delegate.prepareSynchronization(status, definition);
    }
}
