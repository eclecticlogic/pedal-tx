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
package com.eclecticlogic.pedal.dm;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import com.eclecticlogic.pedal.ProviderAccess;
import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.dm.internal.MetamodelUtil;
import com.eclecticlogic.pedal.dm.internal.SelectImpl;
import com.eclecticlogic.pedal.dm.internal.UpdateImpl;

public abstract class AbstractDAO<E extends Serializable, P extends Serializable> implements DAO<E, P>, DAOMeta<E, P> {

    private Transaction transaction;
    private EntityManager entityManager;
    private ProviderAccess providerAccess;
    private boolean insertDateTimeAware, insertDateAware, updateDateTimeAware, updateDateAware, updateDTRequired;


    /**
     * Subclasses should override this and call this via a @PostContruct annotation or other means. 
     */
    protected void init() {
        if (this instanceof DateTimeAwareDAO) {
            setupInsertDateTimeFlags();
            setupUpdateDateTimeFlags();
        }
    }


    private void setupUpdateDateTimeFlags() {
        Attribute<? super E, ?> attr = null;
        try {
            attr = getEntityType().getAttribute(((DateTimeAwareDAO) this).getUpdatedDateProperty());
        } catch (IllegalArgumentException e) {
            // noop. This is thrown if the attribute doesn't exist.
        }
        if (attr != null) {
            Temporal temporalAnnotation = null;
            Member member = attr.getJavaMember();
            if (member instanceof Field && ((Field) member).isAnnotationPresent(Temporal.class)) {
                temporalAnnotation = ((Field) member).getAnnotation(Temporal.class);
            } else if (member instanceof Method && ((Method) member).isAnnotationPresent(Temporal.class)) {
                temporalAnnotation = ((Method) member).getAnnotation(Temporal.class);
            }
            if (temporalAnnotation != null) {
                updateDateTimeAware = temporalAnnotation.value() == TemporalType.TIMESTAMP;
                updateDateAware = temporalAnnotation.value() == TemporalType.DATE;
            }
            updateDTRequired = !((SingularAttribute<? super E, ?>) attr).isOptional();
        }
    }


    private void setupInsertDateTimeFlags() {
        Attribute<? super E, ?> attr = null;
        try {
            attr = getEntityType().getAttribute(((DateTimeAwareDAO) this).getInsertedDateProperty());
        } catch (IllegalArgumentException e) {
            // noop. This is thrown if the attribute doesn't exist.
        }
        if (attr != null) {
            Temporal temporalAnnotation = null;
            Member member = attr.getJavaMember();
            if (member instanceof Field && ((Field) member).isAnnotationPresent(Temporal.class)) {
                temporalAnnotation = ((Field) member).getAnnotation(Temporal.class);
            } else if (member instanceof Method && ((Method) member).isAnnotationPresent(Temporal.class)) {
                temporalAnnotation = ((Method) member).getAnnotation(Temporal.class);
            }
            if (temporalAnnotation != null) {
                insertDateTimeAware = temporalAnnotation.value() == TemporalType.TIMESTAMP;
                insertDateAware = temporalAnnotation.value() == TemporalType.DATE;
            }
        }
    }


    protected EntityManager getEntityManager() {
        return entityManager;
    }


