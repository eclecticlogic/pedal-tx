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

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.dialect.postgresql.CopyCommand;
import com.eclecticlogic.pedal.dialect.postgresql.CopyList;
import com.eclecticlogic.pedal.test.dm.dao.TestDAO;

/**
 * @author kabram.
 *
 */
@Component
public class PlanetDAO extends TestDAO<Planet, String> {

    @Inject
    private CopyCommand copyCommand;


    public void setCopyCommand(CopyCommand copyCommand) {
        this.copyCommand = copyCommand;
    }


    @Override
    @Inject
    public void setTransaction(Transaction transaction) {
        super.setTransaction(transaction);
    }


    @Override
    public Class<Planet> getEntityClass() {
        return Planet.class;
    }


    public void bulkInsert(List<Planet> planets) {
        getTransaction().run(() -> {
            copyCommand.insert(getEntityManager(), new CopyList<Planet>(planets));
        });
    }
}
