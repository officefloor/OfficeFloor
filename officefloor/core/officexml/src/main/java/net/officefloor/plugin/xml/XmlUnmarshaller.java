/*-
 * #%L
 * OfficeXml
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.plugin.xml;

import java.io.InputStream;

/**
 * Contract to unmarshall XML onto an object.
 * 
 * @author Daniel Sagenschneider
 */
public interface XmlUnmarshaller {

    /**
     * Unmarshalls the input xml onto the input target object.
     * 
     * @param xml
     *            XML to unmarshall.
     * @param target
     *            Target object to load XML data onto.
     * @throws XmlMarshallException
     *             Should fail to load XML data onto the target object.
     */
    void unmarshall(InputStream xml, Object target) throws XmlMarshallException;
}
