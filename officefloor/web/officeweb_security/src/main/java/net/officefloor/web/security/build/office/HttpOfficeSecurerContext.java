/*-
 * #%L
 * Web Security
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
