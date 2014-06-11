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

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.eclecticlogic.pedal.test.dm.Grade;
import com.eclecticlogic.pedal.test.dm.Student;
import com.eclecticlogic.pedal.test.dm.dao.StudentDAO;

/**
 * @author kabram.
 *
 */
@Test
public class TestCopyCommand extends AbstractTest {

    public void bulkInsertStudents() {
        StudentDAO sdao = getContext().getBean(StudentDAO.class);

        List<Student> list = new ArrayList<>();
        {
            Student s = new Student();
            s.setName("a");
            s.setGrade(Grade.A);
            s.setZone("a1");
            list.add(s);
        }
        {
            Student s = new Student();
            s.setName("b");
            s.setGrade(Grade.B);
            s.setZone("a2");
            list.add(s);
        }
        {
            Student s = new Student();
            s.setName("c");
            s.setGrade(Grade.C);
            s.setZone("a3");
            list.add(s);
        }
        {
            Student s = new Student();
            s.setName("d");
            s.setGrade(Grade.D);
            s.setZone("a4");
            list.add(s);
        }

        {
            Student s = new Student();
            s.setName("e");
            s.setGrade(Grade.A);
            s.setZone("a1");
            list.add(s);
        }
        {
            Student s = new Student();
            s.setName("f");
            s.setGrade(Grade.B);
            s.setZone("a1");
            list.add(s);
        }
        {
            Student s = new Student();
            s.setName("g");
            s.setGrade(Grade.A);
            s.setZone("a1");
            list.add(s);
        }

        sdao.bulkInsert(list);
        
        assertEquals(sdao.findAll().size(), 7);
    }
}
