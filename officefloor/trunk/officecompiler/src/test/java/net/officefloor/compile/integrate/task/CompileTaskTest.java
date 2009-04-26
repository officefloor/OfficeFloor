/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.integrate.task;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.test.issues.StderrCompilerIssuesWrapper;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;

/**
 * Tests compiling a {@link Task}.
 * 
 * @author Daniel
 */
public class CompileTaskTest extends AbstractCompileTestCase {

	@Override
	protected CompilerIssues enhanceIssues(CompilerIssues issues) {
		return new StderrCompilerIssuesWrapper(issues);
	}

	/**
	 * Tests compiling a simple {@link Task}.
	 */
	public void testSimpleTask() throws Exception {

		// TODO add test back in once refactoring loaders
		if (true) return;

		// Record building the office floor
		this.record_officefloor_addTeam("TEAM", OnePersonTeamSource.class);

		// Compile the office floor
		this.compile(true);
	}

}