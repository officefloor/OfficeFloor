package net.officefloor.web.jwt.authority.jwks;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.web.jwt.authority.JwtAuthority;
import net.officefloor.web.jwt.jwks.JwksSectionSource;
import net.officefloor.web.jwt.validate.JwtValidateKey;

/**
 * <p>
 * JWKS publish {@link SectionSource}.
 * <p>
 * Publishes the {@link JwtValidateKey} instances from the {@link JwtAuthority}
 * for a {@link JwksSectionSource} (or other JWKS consumer) to consume.
 * 
 * @author Daniel Sagenschneider
 */
public class JwksPublishSectionSource extends AbstractSectionSource {

	/**
	 * Name of {@link SectionInput} to publish the {@link JwtValidateKey} instances.
	 */
	public static final String INPUT = "publish";

	/*
	 * ================= SectionSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {
		// TODO implement SectionSource.sourceSection(...)
		throw new UnsupportedOperationException("TODO implement SectionSource.sourceSection(...)");
	}

}