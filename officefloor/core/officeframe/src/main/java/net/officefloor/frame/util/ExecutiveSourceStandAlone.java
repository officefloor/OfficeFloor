/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.util;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.executive.ExecutiveSourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.impl.execute.execution.ManagedExecutionFactoryImpl;
import net.officefloor.frame.impl.execute.execution.ThreadFactoryManufacturer;
import net.officefloor.frame.internal.structure.ManagedExecutionFactory;
import net.officefloor.frame.test.MockClockFactory;

/**
 * Loads {@link ExecutiveSource} for stand-alone use.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveSourceStandAlone {

	/**
	 * {@link SourceProperties}.
	 */
	private final SourcePropertiesImpl properties = new SourcePropertiesImpl();

	/**
	 * {@link Thread} decorator. May be <code>null</code>.
	 */
	private Consumer<Thread> threadDecorator = null;

	/**
	 * {@link ThreadCompletionListener} instances.
	 */
	private final List<ThreadCompletionListener> threadCompletionListeners = new LinkedList<>();

	/**
	 * {@link ClockFactory}.
	 */
	private ClockFactory clockFactory = new MockClockFactory();

	/**
	 * Adds a property for the {@link ManagedObjectSource}.
	 * 
	 * @param name  Name of the property.
	 * @param value Value for the property.
	 */
	public void addProperty(String name, String value) {
		this.properties.addProperty(name, value);
	}

	/**
	 * Specifies the decorator of the {@link Thread} instances created by the
	 * {@link ExecutiveSourceContext}.
	 * 
	 * @param decorator {@link Thread} decorator.
	 */
	public void setThreadDecorator(Consumer<Thread> decorator) {
		this.threadDecorator = decorator;
	}

	/**
	 * Adds a {@link ThreadCompletionListener}.
	 * 
	 * @param threadCompletionListener {@link ThreadCompletionListener}.
	 */
	public void addThreadCompletionListener(ThreadCompletionListener threadCompletionListener) {
		this.threadCompletionListeners.add(threadCompletionListener);
	}

	/**
	 * Specifies the {@link ClockFactory}.
	 * 
	 * @param clockFactory {@link ClockFactory}.
	 */
	public void setClockFactory(ClockFactory clockFactory) {
		this.clockFactory = clockFactory;
	}

	/**
	 * Creates the {@link Executive}.
	 * 
	 * @param <XS>                 {@link ExecutiveSource} type.
	 * @param executiveSourceClass Class of the {@link ExecutiveSource}.
	 * @return Loaded {@link Executive}.
	 * @throws Exception If fails to initialise {@link ExecutiveSource}.
	 */
	public <XS extends ExecutiveSource> Executive loadExecutive(Class<XS> executiveSourceClass) throws Exception {

		// Create a new instance of the executive source
		XS executiveSource = executiveSourceClass.getDeclaredConstructor().newInstance();

		// Return the loaded executive
		return loadExecutive(executiveSource);
	}

	/**
	 * Returns a {@link Executive} from the loaded {@link ExecutiveSource}.
	 * 
	 * @param executiveSource {@link ExecutiveSource}.
	 * @return Loaded {@link Executive}.
	 * @throws Exception If fails to initialise {@link ExecutiveSource}.
	 */
	public Executive loadExecutive(ExecutiveSource executiveSource) throws Exception {

		// Create executive source context
		SourceContext sourceContext = new SourceContextImpl(this.getClass().getName(), false, new String[0],
				Thread.currentThread().getContextClassLoader(), this.clockFactory);
		ManagedExecutionFactory managedExecutionFactory = new ManagedExecutionFactoryImpl(
				this.threadCompletionListeners.toArray(new ThreadCompletionListener[0]));
		ThreadFactoryManufacturer threadFactoryManufacturer = new ThreadFactoryManufacturer(managedExecutionFactory,
				this.threadDecorator);
		ExecutiveSourceContextImpl context = new ExecutiveSourceContextImpl(false, sourceContext, this.properties,
				threadFactoryManufacturer);

		// Return the created executive
		return executiveSource.createExecutive(context);
	}

}
