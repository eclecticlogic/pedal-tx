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
package com.eclecticlogic.pedal.test.dm.dao;

import java.util.List;

import com.eclecticlogic.pedal.test.dm.Manufacturer;

/**
 * @author kabram.
 *
 */
public class ManufacturerDAO extends TestDAO<Manufacturer, String> {

    @Override
    public Class<Manufacturer> getEntityClass() {
        return Manufacturer.class;
    }


    public List<Manufacturer> getByLocation(String location) {
        return select("from Manufacturer where location = :location") //
                .bind("location", location) //
                .list();
    }


    public int updateLocation(String newLocation, String oldLocation) {
        return update("update Manufacturer set location = :newLocation where location = :oldLocation") //
                .bind("newLocation", newLocation) //
                .bind("oldLocation", oldLocation) //
                .update();
    }


    public String getPrimaryKeyProperty() {
        return getIdProperty();
    }


    /**
     * Made public to facilitate testing.
     * @return
     */
    @Override
    public String getTableName() {
        return super.getTableName();
    }
}
