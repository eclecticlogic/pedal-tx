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
package com.eclecticlogic.pedal.forward.dm;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.test.dm.dao.TestDAO;

/**
 * @author kabram.
 *
 */
@Component
public class SimpleTypeDAO extends TestDAO<SimpleType, Integer> {

    @Override
    @Inject
    public void setTransaction(Transaction transaction) {
        super.setTransaction(transaction);
    }


    @Override
    public Class<SimpleType> getEntityClass() {
        return SimpleType.class;
    }

}
