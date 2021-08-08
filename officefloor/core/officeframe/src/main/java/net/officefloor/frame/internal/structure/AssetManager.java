/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
