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
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

/**
 * @author kabram.
 *
 */
@SuppressWarnings("serial")
@Embeddable
public class Embedee implements Serializable {

    private int id;
    private Name name;


    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    @Column(name = "name", nullable = false, length = 100)
    @Convert(converter = NameConverter.class)
    public Name getName() {
        return name;
    }


    public void setName(Name name) {
        this.name = name;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof Embedee == false) {
            return false;
        } else {
            Embedee typed = (Embedee) obj;
            return Objects.equals(this.getId(), typed.getId()) && Objects.equals(this.getName(), typed.getName());
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }
}
