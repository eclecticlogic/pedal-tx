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
 * DAOs that have this provider are able to automatically set inserted and updated timestamps on the entity
 * in a create() or save() call.
 * This is called out as a provider interface so that the implementer can provide their own way of determining 
 * current date/time (such a strategy helps in testing).
 * 
 * It is recommended that an application-level base DAO that derives from AbstractDAO be the one that sets the provider 
 * to a specific implementation (a derivative of this class per required customizations). When this provider is set, 
 * the application's base DAO should call AbstractDAO.init() via dependency-injection (e.g. @PostContruct annotation 
 * on an override of init() that simply calls super.init) or other means (e.g. init property of Spring bean definition).
 * 
 * @author kabram.
 *
 */
public class DateTimeProvider {

    /**
     * @return Date and time.
     */
    public ZonedDateTime getCurrentDateTime() {
        return ZonedDateTime.now();
    }


    /**
     * @return Local date (no time component).
     */
    public LocalDate getCurrentDate() {
        return LocalDate.now();
    }


    /**
     * @return The name of the attribute (java-bean) for storing date/time of insertion. 
     */
    public String getInsertedDateProperty() {
        return "insertedOn";
    }


    /**
     * @return THe name of the attribute (java-bean) for storing date/time of update.
     */
    public String getUpdatedDateProperty() {
        return "updatedOn";
    }


    public ZoneId getZone() {
        return ZoneId.systemDefault();
    }


    public Date fromCurrentDateTime() {
        return Date.from(getCurrentDateTime().toInstant());
    }


    public Date fromCurrentDate() {
        return Date.from(getCurrentDate().atStartOfDay().atZone(getZone()).toInstant());
    }
}
