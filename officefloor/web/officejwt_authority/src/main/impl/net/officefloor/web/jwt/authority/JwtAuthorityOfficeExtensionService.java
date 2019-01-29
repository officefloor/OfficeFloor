package net.officefloor.web.jwt.authority;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.web.jwt.authority.JwtAuthorityManagedObjectSource.Flows;

/**
 * {@link JwtAuthority} {@link OfficeExtensionService}.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtAuthorityOfficeExtensionService implements OfficeExtensionService {

	/*
	 * =================== OfficeExtensionService ===================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		// Register the JWT authority
		OfficeManagedObjectSource jwtAuthoritySource = officeArchitect.addOfficeManagedObjectSource(
				JwtAuthority.class.getName(), JwtAuthorityManagedObjectSource.class.getName());
		OfficeManagedObject jwtAuthority = jwtAuthoritySource.addOfficeManagedObject(JwtAuthority.class.getName(),
				ManagedObjectScope.THREAD);

		// Register the handling functionality (to load keys)
		OfficeSection section = officeArchitect.addOfficeSection(JwtAuthority.class.getName(),
				JwtAuthoritySectionSource.class.getName(), null);

		// Link the JWT Authority flows
		officeArchitect.link(jwtAuthoritySource.getOfficeManagedObjectFlow(Flows.RETRIEVE_ENCODE_KEYS.name()),
				section.getOfficeSectionInput(JwtAuthoritySectionSource.INPUT_RETRIEVE_ENCODE_KEYS));
	}

}