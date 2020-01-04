/*-
 * #%L
 * net.officefloor.gef.shade
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

package org.eclipse.swt;

/**
 * <p>
 * Avoiding importing SWT, which has O/S specific implementations.
 * <p>
 * Providing this only as necessary for GEF.
 * 
 * @author Daniel Sagenschneider
 */
public class SWTError extends Error {

	private static final long serialVersionUID = -9160311124292866538L;
}
