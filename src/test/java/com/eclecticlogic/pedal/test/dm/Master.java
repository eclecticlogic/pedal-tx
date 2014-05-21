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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * @author kabram.
 *
 */
@SuppressWarnings("serial")
@Entity
public class Master implements Serializable {

    private Embedee id;
    private String description;


    @EmbeddedId
    @AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "id", nullable = false)),
            @AttributeOverride(name = "name", column = @Column(name = "name", nullable = false, length = 100)) })
    public Embedee getId() {
        return this.id;
    }


    public void setId(Embedee id) {
        this.id = id;
    }


    @Column(name = "description", nullable = false, length = 50)
    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }

}
