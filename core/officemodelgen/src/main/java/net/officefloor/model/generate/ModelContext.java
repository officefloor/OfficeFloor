/*-
 * #%L
 * Model Generator
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

package net.officefloor.model.generate;

import java.io.InputStream;

/**
 * Context for generating a Model.
 *
 * @author Daniel Sagenschneider
 */
public interface ModelContext {

	/**
	 * Creates the {@link ModelFile} within this {@link ModelContext}.
	 *
	 * @param relativeLocation
	 *            Relative location within this {@link ModelContext} to create
	 *            the {@link ModelFile}.
	 * @param contents
	 *            Contents to be written to the {@link ModelFile}.
	 * @return Created {@link ModelFile}.
	 * @throws Exception
	 *             If fails to create the {@link ModelFile}.
	 */
	ModelFile createModelFile(String relativeLocation, InputStream contents)
			throws Exception;

}
