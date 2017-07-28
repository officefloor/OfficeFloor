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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
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
	 * Specifies the differentiator for this {@link ManagedFunction}.
	 * <p>
	 * This is exposed as is on the {@link FunctionManager} interface for this
	 * {@link ManagedFunction} to allow reflective:
	 * <ol>
	 * <li>identification of this {@link ManagedFunction} (e.g. can check on
	 * type of this object)</li>
	 * <li>means to trigger functionality on this {@link ManagedFunction} (e.g.
	 * can expose functionality to be invoked)</li>
	 * </ol>
	 * 
	 * @param differentiator
	 *            Differentiator.
	 */
	void setDifferentiator(Object differentiator);

	/**
	 * Links in the parameter for this {@link ManagedFunction}.
	 * 
	 * @param key
	 *            Key identifying the parameter.
	 * @param parameterType
	 *            Type of the parameter.
	 */
	void linkParameter(O key, Class<?> parameterType);

	/**
	 * Links in the parameter for this {@link ManagedFunction}.
	 * 
	 * @param index
	 *            Index identifying the parameter.
	 * @param parameterType
	 *            Type of the parameter.
	 */
	void linkParameter(int index, Class<?> parameterType);

	/**
	 * Links in a {@link ManagedObject} to this {@link ManagedFunction}.
	 * 
	 * @param key
	 *            Key identifying the {@link ManagedObject}.
	 * @param scopeManagedObjectName
	 *            Name of the {@link ManagedObject} within the
	 *            {@link ManagedObjectScope}.
	 * @param objectType
	 *            Type required by the {@link ManagedFunction}.
	 */
	void linkManagedObject(O key, String scopeManagedObjectName, Class<?> objectType);

	/**
	 * Links in a {@link ManagedObject} to this {@link ManagedFunction}.
	 * 
	 * @param managedObjectIndex
	 *            Index of the {@link ManagedObject}.
	 * @param scopeManagedObjectName
	 *            Name of the {@link ManagedObject} within the
	 *            {@link ManagedObjectScope}.
	 * @param objectType
	 *            Type required by the {@link ManagedFunction}.
	 */
	void linkManagedObject(int managedObjectIndex, String scopeManagedObjectName, Class<?> objectType);

	/**
	 * Adds {@link Administration} to be undertaken before this
	 * {@link ManagedFunction}.
	 * 
	 * @param administrationName
	 *            Name of the {@link Administration}.
	 * @param extension
	 *            Extension type for {@link Administration}.
	 * @param administrationFactory
	 *            {@link AdministrationFactory}.
	 * @return {@link AdministrationBuilder} to build the
	 *         {@link Administration}.
	 */
	<E, f extends Enum<f>, G extends Enum<G>> AdministrationBuilder<f, G> preAdminister(String administrationName,
			Class<E> extension, AdministrationFactory<E, f, G> administrationFactory);

	/**
	 * Adds {@link Administration} to be undertaken after this
	 * {@link ManagedFunction}.
	 * 
	 * @param administrationName
	 *            Name of the {@link Administration}.
	 * @param extension
	 *            Extension type for {@link Administration}.
	 * @param administrationFactory
	 *            {@link AdministrationFactory}.
	 * @return {@link AdministrationBuilder} to build the
	 *         {@link Administration}.
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
	 * <li>disregarded when an escalation occurs to a {@link ManagedFunction}
	 * not requiring the {@link Governance}. Note that this does allow the
	 * {@link Governance} to stay active should the {@link Escalation}
	 * {@link ManagedFunction} require the {@link Governance}.</li>
	 * <li>Manually managed by an {@link Administration}</li>
	 * </ol>
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
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
	 * @param functionManagedObjectName
	 *            Name of the {@link ManagedObject} to be referenced locally by
	 *            this {@link ManagedFunction}.
	 * @param officeManagedObjectName
	 *            Name of the {@link ManagedObject} referenced locally within
	 *            the {@link Office}.
	 * @return {@link DependencyMappingBuilder}.
	 */
	DependencyMappingBuilder addManagedObject(String functionManagedObjectName, String officeManagedObjectName);

}