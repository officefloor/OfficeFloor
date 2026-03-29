/*-
 * #%L
 * JWT Security
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
