/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.compile.impl;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.OfficeFloorCompilerConfigurationService;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeFloorCompilerConfigurationServiceTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCompilerConfigurationServiceTest extends OfficeFrameTestCase
		implements OfficeFloorCompilerConfigurationService {

	/**
	 * Flags whether to add the {@link OfficeFloorListener}.
	 */
	private static boolean isAddListener = false;

	/**
	 * Flags whether configured.
	 */
	private static boolean isConfigured = false;

	/**
	 * Indicates if open {@link OfficeFloor}.
	 */
	private static boolean isOpen = false;

	/**
	 * Indicates if close {@link OfficeFloor}.
	 */
	private static boolean isClosed = false;

	/**
	 * Failure.
	 */
	private static Exception failure = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Reset to ensure valid test
		isConfigured = false;
		isOpen = false;
		isClosed = false;
		failure = null;

		// Flag to add listener
		isAddListener = true;
	}

	@Override
	protected void tearDown() throws Exception {

		// Flag for remaining tests to not add listener
		isAddListener = false;

		// Do remaining tear down
		super.tearDown();
	}

	/**
	 * Ensure can configure the {@link OfficeFloorCompiler}.
	 */
	public void testConfigureCompiler() throws Exception {

		// Create compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		assertFalse("Should only configure compiler before compiling", isConfigured);

		// Compile the OfficeFloor
		OfficeFloor officeFloor = compiler.compile("OfficeFloor");
		assertTrue("Should have configured compiler", isConfigured);
		assertFalse("Should not yet be open", isOpen);

		// Open the OfficeFloor
		officeFloor.openOfficeFloor();
		assertTrue("Should be open", isOpen);
		assertFalse("Should not yet be closed", isClosed);

		// Close the OfficeFloor
		officeFloor.closeOfficeFloor();
		assertTrue("Should be closed", isClosed);
	}

	/**
	 * Ensure reports issue to {@link CompilerIssues} on failing to configure
	 * the {@link OfficeFloorCompiler}.
	 */
	public void testFailConfigureCompiler() throws Exception {

		MockCompilerIssues issues = new MockCompilerIssues(this);

		// Create compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		assertFalse("Should only configure compiler before compiling", isConfigured);

		// Record issue in configuring compiler
		failure = new Exception("TEST");
		issues.addIssue(compiler, this.getClass().getName() + " failed to configure OfficeFloorCompiler", failure);

		// Attempt to compile
		this.replayMockObjects();
		OfficeFloor officeFloor = compiler.compile("OfficeFloor");
		assertNull("Should not compile OfficeFloor", officeFloor);
		this.verifyMockObjects();
	}

	/*
	 * ================ OfficeFloorCompilerConfigurationService ================
	 */

	@Override
	public void configureOfficeFloorCompiler(OfficeFloorCompiler compiler) throws Exception {
		if (isAddListener) {

			// Flag that configured
			isConfigured = true;

			// Determine if fail
			if (failure != null) {
				throw failure;
			}

			// Ensure able to configure OfficeFloor compiler
			compiler.addOfficeFloorListener(new OfficeFloorListener() {

				@Override
				public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
					isOpen = true;
				}

				@Override
				public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
					isClosed = true;
				}
			});
		}
	}

}