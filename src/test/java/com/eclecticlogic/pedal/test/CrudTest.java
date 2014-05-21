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
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.testng.annotations.Test;

import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.test.dm.Embedee;
import com.eclecticlogic.pedal.test.dm.Employee;
import com.eclecticlogic.pedal.test.dm.Grade;
import com.eclecticlogic.pedal.test.dm.Manufacturer;
import com.eclecticlogic.pedal.test.dm.Master;
import com.eclecticlogic.pedal.test.dm.Name;
import com.eclecticlogic.pedal.test.dm.Primus;
import com.eclecticlogic.pedal.test.dm.Secundus;
import com.eclecticlogic.pedal.test.dm.Student;
import com.eclecticlogic.pedal.test.dm.dao.EmployeeDAO;
import com.eclecticlogic.pedal.test.dm.dao.ManufacturerDAO;
import com.eclecticlogic.pedal.test.dm.dao.MasterDAO;
import com.eclecticlogic.pedal.test.dm.dao.PrimusDAO;
import com.eclecticlogic.pedal.test.dm.dao.SecundusDAO;
import com.eclecticlogic.pedal.test.dm.dao.StudentDAO;
import com.google.common.collect.Lists;

/**
 * @author kabram.
 *
 */
@Test(enabled = true)
public class CrudTest extends AbstractTest {

    public void testCreation() {
        ManufacturerDAO dao = getContext().getBean(ManufacturerDAO.class);

        Manufacturer m1 = new Manufacturer();
        m1.setName("test");
        m1.setLocation("USA");
        dao.create(m1);

        assertEquals(dao.findById("test").isPresent(), true);
        assertEquals(dao.findById("test").get().getLocation(), "USA");
    }


    public void testUpdate() {
        ManufacturerDAO dao = getContext().getBean(ManufacturerDAO.class);
        Manufacturer m1 = new Manufacturer();
        m1.setName("testUpdate");
        m1.setLocation("USA");
        dao.create(m1);

        m1.setLocation("Canada");
        dao.update(m1);

        assertEquals(dao.findById("testUpdate").isPresent(), true);
        assertEquals(dao.findById("testUpdate").get().getLocation(), "Canada");
    }


    public void testDelete() {
        ManufacturerDAO dao = getContext().getBean(ManufacturerDAO.class);
        Manufacturer m1 = new Manufacturer();
        m1.setName("testDelete");
        m1.setLocation("USA");
        dao.create(m1);

        assertEquals(dao.findById("testDelete").isPresent(), true);

        dao.delete(m1);

        assertEquals(dao.findById("testDelete").isPresent(), false);
    }


    public void selectTest() {
        ManufacturerDAO dao = getContext().getBean(ManufacturerDAO.class);

        Manufacturer m1 = new Manufacturer();
        m1.setName("select1");
        m1.setLocation("Washington");
        dao.create(m1);

        Manufacturer m2 = new Manufacturer();
        m2.setName("select2");
        m2.setLocation("Washington");
        dao.create(m2);

        Manufacturer m3 = new Manufacturer();
        m3.setName("select3");
        m3.setLocation("New York");
        dao.create(m3);

        {
            List<Manufacturer> ms = dao.getByLocation("Washington");
            assertEquals(ms, Lists.newArrayList(m1, m2));
        }
        {
            List<Manufacturer> ms = dao.getByLocation("New York");
            assertEquals(ms, Lists.newArrayList(m3));
        }
        {
            List<Manufacturer> ms = dao.getByLocation("Chicago");
            assertEquals(ms, Collections.EMPTY_LIST);
        }
    }


    public void updateTest() {
        ManufacturerDAO dao = getContext().getBean(ManufacturerDAO.class);

        Manufacturer m1 = new Manufacturer();
        m1.setName("update1");
        m1.setLocation("Philly");
        dao.create(m1);

        Manufacturer m2 = new Manufacturer();
        m2.setName("update2");
        m2.setLocation("Philly");
        dao.create(m2);

        Manufacturer m3 = new Manufacturer();
        m3.setName("update3");
        m3.setLocation("New York");
        dao.create(m3);

        int count = dao.updateLocation("Philadelphia", "Philly");
        assertEquals(count, 2);
        assertEquals(dao.findById("update2").get().getLocation(), "Philadelphia");
    }


    public void emptyQueryTest() {
        ManufacturerDAO dao = getContext().getBean(ManufacturerDAO.class);
        assertEquals(dao.getByLocation("timbaktoo").size(), 0);
    }


    public void emptyUpdateTest() {
        ManufacturerDAO dao = getContext().getBean(ManufacturerDAO.class);
        assertEquals(dao.updateLocation("timbaktoo", "Mount Everest"), 0);
    }


    public void testCreateAutoId() {
        EmployeeDAO dao = getContext().getBean(EmployeeDAO.class);
        Employee e = new Employee();
        e.setName("joe");
        dao.create(e);

        e = new Employee();
        e.setName("jane");
        dao.create(e);
    }


    public void testOneToOne() {
        Transaction tx = getContext().getBean(Transaction.class);
        tx.run(() -> {
            PrimusDAO pdao = getContext().getBean(PrimusDAO.class);
            SecundusDAO sdao = getContext().getBean(SecundusDAO.class);

            Primus p = new Primus();
            p.setName("Hello");
            Secundus s = new Secundus();
            s.setName("Second");
            s.setPrimus(p);

            pdao.create(p);
            sdao.create(s);
        });
    }


    public void testWithConverter() {
        StudentDAO sdao = getContext().getBean(StudentDAO.class);
        Student student = new Student();
        student.setGrade(Grade.A);
        student.setName("Joe");
        student.setZone("AZ");
        sdao.create(student);
    }
    
    
    public void testEmbedded() {
        MasterDAO mdao = getContext().getBean(MasterDAO.class);
        
        Embedee e = new Embedee();
        e.setId(5);
        e.setName(Name.JOE);
        
        Master m = new Master();
        m.setId(e);
        m.setDescription("Some text here");
        mdao.create(m);
        Optional<Master> m2 = mdao.findById(e);
        assertTrue(m2.isPresent());
        assertEquals(m2.get().getId().getName(), Name.JOE);
    }
}
