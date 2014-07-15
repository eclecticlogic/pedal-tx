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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.dm.CustomBinding;
import com.eclecticlogic.pedal.dm.Select;

public class SelectImpl<E extends Serializable> extends AbstractDDL<E> implements Select<E> {

    private int maxResults;
    private int firstResult;


    public SelectImpl(EntityManager entityManager, Transaction transaction) {
        super(entityManager, transaction);
    }


    public int getMaxResults() {
        return maxResults;
    }


    public int getFirstResult() {
        return firstResult;
    }


    @Override
    public Select<E> returning(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }


    @Override
    public Select<E> startingAt(int startPosition) {
        this.firstResult = startPosition;
        return this;
    }


    @Override
    public Select<E> bind(String param, Object value) {
        getBindings().add(new Binding(param, value));
        return this;
    }


    @Override
    public Select<E> bind(CustomBinding binding) {
        getBindings().add(new Binding(binding));
        return this;
    }


    @Override
    public Select<E> using(LockModeType lock) {
        setLockModeType(lock);
        return this;
    }


    @Override
    protected Query getQuery() {
        Query query = super.getQuery();

        if (getMaxResults() > 0) {
            query.setMaxResults(getMaxResults());
        }
        if (getFirstResult() > 0) {
            query.setFirstResult(getFirstResult());
        }
        return query;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<E> list() {
        Query query = getQuery();
        return query.getResultList();
    }


    @Override
    public Optional<E> get() {
        List<E> list = list();
        return list.size() == 0 ? Optional.empty() : Optional.of(list.get(0));
    }


    @SuppressWarnings("unchecked")
    @Override
    public <R> Optional<R> scalar() {
        try {
            Query query = getQuery();
            return Optional.ofNullable((R) query.getSingleResult());
        } catch (NoResultException e) {
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public <R> List<R> scalarList() {
        try {
            Query query = getQuery();
            return (List<R>) query.getResultList();
        } catch (NoResultException e) {
            return Collections.EMPTY_LIST;
        }
    }

}
