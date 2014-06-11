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
package com.eclecticlogic.pedal.provider.hibernate.dialect;

import java.util.List;


/**
 * Entities that implement this interface are able to directly return the values required for inserting a row.
 * @author kabram.
 *
 */
public interface CopyCapable {

    /**
     * @return List of column names for the copy command.
     */
    public List<String> copyColumnNames();
    
    
    /**
     * @return Values for the columns (nulls should be entered as nulls).
     */
    public List<Object> copyColumnValues();
}
