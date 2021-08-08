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

package net.officefloor.plugin.xml.unmarshall.translate;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Contract to translate the XML string value to specific typed object.
 * 
 * @author Daniel Sagenschneider
 */
public interface Translator {

    /**
     * Translates the XML string value to specific typed object.
     * 
     * @param value
     *            XML string value.
     * @return Specific type object translated from the input XML string value.
     * @throws XmlMarshallException
     *             Should there be a failure to translate the value.
     */
    Object translate(String value) throws XmlMarshallException;

}
