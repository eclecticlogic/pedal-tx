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

import java.util.List;

import javax.inject.Inject;

import com.eclecticlogic.pedal.provider.hibernate.dialect.CopyCommand;
import com.eclecticlogic.pedal.provider.hibernate.dialect.CopyList;
import com.eclecticlogic.pedal.test.dm.Student;

/**
 * @author kabram.
 *
 */
public class StudentDAO extends TestDAO<Student, String> {

    @Inject
    private CopyCommand copyCommand;


    public void setCopyCommand(CopyCommand copyCommand) {
        this.copyCommand = copyCommand;
    }


    @Override
    public Class<Student> getEntityClass() {
        return Student.class;
    }


    /**
     * Made public to facilitate testing.
     * @return
     */
    @Override
    public String getTableName() {
        return super.getTableName();
    }


    public void bulkInsert(List<Student> students) {
        getTransaction().run(() -> {
            copyCommand.insert(getEntityManager(), new CopyList<Student>(students));
        });
    }
}