    /**
     * This method can be overridden as-is with an added @PersistenceContext annotation to have Spring supply the entity manager. 
     * @param entityManager
     */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    protected Transaction getTransaction() {
        return transaction;
    }


    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }


    protected ProviderAccess getProviderAccess() {
        return providerAccess;
    }


    public void setProviderAccess(ProviderAccess providerAccess) {
        this.providerAccess = providerAccess;
    }


    /**
     * @return The name of the entity's database table. This returns the overridden name if orm.xml modifies it. 
     */
    protected String getTableName() {
        return getProviderAccess().getTableName(getEntityClass());
    }


    @Override
    public EntityType<E> getEntityType() {
        return getEntityManager().getEntityManagerFactory().getMetamodel().entity(getEntityClass());
    }


    @SuppressWarnings("unchecked")
    @Override
    public E create(final E entity) {
        return getTransaction().exec(
                () -> {
                    if (insertDateTimeAware) {
                        Attribute<? super E, Date> attr = (Attribute<? super E, Date>) getEntityType().getAttribute(
                                ((DateTimeAwareDAO) this).getInsertedDateProperty());
                        MetamodelUtil.set(attr, entity, ((DateTimeAwareDAO) this).fromCurrentDateTime());
                    } else if (insertDateAware) {
                        Attribute<? super E, Date> attr = (Attribute<? super E, Date>) getEntityType().getAttribute(
                                ((DateTimeAwareDAO) this).getInsertedDateProperty());
                        MetamodelUtil.set(attr, entity, ((DateTimeAwareDAO) this).fromCurrentDate());
                    }
                    if (updateDTRequired) {
                        setUpdateDateTimeIfRequired(entity);
                    }
                    getEntityManager().persist(entity);
                    return entity;
                });
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<E> create(final E... entities) {
        return getTransaction().exec(() -> {
            List<E> list = new ArrayList<>();
            for (E e : entities) {
                list.add(create(e));
            }
            return list;
        });
    }


    @Override
    public Optional<E> findById(final P id) {
        return getTransaction().exec(() -> {
            return Optional.ofNullable(getEntityManager().find(getEntityClass(), id));
        });
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<E> findById(final P... ids) {
        return getTransaction().exec((context) -> {
            CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<E> cq = builder.createQuery(getEntityClass());
            Root<E> root = cq.from(getEntityClass());
            cq.select(root).where(builder.in(root.get(getIdProperty())).in((Object[]) ids));
            TypedQuery<E> query = getEntityManager().createQuery(cq);
            return query.getResultList();
        });
    }


    /**
     * @return Name of the primary key (id) java bean property.
     */
    protected String getIdProperty() {
        for (SingularAttribute<? super E, ?> attr : getEntityType().getSingularAttributes()) {
            if (attr.isId()) {
                return attr.getName();
            }
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    private void setUpdateDateTimeIfRequired(final E entity) {
        if (updateDateTimeAware) {
            Attribute<? super E, Date> attr = (Attribute<? super E, Date>) getEntityType().getAttribute(
                    ((DateTimeAwareDAO) this).getUpdatedDateProperty());
            MetamodelUtil.set(attr, entity, ((DateTimeAwareDAO) this).fromCurrentDateTime());
        } else if (updateDateAware) {
            Attribute<? super E, Date> attr = (Attribute<? super E, Date>) getEntityType().getAttribute(
                    ((DateTimeAwareDAO) this).getUpdatedDateProperty());
            MetamodelUtil.set(attr, entity, ((DateTimeAwareDAO) this).fromCurrentDate());
        }
    }


    @Override
    public E update(final E entity) {
        return getTransaction().exec(() -> {
            setUpdateDateTimeIfRequired(entity);
            E t = getEntityManager().merge(entity);
            getEntityManager().persist(t);
            return t;
        });
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<E> update(final E... entities) {
        return getTransaction().exec(() -> {
            List<E> list = new ArrayList<>();
            for (E e : entities) {
                list.add(update(e));
            }
            return list;
        });
    }


    @Override
    public E delete(final E entity) {
        return getTransaction().exec(() -> {
            E mergedObject = getEntityManager().merge(entity);
            getEntityManager().remove(mergedObject);
            return mergedObject;
        });
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<E> delete(final E... entities) {
        return getTransaction().exec(() -> {
            List<E> list = new ArrayList<>();
            for (E e : entities) {
                list.add(delete(e));
            }
            return list;
        });
    }


    @Override
    public E lock(final E entity, final LockModeType lockMode) {
        return getTransaction().exec(() -> {
            getEntityManager().refresh(entity, lockMode);
            getEntityManager().flush();
            return entity;
        });
    }


    @Override
    public E lockById(P id, LockModeType lockMode) {
        return getTransaction().exec(() -> {
            return getEntityManager().find(getEntityClass(), id, lockMode);
        });
    }


    protected Select<E> select(String query, boolean nativeQuery) {
        SelectImpl<E> select = new SelectImpl<E>(getEntityManager(), getTransaction());
        select.setQuery(query, nativeQuery);
        return select;
    }


    protected Select<E> select(String query) {
        SelectImpl<E> select = new SelectImpl<E>(getEntityManager(), getTransaction());
        select.setQuery(query);
        return select;
    }


    protected Select<E> select(CriteriaQuery<E> query) {
        SelectImpl<E> select = new SelectImpl<E>(getEntityManager(), getTransaction());
        select.setQuery(query);
        return select;
    }


    protected Select<E> select(TypedQuery<E> query) {
        SelectImpl<E> select = new SelectImpl<E>(getEntityManager(), getTransaction());
        select.setQuery(query);
        return select;
    }


    protected Update<E> update(String query, boolean nativeQuery) {
        UpdateImpl<E> update = new UpdateImpl<E>(getEntityManager(), getTransaction());
        update.setQuery(query, nativeQuery);
        return update;
    }


    protected Update<E> update(String query) {
        UpdateImpl<E> update = new UpdateImpl<E>(getEntityManager(), getTransaction());
        update.setQuery(query);
        return update;
    }


    protected Update<E> update(TypedQuery<E> query) {
        UpdateImpl<E> update = new UpdateImpl<E>(getEntityManager(), getTransaction());
        update.setQuery(query);
        return update;
    }


    protected TypedQuery<E> getFindAllQuery() {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery = builder.createQuery(getEntityClass());
        Root<E> root = criteriaQuery.from(getEntityClass());
        criteriaQuery.select(root);
        return getEntityManager().createQuery(criteriaQuery);
    }


    @Override
    public List<E> findAll() {
        return getFindAllQuery().getResultList();
    }
}
