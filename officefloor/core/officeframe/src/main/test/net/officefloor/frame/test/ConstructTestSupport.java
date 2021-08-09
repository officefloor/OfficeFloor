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

package net.officefloor.frame.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.State;
import java.lang.reflect.Method;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Construction testing of an {@link Office} {@link TestSupport}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConstructTestSupport
		implements TestSupport, BeforeEachCallback, AfterEachCallback, EscalationHandler, MonitorClock {

	/**
	 * Index of the current {@link OfficeFloor} being constructed.
	 */
	private static int officeFloorIndex = 0;

	/**
	 * Index of the current {@link Office} being constructed.
	 */
	private static int officeIndex = 0;

	/**
	 * Constructed {@link OfficeFloor} instances that need to be closed on tear down
	 * of tests.
	 */
	private final List<OfficeFloor> constructedOfficeFloors = new LinkedList<OfficeFloor>();

	/**
	 * {@link Thread} instances used by the {@link OfficeFloor}.
	 */
	private final Deque<Thread> usedThreads = new ConcurrentLinkedDeque<>();

	/**
	 * {@link OfficeFloorBuilder}.
	 */
	private OfficeFloorBuilder officeFloorBuilder;

	/**
	 * {@link OfficeBuilder}.
	 */
	private OfficeBuilder officeBuilder;

	/**
	 * List of method names in order they are invoked by the
	 * {@link ReflectiveFunctionBuilder} instances for the test.
	 */
	private List<String> reflectiveFunctionInvokedMethods = new LinkedList<String>();

	/**
	 * <p>
	 * Flag indicating whether to record the invocations of the
	 * {@link ReflectiveFunctionBuilder} instances.
	 * <p>
	 * This is necessary as stress tests using the {@link ReflectiveFunctionBuilder}
	 * will get {@link OutOfMemoryError} issues should every {@link ManagedFunction}
	 * executed be recorded.
	 */
	private boolean isRecordReflectiveFunctionMethodsInvoked = false;

	/**
	 * {@link Throwable} for the {@link EscalationHandler}.
	 */
	private Throwable exception = null;

	/**
	 * Lock for the {@link EscalationHandler}.
	 */
	private final Object exceptionLock = new Object();

	/**
	 * Current time for the {@link MonitorClock}.
	 */
	private AtomicLong currentTime;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor = null;

	/**
	 * Listing of enhancers of the constructed {@link OfficeFloor} instances.
	 */
	private final List<Consumer<OfficeFloorBuilder>> officeFloorEnhancers = new LinkedList<>();

	/**
	 * Adds an {@link OfficeFloor} enhancer.
	 * 
	 * @param enhancer {@link OfficeFloor} enhancer.
	 */
	public void addOfficeFloorEnhancer(Consumer<OfficeFloorBuilder> enhancer) {

		// Run immediately if already initiated
		if (this.officeFloorBuilder != null) {
			enhancer.accept(this.officeFloorBuilder);
		}

		// Register the enhancer
		this.officeFloorEnhancers.add(enhancer);
	}

	/*
	 * ===================== TestSupport =============================
	 */

	/**
	 * {@link ThreadedTestSupport}.
	 */
	private ThreadedTestSupport threadedTestSupport;

	/**
	 * {@link LogTestSupport}.
	 */
	private LogTestSupport logTestSupport;

	/**
	 * Instantiate with required dependencies.
	 * 
	 * @param threadedTestSupport {@link ThreadedTestSupport}.
	 * @param logTestSupport      {@link LogTestSupport}.
	 */
	public ConstructTestSupport(ThreadedTestSupport threadedTestSupport, LogTestSupport logTestSupport) {
		this.threadedTestSupport = threadedTestSupport;
		this.logTestSupport = logTestSupport;
	}

	/**
	 * Default constructor for {@link TestSupport}.
	 */
	public ConstructTestSupport() {
		// Nothing to initialise
	}

	@Override
	public void init(ExtensionContext context) throws Exception {
		this.threadedTestSupport = TestSupportExtension.getTestSupport(ThreadedTestSupport.class, context);
		this.logTestSupport = TestSupportExtension.getTestSupport(LogTestSupport.class, context);
	}

	/*
	 * ==================== BeforeEachCallback =========================
	 */

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		TestSupportExtension.getTestSupport(ConstructTestSupport.class, context).beforeEach();
	}

	/**
	 * Undertakes the beforeEach functionality.
	 * 
	 * @throws Exception If fails.
	 */
	public void beforeEach() throws Exception {
		this.initiateNewOfficeFloorBuilder();
	}

	/**
	 * Initiates a new {@link OfficeFloorBuilder} for constructing another
	 * {@link OfficeFloor}.
	 */
	private void initiateNewOfficeFloorBuilder() {

		// Initiate for constructing office
		officeFloorIndex++;
		String officeFloorName = this.getOfficeFloorName();
		this.officeFloorBuilder = OfficeFrame.getInstance().createOfficeFloorBuilder(officeFloorName);
		officeIndex++;
		this.officeBuilder = this.officeFloorBuilder.addOffice(this.getOfficeName());

		// Enhance the OfficeFloor
		for (Consumer<OfficeFloorBuilder> enhancer : this.officeFloorEnhancers) {
			enhancer.accept(this.officeFloorBuilder);
		}

		// Initiate to receive the top level escalation to report back in tests
		this.officeFloorBuilder.setEscalationHandler((escalation) -> {

			// Indicate a OfficeFloor level escalation
			System.err.println("OFFICE FLOOR ESCALATION (" + officeFloorName + "): " + escalation.getMessage() + " ["
					+ escalation.getClass().getSimpleName() + " at " + escalation.getStackTrace()[0].toString() + "]");

			// Handle escalation
			this.handleEscalation(escalation);
		});

		// Initiate to control the time to be deterministic
		this.currentTime = new AtomicLong(System.currentTimeMillis());
		this.getOfficeBuilder().setMonitorClock(this);

		// No monitoring by default
		this.getOfficeBuilder().setMonitorOfficeInterval(0);

		// Track the teams (to ensure correctly stopped)
		this.officeFloorBuilder.setThreadDecorator((thread) -> this.usedThreads.add(thread));
	}

	/*
	 * ==================== AfterEachCallback =========================
	 */

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		TestSupportExtension.getTestSupport(ConstructTestSupport.class, context).afterEach();
	}

	/**
	 * Undertakes the afterEach functionality.
	 * 
	 * @throws Exception If fails.
	 */
	public void afterEach() throws Exception {

		// Close the constructed OfficeFloors
		for (OfficeFloor officeFloor : this.constructedOfficeFloors) {
			officeFloor.closeOfficeFloor();
		}

		// Give the thread pools some chance to terminate threads
		long maxEndTime = System.currentTimeMillis() + 1000;
		boolean isContinueChecking;
		do {
			// Determine if all complete
			isContinueChecking = false;
			for (Thread thread : this.usedThreads) {
				if (!State.TERMINATED.equals(thread.getState())) {
					isContinueChecking = true;
					Thread.sleep(1); // allow active threads to complete
				}
			}

			// If timed out waiting, just exit
			if (System.currentTimeMillis() > maxEndTime) {
				isContinueChecking = false;
			}
		} while (isContinueChecking);

		// Enough time provided, so ensure all threads terminated
		while (!this.usedThreads.isEmpty()) {
			Thread usedThread = this.usedThreads.remove();
			Assertions.assertEquals(State.TERMINATED, usedThread.getState(),
					"Thread " + usedThread.getName() + " should be terminated");
		}

		// Propagate possible failure
		synchronized (this.exceptionLock) {

			// Propagate failure
			if (this.exception != null) {
				if (this.exception instanceof Exception) {
					throw (Exception) this.exception;
				} else if (this.exception instanceof Error) {
					throw (Error) this.exception;
				} else {
					StringWriter buffer = new StringWriter();
					this.exception.printStackTrace(new PrintWriter(buffer));
					Assertions
							.fail("Unknown failure " + this.exception.getClass().getName() + ": " + buffer.toString());
				}
			}
		}
	}

	/*
	 * ====================== OfficeClock ================================
	 */

	@Override
	public long currentTimeMillis() {
		return this.currentTime.get();
	}

	/**
	 * Move current time forward the input number of milliseconds.
	 * 
	 * @param timeInMilliseconds Milliseconds to move current time forward.
	 */
	public void adjustCurrentTimeMillis(long timeInMilliseconds) {
		this.currentTime.addAndGet(timeInMilliseconds);
	}

	/*
	 * =================== EscalationHandler =============================
	 */

	@Override
	public void handleEscalation(Throwable escalation) throws Throwable {
		synchronized (this.exceptionLock) {
			// Record exception to be thrown later
			this.exception = escalation;
		}
	}

	/**
	 * <p>
	 * Validates that no top level escalation occurred.
	 * <p>
	 * This method will clear the escalation on exit.
	 * 
	 * @throws Throwable If top level {@link Escalation}.
	 */
	public void validateNoTopLevelEscalation() throws Throwable {
		synchronized (this.exceptionLock) {
			try {
				if (this.exception != null) {
					throw this.exception;
				}
			} finally {
				// Exception thrown, so have it cleared
				this.exception = null;
			}
		}
	}

	/**
	 * Obtains the {@link OfficeFloorBuilder}.
	 * 
	 * @return {@link OfficeFloorBuilder}.
	 */
	public OfficeFloorBuilder getOfficeFloorBuilder() {
		return this.officeFloorBuilder;
	}

	/**
	 * Obtains the {@link OfficeBuilder}.
	 * 
	 * @return {@link OfficeBuilder}.
	 */
	public OfficeBuilder getOfficeBuilder() {
		return this.officeBuilder;
	}

	/**
	 * Obtains the name of the {@link OfficeFloor} currently being constructed.
	 * 
	 * @return Name of the {@link OfficeFloor} currently being constructed.
	 */
	public String getOfficeFloorName() {
		return "officefloor-" + officeFloorIndex;
	}

	/**
	 * Obtains the name of the {@link Office} currently being constructed.
	 * 
	 * @return Name of the {@link Office} currently being constructed.
	 */
	public String getOfficeName() {
		return this.getOfficeName(officeIndex);
	}

	/**
	 * Obtains the name of the {@link Office} for specified index.
	 * 
	 * @param officeIndex Index of the {@link Office}.
	 * @return Name of the {@link Office} for the specified index.
	 */
	private String getOfficeName(int officeIndex) {
		return "office-" + officeIndex;
	}

	/**
	 * Constructs the {@link ReflectiveFunctionBuilder}.
	 * 
	 * @param object     {@link Object} containing the {@link Method}.
	 * @param methodName Name of the {@link Method}.
	 * @return {@link ReflectiveFunctionBuilder}.
	 */
	public ReflectiveFunctionBuilder constructFunction(Object object, String methodName) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ReflectiveFunctionBuilder builder = new ReflectiveFunctionBuilder((Class) object.getClass(), object, methodName,
				this.officeBuilder, this);
		return builder;
	}

	/**
	 * Constructs the {@link ReflectiveFunctionBuilder} for a static {@link Method}.
	 * 
	 * @param clazz      {@link Class} containing the static {@link Method}.
	 * @param methodName Name of the {@link Method}.
	 * @return {@link ReflectiveFunctionBuilder}.
	 */
	public ReflectiveFunctionBuilder constructStaticFunction(Class<?> clazz, String methodName) {
		return new ReflectiveFunctionBuilder(clazz, null, methodName, this.officeBuilder, this);
	}

	/**
	 * <p>
	 * Specifies whether to record the invocations of the
	 * {@link ReflectiveFunctionBuilder} instances.
	 * <p>
	 * This is necessary as stress tests using the {@link ReflectiveFunctionBuilder}
	 * will get {@link OutOfMemoryError} issues should every {@link ManagedFunction}
	 * executed be recorded.
	 * <p>
	 * By default this is <code>false</code> to not record.
	 * 
	 * @param isRecord <code>true</code> to record the {@link ManagedFunction}
	 *                 instances invoked.
	 */
	public void setRecordReflectiveFunctionMethodsInvoked(boolean isRecord) {
		synchronized (this.reflectiveFunctionInvokedMethods) {
			this.isRecordReflectiveFunctionMethodsInvoked = isRecord;
		}
	}

	/**
	 * Invoked by the {@link ReflectiveFunctionBuilder} when it executes the method.
	 * 
	 * @param methodName Name of method being invoked.
	 */
	public void recordReflectiveFunctionMethodInvoked(String methodName) {
		synchronized (this.reflectiveFunctionInvokedMethods) {
			if (this.isRecordReflectiveFunctionMethodsInvoked) {
				this.reflectiveFunctionInvokedMethods.add(methodName);
			}
		}
	}

	/**
	 * Validates the order the {@link ReflectiveFunctionBuilder} invoked the
	 * methods.
	 * 
	 * @param methodNames Order that the reflective methods should be invoked.
	 * @see #setRecordReflectiveFunctionMethodsInvoked(boolean)
	 */
	public void validateReflectiveMethodOrder(String... methodNames) {
		synchronized (this.reflectiveFunctionInvokedMethods) {

			// Create expected method calls
			StringBuilder expectedMethods = new StringBuilder();
			for (String methodName : methodNames) {
				expectedMethods.append(methodName.trim() + " ");
			}

			// Create the actual method calls
			StringBuilder actualMethods = new StringBuilder();
			for (String methodName : this.reflectiveFunctionInvokedMethods) {
				actualMethods.append(methodName.trim() + " ");
			}

			// Validate appropriate methods called
			Assertions.assertEquals(expectedMethods.toString(), actualMethods.toString(),
					"Incorrect methods invoked [" + actualMethods.toString() + "]");
		}
	}

	/**
	 * Ensures the {@link Thread} is used.
	 * 
	 * @param thread {@link Thread}.
	 */
	public void assertThreadUsed(Thread thread) {
		Assertions.assertTrue(this.usedThreads.contains(thread), "Thread is not used");
	}

	/**
	 * Facade method to register a {@link ManagedFunction}.
	 * 
	 * @param <O>             Dependency key type.
	 * @param <F>             Flow key type.
	 * @param functionName    Name of the {@link ManagedFunction}.
	 * @param functionFactory {@link ManagedFunctionFactory}.
	 * @return {@link ManagedFunctionBuilder} for the {@link ManagedFunction}.
	 */
	public <O extends Enum<O>, F extends Enum<F>> ManagedFunctionBuilder<O, F> constructFunction(String functionName,
			ManagedFunctionFactory<O, F> functionFactory) {
		return this.officeBuilder.addManagedFunction(functionName, functionFactory);
	}

	/**
	 * Facade method to register a {@link ManagedFunction}.
	 * 
	 * @param <O>          Dependency key type.
	 * @param <F>          Flow key type.
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @param function     {@link ManagedFunction}.
	 * @return {@link ManagedFunctionBuilder} for the {@link ManagedFunction}.
	 */
	public <O extends Enum<O>, F extends Enum<F>> ManagedFunctionBuilder<O, F> constructFunction(String functionName,
			final ManagedFunction<O, F> function) {

		// Create the Function Factory
		ManagedFunctionFactory<O, F> functionFactory = new ManagedFunctionFactory<O, F>() {
			public ManagedFunction<O, F> createManagedFunction() {
				return function;
			}
		};

		// Construct and return the function
		return this.constructFunction(functionName, functionFactory);
	}

	/**
	 * Facade method to register a {@link ManagedObject}.
	 * 
	 * @param <D>                      Dependency key type.
	 * @param <F>                      Flow key type.
	 * @param <MS>                     {@link ManagedObjectSource} type.
	 * @param managedObjectName        Name of the {@link ManagedObject}.
	 * @param managedObjectSourceClass {@link ManagedObjectSource} {@link Class}.
	 * @param managingOffice           Name of the managing {@link Office}. May be
	 *                                 <code>null</code> to manually register for
	 *                                 {@link ManagingOfficeBuilder}.
	 * @return {@link ManagedObjectBuilder}.
	 */
	public <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> ManagedObjectBuilder<F> constructManagedObject(
			String managedObjectName, Class<MS> managedObjectSourceClass, String managingOffice) {

		// Obtain the managed object source name
		String managedObjectSourceName = "of-" + managedObjectName;

		// Create the Managed Object Builder
		ManagedObjectBuilder<F> managedObjectBuilder = this.getOfficeFloorBuilder()
				.addManagedObject(managedObjectSourceName, managedObjectSourceClass);

		// Flag managing office
		if (managingOffice != null) {
			managedObjectBuilder.setManagingOffice(managingOffice);
		}

		// Link into the Office
		this.officeBuilder.registerManagedObjectSource(managedObjectName, managedObjectSourceName);

		// Return the Managed Object Builder
		return managedObjectBuilder;
	}

	/**
	 * Facade method to register a {@link ManagedObject}.
	 * 
	 * @param <D>                 Dependency key type.
	 * @param <F>                 Flow key type.
	 * @param <MS>                {@link ManagedObjectSource} type.
	 * @param managedObjectName   Name of the {@link ManagedObject}.
	 * @param managedObjectSource {@link ManagedObjectSource} instance.
	 * @param managingOffice      Name of the managing {@link Office}. May be
	 *                            <code>null</code> to manually register for
	 *                            {@link ManagingOfficeBuilder}.
	 * @return {@link ManagedObjectBuilder}.
	 */
	public <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> ManagedObjectBuilder<F> constructManagedObject(
			String managedObjectName, MS managedObjectSource, String managingOffice) {

		// Obtain the managed object source name
		String managedObjectSourceName = "of-" + managedObjectName;

		// Create the Managed Object Builder
		ManagedObjectBuilder<F> managedObjectBuilder = this.getOfficeFloorBuilder()
				.addManagedObject(managedObjectSourceName, managedObjectSource);

		// Flag managing office
		if (managingOffice != null) {
			managedObjectBuilder.setManagingOffice(managingOffice);
		}

		// Link into the Office
		this.officeBuilder.registerManagedObjectSource(managedObjectName, managedObjectSourceName);

		// Return the Managed Object Builder
		return managedObjectBuilder;
	}

	/**
	 * Facade method to register a {@link ManagedObject}.
	 * 
	 * @param object            Object for the {@link ManagedObject}.
	 * @param managedObjectName Name of the {@link ManagedObject}.
	 * @param managingOffice    Name of the mananaging {@link Office}. May be
	 *                          <code>null</code> to manually register for
	 *                          {@link ManagingOfficeBuilder}.
	 * @return {@link ManagedObjectBuilder}.
	 */
	public ManagedObjectBuilder<?> constructManagedObject(final Object object, String managedObjectName,
			String managingOffice) {

		// Create the wrapping Managed Object
		ManagedObject managedObject = new ManagedObject() {
			public Object getObject() {
				return object;
			}
		};

		// Obtain managed object source name
		String managedObjectSourceName = "of-" + managedObjectName;

		// Bind Managed Object
		@SuppressWarnings("rawtypes")
		ManagedObjectSourceMetaData metaData = new MockManagedObjectSourceMetaData(managedObject);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ManagedObjectBuilder managedObjectBuilder = MockManagedObjectSource.bindManagedObject(managedObjectSourceName,
				managedObject, metaData, this.getOfficeFloorBuilder());

		// Flag managing office
		if (managingOffice != null) {
			managedObjectBuilder.setManagingOffice(managingOffice);
		}

		// Link into the Office
		this.officeBuilder.registerManagedObjectSource(managedObjectName, managedObjectSourceName);

		// Return the builder
		return managedObjectBuilder;
	}

	/**
	 * Builds the {@link ManagedObject} for use at the desired
	 * {@link ManagedObjectScope}.
	 * 
	 * @param bindName               Name to bind the {@link ManagedObject} under.
	 * @param managedObjectScope     {@link ManagedObjectScope} for the
	 *                               {@link ManagedObject}.
	 * @param managedFunctionBuilder {@link ManagedFunctionBuilder} if binding to
	 *                               {@link ManagedObjectScope#FUNCTION}.
	 * @return {@link DependencyMappingBuilder} for the bound {@link ManagedObject}.
	 */
	public DependencyMappingBuilder bindManagedObject(String bindName, ManagedObjectScope managedObjectScope,
			ManagedFunctionBuilder<?, ?> managedFunctionBuilder) {

		// Build the managed object based on scope
		switch (managedObjectScope) {
		case FUNCTION:
			return managedFunctionBuilder.addManagedObject(bindName, bindName);

		case THREAD:
			return this.officeBuilder.addThreadManagedObject(bindName, bindName);

		case PROCESS:
			return this.officeBuilder.addProcessManagedObject(bindName, bindName);

		default:
			return Assertions.fail("Unknown managed object scope " + managedObjectScope);
		}
	}

	/**
	 * Constructs the {@link Governance}.
	 * 
	 * @param object         {@link Object} containing the {@link Method} instances
	 *                       used for {@link Governance}.
	 * @param governanceName Name of the {@link Governance}.
	 * @return {@link ReflectiveGovernanceBuilder}.
	 */
	public ReflectiveGovernanceBuilder constructGovernance(Object object, String governanceName) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ReflectiveGovernanceBuilder builder = new ReflectiveGovernanceBuilder((Class) object.getClass(), object,
				governanceName, this.officeBuilder, this);
		return builder;
	}

	/**
	 * Facade method to create a {@link Team}.
	 * 
	 * @param teamName Name of the {@link Team}.
	 * @param team     {@link Team}.
	 * @return {@link TeamBuilder}.
	 */
	public TeamBuilder<?> constructTeam(String teamName, Team team) {

		// Obtain the office floor team name
		String officeFloorTeamName = "of-" + teamName;

		// Bind the team into the office floor
		TeamBuilder<?> teamBuilder = MockTeamSource.bindTeamBuilder(this.officeFloorBuilder, officeFloorTeamName, team);

		// Link into the Office
		this.officeBuilder.registerTeam(teamName, officeFloorTeamName);

		// Return the team builder
		return teamBuilder;
	}

	/**
	 * Facade method to create a {@link Team}.
	 * 
	 * @param <TS>            {@link TeamSource} type.
	 * @param teamName        Name of the {@link Team}.
	 * @param teamSourceClass {@link TeamSource} class.
	 * @return {@link TeamBuilder}.
	 */
	public <TS extends TeamSource> TeamBuilder<?> constructTeam(String teamName, Class<TS> teamSourceClass) {

		// Obtain the office floor team name
		String officeFloorTeamName = "of-" + teamName;

		// Add the team to the office floor
		TeamBuilder<?> teamBuilder = this.officeFloorBuilder.addTeam(officeFloorTeamName, teamSourceClass);

		// Link into the office
		this.officeBuilder.registerTeam(teamName, officeFloorTeamName);

		// Return the team builder
		return teamBuilder;
	}

	/**
	 * Facade method to create the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 * @throws Exception If fails to construct the {@link OfficeFloor}.
	 */
	public OfficeFloor constructOfficeFloor() throws Exception {

		// Construct the Office Floor
		this.officeFloor = this.officeFloorBuilder.buildOfficeFloor();
		this.constructedOfficeFloors.add(this.officeFloor);

		// Initiate for constructing another office
		this.initiateNewOfficeFloorBuilder();

		// Return the OfficeFloor
		return this.officeFloor;
	}

	/**
	 * Triggers the {@link ManagedFunction} but does not wait for its completion.
	 * 
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @param parameter    Parameter for the {@link ManagedFunction}.
	 * @param callback     {@link FlowCallback}. May be <code>null</code>.
	 * @return {@link Office} containing the {@link ManagedFunction}.
	 * @throws Exception If fails to trigger the {@link ManagedFunction}.
	 */
	public Office triggerFunction(String functionName, Object parameter, FlowCallback callback) throws Exception {

		// Obtain the name of the office being constructed
		String officeName;

		// Determine if required to construct
		if (this.officeFloor != null) {
			// Already opened, so assume previous office
			officeName = this.getOfficeName(officeIndex - 1);

		} else {
			// Opening OfficeFloor so use current Office name
			officeName = this.getOfficeName();

			// Construct the OfficeFloor
			this.officeFloor = this.constructOfficeFloor();

			// Open the OfficeFloor
			this.officeFloor.openOfficeFloor();
		}

		// Invoke the function
		Office office = this.officeFloor.getOffice(officeName);
		FunctionManager functionManager = office.getFunctionManager(functionName);
		functionManager.invokeProcess(parameter, callback);

		// Return the office
		return office;
	}

	/**
	 * Facade method to invoke {@link ManagedFunction} of an {@link Office}. It will
	 * create the {@link OfficeFloor} if necessary and times out after 3 seconds if
	 * invoked {@link ManagedFunction} is not complete.
	 * 
	 * @param functionName Name of the {@link ManagedFunction} to invoke.
	 * @param parameter    Parameter.
	 * @return {@link OfficeFloor}.
	 * @throws Exception If fails to construct {@link Office} or
	 *                   {@link ManagedFunction} invocation failure.
	 */
	public OfficeFloor invokeFunction(String functionName, Object parameter) throws Exception {
		return this.invokeFunction(functionName, parameter, 3);
	}

	/**
	 * Facade method to invoke the {@link ManagedFunction} of an {@link Office} and
	 * validate the {@link ManagedFunction} instances invoked.
	 * 
	 * @param functionName      Name of the {@link ManagedFunction} to invoke.
	 * @param parameter         Parameter.
	 * @param expectedFunctions Names of the expected {@link ManagedFunction}
	 *                          instances to be invoked in the order specified.
	 * @throws Exception If fails to construct {@link Office} or
	 *                   {@link ManagedFunction} invocation failure.
	 */
	public void invokeFunctionAndValidate(String functionName, Object parameter, String... expectedFunctions)
			throws Exception {
		this.setRecordReflectiveFunctionMethodsInvoked(true);
		this.invokeFunction(functionName, parameter);
		this.validateReflectiveMethodOrder(expectedFunctions);
	}

	/**
	 * Facade method to invoke {@link ManagedFunction} of an {@link Office}. It will
	 * create the {@link OfficeFloor} if necessary.
	 * 
	 * @param functionName Name of the {@link ManagedFunction} to invoke.
	 * @param parameter    Parameter.
	 * @param secondsToRun Seconds to run.
	 * @return {@link OfficeFloor}.
	 * @throws Exception If fails to construct {@link Office} or
	 *                   {@link ManagedFunction} invocation failure.
	 */
	public OfficeFloor invokeFunction(String functionName, Object parameter, int secondsToRun) throws Exception {

		// Wait on this object
		Closure<Boolean> isComplete = new Closure<Boolean>(false);
		Closure<Throwable> failure = new Closure<>();

		// Invoke the function
		this.triggerFunction(functionName, parameter, (escalation) -> {
			try {
				// Notify complete
				this.logTestSupport.printMessage("Complete");

			} finally {
				// Flag complete
				synchronized (isComplete) {
					failure.value = escalation;
					isComplete.value = true;
					isComplete.notify();
				}
			}
		});

		try {
			// Block until flow is complete (or times out)
			int iteration = 0;
			long startBlockTime = System.currentTimeMillis();
			synchronized (isComplete) {
				while (!isComplete.value) {

					// Only timeout if positive time to run
					if (secondsToRun > 0) {
						// Provide heap diagnostics and time out
						this.threadedTestSupport.timeout(startBlockTime, secondsToRun);
					}

					// Output heap diagnostics after every approximate 3 seconds
					iteration++;
					if (iteration >= 300) {
						iteration = 0;
						this.logTestSupport.printHeapMemoryDiagnostics();
					}

					// Wait some time as still executing
					isComplete.wait(10);
				}

				// Ensure propagate failure
				if (failure.value != null) {
					throw failure.value;
				}
			}

			// Ensure propagate escalations
			// (runs in own lock, so must be outside lock)
			this.validateNoTopLevelEscalation();

		} catch (Throwable ex) {
			return JUnitAgnosticAssert.fail(ex);
		}

		// Return the OfficeFloor
		return this.officeFloor;
	}

}
