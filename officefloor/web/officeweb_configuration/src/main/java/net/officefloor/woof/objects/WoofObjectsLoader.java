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
package net.officefloor.woof.objects;

import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.woof.model.objects.WoofObjectsModel;

/**
 * Loads the {@link WoofObjectsModel} and configures the {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofObjectsLoader {

	/**
	 * Loads the {@link WoofObjectsModel} configuration and configures the
	 * {@link SupplierSource}.
	 * 
	 * @param context
	 *            {@link WoofObjectsLoaderContext}.
	 * @throws Exception
	 *             If fails to load the configuration.
	 */
	void loadWoofObjectsConfiguration(WoofObjectsLoaderContext context) throws Exception;

}