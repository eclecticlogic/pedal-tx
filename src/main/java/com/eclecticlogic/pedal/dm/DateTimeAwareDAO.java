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
package com.eclecticlogic.pedal.dm;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * DAOs that implement this interface are able to automatically set inserted and updated timestamps on the entity
 * in a create() or save() call.
 * This is called out as a separate interface so that the implementer can provide their own way of determining 
 * current date/time (such a strategy helps in testing).
 * 
 * It is recommended that an application-level base DAO that derives from AbstractDAO be the one that implements this
 * interface. When implementing this interface, the base DAO should call AbstractDAO.init() via dependency-injection
 * (e.g. @PostContruct annotation on an override of init() that simply calls super.init) or other means 
 * (e.g. init property of Spring bean definition).
 * 
 * @author kabram.
 *
 */
public interface DateTimeAwareDAO {

    /**
     * @return Date and time.
     */
    default ZonedDateTime getCurrentDateTime() {
        return ZonedDateTime.now();
    }


    /**
     * @return Local date (no time component).
     */
    default LocalDate getCurrentDate() {
        return LocalDate.now(); 
    }


    /**
     * @return The name of the attribute (java-bean) for storing date/time of insertion. 
     */
    default String getInsertedDateProperty() {
        return "insertedOn";
    }


    /**
     * @return THe name of the attribute (java-bean) for storing date/time of update.
     */
    default String getUpdatedDateProperty() {
        return "updatedOn";
    }


    default ZoneId getZone() {
        return ZoneId.systemDefault();
    }


    default Date fromCurrentDateTime() {
        return Date.from(getCurrentDateTime().toInstant());
    }


    default Date fromCurrentDate() {
        return Date.from(getCurrentDate().atStartOfDay().atZone(getZone()).toInstant());
    }
}
