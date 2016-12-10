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
package net.officefloor.plugin.work.clazz;

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;

/**
 * {@link WorkFactory} for the {@link ClassWork}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassWorkFactory implements WorkFactory<ClassWork> {

	/**
	 * Class of the {@link Work}.
	 */
	private final Class<?> workClass;

	/**
	 * Initiate.
	 * 
	 * @param workClass
	 *            Class of the {@link Work}.
	 */
	public ClassWorkFactory(Class<?> workClass) {
		this.workClass = workClass;
	}

	/*
	 * ==================== WorkFactory ================================
	 */

	@Override
	public ClassWork createWork() {
		try {
			return new ClassWork(this.workClass.newInstance());
		} catch (InstantiationException ex) {
			throw new InstantiationError();
		} catch (IllegalAccessException ex) {
			throw new IllegalAccessError();
		}
	}

}