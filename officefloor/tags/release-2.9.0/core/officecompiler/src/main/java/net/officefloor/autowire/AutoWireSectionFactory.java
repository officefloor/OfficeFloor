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
package net.officefloor.autowire;

/**
 * <p>
 * Provided a {@link AutoWireSection} it returns the {@link AutoWireSection} to
 * utilise.
 * <p>
 * This allows wrapping the provided {@link AutoWireSection} for further
 * functionality, or replacing it with a more appropriate implementation.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireSectionFactory<A extends AutoWireSection> {

	/**
	 * Creates an {@link AutoWireSection} from the seed {@link AutoWireSection}.
	 * 
	 * @param seed
	 *            Seed {@link AutoWireSection}.
	 * @return {@link AutoWireSection} to utilise.
	 */
	A createAutoWireSection(AutoWireSection seed);

}