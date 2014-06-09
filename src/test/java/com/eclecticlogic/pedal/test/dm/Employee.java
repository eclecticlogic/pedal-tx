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
package com.eclecticlogic.pedal.test.dm;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author kabram.
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "employee")
public class Employee implements Serializable {

    private int id;
    private String name;
    private Date insertedOn;
    private Date updatedOn;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(insertable = false, nullable = false)
    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    @Column(length = 50, nullable = false)
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "inserted_on", nullable = false)
    public Date getInsertedOn() {
        return insertedOn;
    }


    public void setInsertedOn(Date insertedOn) {
        this.insertedOn = insertedOn;
    }


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_on")
    public Date getUpdatedOn() {
        return updatedOn;
    }


    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

}
