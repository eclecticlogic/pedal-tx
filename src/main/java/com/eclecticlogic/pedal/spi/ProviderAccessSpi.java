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
package com.eclecticlogic.pedal.spi;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

import com.eclecticlogic.pedal.Context;
import com.eclecticlogic.pedal.ProviderAccess;


/**
 * Service provider implementation specific methods.
 * 
 * @author kabram.
 *
 */
public interface ProviderAccessSpi extends ProviderAccess {

    /**
     * @param context The context of the transaction.
     * @param work Execute the work passing in the underlying JDBC connection object.
     */
    void run(Context context, Consumer<Connection> work);

    /**
     * @param context The context of the transaction.
     * @param work Work to execute passing in the underlying JDBC connection object.
     * @return the output of the work.
     */
    <R> R exec(Context context, Function<Connection, R> work);
}
