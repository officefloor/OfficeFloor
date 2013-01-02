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
package net.officefloor.plugin.gwt.module;

import net.officefloor.model.change.Change;
import net.officefloor.model.gwt.module.GwtModuleModel;

/**
 * Changes that can be made to a GWT Module.
 * 
 * @author Daniel Sagenschneider
 */
public interface GwtChanges {

	/**
	 * Obtains the path to the GWT Module.
	 * 
	 * @param module
	 *            {@link GwtModuleModel}.
	 * @return GWT Module path.
	 */
	String createGwtModulePath(GwtModuleModel module);

	/**
	 * Retrieves the GWT Module.
	 * 
	 * @param gwtModulePath
	 *            GWT Module path.
	 * @return {@link GwtModuleModel}.
	 */
	GwtModuleModel retrieveGwtModule(String gwtModulePath);

	/**
	 * Updates the GWT Module.
	 * 
	 * @param module
	 *            {@link GwtModuleModel} details.
	 * @param existingGwtModulePath
	 *            Existing GWT Module path. May be <code>null</code> if no
	 *            existing GWT Module.
	 * @return {@link Change} to update the GWT Module.
	 */
	Change<GwtModuleModel> updateGwtModule(GwtModuleModel module,
			String existingGwtModulePath);

}