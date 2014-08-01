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
public interface DAO<E extends Serializable, P extends Serializable> extends DAOMeta<E, P>, DAOSingular<E> {

    /**
     * @param entities Collection of entities
     * @return List of entities that were created.
     */
    List<? extends E> create(Collection<? extends E> entities);


    Optional<E> findById(P id);


    List<E> findById(Collection<? extends P> ids);


    List<E> findAll();


    List<E> update(Collection<? extends E> entities);


    List<E> delete(Collection<? extends E> entities);


    /**
     * @param id Primary key of row to be locked.
     * @param lockMode Locking mode
     * @return Locked entity.
     */
    E lockById(P id, LockModeType lockMode);

}