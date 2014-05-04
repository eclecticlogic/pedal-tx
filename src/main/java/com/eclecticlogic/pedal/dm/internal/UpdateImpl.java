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
package com.eclecticlogic.pedal.dm.internal;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.dm.Update;

public class UpdateImpl<T extends Serializable> extends AbstractDDL<T> implements Update<T> {

    public UpdateImpl(EntityManager entityManager, Transaction transaction) {
        super(entityManager, transaction);
    }


    @Override
    public Update<T> using(LockModeType lock) {
        setLockModeType(lock);
        return this;
    }


    @Override
    public Update<T> bind(String param, Object value) {
        getBindings().add(new Binding(param, value));
        return this;
    }


    @Override
    public int update() {
        return getTransaction().exec(() -> {
            Query query = getQuery();
            return query.executeUpdate();
        });
    }

}
