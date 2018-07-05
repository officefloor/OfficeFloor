/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.integrate.profile;

import java.util.List;

import net.officefloor.extension.CompileOffice;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.ProfiledManagedFunction;
import net.officefloor.frame.api.profile.ProfiledProcessState;
import net.officefloor.frame.api.profile.ProfiledThreadState;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Tests making the {@link Profiler} available to the {@link OfficeFrame}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProfilerIntegrationTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to configure the {@link Profiler}.
	 */
	public void testConfigureProfiler() throws Exception {

		// Configure OfficeFloor
		CompileOffice compile = new CompileOffice();

		// Configure the profiler
		final ProfiledProcessState[] profiledProcess = new ProfiledProcessState[1];
		compile.getOfficeFloorCompiler().addProfiler("OFFICE", new Profiler() {
			@Override
			public void profileProcessState(ProfiledProcessState process) {
				profiledProcess[0] = process;
			}
		});

		// Compile the Office
		OfficeFloor officeFloor = compile.compileAndOpenOffice((architect, context) -> {
			architect.addOfficeSection("SECTION", ClassSectionSource.class.getName(), ProfiledClass.class.getName());
		});

		// Invoke the function
		officeFloor.getOffice("OFFICE").getFunctionManager("SECTION.function").invokeProcess(null, null);
		officeFloor.closeOfficeFloor();

		// Ensure profiled
		assertNotNull("Should be profiling office", profiledProcess[0]);
		List<ProfiledThreadState> threads = profiledProcess[0].getProfiledThreadStates();
		assertEquals("Should have one thread profiled", 1, threads.size());
		List<ProfiledManagedFunction> functions = threads.get(0).getProfiledManagedFunctions();
		assertEquals("Should just be one function profiled", 1, functions.size());
		ProfiledManagedFunction function = functions.get(0);
		assertEquals("Incorrect profiled function", "SECTION.function", function.getFunctionName());
	}

	/**
	 * Profiled {@link Class}.
	 */
	public static class ProfiledClass {
		public void function() {
		}
	}

}