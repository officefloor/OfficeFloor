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
/*
 * Created on 24/02/2006
 */
package net.officefloor.plugin.jms;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;

/**
 * Factory for the creation of the {@link ConnectionFactory} and
 * {@link Destination} administered objects.
 * 
 * @author Daniel Sagenschneider
 */
public interface JmsAdminObjectFactory {

	/**
	 * Enable configuration of JMS objects.
	 * 
	 * @param context
	 *            {@link ManagedObjectSourceContext}.
	 */
	void init(ManagedObjectSourceContext<?> context);

	/**
	 * Creates the {@link ConnectionFactory} administered object.
	 * 
	 * @return {@link ConnectionFactory} administered object.
	 * @throws Exception
	 *             If fail to create.
	 */
	ConnectionFactory createConnectionFactory() throws Exception;

	/**
	 * Creates the {@link Destination} administered object.
	 * 
	 * @return {@link Destination} administered object.
	 * @throws Exception
	 *             If fail to create.
	 */
	Destination createDestination() throws Exception;
}
