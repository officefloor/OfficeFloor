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

package net.officefloor.plugin.xml.unmarshall.flat;

/**
 * Provides an mapping of the XML element/attribute to a load method of a target
 * object.
 * 
 * @author Daniel Sagenschneider
 */
public class XmlMapping {

    /**
     * Name of XML element containing the value.
     */
    protected final String elementName;

    /**
     * Name of method on the target object to load the value.
     */
    protected final String loadMethodName;

    /**
     * Initiate with the element name to obtain the value from to load via the
     * load method to the target object.
     * 
     * @param elementName
     *            Name of XML element containing the value.
     * @param loadMethodName
     *            Method of target object to utilise to load the value.
     */
    public XmlMapping(String elementName, String loadMethodName) {
        // Store state
        this.elementName = elementName;
        this.loadMethodName = loadMethodName;
    }

    /**
     * Obtains the element name.
     * 
     * @return Element name.
     */
    public String getElementName() {
        return this.elementName;
    }

    /**
     * Obtains the load method name.
     * 
     * @return Load method name.
     */
    public String getLoadMethodName() {
        return this.loadMethodName;
    }

}
