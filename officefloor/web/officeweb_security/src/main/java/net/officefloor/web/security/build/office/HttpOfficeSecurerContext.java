package net.officefloor.web.security.build.office;

import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.web.security.HttpAccessControl;

/**
 * Context for the {@link HttpOfficeSecurer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpOfficeSecurerContext {

	/**
	 * Obtains the {@link OfficeAdministration} to undertake
	 * {@link HttpAccessControl} for the {@link HttpOfficeSecurer}.
	 * 
	 * @return {@link OfficeAdministration}.
	 */
	OfficeAdministration getAdministration();

	/**
	 * Creates a {@link OfficeFlowSinkNode} to either a secure / insecure
	 * {@link OfficeFlowSinkNode}.
	 * 
	 * @param argumentType
	 *            Type of argument to {@link Flow}. May be <code>null</code> if
	 *            no argument.
	 * @param secureFlowSink
	 *            Secure {@link OfficeFlowSinkNode}.
	 * @param insecureFlowSink
	 *            Insecure {@link OfficeFlowSinkNode}.
	 * @return {@link OfficeFlowSinkNode} to either a secure / insecure
	 *         {@link OfficeFlowSinkNode}.
	 */
	OfficeFlowSinkNode secureFlow(Class<?> argumentType, OfficeFlowSinkNode secureFlowSink,
			OfficeFlowSinkNode insecureFlowSink);

}