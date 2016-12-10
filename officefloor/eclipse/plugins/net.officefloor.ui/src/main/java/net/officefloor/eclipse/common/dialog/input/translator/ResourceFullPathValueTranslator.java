/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.eclipse.common.dialog.input.translator;

import org.eclipse.core.resources.IResource;

import net.officefloor.eclipse.common.dialog.input.InvalidValueException;
import net.officefloor.eclipse.common.dialog.input.ValueTranslator;

/**
 * {@link ValueTranslator} to obtain the full path from an input
 * {@link IResource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ResourceFullPathValueTranslator implements ValueTranslator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.dialog.input.ValueTranslator#translate
	 * (java.lang.Object)
	 */
	@Override
	public Object translate(Object inputValue) throws InvalidValueException {
		if (inputValue instanceof IResource) {
			return ((IResource) inputValue).getFullPath().toString();
		} else {
			return null;
		}
	}

}
