/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.internal.structure;

/**
 * {@link AssetManager} to manage {@link Asset} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface AssetManager extends FunctionState {

	/**
	 * <p>
	 * Creates a new {@link AssetLatch}.
	 * <p>
	 * The returned {@link AssetLatch} is not being managed by this
	 * {@link AssetManager}. To have the {@link AssetLatch} managed, it must be
	 * registered with this {@link AssetManager}. This allows for only the list
	 * of {@link AssetLatch} instances requiring management to be managed.
	 * 
	 * @param asset
	 *            {@link Asset} that {@link FunctionState} instances will wait on.
	 * @return {@link AssetLatch} for the {@link Asset}.
	 */
	AssetLatch createAssetLatch(Asset asset);

}
