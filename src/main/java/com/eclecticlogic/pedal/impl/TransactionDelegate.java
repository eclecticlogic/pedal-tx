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

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.eclecticlogic.pedal.Context;
import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.TransactionRunner;
import com.eclecticlogic.pedal.dm.internal.TransactionManagerAccessor;

/**
 * The delegate class has the functionality common to JPA and JTA (to implement the Transaction interface) pulled out
 * to avoid duplicating it.
 *  
 * @author kabram.
 *
 */
public class TransactionDelegate implements Transaction {

    private TransactionManagerAccessor accessor;
    private ThreadLocal<TransactionDefinition> localTransactionDefinition = new ThreadLocal<TransactionDefinition>() {

        @Override
        protected TransactionDefinition initialValue() {
            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            transactionDefinition.setPropagationBehavior(Propagation.REQUIRED.value());
            return transactionDefinition;
        }
    };

    private static Logger logger = LoggerFactory.getLogger(TransactionDelegate.class);


    public TransactionDelegate(TransactionManagerAccessor accessor) {
        this.accessor = accessor;
    }


    @Override
    public TransactionRunner with(Propagation propagation) {
        if (propagation == Propagation.NOT_SUPPORTED) {
            throw new IllegalArgumentException(propagation.name());
        } else {
            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            transactionDefinition.setPropagationBehavior(propagation.value());
            localTransactionDefinition.set(transactionDefinition);
            return this;
        }
    }


    @Override
    public void run(Consumer<Context> block) {
        _exec(context -> {
            block.accept(context);
            return null;
        });
    }


    @Override
    public void run(Runnable block) {
        _exec(context -> {
            block.run();
            return null;
        });
    }


    @Override
    public <R> R exec(Supplier<R> block) {
        return _exec(context -> block.get());
    }


    @Override
    public <R> R exec(Function<Context, R> block) {
        return _exec(context -> block.apply(context));
    }


    public <R> R _exec(Function<Context, R> runner) {
        TransactionStatus status = null;
        try {
            status = accessor.getTransaction(localTransactionDefinition.get());
            logger.trace("start: new = {}", status.isNewTransaction() ? "yes" : "no");

            EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(accessor
                    .getEntityManagerFactory());

            Context context = new ContextImpl(em, accessor.getProviderAccessSpi());

            R retval = runner.apply(context);

            if (status.isRollbackOnly()) {
                accessor.rollback(status);
                logger.trace("rollback");
            } else {
                accessor.commit(status);
                logger.trace("commit");
            }

            return retval;
        } catch (RuntimeException e) {
            if (status != null && status.isCompleted() == false) {
                accessor.rollback(status);
                logger.trace("rollback");
            } else {
                logger.trace("Transaction already complete.");
            }
            throw e;
        }
    }


    public void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
        if (status.isNewTransaction()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapterData());
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapterTask());
        }
    }
}
