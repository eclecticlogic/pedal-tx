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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.LockModeType;

/**
 * @author kabram.
 *
 * @param <E> The Entity type.
 * @param <P> The type of the primary key.
 */
public interface DAO<E extends Serializable, P extends Serializable> extends DAOMeta<E, P> {

    /**
     * @param entity Entity to create (persist) in the database.
     * @return
     */
    public E create(E entity);


    /**
     * @param entities Collection of entities
     * @return List of entities that were created.
     */
    public List<? extends E> create(Collection<? extends E> entities);


    public Optional<E> findById(P id);


    public List<E> findById(Collection<? extends P> ids);


    public List<E> findAll();


    public E update(E entity);


    public List<E> update(Collection<? extends E> entities);


    public E delete(E entity);


    public List<E> delete(Collection<? extends E> entities);


    /**
     * @param entity Entity to lock
     * @param lockMode Locking mode
     * @return Locked entity refreshed to reflect state after locking.
     */
    public E lock(E entity, LockModeType lockMode);


    /**
     * @param id Primary key of row to be locked.
     * @param lockMode Locking mode
     * @return Locked entity.
     */
    public E lockById(P id, LockModeType lockMode);

}