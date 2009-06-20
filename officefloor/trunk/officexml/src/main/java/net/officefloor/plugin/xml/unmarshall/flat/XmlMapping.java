/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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