/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.template;

import java.util.Collections;

import net.officefloor.compile.properties.Property;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.impl.change.NoChange;
import net.officefloor.woof.model.woof.WoofChangeIssues;
import net.officefloor.woof.model.woof.WoofTemplateExtensionModel;
import net.officefloor.woof.template.impl.AbstractWoofTemplateExtensionSource;

/**
 * Tests the {@link WoofTemplateExtensionLoader} to refactor.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorWoofTemplateExtensionLoaderTest extends OfficeFrameTestCase {

	/**
	 * {@link WoofTemplateExtensionLoader} to test.
	 */
	private final WoofTemplateExtensionLoader loader = new WoofTemplateExtensionLoaderImpl();

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext = this.createMock(ConfigurationContext.class);

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext = this.createMock(SourceContext.class);

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

	/**
	 * {@link WoofChangeIssues}.
	 */
	private final WoofChangeIssues issues = this.createMock(WoofChangeIssues.class);

	/**
	 * Indicates if verify after refactoring.
	 */
	private boolean isRefactorVerify = true;

	/**
	 * Old URI.
	 */
	private String oldUri;

	/**
	 * Old properties.
	 */
	private String[] oldProperties;

	/**
	 * New URI.
	 */
	private String newUri;

	/**
	 * New properties.
	 */
	private String[] newProperties;

	/**
	 * Record setting up {@link SourceContext}.
	 */
	private void recordSetupContext() {
		this.recordReturn(this.sourceContext, this.sourceContext.getClassLoader(), this.classLoader);
		this.recordReturn(this.sourceContext, this.sourceContext.getName(), "template");
		this.recordReturn(this.sourceContext, this.sourceContext.getProfiles(), Collections.emptyList());
	}

	/**
	 * Ensure handle unknown {@link WoofTemplateExtensionSource}.
	 */
	public void testUnknownTemplateExtensionSource() {

		final UnknownClassError error = new UnknownClassError("UNKNOWN CLASS");

		// Configure extension
		MockWoofTemplateExtensionSource.reset(this, null, null, null);

		// Record adapting
		this.recordSetupContext();

		// Record failing to create extension
		this.recordThrows(this.sourceContext, this.sourceContext.loadClass("UNKNOWN CLASS"), error);

		// Attempt to refactor extension
		Change<?> change = this.refactorTemplateExtension("UNKNOWN CLASS", "OLD", null, null, null);

		// Ensure correct conflict reporting issue
		assertConflictForChange(change, "Extension UNKNOWN CLASS on template OLD prevented change as "
				+ error.getMessage() + " [" + error.getClass().getName() + "]");
	}

	/**
	 * Ensure handles failure in instantiating the
	 * {@link WoofTemplateExtensionSource}.
	 */
	public void testInstantiateFailureOfTemplateExtensionSource() {

		final Error failure = new Error("TEST");

		// Configure extension
		MockWoofTemplateExtensionSource.reset(this, failure, null, null);

		// Record adapting
		this.recordSetupContext();

		// Do refactoring
		Change<?> change = this.refactorTemplateExtension("OLD", new String[] { "OldName", "OldValue" }, "NEW",
				new String[] { "NewName", "NewValue" });

		// Ensure correct conflict reporting issue
		assertConflictForChange(change, "Extension " + MockWoofTemplateExtensionSource.class.getName()
				+ " on template OLD prevented change as TEST [" + failure.getClass().getName() + "]");
	}

	/**
	 * Ensure handles failure in creating the {@link Change}.
	 */
	public void testCreateChangeFailureOfTemplateExtensionSource() {

		final RuntimeException failure = new RuntimeException("TEST");

		// Configure extension
		MockWoofTemplateExtensionSource.reset(this, null, failure, null);

		// Record adapting
		this.recordSetupContext();

		// Do refactoring
		Change<?> change = this.refactorTemplateExtension("OLD", new String[] { "OldName", "OldValue" }, "NEW",
				new String[] { "NewName", "NewValue" });

		// Ensure correct conflict reporting issue
		assertConflictForChange(change, "Extension " + MockWoofTemplateExtensionSource.class.getName()
				+ " on template OLD prevented change as TEST [" + failure.getClass().getName() + "]");
	}

	/**
	 * Ensure can provide no {@link Change}.
	 */
	public void testNoChange() throws Exception {

		// Configure extension
		MockWoofTemplateExtensionSource.reset(this, null, null, null);

		// Record adapting
		this.recordSetupContext();

		// Do refactoring
		Change<?> change = this.refactorTemplateExtension("OLD", new String[] { "OldName", "OldValue" }, "NEW",
				new String[] { "NewName", "NewValue" });

		// Ensure no change
		assertNull("Should be no change", change);
	}

	/**
	 * Ensure can create {@link Change}.
	 */
	public void testCreateChange() throws Exception {

		final Change<?> mockChange = this.createMock(Change.class);

		// Configure extension
		MockWoofTemplateExtensionSource.reset(this, null, null, new MockChangeFactory(mockChange));

		// Record adapting
		this.recordSetupContext();

		// Do refactoring
		Change<?> change = this.refactorTemplateExtension("OLD", new String[] { "OldName", "OldValue" }, "NEW",
				new String[] { "NewName", "NewValue" });

		// Ensure correct change
		assertSame("Incorrect change", mockChange, change);
	}

	/**
	 * Ensure can create {@link Change} without providing the
	 * {@link WoofTemplateExtensionConfiguration} (for adding/removing the
	 * extension).
	 */
	public void testWithNoConfiguration() throws Exception {

		final Change<?> mockChange = this.createMock(Change.class);

		// Configure extension
		MockWoofTemplateExtensionSource.reset(this, null, null, new MockChangeFactory(mockChange));

		// Record adapting
		this.recordSetupContext();

		// Do refactoring with no configuration
		Change<?> change = this.refactorTemplateExtension(null, null, null, null);

		// Ensure correct change
		assertSame("Incorrect change", mockChange, change);
	}

	/**
	 * Ensure may report issue to {@link WoofChangeIssues}.
	 */
	public void testChangeIssue() {

		// Configure extension
		MockWoofTemplateExtensionSource.reset(this, null, null, new IssueChange());

		// Record adapting
		this.recordSetupContext();

		// Record the change issues
		this.issues.addIssue("Template OLD Extension " + MockWoofTemplateExtensionSource.class.getName() + ": APPLY");
		this.issues.addIssue("Template OLD Extension " + MockWoofTemplateExtensionSource.class.getName() + ": REVERT");

		// Allow to run change and verify afterwards
		this.isRefactorVerify = false;

		// Do refactoring with no configuration
		Change<?> change = this.refactorTemplateExtension("OLD", new String[0], null, null);

		// Trigger reporting issues
		change.apply();
		change.revert();

		// Validate reported issues
		this.verifyMockObjects();
	}

	/**
	 * {@link Change} to report issue.
	 */
	private static class IssueChange extends AbstractChange<WoofTemplateExtensionModel> implements ChangeFactory {

		/**
		 * {@link WoofChangeIssues}.
		 */
		private WoofChangeIssues issues;

		/**
		 * Initiate.
		 */
		public IssueChange() {
			super(new WoofTemplateExtensionModel("TEST"), "Mock change");
		}

		/*
		 * ================= ChangeFactory =======================
		 */

		@Override
		public Change<?> createChange(WoofTemplateExtensionChangeContext context) {
			this.issues = context.getWoofChangeIssues();
			return this;
		}

		/*
		 * =================== Change ============================
		 */

		@Override
		public void apply() {
			// Report issue
			this.issues.addIssue("APPLY");
		}

		@Override
		public void revert() {
			// Report issue
			this.issues.addIssue("REVERT");
		}
	}

	/**
	 * Ensure can adapt the refactoring.
	 */
	public void testAdaptRefactor() throws Exception {

		final ClassLoader adaptClassLoader = createNewClassLoader();
		final String adaptClassName = AdaptWoofTemplateExtensionSource.class.getName();
		final Class<?> adaptClass = adaptClassLoader.loadClass(adaptClassName);

		// Record adapting
		this.recordReturn(this.sourceContext, this.sourceContext.getClassLoader(), adaptClassLoader);
		this.recordReturn(this.sourceContext, this.sourceContext.getName(), "template");
		this.recordReturn(this.sourceContext, this.sourceContext.getProfiles(), Collections.emptyList());

		// Record loading class
		this.recordReturn(this.sourceContext, this.sourceContext.loadClass(adaptClassName), adaptClass);

		// Do refactoring requiring adapting
		Change<?> change = this.refactorTemplateExtension(adaptClassName, null, null, null, null);

		// Ensure correct change
		assertEquals("Incorrect change", "CHANGE", change.getChangeDescription());

		// Ensure can obtain conflicts
		assertConflictForChange(change, "CONFLICT");
	}

	/**
	 * Mock {@link WoofTemplateExtensionSource} for adaption testing.
	 */
	public static class AdaptWoofTemplateExtensionSource extends AbstractWoofTemplateExtensionSource {

		@Override
		public Change<?> createConfigurationChange(WoofTemplateExtensionChangeContext context) {
			return new NoChange<WoofTemplateExtensionModel>(new WoofTemplateExtensionModel("TEST"), "CHANGE",
					"CONFLICT");
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not be invoked for refactoring template extension");
		}

		@Override
		public void extendTemplate(WoofTemplateExtensionSourceContext context) throws Exception {
			fail("Should not be invoked for refactoring template extension");
		}
	}

	/**
	 * Asserts a {@link Conflict} on the {@link Change}.
	 * 
	 * @param change                      {@link Change}.
	 * @param expectedConflictDescription Expected description for the
	 *                                    {@link Conflict}.
	 */
	private static void assertConflictForChange(Change<?> change, String expectedConflictDescription) {
		// Ensure no change and correct conflict reporting issue
		assertFalse("Should not be able to apply change", change.canApply());
		Conflict[] conflicts = change.getConflicts();
		assertEquals("Incorrect number of conflicts", 1, conflicts.length);
		assertEquals("Incorrect conflict", expectedConflictDescription, conflicts[0].getConflictDescription());
	}

	/**
	 * Undertakes the refactoring of the {@link WoofTemplateExtensionSource}.
	 * 
	 * @param oldUri                Old URI.
	 * @param oldPropertyNameValues Old {@link Property} name/value pairs.
	 * @param newUri                New URI.
	 * @param newPropertyNameValues New {@link Property} name/value pairs.
	 * @return {@link Change}.
	 * @throws WoofTemplateExtensionException If failure in refactoring.
	 */
	private Change<?> refactorTemplateExtension(String oldUri, String[] oldPropertyNameValues, String newUri,
			String[] newPropertyNameValues) {

		// Record creating the extension
		this.recordReturn(this.sourceContext,
				this.sourceContext.loadClass(MockWoofTemplateExtensionSource.class.getName()),
				MockWoofTemplateExtensionSource.class);

		// Undertake the refactoring of the template extension
		return this.refactorTemplateExtension(MockWoofTemplateExtensionSource.class.getName(), oldUri,
				oldPropertyNameValues, newUri, newPropertyNameValues);
	}

	/**
	 * Undertakes the refactoring of the {@link WoofTemplateExtensionSource}.
	 * 
	 * @param extensionSourceClassName {@link WoofTemplateExtensionSource} class
	 *                                 name.
	 * @param oldUri                   Old URI.
	 * @param oldPropertyNameValues    Old {@link Property} name/value pairs.
	 * @param newUri                   New URI.
	 * @param newPropertyNameValues    New {@link Property} name/value pairs.
	 * @return {@link Change}.
	 * @throws WoofTemplateExtensionException If failure in refactoring.
	 */
	private Change<?> refactorTemplateExtension(String extensionSourceClassName, String oldUri,
			String[] oldPropertyNameValues, String newUri, String[] newPropertyNameValues) {

		// Load the state for testing
		this.oldUri = oldUri;
		this.oldProperties = oldPropertyNameValues;
		this.newUri = newUri;
		this.newProperties = newPropertyNameValues;

		// Create the properties
		SourceProperties oldProperties = new SourcePropertiesImpl(
				oldPropertyNameValues == null ? new String[0] : oldPropertyNameValues);
		SourceProperties newProperties = new SourcePropertiesImpl(
				newPropertyNameValues == null ? new String[0] : newPropertyNameValues);

		// Test
		this.replayMockObjects();
		Change<?> change = this.loader.refactorTemplateExtension(extensionSourceClassName, oldUri, oldProperties,
				newUri, newProperties, this.configurationContext, this.sourceContext, this.issues);

		// Determine if verify
		if (this.isRefactorVerify) {
			this.verifyMockObjects();
		}

		// Return the change
		return change;
	}

	/**
	 * {@link ChangeFactory}.
	 */
	private static interface ChangeFactory {

		/**
		 * Creates the {@link Change}.
		 * 
		 * @param context {@link WoofTemplateExtensionChangeContext}.
		 * @return {@link Change}.
		 */
		Change<?> createChange(WoofTemplateExtensionChangeContext context);
	}

	/**
	 * Mock {@link ChangeFactory}.
	 */
	private static class MockChangeFactory implements ChangeFactory {

		/**
		 * {@link Change}.
		 */
		private final Change<?> change;

		/**
		 * Initiate.
		 * 
		 * @param change {@link Change}.
		 */
		public MockChangeFactory(Change<?> change) {
			this.change = change;
		}

		/*
		 * ================ ChangeFactory ====================
		 */

		@Override
		public Change<?> createChange(WoofTemplateExtensionChangeContext context) {
			return this.change;
		}
	}

	/**
	 * Mock {@link WoofTemplateExtensionSource}.
	 */
	public static class MockWoofTemplateExtensionSource extends AbstractWoofTemplateExtensionSource {

		/**
		 * {@link RefactorWoofTemplateExtensionLoaderTest}.
		 */
		private static RefactorWoofTemplateExtensionLoaderTest test;

		/**
		 * Failure in instantiating the extension.
		 */
		private static Error instantiateFailure;

		/**
		 * Failure in creating the extension.
		 */
		private static RuntimeException createFailure;

		/**
		 * {@link ChangeFactory}.
		 */
		private static ChangeFactory changeFactory = null;

		/**
		 * Resets for next test.
		 * 
		 * @param test               {@link RefactorWoofTemplateExtensionLoaderTest}.
		 * @param instantiateFailure Failure in instantiating the extension.
		 * @param createFailure      Failure in creating the extension.
		 * @param changeFactory      {@link ChangeFactory}.
		 */
		public static void reset(RefactorWoofTemplateExtensionLoaderTest test, Error instantiateFailure,
				RuntimeException createFailure, ChangeFactory changeFactory) {
			MockWoofTemplateExtensionSource.test = test;
			MockWoofTemplateExtensionSource.instantiateFailure = instantiateFailure;
			MockWoofTemplateExtensionSource.createFailure = createFailure;
			MockWoofTemplateExtensionSource.changeFactory = changeFactory;
		}

		/**
		 * Initiate to throw exception if configured.
		 */
		public MockWoofTemplateExtensionSource() {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * =================== WoofTemplateExtensionSource ==================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not be invoked for refactoring template extension");
		}

		@Override
		public Change<?> createConfigurationChange(WoofTemplateExtensionChangeContext context) {

			// Validate the configuration
			assertConfiguration("old", context.getOldConfiguration(), test.oldUri, test.oldProperties);
			assertConfiguration("new", context.getNewConfiguration(), test.newUri, test.newProperties);
			assertSame("Incorrect configuration context", test.configurationContext, context.getConfigurationContext());

			// Determine if failure to create
			if (createFailure != null) {
				throw createFailure;
			}

			// Potentially create and return the change
			return (changeFactory == null ? null : changeFactory.createChange(context));
		}

		/**
		 * Ensure {@link WoofTemplateExtensionConfiguration} is as expected.
		 * 
		 * @param configurationType Type of {@link WoofTemplateExtensionConfiguration}
		 *                          (old or new).
		 * @param configuration     {@link WoofTemplateExtensionConfiguration} to test.
		 * @param uri               Expected URI.
		 * @param properties        Expected properties.
		 */
		private static void assertConfiguration(String configurationType,
				WoofTemplateExtensionConfiguration configuration, String uri, String[] properties) {

			// Determine if should not have configuration
			if (uri == null) {
				assertNull("Should not have " + configurationType + " configuration", configuration);
				return;
			}

			// Validate the configuration
			assertNotNull("Should have " + configurationType + " configuration", configuration);
			assertEquals("Incorrect " + configurationType + " URI", uri, configuration.getApplicationPath());
			assertEquals("Incorrect number of " + configurationType + " properties", (properties.length / 2),
					configuration.getPropertyNames().length);
			for (int i = 0; i < properties.length; i += 2) {
				String name = properties[i];
				String value = properties[i + 1];
				assertEquals("Incorrect " + configurationType + " '" + name + "' property value", value,
						configuration.getProperty(name));
			}
		}

		@Override
		public void extendTemplate(WoofTemplateExtensionSourceContext context) throws Exception {
			fail("Should not be invoked for refactoring template extension");
		}
	}

}
