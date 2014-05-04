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
package com.eclecticlogic.pedal.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.eclecticlogic.pedal.Context;
import com.eclecticlogic.pedal.DataContext;
import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.test.dm.Manufacturer;
import com.eclecticlogic.pedal.test.dm.dao.ManufacturerDAO;
import com.eclecticlogic.pedal.test.dm.dao.StudentDAO;

/**
 * @author kabram.
 *
 */
@Test(enabled = true)
public class BasicTest extends AbstractTest {

    private boolean beforePresent = true;


    public void testBeforeCommit() throws InterruptedException {
        Transaction tx = getContext().getBean(Transaction.class);

        tx.run((Context context) -> {
            ManufacturerDAO dao = getContext().getBean(ManufacturerDAO.class);
            Manufacturer m1 = new Manufacturer();
            m1.setName("beforeCommit");
            m1.setLocation("USA");
            dao.create(m1);

            context.beforeCommit((DataContext dc) -> {
                Thread t1 = new Thread(() -> {
                    beforePresent = getContext().getBean(ManufacturerDAO.class).findById("beforeCommit").isPresent();
                });
                t1.start();
                try {
                    t1.join();
                    // If checked in thread t1, testng doesn't pickup the assertion error.
                    assertFalse(beforePresent);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
        });
    }


    public void testAfterCommit() throws InterruptedException {
        Transaction tx = getContext().getBean(Transaction.class);

        tx.run(context -> {
            ManufacturerDAO dao = getContext().getBean(ManufacturerDAO.class);
            Manufacturer m1 = new Manufacturer();
            m1.setName("afterCommit");
            m1.setLocation("USA");
            dao.create(m1);

            context.afterCommit((DataContext dc) -> {
                Thread t1 = new Thread(() -> beforePresent = getContext().getBean(ManufacturerDAO.class)
                        .findById("afterCommit").isPresent());
                t1.start();
                try {
                    t1.join();
                    // If checked in thread t1, testng doesn't pickup the assertion error.
                    assertTrue(beforePresent);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
        });
    }


    public void testAttachedData() {
        Transaction tx = getContext().getBean(Transaction.class);

        tx.run(context -> {
            _testAttachedData();

            context.beforeCommit((DataContext dc) -> {
                assertEquals(dc.get("test"), "attachedData");
            });
            context.afterCommit((DataContext dc) -> {
                assertEquals(dc.get("test"), "attachedData");
            });
        });
    }


    private void _testAttachedData() {
        Transaction tx = getContext().getBean(Transaction.class);

        tx.exec((Context context) -> {
            context.put("test", "attachedData");
            return null;
        });

        tx.exec((Context context) -> {
            assertEquals(context.get("test"), "attachedData");
            return null;
        });
    }


    public void testIdRetrieval() {
        ManufacturerDAO dao = getContext().getBean(ManufacturerDAO.class);
        assertEquals(dao.getPrimaryKeyProperty(), "name");
    }


    public void testTableName() {
        ManufacturerDAO dao = getContext().getBean(ManufacturerDAO.class);
        assertEquals(dao.getTableName(), "basic.manufacturer");

        StudentDAO stdao = getContext().getBean(StudentDAO.class);
        assertEquals(stdao.getTableName(), "basic.graduate_student");
    }
}
