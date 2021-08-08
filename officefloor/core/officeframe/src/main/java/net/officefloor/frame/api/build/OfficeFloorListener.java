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

package net.officefloor.frame.api.build;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Listens to the open/close of the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorListener {

	/**
	 * Notifies that the {@link OfficeFloor} has been opened.
	 * 
	 * @param event
	 *            {@link OfficeFloorEvent}.
	 * @throws Exception
	 *             If fails to handle open listen logic.
	 */
	void officeFloorOpened(OfficeFloorEvent event) throws Exception;

	/**
	 * Notifies that the {@link OfficeFloor} has been closed.
	 * 
	 * @param event
	 *            {@link OfficeFloorEvent}.
	 * @throws Exception
	 *             If fails to handle close listen logic.
	 */
	void officeFloorClosed(OfficeFloorEvent event) throws Exception;

}
