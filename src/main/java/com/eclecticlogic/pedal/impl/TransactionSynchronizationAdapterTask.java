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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.eclecticlogic.pedal.DataContext;

/**
 * @author karthik abram
 */
public class TransactionSynchronizationAdapterTask extends TransactionSynchronizationAdapter {

    private List<Consumer<DataContext>> preCommit = new ArrayList<>();
    private List<Consumer<DataContext>> postCommit = new ArrayList<>();


    static TransactionSynchronizationAdapterTask instance() {
        for (TransactionSynchronization txSync : TransactionSynchronizationManager.getSynchronizations()) {
            if (txSync instanceof TransactionSynchronizationAdapterTask) {
                return (TransactionSynchronizationAdapterTask) txSync;
            }
        }
        return null;
    }


    public void addBeforeCommit(Consumer<DataContext> task) {
        preCommit.add(task);
    }


    public void addAfterCommit(Consumer<DataContext> task) {
        postCommit.add(task);
    }


    @Override
    public void beforeCommit(boolean readOnly) {
        DataContextImpl data = new DataContextImpl();
        for (Consumer<DataContext> task : preCommit) {
            task.accept(data);
        }
    }


    @Override
    public void afterCommit() {
        DataContextImpl data = new DataContextImpl();
        for (Consumer<DataContext> task : postCommit) {
            task.accept(data);
        }
    }

    public class DataContextImpl implements DataContext {

        @Override
        public void put(Object key, Object value) {
            TransactionSynchronizationAdapterData.instance().put(key, value);
        }


        @Override
        public <T> T get(Object key) {
            return TransactionSynchronizationAdapterData.instance().get(key);
        }

    }
}
