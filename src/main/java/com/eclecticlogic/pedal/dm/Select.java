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

/**
 * @author kabram.
 *
 */
public interface Select<E extends Serializable> extends OperationQualification<E, Select<E>> {

    /**
     * @param maxResults Maximum number of results to get.
     * @return Fluent interface to continue select definition.
     */
    public Select<E> returning(int maxResults);


    public Select<E> startingAt(int startPosition);
    
    
    public List<E> list();


    public Optional<E> get();


    public <R> Optional<R> scalar();


    public <R> List<R> scalarList();

}
