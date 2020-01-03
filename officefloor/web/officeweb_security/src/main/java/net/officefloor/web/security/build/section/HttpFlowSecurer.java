package net.officefloor.web.security.build.section;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Provides {@link HttpSecurity} {@link Flow} decision.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpFlowSecurer {

	/**
	 * Creates a {@link SectionFlowSinkNode} to either a secure / insecure
	 * {@link SectionFlowSinkNode}.
	 * 
	 * @param designer
	 *            {@link SectionDesigner}.
	 * @param argumentType
	 *            Type of argument to the {@link Flow}. May be <code>null</code>
	 *            for no argument.
	 * @param secureFlowSink
	 *            Secure {@link SectionFlowSinkNode}.
	 * @param insecureFlowSink
	 *            Insecure {@link SectionFlowSinkNode}.
	 * @return {@link SectionFlowSinkNode} to either a secure / insecure
	 *         {@link SectionFlowSinkNode}.
	 */
	SectionFlowSinkNode secureFlow(SectionDesigner designer, Class<?> argumentType, SectionFlowSinkNode secureFlowSink,
			SectionFlowSinkNode insecureFlowSink);

}