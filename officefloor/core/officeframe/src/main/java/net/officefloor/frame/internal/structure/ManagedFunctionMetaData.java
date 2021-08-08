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

package net.officefloor.frame.internal.structure;

import java.util.logging.Logger;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Meta-data for the {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionMetaData<O extends Enum<O>, F extends Enum<F>> extends ManagedFunctionLogicMetaData {

	/**
	 * Obtains the {@link ManagedFunctionFactory} to create the
	 * {@link ManagedFunction} for this {@link ManagedFunctionMetaData}.
	 * 
	 * @return {@link ManagedFunctionFactory}
	 */
	ManagedFunctionFactory<O, F> getManagedFunctionFactory();

	/**
	 * Obtains the annotations for the {@link ManagedFunction}.
	 * 
	 * @return Annotations.
	 */
	Object[] getAnnotations();

	/**
	 * Obtains the parameter type for the {@link ManagedFunction}.
	 * 
	 * @return Parameter type for the {@link ManagedFunction}. May be
	 *         <code>null</code> to indicate no parameter.
	 */
	Class<?> getParameterType();

	/**
	 * Obtains the {@link Logger} for {@link ManagedFunctionContext}.
	 * 
	 * @return {@link Logger} for {@link ManagedFunctionContext}.
	 */
	Logger getLogger();

	/**
	 * <p>
	 * Obtains the {@link ManagedObjectIndex} instances identifying the
	 * {@link ManagedObject} instances that must be loaded before the
	 * {@link ManagedFunction} may be executed.
	 * <p>
	 * The order of the {@link ManagedObjectIndex} instances must be respected as
	 * they are sorted to enable appropriate {@link CoordinatingManagedObject} to
	 * co-ordinate with dependencies.
	 * 
	 * @return Listing of {@link ManagedObjectIndex} instances.
	 */
	ManagedObjectIndex[] getRequiredManagedObjects();

	/**
	 * Obtains the activation flags for the {@link Governance}. The index into the
	 * array identifies the {@link Governance} for the respective activation flag.
	 * 
	 * @return Activation flags for the {@link Governance}.
	 */
	boolean[] getRequiredGovernance();

	/**
	 * Obtains the {@link ManagedObjectIndex} for the {@link ManagedFunction} index.
	 * 
	 * @param managedObjectIndex {@link ManagedObjectIndex} for the
	 *                           {@link ManagedFunction} index.
	 * @return {@link ManagedObjectIndex} to obtain the {@link ManagedObject} for
	 *         the {@link ManagedFunction}.
	 */
	ManagedObjectIndex getManagedObject(int managedObjectIndex);

	/**
	 * Obtains the meta-data of the {@link ManagedObject} instances bound to the
	 * {@link ManagedFunction}.
	 * 
	 * @return Meta-data of the {@link ManagedObject} instances bound to the
	 *         {@link ManagedFunction}.
	 */
	ManagedObjectMetaData<?>[] getManagedObjectMetaData();

	/**
	 * Meta-data of the {@link Administration} to undertake before executing the
	 * {@link ManagedFunction}.
	 * 
	 * @return Listing of the {@link Administration} instances to undertake before
	 *         executing the {@link ManagedFunction}.
	 */
	ManagedFunctionAdministrationMetaData<?, ?, ?>[] getPreAdministrationMetaData();

	/**
	 * Meta-data of the {@link Administration} to undertake after executing the
	 * {@link ManagedFunction}.
	 * 
	 * @return Listing the {@link Administration} instances to undertake after
	 *         executing the {@link ManagedFunction}.
	 */
	ManagedFunctionAdministrationMetaData<?, ?, ?>[] getPostAdministrationMetaData();

}
