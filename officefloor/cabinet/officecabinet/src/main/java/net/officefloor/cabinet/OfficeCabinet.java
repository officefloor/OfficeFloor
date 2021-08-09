/*-
 * #%L
 * OfficeFloor Filing Cabinet
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.cabinet;

import java.util.Optional;

/**
 * Office Cabinet.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeCabinet<D> {

	/**
	 * Retrieves the document by key.
	 * 
	 * @param key Key.
	 * @return {@link Optional} to possibly retrieved entity.
	 */
	Optional<D> retrieveByKey(String key);

	/**
	 * Stores the document.
	 * 
	 * @param document Document.
	 */
	void store(D document);

}
