/*-
 * #%L
 * Model Generator
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
