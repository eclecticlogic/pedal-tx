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
package com.eclecticlogic.pedal.provider.hibernate;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.eclecticlogic.pedal.ProviderAccess;
import com.eclecticlogic.pedal.test.AbstractTest;

/**
 * @author kabram.
 *
 */
@Test(enabled = true)
public class HibernateProviderTest extends AbstractTest {

    public void testSchemaName() {
        ProviderAccess pa = getContext().getBean(ProviderAccess.class);
        assertEquals(pa.getSchemaName(), "provider");
    }
}
