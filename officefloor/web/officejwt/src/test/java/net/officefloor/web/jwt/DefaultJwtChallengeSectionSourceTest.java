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