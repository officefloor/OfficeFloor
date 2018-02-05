/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.web.security.build.section;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFlowSourceNode;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Provides {@link HttpSecurity} {@link Flow} decision.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpFlowSecurer {

	/**
	 * Links the {@link SectionFlowSourceNode} to either a secure / insecure
	 * {@link SectionFlowSinkNode}.
	 * 
	 * @param designer
	 *            {@link SectionDesigner}.
	 * @param flowSourceNode
	 *            {@link SectionFlowSourceNode}.
	 * @param secureFlowSink
	 *            Secure {@link SectionFlowSinkNode}.
	 * @param insecureFlowSink
	 *            Insecure {@link SectionFlowSinkNode}.
	 */
	void link(SectionDesigner designer, SectionFlowSourceNode flowSourceNode, SectionFlowSinkNode secureFlowSink,
			SectionFlowSinkNode insecureFlowSink);

}