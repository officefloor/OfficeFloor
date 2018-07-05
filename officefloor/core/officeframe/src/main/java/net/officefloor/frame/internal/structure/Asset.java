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
package net.officefloor.frame.internal.structure;

/**
 * {@link Asset} of an {@link AssetManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Asset {

	/**
	 * Obtains the {@link ThreadState} owning this {@link Asset}.
	 * 
	 * @return {@link ThreadState} owning this {@link Asset}.
	 */
	ThreadState getOwningThreadState();

	/**
	 * Checks on the {@link Asset}.
	 * 
	 * @param context
	 *            {@link CheckAssetContext} for checking on the {@link Asset}.
	 */
	void checkOnAsset(CheckAssetContext context);

}