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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link OfficeAdministration} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationNode extends LinkTeamNode, OfficeAdministration {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Administration";

	/**
	 * Initialises this {@link AdministrationNode}.
	 * 
	 * @param administrationSourceClassName
	 *            Class name of the {@link AdministrationSource}.
	 * @param administrationSource
	 *            Optional instantiated {@link AdministrationSource}. May be
	 *            <code>null</code>.
	 */
	void initialise(String administrationSourceClassName, AdministrationSource<?, ?, ?> administrationSource);

	/**
	 * <p>
	 * Obtains the {@link AdministrationType} for this
	 * {@link AdministrationNode}.
	 * <p>
	 * The {@link OfficeAdministration} must be fully populated with the
	 * necessary {@link Property} instances before calling this.
	 * 
	 * @return {@link AdministrationType} for this {@link AdministrationNode}.
	 */
	AdministrationType<?, ?, ?> loadAdministrationType();

	/**
	 * Builds the pre {@link ManagedFunction} {@link Administration}.
	 * 
	 * @param managedFunctionBuilder
	 *            {@link ManagedFunctionBuilder}.
	 */
	void buildPreFunctionAdministration(ManagedFunctionBuilder<?, ?> managedFunctionBuilder);

	/**
	 * Builds the post {@link ManagedFunction} {@link Administration}.
	 * 
	 * @param managedFunctionBuilder
	 *            {@link ManagedFunctionBuilder}.
	 */
	void buildPostFunctionAdministration(ManagedFunctionBuilder<?, ?> managedFunctionBuilder);

}