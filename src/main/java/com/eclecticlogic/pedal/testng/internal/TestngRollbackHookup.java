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
package com.eclecticlogic.pedal.testng.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;

import com.eclecticlogic.pedal.Transaction;
import com.eclecticlogic.pedal.testng.RollbackOnSuccess;

/**
 * @author kabram.
 *
 */
public class TestngRollbackHookup implements IHookable {

    private static final Logger logger = LoggerFactory.getLogger(TestngRollbackHookup.class);


    /**
     * @see org.testng.IHookable#run(org.testng.IHookCallBack, org.testng.ITestResult)
     */
    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        // If this is not a test method, let it run
        if (testResult.getMethod().isTest()) {
            callBack.runTestMethod(testResult);
        } else {
            // Check if class level annotation exists.
            RollbackOnSuccess classLevel = testResult.getInstance().getClass().getAnnotation(RollbackOnSuccess.class);
            RollbackOnSuccess methodLevel = testResult.getMethod().getConstructorOrMethod().getMethod()
                    .getAnnotation(RollbackOnSuccess.class);
            boolean rollbackOnSuccess = (classLevel != null && classLevel.enabled() && methodLevel == null || methodLevel
                    .enabled()) || (classLevel == null && methodLevel != null && methodLevel.enabled());
            if (rollbackOnSuccess) {
                Transaction tx = getTransaction(testResult.getInstance());
                if (tx == null) {
                    logger.error("@RollbackOnSuccess is defined but no field in the test provides access to "
                            + "pedal's Transaction object.");
                    callBack.runTestMethod(testResult);
                } else {
                    try {
                        tx.run(() -> {
                            callBack.runTestMethod(testResult);
                            if (!testResult.isSuccess()) {
                                throw new RollbackException();
                            }
                        });
                    } catch (RollbackException e) {
                        // Eat it.
                    }
                }
            }
        } // end else
    }


    /**
     * @param instance
     * @return Transaction reference from a field of the type in the instance, null if no field exists.
     */
    private Transaction getTransaction(Object instance) {
        // TODO Auto-generated method stub
        return null;
    }

}
