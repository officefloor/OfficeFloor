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

import net.officefloor.frame.api.manage.Office;

/**
 * Manages {@link AssetManager} instances within an {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeManager {

	/**
	 * Obtains the {@link AssetManager} for the {@link AssetManagerReference}.
	 * 
	 * @param assetManagerReference {@link AssetManagerReference}.
	 * @return {@link AssetManager} for the {@link AssetManagerReference}.
	 */
	AssetManager getAssetManager(AssetManagerReference assetManagerReference);

	/**
	 * Obtains the interval in milliseconds to monitor the {@link Asset} instances.
	 * 
	 * @return Interval in milliseconds to monitor the {@link Asset} instances.
	 */
	long getMonitorInterval();

	/**
	 * Runs the checks on the {@link Asset} instances managed by this
	 * {@link OfficeManager}.
	 */
	void runAssetChecks();

}
