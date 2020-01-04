/*-
 * #%L
 * OfficeXml
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
