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
package com.eclecticlogic.pedal.test.dm.dao;

import javax.annotation.PostConstruct;

import com.eclecticlogic.pedal.dm.DateTimeAwareDAO;
import com.eclecticlogic.pedal.test.dm.Embedee;
import com.eclecticlogic.pedal.test.dm.Master;

/**
 * @author kabram.
 *
 */
public class MasterDAO extends TestDAO<Master, Embedee> implements DateTimeAwareDAO {

    @PostConstruct
    @Override
    public void init() {
        super.init();
    }


    @Override
    public Class<Master> getEntityClass() {
        return Master.class;
    }
}
