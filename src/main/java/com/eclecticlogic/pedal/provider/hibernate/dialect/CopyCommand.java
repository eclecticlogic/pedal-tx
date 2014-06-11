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
package com.eclecticlogic.pedal.provider.hibernate.dialect;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import com.eclecticlogic.pedal.provider.ConnectionAccessor;
import com.eclecticlogic.pedal.spi.ProviderAccessSpi;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * 
 * @author kabram.
 *
 */
public class CopyCommand {

    private ConnectionAccessor connectionAccessor;
    private ProviderAccessSpi providerAccessSpi;


    public void setConnectionAccessor(ConnectionAccessor connectionAccessor) {
        this.connectionAccessor = connectionAccessor;
    }


    public void setProviderAccessSpi(ProviderAccessSpi providerAccessSpi) {
        this.providerAccessSpi = providerAccessSpi;
    }


    /**
     * @param lists Entities to be inserted using the Postgres COPY command.
     */
    public <E extends Serializable> void insert(EntityManager entityManager, CopyList<E> entityList) {
        providerAccessSpi.run(
                entityManager,
                connection -> {
                    try {
                        CopyManager copyManager = new CopyManager((BaseConnection) connectionAccessor
                                .getRawConnection(connection));
                        if (entityList.size() != 0) {
                            copyManager.copyIn("copy " + getEntityName(entityList) + "(" + getFieldNames(entityList)
                                    + ") from stdin", new CopyListReader<E>(entityList));
                        }
                    } catch (Exception e) {
                        throw Throwables.propagate(e);
                    }
                });
    }


    private <E extends Serializable> String getEntityName(CopyList<E> copyList) {
        if (Strings.isNullOrEmpty(copyList.getAlternateTableName())) {
            return providerAccessSpi.getTableName(copyList.get(0).getClass());
        } else {
            String schemaName = providerAccessSpi.getSchemaName();
            if (Strings.isNullOrEmpty(schemaName)) {
                return copyList.getAlternateTableName();
            } else {
                return schemaName + "." + copyList.getAlternateTableName();
            }
        }
    }


    private <E extends Serializable> String getFieldNames(CopyList<E> copyList) {
        E entity = copyList.get(0);
        if (entity instanceof CopyCapable) {
            return String.join(", ", ((CopyCapable) entity).copyColumnNames());
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
