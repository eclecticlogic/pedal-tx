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
import java.io.Reader;
import java.io.Serializable;
import java.util.List;

/**
 * Current implementation only supports entities implementing the CopyCapable interface.
 * 
 * Specifications:
 * The entity either implements the CopyCapable interface or:
 *  
 * 1. All getter methods corresponding to fields must be annotated with @Column annotation.
 * 2. Superclasses will be traversed if they are annotated with @MappedSuperClass.
 * 
 * When getting data from entities, need to account for
 * 1. Embedded types (defer supporting this)
 * 2. Association - Join Column (need to get column name and value)
 * 3. Ignore collection types
 * 4. @Column annotation that is set to insertable = false
 * 5. @Convert annotation - need to convert the value to primitive type
 * 6. @Id (will be ignored only if insertable = false). Id value generation is not supported.
 * 7. Custom user types (e.g., array type).
 * @author kabram.
 *
 */
public class CopyListReader<E extends Serializable> extends Reader {

    private CopyList<E> copyList;
    private int listIndex;

    private StringBuilder buffer = new StringBuilder(8192);


    public CopyListReader(CopyList<E> copyList) {
        this.copyList = copyList;
    }


    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (buffer.length() > len) {
            System.arraycopy(buffer.toString().toCharArray(), 0, cbuf, off, len);
            buffer.replace(0, len, "");
            return len;
        } else {
            // Asking for more. Try to encode more
            while (listIndex < copyList.size()) {
                buffer.append(copyEncode(copyList.get(listIndex++)));
                if (buffer.length() > len) {
                    break;
                }
            }
            if (listIndex >= copyList.size() && buffer.length() == 0) {
                return -1;
            } else {
                int min = Integer.min(buffer.length(), len);
                System.arraycopy(buffer.toString().toCharArray(), 0, cbuf, off, min);
                buffer.replace(0, min, "");
                return min;
            }
        }
    }


    private String copyEncode(E entity) {
        if (entity instanceof CopyCapable) {
            List<Object> values = ((CopyCapable) entity).copyColumnValues();
            StringBuilder builder = new StringBuilder();
            for (Object value : values) {
                builder.append("\t");
                if (value != null) {
                    builder.append(value.toString());
                } else {
                    builder.append("\\N");
                }
            }
            builder.append("\n");
            return builder.substring(1);
        } else {
            throw new UnsupportedOperationException(
                    "No support for copy insert of entites that don't implement CopyCapable");
        }
    }


    @Override
    public void close() throws IOException {
        buffer = null;
    }

}
