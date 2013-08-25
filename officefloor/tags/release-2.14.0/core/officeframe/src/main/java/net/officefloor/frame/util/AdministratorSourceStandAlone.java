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
package net.officefloor.frame.util;

import net.officefloor.frame.impl.construct.administrator.AdministratorSourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.impl.execute.duty.DutyKeyImpl;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceContext;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.SourceProperties;

/**
 * Loads {@link AdministratorSource} for stand-alone use.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorSourceStandAlone {

	/**
	 * {@link SourceProperties}.
	 */
	private final SourcePropertiesImpl properties = new SourcePropertiesImpl();

	/**
	 * Adds a property for the {@link AdministratorSource}.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param value
	 *            Value for the property.
	 */
	public void addProperty(String name, String value) {
		this.properties.addProperty(name, value);
	}

	/**
	 * Loads the {@link AdministratorSource}.
	 * 
	 * @param administratorSourceClass
	 *            Class of the {@link AdministratorSource}.
	 * @return Initialised {@link AdministratorSource}.
	 * @throws Exception
	 *             If fails to initialise {@link AdministratorSource}.
	 */
	public <I, A extends Enum<A>, AS extends AdministratorSource<I, A>> AS loadAdministratorSource(
			Class<AS> administratorSourceClass) throws Exception {

		// Create a new instance of the administrator source
		AS administratorSource = administratorSourceClass.newInstance();

		// Create the source context
		SourceContext sourceContext = new SourceContextImpl(false, Thread
				.currentThread().getContextClassLoader());

		// Initialise the administrator source
		AdministratorSourceContext context = new AdministratorSourceContextImpl(
				false, this.properties, sourceContext);
		administratorSource.init(context);

		// Return the initialised administrator source
		return administratorSource;
	}

	/**
	 * Obtains the {@link Duty} from the {@link Administrator}.
	 * 
	 * @param administrator
	 *            {@link Administrator}.
	 * @param dutyKey
	 *            Key identifying the {@link Duty}.
	 * @return {@link Duty} for the key.
	 */
	public <I, A extends Enum<A>> Duty<I, ?, ?> getDuty(
			Administrator<I, A> administrator, A dutyKey) {
		return administrator.getDuty(new DutyKeyImpl<A>(dutyKey));
	}

	/**
	 * Obtains the {@link Duty} from the {@link Administrator}.
	 * 
	 * @param administrator
	 *            {@link Administrator}.
	 * @param dutyIndex
	 *            Index identifying the {@link Duty}.
	 * @return {@link Duty} for the index.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <I> Duty<I, ?, ?> getDuty(Administrator<I, ?> administrator,
			int dutyIndex) {
		return administrator.getDuty(new DutyKeyImpl(dutyIndex));
	}
}