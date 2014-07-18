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
package com.eclecticlogic.pedal.forward;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.forward.dm.ExoticTypes;
import com.eclecticlogic.pedal.forward.dm.ExoticTypesDAO;
import com.eclecticlogic.pedal.forward.dm.Status;
import com.eclecticlogic.pedal.loader.Loader;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author kabram.
 *
 */
@Test(singleThreaded = true)
public class TestExoticTypes {

    private ConfigurableApplicationContext context = new GenericXmlApplicationContext();


    public ConfigurableApplicationContext getContext() {
        return context;
    }


    @BeforeTest
    public void beforeTest(XmlTest xmlTest) throws IOException {
        ((GenericXmlApplicationContext) context).load("classpath:ioc/pedal-test-forward.xml");
        context.refresh();
    }


    @AfterTest
    public void afterTest() {
        context.close();
    }


    protected Transaction getTransaction() {
        return getContext().getBean(Transaction.class);
    }


    public void testInsert() {
        ExoticTypesDAO dao = getContext().getBean(ExoticTypesDAO.class);
        ExoticTypes et = new ExoticTypes();
        et.setLogin("inserter");
        et.setCountries(Lists.newArrayList(false, false, true, false, false, false, true));
        et.setAuthorizations(Sets.newHashSet("a", "b", "b", "c"));
        et.setScores(Lists.newArrayList(1L, 2L, 3L));
        et.setStatus(Status.ACTIVE);

        dao.create(et);
    }


    public void testRead() {
        ExoticTypesDAO dao = getContext().getBean(ExoticTypesDAO.class);

        ExoticTypes et = new ExoticTypes();
        et.setLogin("testRead");
        et.setCountries(Lists.newArrayList(false, false, true, false, false, false, true));
        et.setAuthorizations(Sets.newHashSet("a", "b", "b", "c"));
        et.setScores(Lists.newArrayList(1L, 2L, 3L));
        et.setStatus(Status.INACTIVE);

        dao.create(et);

        ExoticTypes et1 = dao.findById("testRead").get();
        assertEquals(et1.getLogin(), "testRead");
        assertTrue(et1.getCountries().get(2));
        assertTrue(et1.getStatus() == Status.INACTIVE);
    }


    public void testQueryDSL() {
        ExoticTypesDAO dao = getContext().getBean(ExoticTypesDAO.class);

        ExoticTypes t = dao.testWithQueryDSL().get(0);
        assertNotNull(t);
        assertTrue(t.getStatus() == Status.ACTIVE);
    }


    public void testArrayQuery() {
        ExoticTypesDAO dao = getContext().getBean(ExoticTypesDAO.class);

        ExoticTypes et = new ExoticTypes();
        et.setLogin("testQuery");
        et.setCountries(Lists.newArrayList(false, false, true, false, false, false, true));
        et.setAuthorizations(Sets.newHashSet("a", "b", "b", "c"));
        et.setScores(Lists.newArrayList(3L, 7L, 21L));
        et.setStatus(Status.INACTIVE);

        dao.create(et);

        List<ExoticTypes> types = dao.queryArray(Lists.newArrayList(3L, 7L, 21L));
        assertEquals(types.size(), 1);
    }


    public void testLoader() {
        Loader loader = getContext().getBean(Loader.class);
        loader.load("loader/test.loader.groovy");
    }
}
