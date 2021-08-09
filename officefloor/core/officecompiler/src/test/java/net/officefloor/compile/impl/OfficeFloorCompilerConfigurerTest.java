/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl;

import java.net.URL;
import java.net.URLClassLoader;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.OfficeFloorCompilerConfigurer;
import net.officefloor.compile.OfficeFloorCompilerConfigurerContext;
import net.officefloor.compile.OfficeFloorCompilerConfigurerServiceFactory;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeFloorCompilerConfigurerTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCompilerConfigurerTest extends OfficeFrameTestCase
		implements OfficeFloorCompilerConfigurer, OfficeFloorCompilerConfigurerServiceFactory {

	/**
	 * Flags whether to add the {@link OfficeFloorListener}.
	 */
	private static boolean isAddListener = false;

	/**
	 * Possible {@link ClassLoader} to configured into {@link OfficeFloorCompiler}.
	 */
	private static ClassLoader configureClassLoader = null;

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
		configureClassLoader = null;
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
	 * Ensure reports issue to {@link CompilerIssues} on failing to configure the
	 * {@link OfficeFloorCompiler}.
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

	/**
	 * Ensure can specify child {@link ClassLoader}.
	 */
	public void testSpecifyChildClassLoader() throws Exception {

		// Create compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		NodeContext context = (NodeContext) compiler;

		// Create child class loader
		ClassLoader childClassLoader = new URLClassLoader(new URL[0], compiler.getClassLoader());
		configureClassLoader = childClassLoader;

		// Compile and ensure override class loader
		assertNotNull("Should compile OfficeFloor", compiler.compile("OfficeFloor"));
		assertSame("Incorrect child class loader", childClassLoader, compiler.getClassLoader());

		// Ensure root source context now uses class loader
		assertSame("Incorrect source context class loader", childClassLoader,
				context.getRootSourceContext().getClassLoader());
	}

	/**
	 * Ensure can specify same {@link ClassLoader}.
	 */
	public void testSpecifySameClassLoader() throws Exception {

		// Create compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

		// Create child class loader
		ClassLoader childClassLoader = compiler.getClassLoader();
		configureClassLoader = childClassLoader;

		// Compile and ensure override class loader
		assertNotNull("Should compile OfficeFloor", compiler.compile("OfficeFloor"));
		assertSame("Incorrect same class loader", childClassLoader, compiler.getClassLoader());
	}

	/**
	 * Ensure not able to specify non-child {@link ClassLoader}.
	 */
	public void testNotSpecifyNonChildClassLoader() throws Exception {

		MockCompilerIssues issues = new MockCompilerIssues(this);

		// Create compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		NodeContext context = (NodeContext) compiler;

		// Obtain class loader
		ClassLoader classLoader = compiler.getClassLoader();
		configureClassLoader = new URLClassLoader(new URL[0], null);

		// Record issue in configuring class loader
		issues.addIssue(compiler, this.getClass().getName() + " failed to configure OfficeFloorCompiler",
				new IllegalArgumentException("ClassLoader must be a child of existing ClassLoader"));

		// Compile
		this.replayMockObjects();
		OfficeFloor officeFloor = compiler.compile("OfficeFloor");
		this.verifyMockObjects();

		// Compile and ensure override class loader
		assertNull("Should not compile OfficeFloor", officeFloor);
		assertSame("Should not change class loader", classLoader, compiler.getClassLoader());

		// Ensure continue to use class loader
		assertSame("Source context should not change class loader", classLoader,
				context.getRootSourceContext().getClassLoader());
	}

	/*
	 * ================ OfficeFloorCompilerConfigurationService ================
	 */

	@Override
	public OfficeFloorCompilerConfigurer createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void configureOfficeFloorCompiler(OfficeFloorCompilerConfigurerContext context) throws Exception {
		if (isAddListener) {

			// Flag that configured
			isConfigured = true;

			// Determine if fail
			if (failure != null) {
				throw failure;
			}

			// Determine if configure class loader
			if (configureClassLoader != null) {
				context.setClassLoader(configureClassLoader);
			}

			// Ensure able to configure OfficeFloor compiler
			context.getOfficeFloorCompiler().addOfficeFloorListener(new OfficeFloorListener() {

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
