/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.jndi.context;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.sun.jndi.url.mock.ContextEnvironmentValidator;
import com.sun.jndi.url.mock.mockURLContextFactory;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;

/**
 * Tests the {@link JndiContextManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiContextManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(JndiContextManagedObjectSource.class);
	}

	/**
	 * Validates the type.
	 */
	public void testType() {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(Context.class);

		// Validate the type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, JndiContextManagedObjectSource.class);
	}

	/**
	 * Ensure able to obtain the {@link Context} (specifically the
	 * {@link InitialContext}).
	 */
	public void testObtainInitialContext() throws Throwable {
		// Source the InitialContext (no configuration)
		Context context = this.sourceContext(null);
		assertNotNull("Must have context", context);
	}

	/**
	 * Ensure able to obtain the configured {@link Context}.
	 */
	public void testObtainConfiguredContext() throws Throwable {

		final Context context = this.createMock(Context.class);

		// Record using context to ensure correct context
		context.rename("correct", "context");

		// Replay
		this.replayMockObjects();

		// Source the Context
		Context sourcedContext = this.sourceContext(context, Context.INITIAL_CONTEXT_FACTORY,
				mockURLContextFactory.class.getName());

		// Ensure correct Context
		sourcedContext.rename("correct", "context");

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain sub {@link Context} without validation.
	 */
	public void testObtainSubContextWithoutValidation() throws Throwable {

		// Mocks
		final Context context = this.createMock(Context.class);
		final Context subContext = this.createMock(Context.class);

		// Record create and return Context
		this.recordReturn(context, context.lookup("test"), subContext);

		// Replay
		this.replayMockObjects();

		// Source the Context
		Context sourcedContext = this.sourceContext(context, Context.INITIAL_CONTEXT_FACTORY,
				mockURLContextFactory.class.getName(), JndiContextManagedObjectSource.PROPERTY_SUB_CONTEXT_NAME,
				"test");

		// Ensure correct Context
		assertEquals("Incorrect Context", subContext, sourcedContext);

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain sub {@link Context} with validation.
	 */
	public void testObtainSubContextWithValidation() throws Throwable {

		// Mocks
		final Context context = this.createMock(Context.class);
		final Context subContext = this.createMock(Context.class);

		// Record obtain lookup twice.
		// 1) validate configuration by creating Context
		// 2) create and return Context
		this.recordReturn(context, context.lookup("test"), subContext);
		this.recordReturn(context, context.lookup("test"), subContext);

		// Replay
		this.replayMockObjects();

		// Source the Context
		Context sourcedContext = this.sourceContext(context, Context.INITIAL_CONTEXT_FACTORY,
				mockURLContextFactory.class.getName(), JndiContextManagedObjectSource.PROPERTY_SUB_CONTEXT_NAME, "test",
				JndiContextManagedObjectSource.PROPERTY_VALIDATE, "true");

		// Ensure correct Context
		assertEquals("Incorrect Context", subContext, sourcedContext);

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Sources the {@link Context} from the
	 * {@link JndiContextManagedObjectSource}.
	 * 
	 * @param context
	 *            {@link Context}.
	 * @param nameValuePropertyPairs
	 *            Name value property pairs.
	 * @return Sourced {@link Context}.
	 */
	private Context sourceContext(Context context, final String... nameValuePropertyPairs) throws Throwable {

		// Setup to source InitialContext
		mockURLContextFactory.reset();
		mockURLContextFactory.setContext(context);
		mockURLContextFactory.addValidator(new ContextEnvironmentValidator() {
			@Override
			public void validateEnvironment(Hashtable<?, ?> environment) {
				for (int i = 0; i < nameValuePropertyPairs.length; i += 2) {
					String name = nameValuePropertyPairs[i];
					String value = nameValuePropertyPairs[i + 1];
					assertEquals("Incorrect property", value, environment.get(name));
				}
			}
		});

		// Obtain the JNDI Context Managed Object Source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		for (int i = 0; i < nameValuePropertyPairs.length; i += 2) {
			String name = nameValuePropertyPairs[i];
			String value = nameValuePropertyPairs[i + 1];
			loader.addProperty(name, value);
		}
		JndiContextManagedObjectSource mos = loader.loadManagedObjectSource(JndiContextManagedObjectSource.class);

		// Obtain the Managed Object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		ManagedObject mo = user.sourceManagedObject(mos);

		// Obtain the Context
		Object object = mo.getObject();
		assertNotNull("Must have context", object);
		assertTrue("Must be Context type", object instanceof Context);
		assertTrue("Must be Synchronised Context", object instanceof SynchronisedContext);

		// Return the Context delegate
		return ((SynchronisedContext) object).getDelegateContext();
	}

}