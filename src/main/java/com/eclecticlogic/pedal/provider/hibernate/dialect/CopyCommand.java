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

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import com.eclecticlogic.pedal.Context;
import com.eclecticlogic.pedal.ProviderAccess;
import com.eclecticlogic.pedal.provider.ConnectionAccessor;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * 
 * @author kabram.
 *
 */
public class CopyCommand {

    private ConnectionAccessor connectionAccessor;
    private ProviderAccess providerAccess;


    public void setConnectionAccessor(ConnectionAccessor connectionAccessor) {
        this.connectionAccessor = connectionAccessor;
    }


    public void setProviderAccess(ProviderAccess providerAccess) {
        this.providerAccess = providerAccess;
    }


    /**
     * @param context Pedal context
     * @param lists Entities to be inserted using the Postgres COPY command.
     */
    @SuppressWarnings("unchecked")
    public <E extends Serializable> void insert(Context context, CopyList<E>... lists) {
        if (lists != null) {
            context.run(connection -> {
                try {
                    CopyManager copyManager = new CopyManager((BaseConnection) connectionAccessor
                            .getRawConnection(connection));
                    for (CopyList<E> copyList : lists) {
                        _insert(copyManager, copyList);
                    }
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            });

        }
    }


    private <E extends Serializable> void _insert(CopyManager copyManager, CopyList<E> copyList) throws IOException,
            SQLException {
        if (copyList.size() != 0) {
            copyManager.copyIn("copy " + getEntityName(copyList) + "(" + getFieldNames(copyList) + ") from stdin",
                    new CopyListReader<E>(copyList));
        }
    }


    private <E extends Serializable> String getEntityName(CopyList<E> copyList) {
        if (Strings.isNullOrEmpty(copyList.getAlternateTableName())) {
            return providerAccess.getTableName(copyList.get(0).getClass());
        } else {
            String schemaName = providerAccess.getSchemaName();
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
            return String.join(", ", ((CopyCapable) entity).getCopyColumnNames());
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
