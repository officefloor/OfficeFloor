/*-
 * #%L
 * JWT Security
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

package net.officefloor.web.jwt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.jwt.JwtHttpSecuritySource.Flows;

/**
 * Tests the {@link DefaultJwtChallengeSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultJwtChallengeSectionSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		SectionLoaderUtil.validateSpecification(DefaultJwtChallengeSectionSource.class);
	}

	/**
	 * Validate the type.
	 */
	public void testType() {

		// Create the expected type
		SectionDesigner designer = SectionLoaderUtil.createSectionDesigner();
		Set<Flows> nonChallenge = new HashSet<>(Arrays.asList(Flows.RETRIEVE_KEYS, Flows.RETRIEVE_ROLES));
		NEXT_FLOW: for (Flows flow : Flows.values()) {
			if (nonChallenge.contains(flow)) {
				continue NEXT_FLOW;
			}
			designer.addSectionInput(flow.name(), null);
		}

		// Validate type
		SectionLoaderUtil.validateSectionType(designer, DefaultJwtChallengeSectionSource.class, null);
	}
}
