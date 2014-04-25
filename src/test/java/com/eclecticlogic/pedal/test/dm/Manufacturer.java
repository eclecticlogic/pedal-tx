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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

/**
 * @author kabram.
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "manufacturer")
public class Manufacturer implements Serializable {

    private String name;
    private String location;


    @Id
    @Column(name = "name", unique = true, nullable = false, length = 50)
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    @Column(name = "location", nullable = false)
    public String getLocation() {
        return location;
    }


    public void setLocation(String location) {
        this.location = location;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof Manufacturer == false) {
            return false;
        } else {
            Manufacturer typed = (Manufacturer) obj;
            return java.util.Objects.equals(this.getName(), typed.getName());
        }
    }


    @Override
    public String toString() {
        return Objects.toStringHelper(this) //
                .add("name", getName()) //
                .add("location", getLocation()) //
                .toString();
    }
}
