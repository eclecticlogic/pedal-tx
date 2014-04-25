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
     * @param entities Collection of entries to create.
     * @return List of the entries that were created.
     */
    @SuppressWarnings("unchecked")
    public List<E> create(E... entities);


    public Optional<E> findById(P id);


    @SuppressWarnings("unchecked")
    public List<E> findById(P... ids);


    public List<E> findAll();


    public E update(E entity);


    @SuppressWarnings("unchecked")
    public List<E> update(E... entities);


    public E delete(E entity);


    @SuppressWarnings("unchecked")
    public List<E> delete(E... entities);


    public E lock(E entity, LockModeType lockMode);

}