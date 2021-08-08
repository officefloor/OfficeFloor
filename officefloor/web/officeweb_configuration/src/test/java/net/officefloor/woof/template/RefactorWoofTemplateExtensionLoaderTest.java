/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.template;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.compile.properties.Property;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.test.ClassLoaderTestSupport;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
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
@ExtendWith(TestSupportExtension.class)
public class RefactorWoofTemplateExtensionLoaderTest {

	private final MockTestSupport mocks = new MockTestSupport();

	private final ClassLoaderTestSupport classLoading = new ClassLoaderTestSupport();

	/**
	 * {@link WoofTemplateExtensionLoader} to test.
	 */
	private final WoofTemplateExtensionLoader loader = new WoofTemplateExtensionLoaderImpl();

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext = this.mocks.createMock(ConfigurationContext.class);

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext = this.mocks.createMock(SourceContext.class);

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

	/**
	 * {@link WoofChangeIssues}.
	 */
	private final WoofChangeIssues issues = this.mocks.createMock(WoofChangeIssues.class);

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
		this.mocks.recordReturn(this.sourceContext, this.sourceContext.getClassLoader(), this.classLoader);
		this.mocks.recordReturn(this.sourceContext, this.sourceContext.getName(), "template");
		this.mocks.recordReturn(this.sourceContext, this.sourceContext.getProfiles(), Collections.emptyList());
	}

	/**
	 * Ensure handle unknown {@link WoofTemplateExtensionSource}.
	 */
	@Test
	public void unknownTemplateExtensionSource() {

		final UnknownClassError error = new UnknownClassError("UNKNOWN CLASS");

		// Configure extension
		MockWoofTemplateExtensionSource.reset(this, null, null, null);

		// Record adapting
		this.recordSetupContext();

		// Record failing to create extension
		this.mocks.recordThrows(this.sourceContext, this.sourceContext.loadClass("UNKNOWN CLASS"), error);

		// Attempt to refactor extension
		this.refactorTemplateExtension("UNKNOWN CLASS", "OLD", null, null, null, (change) -> {

			// Ensure correct conflict reporting issue
			assertConflictForChange(change, "Extension UNKNOWN CLASS on template OLD prevented change as "
					+ error.getMessage() + " [" + error.getClass().getName() + "]");
		});
	}

	/**
	 * Ensure handles failure in instantiating the
	 * {@link WoofTemplateExtensionSource}.
	 */
	@Test
	public void instantiateFailureOfTemplateExtensionSource() {

		final Error failure = new Error("TEST");

		// Configure extension
		MockWoofTemplateExtensionSource.reset(this, failure, null, null);

		// Record adapting
		this.recordSetupContext();

		// Do refactoring
		this.refactorTemplateExtension("OLD", new String[] { "OldName", "OldValue" }, "NEW",
				new String[] { "NewName", "NewValue" }, (change) -> {

					// Ensure correct conflict reporting issue
					assertConflictForChange(change, "Extension " + MockWoofTemplateExtensionSource.class.getName()
							+ " on template OLD prevented change as TEST [" + failure.getClass().getName() + "]");
				});
	}

	/**
	 * Ensure handles failure in creating the {@link Change}.
	 */
	@Test
	public void createChangeFailureOfTemplateExtensionSource() {

		final RuntimeException failure = new RuntimeException("TEST");

		// Configure extension
		MockWoofTemplateExtensionSource.reset(this, null, failure, null);

		// Record adapting
		this.recordSetupContext();

		// Do refactoring
		this.refactorTemplateExtension("OLD", new String[] { "OldName", "OldValue" }, "NEW",
				new String[] { "NewName", "NewValue" }, (change) -> {

					// Ensure correct conflict reporting issue
					assertConflictForChange(change, "Extension " + MockWoofTemplateExtensionSource.class.getName()
							+ " on template OLD prevented change as TEST [" + failure.getClass().getName() + "]");
				});
	}

	/**
	 * Ensure can provide no {@link Change}.
	 */
	@Test
	public void noChange() throws Exception {

		// Configure extension
		MockWoofTemplateExtensionSource.reset(this, null, null, null);

		// Record adapting
		this.recordSetupContext();

		// Do refactoring
		this.refactorTemplateExtension("OLD", new String[] { "OldName", "OldValue" }, "NEW",
				new String[] { "NewName", "NewValue" }, (change) -> {

					// Ensure no change
					assertNull(change, "Should be no change");
				});
	}

	/**
	 * Ensure can create {@link Change}.
	 */
	@Test
	public void createChange() throws Exception {

		final Change<?> mockChange = this.mocks.createMock(Change.class);

		// Configure extension
		MockWoofTemplateExtensionSource.reset(this, null, null, new MockChangeFactory(mockChange));

		// Record adapting
		this.recordSetupContext();

		// Do refactoring
		this.refactorTemplateExtension("OLD", new String[] { "OldName", "OldValue" }, "NEW",
				new String[] { "NewName", "NewValue" }, (change) -> {

					// Ensure correct change
					assertSame(mockChange, change, "Incorrect change");
				});
	}

	/**
	 * Ensure can create {@link Change} without providing the
	 * {@link WoofTemplateExtensionConfiguration} (for adding/removing the
	 * extension).
	 */
	@Test
	public void withNoConfiguration() throws Exception {

		final Change<?> mockChange = this.mocks.createMock(Change.class);

		// Configure extension
		MockWoofTemplateExtensionSource.reset(this, null, null, new MockChangeFactory(mockChange));

		// Record adapting
		this.recordSetupContext();

		// Do refactoring with no configuration
		this.refactorTemplateExtension(null, null, null, null, (change) -> {

			// Ensure correct change
			assertSame(mockChange, change, "Incorrect change");
		});
	}

	/**
	 * Ensure may report issue to {@link WoofChangeIssues}.
	 */
	@Test
	public void changeIssue() {

		// Configure extension
		MockWoofTemplateExtensionSource.reset(this, null, null, new IssueChange());

		// Record adapting
		this.recordSetupContext();

		// Record the change issues
		this.issues.addIssue("Template OLD Extension " + MockWoofTemplateExtensionSource.class.getName() + ": APPLY");
		this.issues.addIssue("Template OLD Extension " + MockWoofTemplateExtensionSource.class.getName() + ": REVERT");

		// Do refactoring with no configuration
		this.refactorTemplateExtension("OLD", new String[0], null, null, (change) -> {

			// Trigger reporting issues
			change.apply();
			change.revert();
		});
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
	@Test
	public void adaptRefactor() throws Exception {

		final ClassLoader adaptClassLoader = this.classLoading.createNewClassLoader();
		final String adaptClassName = AdaptWoofTemplateExtensionSource.class.getName();
		final Class<?> adaptClass = adaptClassLoader.loadClass(adaptClassName);

		// Record adapting
		this.mocks.recordReturn(this.sourceContext, this.sourceContext.getClassLoader(), adaptClassLoader);
		this.mocks.recordReturn(this.sourceContext, this.sourceContext.getName(), "template");
		this.mocks.recordReturn(this.sourceContext, this.sourceContext.getProfiles(), Collections.emptyList());

		// Record loading class
		this.mocks.recordReturn(this.sourceContext, this.sourceContext.loadClass(adaptClassName), adaptClass);

		// Do refactoring requiring adapting
		this.refactorTemplateExtension(adaptClassName, null, null, null, null, (change) -> {

			// Ensure can obtain conflicts
			assertConflictForChange(change, "CONFLICT");

			// Ensure correct change
			assertEquals("CHANGE", change.getChangeDescription(), "Incorrect change");
		});
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
		assertFalse(change.canApply(), "Should not be able to apply change");
		Conflict[] conflicts = change.getConflicts();
		assertEquals(1, conflicts.length, "Incorrect number of conflicts");
		assertEquals(expectedConflictDescription, conflicts[0].getConflictDescription(), "Incorrect conflict");
	}

	/**
	 * Undertakes the refactoring of the {@link WoofTemplateExtensionSource}.
	 * 
	 * @param oldUri                Old URI.
	 * @param oldPropertyNameValues Old {@link Property} name/value pairs.
	 * @param newUri                New URI.
	 * @param newPropertyNameValues New {@link Property} name/value pairs.
	 * @param verifier              Verifies the {@link Change}.
	 * @throws WoofTemplateExtensionException If failure in refactoring.
	 */
	private void refactorTemplateExtension(String oldUri, String[] oldPropertyNameValues, String newUri,
			String[] newPropertyNameValues, Consumer<Change<?>> verifier) {

		// Record creating the extension
		this.mocks.recordReturn(this.sourceContext,
				this.sourceContext.loadClass(MockWoofTemplateExtensionSource.class.getName()),
				MockWoofTemplateExtensionSource.class);

		// Undertake the refactoring of the template extension
		this.refactorTemplateExtension(MockWoofTemplateExtensionSource.class.getName(), oldUri, oldPropertyNameValues,
				newUri, newPropertyNameValues, verifier);
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
	 * @param verifier                 Verifies the {@link Change}.
	 * @throws WoofTemplateExtensionException If failure in refactoring.
	 */
	private void refactorTemplateExtension(String extensionSourceClassName, String oldUri,
			String[] oldPropertyNameValues, String newUri, String[] newPropertyNameValues,
			Consumer<Change<?>> verifier) {

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
		this.mocks.replayMockObjects();
		Change<?> change = this.loader.refactorTemplateExtension(extensionSourceClassName, oldUri, oldProperties,
				newUri, newProperties, this.configurationContext, this.sourceContext, this.issues);
		verifier.accept(change);

		// Determine if verify
		this.mocks.verifyMockObjects();
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
			assertSame(test.configurationContext, context.getConfigurationContext(), "Incorrect configuration context");

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
				assertNull(configuration, "Should not have " + configurationType + " configuration");
				return;
			}

			// Validate the configuration
			assertNotNull(configuration, "Should have " + configurationType + " configuration");
			assertEquals(uri, configuration.getApplicationPath(), "Incorrect " + configurationType + " URI");
			assertEquals((properties.length / 2), configuration.getPropertyNames().length,
					"Incorrect number of " + configurationType + " properties");
			for (int i = 0; i < properties.length; i += 2) {
				String name = properties[i];
				String value = properties[i + 1];
				assertEquals(value, configuration.getProperty(name),
						"Incorrect " + configurationType + " '" + name + "' property value");
			}
		}

		@Override
		public void extendTemplate(WoofTemplateExtensionSourceContext context) throws Exception {
			fail("Should not be invoked for refactoring template extension");
		}
	}

}
