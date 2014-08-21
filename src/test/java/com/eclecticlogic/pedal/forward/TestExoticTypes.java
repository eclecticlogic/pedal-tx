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
import groovy.lang.Closure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.forward.dm.ExoticTypes;
import com.eclecticlogic.pedal.forward.dm.ExoticTypesDAO;
import com.eclecticlogic.pedal.forward.dm.SimpleType;
import com.eclecticlogic.pedal.forward.dm.Status;
import com.eclecticlogic.pedal.loader.Loader;
import com.eclecticlogic.pedal.loader.Script;
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
        assertTrue(et1.getScores().contains(3L));
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


    public void testEmptyArrayStoredAsNull() {
        ExoticTypesDAO dao = getContext().getBean(ExoticTypesDAO.class);

        ExoticTypes et = new ExoticTypes();
        et.setLogin("emptyArray");
        et.setCountries(Lists.newArrayList(false, false, true, false, false, false, true));
        et.setAuthorizations(Sets.newHashSet("a", "b", "b", "c"));
        et.setScores(Lists.newArrayList());
        et.setStatus(Status.INACTIVE);

        dao.create(et);

        assertEquals(dao.getNullScores("emptyArray").size(), 1);
    }


    public void testCopyCommand() {
        List<ExoticTypes> list = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            ExoticTypes et = new ExoticTypes();
            et.setLogin("copyCommand" + i);
            et.setCountries(Lists.newArrayList(false, false, true, false, false, false, true));
            et.setAuthorizations(Sets.newHashSet("a", "b", "b", "c"));
            if (i != 9) {
                et.setScores(Lists.newArrayList(1L, 2L, 3L));
            } else {
                et.setScores(Lists.newArrayList());
            }
            et.setStatus(Status.ACTIVE);
            list.add(et);
        }

        ExoticTypesDAO dao = getContext().getBean(ExoticTypesDAO.class);
        dao.bulkInsert(list);
        assertTrue(dao.findById("copyCommand0").isPresent());
        assertTrue(dao.findById("copyCommand9").isPresent());
    }


    @SuppressWarnings("unchecked")
    public void testLoaderWithNamespaces() {
        Loader loader = getContext().getBean(Loader.class);
        Map<String, Object> variables = loader //
                .withScriptDirectory("loader") //
                .load(Script.with("simple.loader.groovy", "a"), Script.with("simple.loader.groovy", "b"));
        Map<String, Object> avars = (Map<String, Object>) variables.get("a");
        assertEquals(((SimpleType) avars.get("simple1")).getAmount(), 10);
        Map<String, Object> bvars = (Map<String, Object>) variables.get("b");
        assertEquals(((SimpleType) bvars.get("simple2")).getAmount(), 20);
    }


    @SuppressWarnings("unchecked")
    public void testLoadWithinLoadScript() {
        Loader loader = getContext().getBean(Loader.class);
        Map<String, Object> variables = loader.withScriptDirectory("loader") //
                .load("test.loader.groovy");
        Map<String, Object> output = (Map<String, Object>) variables.get("output");
        Map<String, Object> avars = (Map<String, Object>) output.get("a");
        assertEquals(((SimpleType) avars.get("simple1")).getAmount(), 10);
    }


    @SuppressWarnings("serial")
    public void testCustomClosures() {
        Loader loader = getContext().getBean(Loader.class);
        Map<String, Object> variables = loader //
                .withCustomMethod("doubler", new Closure<Object>(this) {

                    @Override
                    public Object call(Object ...args) {
                        Integer i = (Integer)args[0];
                        return i * 2;
                    }
                }).withScriptDirectory("loader") //
                .load("customMethod.loader.groovy");
        assertEquals(variables.get("myvar"), 400);
    }

}
