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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.transaction.annotation.Propagation;

import com.eclecticlogic.pedal.Context;
import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.TransactionRunner;

/**
 * @author kabram.
 *
 */
public class NoopTransactionMock implements Transaction {

    /**
     * @see com.eclecticlogic.pedal.TransactionRunner#run(java.util.function.Consumer)
     */
    @Override
    public void run(Consumer<Context> block) {
        block.accept(new NoopContext());
    }


    /**
     * @see com.eclecticlogic.pedal.TransactionRunner#run(java.lang.Runnable)
     */
    @Override
    public void run(Runnable block) {
        block.run();
    }


    /**
     * @see com.eclecticlogic.pedal.TransactionRunner#exec(java.util.function.Supplier)
     */
    @Override
    public <R> R exec(Supplier<R> block) {
        return block.get();
    }


    /**
     * @see com.eclecticlogic.pedal.TransactionRunner#exec(java.util.function.Function)
     */
    @Override
    public <R> R exec(Function<Context, R> block) {
        return block.apply(new NoopContext());
    }


    /**
     * @see com.eclecticlogic.pedal.TransactionRunner#flush()
     */
    @Override
    public void flush() {
        // Noop
    }


    /**
     * @see com.eclecticlogic.pedal.Transaction#with(org.springframework.transaction.annotation.Propagation)
     */
    @Override
    public TransactionRunner with(Propagation propagation) {
        return this;
    }

}
