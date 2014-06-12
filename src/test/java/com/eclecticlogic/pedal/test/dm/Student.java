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
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * @author kabram.
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "student")
public class Student implements Serializable {

    private String id;
    private String name;
    private Grade grade;
    private String zone;
    private Teacher teacher;
    private Date insertedOn;


    @Id
    @GenericGenerator(name = "uuid", strategy = "com.eclecticlogic.pedal.provider.hibernate.UUIDBasedIdGenerator")
    @GeneratedValue(generator = "uuid")
    @Column(name = "student_id", unique = true, nullable = false, length = 36)
    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    @Column(name = "name", length = 25, nullable = false)
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    @Column(name = "grade", nullable = true, length = 1)
    @Convert(converter = GradeConverter.class)
    public Grade getGrade() {
        return grade;
    }


    public void setGrade(Grade grade) {
        this.grade = grade;
    }


    @Column(name = "zone", length = 5)
    public String getZone() {
        return zone;
    }


    public void setZone(String zone) {
        this.zone = zone;
    }


    @JoinColumn(name = "teacher_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    public Teacher getTeacher() {
        return teacher;
    }


    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }


    @Column(name = "inserted_on", nullable = false)
    public Date getInsertedOn() {
        return insertedOn;
    }


    public void setInsertedOn(Date insertedOn) {
        this.insertedOn = insertedOn;
    }

}
