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
package com.eclecticlogic.pedal.dm.internal;

import javax.persistence.EntityManagerFactory;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import com.eclecticlogic.pedal.spi.ProviderAccessSpi;

/**
 * This interface provides access to common methods provided by JPA and JTA transaction managers. Some of these are 
 * present in the PlatformTransactionManager class but that is too broad. Therefore only methods of interest are 
 * repeated here. 
 *   
 * @author kabram.
 *
 */
public interface TransactionManagerAccessor {

    /**
     * This is required by the transaction runner. The method is implemented by the Spring JPA transaction object
     * which the JPATransactionWrapper derives from.
     * @return
     */
    public EntityManagerFactory getEntityManagerFactory();


    public void commit(TransactionStatus status) throws TransactionException;


    public void rollback(TransactionStatus status) throws TransactionException;


    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException;

    
    /**
     * @return Access to underlying provider access implementation.
     */
    public ProviderAccessSpi getProviderAccessSpi();
}
