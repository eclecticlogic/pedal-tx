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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import com.eclecticlogic.pedal.Transaction;

/**
 * @author kabram.
 *
 */
public class AbstractDDL<E extends Serializable> {

    private EntityManager entityManager;
    private Transaction transaction;

    private enum QueryType {
        NATIVE, STRING, TYPED, CRITERIA
    };

    private QueryType queryType;

    private String queryString;
    private TypedQuery<E> queryTyped;
    private CriteriaQuery<E> queryCriteria;

    private LockModeType lockModeType;

    protected class Binding {

        private String name;
        private Object value;


        public Binding(String name, Object value) {
            super();
            this.name = name;
            this.value = value;
        }


        public void bind(Query q) {
            q.setParameter(name, value);
        }

    }

    private List<Binding> bindings = new ArrayList<>();


    protected AbstractDDL(EntityManager entityManager, Transaction transaction) {
        this.entityManager = entityManager;
        this.transaction = transaction;
    }


    protected EntityManager getEntityManager() {
        return entityManager;
    }


    protected Transaction getTransaction() {
        return transaction;
    }


    protected List<Binding> getBindings() {
        return bindings;
    }


    protected void setLockModeType(LockModeType lockModeType) {
        this.lockModeType = lockModeType;
    }


    public void setQuery(String query, boolean nativeQuery) {
        this.queryString = query;
        queryType = nativeQuery ? QueryType.NATIVE : QueryType.STRING;
    }


    public void setQuery(String query) {
        this.queryString = query;
        queryType = QueryType.STRING;
    }


    public void setQuery(TypedQuery<E> typedQuery) {
        this.queryTyped = typedQuery;
        queryType = QueryType.TYPED;
    }


    public void setQuery(CriteriaQuery<E> criteriaQuery) {
        this.queryCriteria = criteriaQuery;
        queryType = QueryType.CRITERIA;
    }


    protected Query getQuery() {
        Query q = null;
        switch (queryType) {
            case NATIVE:
                q = getEntityManager().createNativeQuery(queryString);
                break;
            case STRING:
                q = getEntityManager().createQuery(queryString);
                break;
            case TYPED:
                q = queryTyped;
                break;
            case CRITERIA:
                q = getEntityManager().createQuery(queryCriteria);
                break;
        }
        for (Binding binding : bindings) {
            binding.bind(q);
        }

        if (lockModeType != null) {
            q.setLockMode(lockModeType);
        }
        return q;
    }

}
