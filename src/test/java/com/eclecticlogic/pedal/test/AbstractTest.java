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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.xml.XmlTest;

import com.eclecticlogic.pedal.Transaction;
import com.google.common.collect.Lists;

/**
 * @author kabram.
 *
 */
public abstract class AbstractTest {

    private ConfigurableApplicationContext context = new GenericXmlApplicationContext();


    public ConfigurableApplicationContext getContext() {
        return context;
    }


    @BeforeTest
    public void beforeTest(XmlTest xmlTest) throws IOException {
        String schemaName = xmlTest.getName();

        context.getEnvironment().getPropertySources().addFirst(new TestNameAwarePropertySource(schemaName));

        // Setup the create schema file.
        {
            Path path = Paths.get("src", "test", "scripts", "create-schema-" + schemaName + ".sql");
            Files.write(path, Lists.newArrayList("create schema " + schemaName + ";"));
        }

        {
            Path path = Paths.get("src", "test", "scripts", "drop-schema-" + schemaName + ".sql");
            Files.write(path, Lists.newArrayList("drop schema " + schemaName + " cascade;"));
        }
        ((GenericXmlApplicationContext) context).load("classpath:ioc/pedal-test-main.xml");
        context.refresh();
    }


    @AfterTest
    public void afterTest() {
        context.close();
    }


    protected Transaction getTransaction() {
        return getContext().getBean(Transaction.class);
    }
}
