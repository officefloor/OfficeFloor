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

import net.officefloor.frame.compatibility.JavaFacet;
import net.officefloor.frame.compatibility.JavaFacetContext;

/**
 * {@link JavaFacet} for the <code>Generated</code> annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class GeneratedAnnotationJavaFacet implements JavaFacet {

	/**
	 * Obtains the appropriate Generated annotation {@link Class} name.
	 * 
	 * @return Appropriate Generated annotation {@link Class} name.
	 */
	public static String getGeneratedClassName() {
		return new GeneratedAnnotationJavaFacet().isSupported() ? "javax.annotation.processing.Generated"
				: "javax.annotation.Generated";
	}

	/*
	 * =================== JavaFacet ====================
	 */

	@Override
	public boolean isSupported(JavaFacetContext context) throws Exception {
		return context.getFeature() >= 9;
	}

}
