package net.officefloor.web.security.build;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.web.security.build.office.HttpOfficeSecurer;
import net.officefloor.web.security.build.section.HttpFlowSecurer;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Configures {@link HttpSecurity} around configuration for the
 * {@link OfficeArchitect} and {@link SectionDesigner}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurer {

	/**
	 * Registers {@link HttpOfficeSecurer}.
	 * 
	 * @param securer
	 *            {@link HttpOfficeSecurer}.
	 */
	void secure(HttpOfficeSecurer securer);

	/**
	 * Creates the {@link HttpFlowSecurer}.
	 * 
	 * @return {@link HttpFlowSecurer}.
	 */
	HttpFlowSecurer createFlowSecurer();

}