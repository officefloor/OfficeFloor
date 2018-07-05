/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile;

import java.util.ServiceLoader;

import net.officefloor.compile.spi.governance.source.GovernanceSource;

/**
 * <p>
 * {@link ServiceLoader} service to plug-in an {@link GovernanceSource}
 * {@link Class} alias by including the extension {@link GovernanceSource} jar
 * on the class path.
 * <p>
 * {@link OfficeFloorCompiler#addGovernanceSourceAlias(String, Class)} will be
 * invoked for each found {@link GovernanceSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceSourceService<I, F extends Enum<F>, S extends GovernanceSource<I, F>> {

	/**
	 * Obtains the alias for the {@link GovernanceSource} {@link Class}.
	 * 
	 * @return Alias for the {@link GovernanceSource} {@link Class}.
	 */
	String getGovernanceSourceAlias();

	/**
	 * Obtains the {@link GovernanceSource} {@link Class}.
	 * 
	 * @return {@link GovernanceSource} {@link Class}.
	 */
	Class<S> getGovernanceSourceClass();

}