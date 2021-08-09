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
