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
package net.officefloor.frame.spi.administration.source;


/**
 * Specification of a {@link AdministratorSource}. This is different to the
 * {@link AdministratorSourceMetaData} as it specifies how to configure the
 * {@link AdministratorSource} to then obtain its
 * {@link AdministratorSourceMetaData} based on the configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorSourceSpecification {

	/**
	 * Obtains the specification of the properties for the
	 * {@link AdministratorSource}.
	 * 
	 * @return Property specification.
	 */
	AdministratorSourceProperty[] getProperties();
}
