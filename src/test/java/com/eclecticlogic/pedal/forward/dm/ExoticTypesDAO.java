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

import org.hibernate.type.CustomType;
import org.springframework.stereotype.Component;

import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.dialect.postgresql.CopyCommand;
import com.eclecticlogic.pedal.dialect.postgresql.CopyList;
import com.eclecticlogic.pedal.provider.hibernate.ListType;
import com.eclecticlogic.pedal.provider.hibernate.dialect.PostgresqlArrayPrimitiveName;
import com.eclecticlogic.pedal.test.dm.dao.TestDAO;
import com.mysema.query.jpa.impl.JPAQuery;

/**
 * @author kabram.
 *
 */
@Component
public class ExoticTypesDAO extends TestDAO<ExoticTypes, String> {

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
    public Class<ExoticTypes> getEntityClass() {
        return ExoticTypes.class;
    }


    public List<ExoticTypes> testWithQueryDSL() {
        JPAQuery query = new JPAQuery(getEntityManager());
        QExoticTypes et = QExoticTypes.exoticTypes;
        return query.from(et) //
                .where(et.status.eq(Status.ACTIVE)) //
                .list(et);
    }


    public List<ExoticTypes> queryArray(List<Long> scores) {
        return select("from ExoticTypes where scores = :scores") //
                .bind(query -> query.unwrap(org.hibernate.Query.class).setParameter("scores", scores,
                        new CustomType(new ListType(PostgresqlArrayPrimitiveName.LONG)))) //
                .list();
    }


    public List<ExoticTypes> getNullScores(String login) {
        return select("from ExoticTypes where login = :login and scores is null") //
                .bind("login", login) //
                .list();
    }


    public void bulkInsert(List<ExoticTypes> types) {
        getTransaction().run(() -> {
            copyCommand.insert(getEntityManager(), new CopyList<ExoticTypes>(types));
        });
    }
}
