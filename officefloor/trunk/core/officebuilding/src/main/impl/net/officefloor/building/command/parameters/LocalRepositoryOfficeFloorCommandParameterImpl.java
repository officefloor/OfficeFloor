/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.building.command.parameters;

import java.io.File;
import java.util.Properties;

import net.officefloor.building.command.LocalRepositoryOfficeFloorCommandParameter;
import net.officefloor.building.command.OfficeFloorCommandParameter;

/**
 * {@link OfficeFloorCommandParameter} for the local repository.
 * 
 * @author Daniel Sagenschneider
 */
public class LocalRepositoryOfficeFloorCommandParameterImpl extends
		AbstractSingleValueOfficeFloorCommandParameter implements
		LocalRepositoryOfficeFloorCommandParameter {

	/**
	 * Obtains the local repository from the environment {@link Properties}.
	 * 
	 * @param environment
	 *            Environment {@link Properties}.
	 * @return Local repository.
	 */
	public static File getLocalRepository(Properties environment) {
		return getLocalRepository(environment
				.getProperty(PARAMETER_LOCAL_REPOSITORY));
	}

	/**
	 * Obtains the {@link File} to the local repository (if provided).
	 * 
	 * @param localRepository
	 *            Location of the local repository.
	 * @return {@link File} for the local repository.
	 */
	private static File getLocalRepository(String localRepository) {
		return (localRepository == null ? null : new File(localRepository));
	}

	/**
	 * Initiate.
	 */
	public LocalRepositoryOfficeFloorCommandParameterImpl() {
		super(PARAMETER_LOCAL_REPOSITORY, "lr",
				"Local repository for caching Artifacts");
	}

	/*
	 * ============== LocalRepositoryOfficeFloorCommandParameter ==========
	 */

	@Override
	public File getLocalRepository() {
		return getLocalRepository(this.getValue());
	}

}