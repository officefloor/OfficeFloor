/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.gef.bridge;

/**
 * {@link ClassLoader} {@link EnvironmentBridge}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassLoaderEnvironmentBridge implements EnvironmentBridge {

	/*
	 * ================= EnvironmentBridge =====================
	 */

	@Override
	public boolean isClassOnClassPath(String className) {
		// TODO implement EnvironmentBridge.isClassOnClassPath
		throw new UnsupportedOperationException("TODO implement EnvironmentBridge.isClassOnClassPath");
	}

	@Override
	public boolean isSuperType(String className, String superType) {
		// TODO implement EnvironmentBridge.isSuperType
		throw new UnsupportedOperationException("TODO implement EnvironmentBridge.isSuperType");
	}

	@Override
	public void selectClass(String searchText, String superType, SelectionHandler handler) {
		// TODO implement EnvironmentBridge.selectClass
		throw new UnsupportedOperationException("TODO implement EnvironmentBridge.selectClass");
	}

	@Override
	public boolean isResourceOnClassPath(String resourcePath) {
		// TODO implement EnvironmentBridge.isResourceOnClassPath
		throw new UnsupportedOperationException("TODO implement EnvironmentBridge.isResourceOnClassPath");
	}

	@Override
	public void selectClassPathResource(String searchText, SelectionHandler handler) {
		// TODO implement EnvironmentBridge.selectClassPathResource
		throw new UnsupportedOperationException("TODO implement EnvironmentBridge.selectClassPathResource");
	}

}