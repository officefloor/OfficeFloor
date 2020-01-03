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