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
import java.util.Optional;

/**
 * When performing simple operations against single entities, using a DAOLite instance is easier than getting references
 * to specific DAO instances. This supports single entity create, find by Id, update and delete operations. 
 * 
 * @author kabram.
 *
 */
public interface DAOLite<E extends Serializable, P extends Serializable> extends DAOSingular<E> {

    /**
     * @param clz Entity class type.
     * @param id Primary key
     * @return Entity instance in an optional container.
     */
    Optional<E> findById(Class<E> clz, P id);
}
