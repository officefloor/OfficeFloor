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

package net.officefloor.frame.api.build;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Builder of the {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionBuilder<O extends Enum<O>, F extends Enum<F>> extends FlowBuilder<F> {

	/**
	 * <p>
	 * Adds the annotation for this {@link ManagedFunction}.
	 * <p>
	 * This is exposed as is on the {@link FunctionManager} interface for this
	 * {@link ManagedFunction} to allow reflective:
	 * <ol>
	 * <li>identification of this {@link ManagedFunction} (e.g. can check on type of
	 * this object)</li>
	 * <li>means to trigger functionality on this {@link ManagedFunction} (e.g. can
	 * expose functionality to be invoked)</li>
	 * </ol>
	 * 
	 * @param annotation Annotation.
	 */
	void addAnnotation(Object annotation);

	/**
	 * Links in the parameter for this {@link ManagedFunction}.
	 * 
	 * @param key           Key identifying the parameter.
	 * @param parameterType Type of the parameter.
	 */
	void linkParameter(O key, Class<?> parameterType);

	/**
	 * Links in the parameter for this {@link ManagedFunction}.
	 * 
	 * @param index         Index identifying the parameter.
	 * @param parameterType Type of the parameter.
	 */
	void linkParameter(int index, Class<?> parameterType);

	/**
	 * Links in a {@link ManagedObject} to this {@link ManagedFunction}.
	 * 
	 * @param key                    Key identifying the {@link ManagedObject}.
	 * @param scopeManagedObjectName Name of the {@link ManagedObject} within the
	 *                               {@link ManagedObjectScope}.
	 * @param objectType             Type required by the {@link ManagedFunction}.
	 */
	void linkManagedObject(O key, String scopeManagedObjectName, Class<?> objectType);

	/**
	 * Links in a {@link ManagedObject} to this {@link ManagedFunction}.
	 * 
	 * @param managedObjectIndex     Index of the {@link ManagedObject}.
	 * @param scopeManagedObjectName Name of the {@link ManagedObject} within the
	 *                               {@link ManagedObjectScope}.
	 * @param objectType             Type required by the {@link ManagedFunction}.
	 */
	void linkManagedObject(int managedObjectIndex, String scopeManagedObjectName, Class<?> objectType);

	/**
	 * Adds {@link Administration} to be undertaken before this
	 * {@link ManagedFunction}.
	 *
	 * @param                       <E> Extension type.
	 * @param                       <f> {@link Flow} key type.
	 * @param                       <G> {@link Governance} key type.
	 * @param administrationName    Name of the {@link Administration}.
	 * @param extension             Extension type for {@link Administration}.
	 * @param administrationFactory {@link AdministrationFactory}.
	 * @return {@link AdministrationBuilder} to build the {@link Administration}.
	 */
	<E, f extends Enum<f>, G extends Enum<G>> AdministrationBuilder<f, G> preAdminister(String administrationName,
			Class<E> extension, AdministrationFactory<E, f, G> administrationFactory);

	/**
	 * Adds {@link Administration} to be undertaken after this
	 * {@link ManagedFunction}.
	 * 
	 * @param                       <E> Extension type.
	 * @param                       <f> {@link Flow} key type.
	 * @param                       <G> {@link Governance} key type.
	 * @param administrationName    Name of the {@link Administration}.
	 * @param extension             Extension type for {@link Administration}.
	 * @param administrationFactory {@link AdministrationFactory}.
	 * @return {@link AdministrationBuilder} to build the {@link Administration}.
	 */
	<E, f extends Enum<f>, G extends Enum<G>> AdministrationBuilder<f, G> postAdminister(String administrationName,
			Class<E> extension, AdministrationFactory<E, f, G> administrationFactory);

	/**
	 * <p>
	 * Adds {@link Governance} to this {@link ManagedFunction}.
	 * <p>
	 * In other words, to execute this {@link ManagedFunction} the
	 * {@link Governance} will be automatically activated before the
	 * {@link ManagedFunction} is executed (or stay active from previous
	 * {@link ManagedFunction}).
	 * <p>
	 * The {@link Governance} will be:
	 * <ol>
	 * <li>enforced when either a {@link ManagedFunction} does not require the
	 * {@link Governance} or the {@link ThreadState} completes.
	 * <li>
	 * <li>disregarded when an escalation occurs to a {@link ManagedFunction} not
	 * requiring the {@link Governance}. Note that this does allow the
	 * {@link Governance} to stay active should the {@link Escalation}
	 * {@link ManagedFunction} require the {@link Governance}.</li>
	 * <li>Manually managed by an {@link Administration}</li>
	 * </ol>
	 * 
	 * @param governanceName Name of the {@link Governance}.
	 */
	void addGovernance(String governanceName);

	/**
	 * <p>
	 * Adds a {@link ManagedObject} bound to this {@link ManagedFunction}.
	 * <p>
	 * Dependency scope:
	 * <ol>
	 * <li>Other {@link ManagedObject} instances added via this method.</li>
	 * <li>{@link ThreadState} bound {@link ManagedObject} instances.</li>
	 * <li>{@link ProcessState} bound {@link ManagedObject} instances.</li>
	 * </ol>
	 * 
	 * @param functionManagedObjectName Name of the {@link ManagedObject} to be
	 *                                  referenced locally by this
	 *                                  {@link ManagedFunction}.
	 * @param officeManagedObjectName   Name of the {@link ManagedObject} referenced
	 *                                  locally within the {@link Office}.
	 * @return {@link DependencyMappingBuilder}.
	 */
	DependencyMappingBuilder addManagedObject(String functionManagedObjectName, String officeManagedObjectName);

	/**
	 * Specifies the timeout to for {@link AsynchronousFlow} instances for this
	 * {@link ManagedFunction}.
	 *
	 * @param timeout Timeout.
	 */
	void setAsynchronousFlowTimeout(long timeout);

}
