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
package net.officefloor.frame.spi.managedobject.source;

/**
 * Specification of a {@link ManagedObjectSource}. This is different to the
 * {@link ManagedObjectSourceMetaData} as it specifies how to configure the
 * {@link ManagedObjectSource} to then obtain its
 * {@link ManagedObjectSourceMetaData} based on the configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceSpecification {

	/**
	 * Obtains the specification of the properties for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return Property specification.
	 */
	ManagedObjectSourceProperty[] getProperties();
}
