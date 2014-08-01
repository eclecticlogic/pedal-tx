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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.dm.DAO;
import com.eclecticlogic.pedal.dm.DAORegistry;
import com.eclecticlogic.pedal.dm.TestableDAO;

public class DAORegistryImpl implements DAORegistry, BeanPostProcessor {

    private Transaction transaction;
    private EntityManagerFactory entityManagerFactory;
    private Map<Class<?>, DAO<? extends Serializable, ? extends Serializable>> daosByEntityClass = new HashMap<>();

    private static Logger logger = LoggerFactory.getLogger(DAORegistryImpl.class);


    protected Transaction getTransaction() {
        return transaction;
    }


    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }


    protected EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }


    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DAO<?, ?>) {
            DAO<?, ?> dao = (DAO<?, ?>) bean;
            daosByEntityClass.put(dao.getEntityClass(), dao);
        }
        return bean;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <E extends Serializable, P extends Serializable> DAO<E, P> get(Class<E> clz) {
        return (DAO<E, P>) daosByEntityClass.get(clz);
    }


    @Override
    @SuppressWarnings("unchecked")
    public <E extends Serializable, P extends Serializable> DAO<E, P> get(E entity) {
        return (DAO<E, P>) daosByEntityClass.get(getEntityClass(entity));
    }


    @Override
    @SuppressWarnings("unchecked")
    public <E extends Serializable, P extends Serializable> void testDAOs() {
        for (DAO<? extends Serializable, ? extends Serializable> udao : daosByEntityClass.values()) {
            DAO<E, P> dao = getGenericizedDAO(udao);
            if (dao instanceof TestableDAO) {
                logger.debug("Testing DAO " + dao.getClass().getName());
                P pk = ((TestableDAO<P>) dao).getPrototypicalPrimaryKey();
                logger.trace("Testing find method with pk " + pk);
                dao.findById(pk);
            }
        }
    }


    /**
     * This is to work around java generics coercion.
     * @param dao
     * @return
     */
    @SuppressWarnings("unchecked")
    private <E extends Serializable, P extends Serializable> DAO<E, P> getGenericizedDAO(
            DAO<? extends Serializable, ? extends Serializable> dao) {
        return (DAO<E, P>) dao;
    }


    private Class<?> getEntityClass(Object object) {
        Class<?> clz = object.getClass();

        while (clz != null) {
            try {
                getEntityManagerFactory().getMetamodel().entity(clz);
                break;
            } catch (IllegalArgumentException e) {
                clz = clz.getSuperclass();
            }
        }

        return clz;
    }


    // DAOLite methods.

    @Override
    public <E extends Serializable, P extends Serializable> Optional<E> findById(Class<E> clz, P id) {
        return get(clz).findById(id);
    }


    @Override
    public <E extends Serializable> E create(E entity) {
        return get(entity).create(entity);
    }


    @Override
    public <E extends Serializable> E update(E entity) {
        return get(entity).update(entity);
    }


    @Override
    public <E extends Serializable> E delete(E entity) {
        return get(entity).delete(entity);
    }


    @Override
    public <E extends Serializable> E lock(E entity, LockModeType lockMode) {
        return get(entity).lock(entity, lockMode);
    }
}
