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

import java.lang.reflect.Method;

import junit.framework.TestCase;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Abstract {@link TestCase} for construction testing of an Office.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeConstructTestCase extends OfficeFrameTestCase {

	/**
	 * {@link ConstructTestSupport}.
	 */
	private ConstructTestSupport constructTestSupport = new ConstructTestSupport(this.threadedTestSupport,
			this.logTestSupport);

	/*
	 * =========================== TestCase =======================
	 */

	@Override
	protected void setUp() throws Exception {
		this.constructTestSupport.beforeEach();
	}

	@Override
	protected void tearDown() throws Exception {
		this.constructTestSupport.afterEach();
	}

	/**
	 * Move current time forward the input number of milliseconds.
	 * 
	 * @param timeInMilliseconds Milliseconds to move current time forward.
	 */
	public void adjustCurrentTimeMillis(long timeInMilliseconds) {
		this.constructTestSupport.adjustCurrentTimeMillis(timeInMilliseconds);
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
		this.constructTestSupport.validateNoTopLevelEscalation();
	}

	/**
	 * Obtains the {@link OfficeFloorBuilder}.
	 * 
	 * @return {@link OfficeFloorBuilder}.
	 */
	public OfficeFloorBuilder getOfficeFloorBuilder() {
		return this.constructTestSupport.getOfficeFloorBuilder();
	}

	/**
	 * Obtains the {@link OfficeBuilder}.
	 * 
	 * @return {@link OfficeBuilder}.
	 */
	public OfficeBuilder getOfficeBuilder() {
		return this.constructTestSupport.getOfficeBuilder();
	}

	/**
	 * Obtains the name of the {@link OfficeFloor} currently being constructed.
	 * 
	 * @return Name of the {@link OfficeFloor} currently being constructed.
	 */
	public String getOfficeFloorName() {
		return this.constructTestSupport.getOfficeFloorName();
	}

	/**
	 * Obtains the name of the {@link Office} currently being constructed.
	 * 
	 * @return Name of the {@link Office} currently being constructed.
	 */
	public String getOfficeName() {
		return this.constructTestSupport.getOfficeName();
	}

	/**
	 * Constructs the {@link ReflectiveFunctionBuilder}.
	 * 
	 * @param object     {@link Object} containing the {@link Method}.
	 * @param methodName Name of the {@link Method}.
	 * @return {@link ReflectiveFunctionBuilder}.
	 */
	public ReflectiveFunctionBuilder constructFunction(Object object, String methodName) {
		return this.constructTestSupport.constructFunction(object, methodName);
	}

	/**
	 * Constructs the {@link ReflectiveFunctionBuilder} for a static {@link Method}.
	 * 
	 * @param clazz      {@link Class} containing the static {@link Method}.
	 * @param methodName Name of the {@link Method}.
	 * @return {@link ReflectiveFunctionBuilder}.
	 */
	public ReflectiveFunctionBuilder constructStaticFunction(Class<?> clazz, String methodName) {
		return this.constructTestSupport.constructStaticFunction(clazz, methodName);
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
		this.constructTestSupport.setRecordReflectiveFunctionMethodsInvoked(isRecord);
	}

	/**
	 * Invoked by the {@link ReflectiveFunctionBuilder} when it executes the method.
	 * 
	 * @param methodName Name of method being invoked.
	 */
	public void recordReflectiveFunctionMethodInvoked(String methodName) {
		this.constructTestSupport.recordReflectiveFunctionMethodInvoked(methodName);
	}

	/**
	 * Validates the order the {@link ReflectiveFunctionBuilder} invoked the
	 * methods.
	 * 
	 * @param methodNames Order that the reflective methods should be invoked.
	 * @see #setRecordReflectiveFunctionMethodsInvoked(boolean)
	 */
	public void validateReflectiveMethodOrder(String... methodNames) {
		this.constructTestSupport.validateReflectiveMethodOrder(methodNames);
	}

	/**
	 * Ensures the {@link Thread} is used.
	 * 
	 * @param thread {@link Thread}.
	 */
	public void assertThreadUsed(Thread thread) {
		this.constructTestSupport.assertThreadUsed(thread);
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
		return this.constructTestSupport.constructFunction(functionName, functionFactory);
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
		return this.constructTestSupport.constructFunction(functionName, function);
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
		return this.constructTestSupport.constructManagedObject(managedObjectName, managedObjectSourceClass,
				managingOffice);
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
		return this.constructTestSupport.constructManagedObject(managedObjectName, managedObjectSource, managingOffice);
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
		return this.constructTestSupport.constructManagedObject(object, managedObjectName, managingOffice);
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
		return this.constructTestSupport.bindManagedObject(bindName, managedObjectScope, managedFunctionBuilder);
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
		return this.constructTestSupport.constructGovernance(object, governanceName);
	}

	/**
	 * Facade method to create a {@link Team}.
	 * 
	 * @param teamName Name of the {@link Team}.
	 * @param team     {@link Team}.
	 * @return {@link TeamBuilder}.
	 */
	public TeamBuilder<?> constructTeam(String teamName, Team team) {
		return this.constructTestSupport.constructTeam(teamName, team);
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
		return this.constructTestSupport.constructTeam(teamName, teamSourceClass);
	}

	/**
	 * Facade method to create the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 * @throws Exception If fails to construct the {@link OfficeFloor}.
	 */
	public OfficeFloor constructOfficeFloor() throws Exception {
		return this.constructTestSupport.constructOfficeFloor();
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
		return this.constructTestSupport.triggerFunction(functionName, parameter, callback);
	}

	/**
	 * Facade method to invoke {@link ManagedFunction} of an {@link Office}. It will
	 * create the {@link OfficeFloor} if necessary and times out after 3 seconds if
	 * invoked {@link ManagedFunction} is not complete.
	 * 
	 * @param functionName Name of the {@link ManagedFunction} to invoke.
	 * @param parameter    Parameter.
	 * @throws Exception If fails to construct {@link Office} or
	 *                   {@link ManagedFunction} invocation failure.
	 */
	public void invokeFunction(String functionName, Object parameter) throws Exception {
		this.constructTestSupport.invokeFunction(functionName, parameter);
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
		this.constructTestSupport.invokeFunctionAndValidate(functionName, parameter, expectedFunctions);
	}

	/**
	 * Facade method to invoke {@link ManagedFunction} of an {@link Office}. It will
	 * create the {@link OfficeFloor} if necessary.
	 * 
	 * @param functionName Name of the {@link ManagedFunction} to invoke.
	 * @param parameter    Parameter.
	 * @param secondsToRun Seconds to run.
	 * @throws Exception If fails to construct {@link Office} or
	 *                   {@link ManagedFunction} invocation failure.
	 */
	public void invokeFunction(String functionName, Object parameter, int secondsToRun) throws Exception {
		this.constructTestSupport.invokeFunction(functionName, parameter, secondsToRun);
	}

}
