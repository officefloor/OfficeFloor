/*-
 * #%L
 * OfficeFloor Java8 Shim
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

package java.net.spi;

import java.net.URLStreamHandlerFactory;

/**
 * This class is only available in Java 9 and above. This is only provided to
 * enable compiling code dependent on this class in Java 8.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class URLStreamHandlerProvider implements URLStreamHandlerFactory {
}
