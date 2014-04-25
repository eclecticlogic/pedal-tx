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
package com.eclecticlogic.pedal;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface TransactionRunner {

    /**
     * @param block Block to execute a transaction in returning void.
     */
    public void run(Consumer<Context> block);


    /**
     * @param block Block to execute a transaction in accepting nothing and returning nothing.
     */
    public void run(Runnable block);


    /**
     * @param block Block to execute a transaction in returning object R
     * @return 
     */
    public <R> R exec(Supplier<R> block);


    /**
     * @param block Block to execute a transaction in accepting context and returning object R.
     * @return
     */
    public <R> R exec(Function<Context, R> block);

}
