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

import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.eclecticlogic.pedal.Context;
import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.TransactionRunner;

/**
 * @author kabram.
 *
 */
public class TransactionImpl implements Transaction {

    private PlatformTransactionManager platformTransactionManager;
    private ThreadLocal<Stack<TransactionDefinition>> localTransactionDefinition = new ThreadLocal<Stack<TransactionDefinition>>() {

        @Override
        protected Stack<TransactionDefinition> initialValue() {
            Stack<TransactionDefinition> stack = new Stack<>();
            return stack;
        }
    };

    private static Logger logger = LoggerFactory.getLogger(TransactionImpl.class);


    protected PlatformTransactionManager getPlatformTransactionManager() {
        return platformTransactionManager;
    }


    public void setPlatformTransactionManager(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }


    /**
     * @param propagation
     * @return
     */
    @Override
    public TransactionRunner with(Propagation propagation) {
        if (propagation == Propagation.NOT_SUPPORTED) {
            throw new IllegalArgumentException(propagation.name());
        } else {
            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            transactionDefinition.setPropagationBehavior(propagation.value());
            localTransactionDefinition.get().push(transactionDefinition);
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
            status = getPlatformTransactionManager().getTransaction(getTransactionDefinition());
            logger.trace("start: new = {}", status.isNewTransaction() ? "yes" : "no");

            Context context = new ContextImpl();

            R retval = runner.apply(context);

            if (status.isRollbackOnly()) {
                getPlatformTransactionManager().rollback(status);
                logger.trace("rollback");
            } else {
                getPlatformTransactionManager().commit(status);
                logger.trace("commit");
            }

            return retval;
        } catch (RuntimeException e) {
            if (status != null && status.isCompleted() == false) {
                getPlatformTransactionManager().rollback(status);
                logger.trace("rollback");
            }
            throw e;
        }
    }


    private TransactionDefinition getTransactionDefinition() {
        if (localTransactionDefinition.get().isEmpty()) {
            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            transactionDefinition.setPropagationBehavior(Propagation.REQUIRED.value());
            return transactionDefinition;
        } else {
            return localTransactionDefinition.get().pop();
        }
    }
}
