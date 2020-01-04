/*-
 * #%L
 * Web Security
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
