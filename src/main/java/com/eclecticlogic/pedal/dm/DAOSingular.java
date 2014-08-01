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

import javax.persistence.LockModeType;

/**
 * Create/update/delete on a single entity.  
 * @author kabram.
 *
 */
public interface DAOSingular<E extends Serializable> {

    /**
     * @param entity Entity to create (persist) in the database.
     * @return
     */
    E create(E entity);


    E update(E entity);


    E delete(E entity);


    /**
     * @param entity Entity to lock
     * @param lockMode Locking mode
     * @return Locked entity refreshed to reflect state after locking.
     */
    E lock(E entity, LockModeType lockMode);
}
