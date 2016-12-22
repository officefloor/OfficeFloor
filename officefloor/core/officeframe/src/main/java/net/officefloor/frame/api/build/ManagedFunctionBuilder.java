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

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.TaskManager;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorDutyMetaData;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Builder of the {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionBuilder<W extends Work, D extends Enum<D>, F extends Enum<F>>
		extends FlowNodeBuilder<F> {

	/**
	 * <p>
	 * Specifies the differentiator for this {@link ManagedFunction}.
	 * <p>
	 * This is exposed as is on the {@link TaskManager} interface for this
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
	void linkParameter(D key, Class<?> parameterType);

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
	void linkManagedObject(D key, String scopeManagedObjectName, Class<?> objectType);

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
	 * Links in a {@link Duty} to be executed before the
	 * {@link ManagedFunction}.
	 * 
	 * @param <A>
	 *            {@link Administrator} key type.
	 * @param scopeAdministratorName
	 *            Name of the {@link Administrator} within the
	 *            {@link AdministratorScope}.
	 * @param dutyKey
	 *            Key identifying the {@link Duty}.
	 */
	<A extends Enum<A>> void linkPreTaskAdministration(String scopeAdministratorName, A dutyKey);

	/**
	 * Links in a {@link Duty} to be executed before the
	 * {@link ManagedFunction}.
	 * 
	 * @param scopeAdministratorName
	 *            Name of the {@link Administrator} within the
	 *            {@link AdministratorScope}.
	 * @param dutyName
	 *            Name identifying the {@link Duty} (as per
	 *            {@link AdministratorDutyMetaData}).
	 */
	void linkPreTaskAdministration(String scopeAdministratorName, String dutyName);

	/**
	 * Links in a {@link Duty} to be executed after the {@link ManagedFunction}.
	 * 
	 * @param <A>
	 *            {@link Administrator} key type.
	 * @param scopeAdministratorName
	 *            Name of the {@link Administrator} within the
	 *            {@link AdministratorScope}.
	 * @param dutyKey
	 *            Key identifying the {@link Duty}.
	 */
	<A extends Enum<A>> void linkPostTaskAdministration(String scopeAdministratorName, A dutyKey);

	/**
	 * Links in a {@link Duty} to be executed after the {@link ManagedFunction}.
	 * 
	 * @param scopeAdministratorName
	 *            Name of the {@link Administrator} within the
	 *            {@link AdministratorScope}.
	 * @param dutyName
	 *            Name identifying the {@link Duty} (as per
	 *            {@link AdministratorDutyMetaData}).
	 */
	void linkPostTaskAdministration(String scopeAdministratorName, String dutyName);

	/**
	 * <p>
	 * Adds {@link OfficeFloor} managed {@link Governance} to this
	 * {@link ManagedFunction}.
	 * <p>
	 * In other words, to execute this {@link ManagedFunction} the
	 * {@link Governance} will be automatically activated before the
	 * {@link ManagedFunction} is executed (or stay active from previous
	 * {@link ManagedFunction}).
	 * <p>
	 * The {@link Governance} will be:
	 * <ol>
	 * <li>enforced when either a {@link ManagedFunction} in the flow does not
	 * require the {@link Governance} or the {@link ThreadState} completes.
	 * <li>
	 * <li>disregarded when an escalation occurs to a {@link ManagedFunction}
	 * not requiring the {@link Governance}. Note that this does allow
	 * {@link Governance} to stay active should the {@link Escalation}
	 * {@link ManagedFunction} require the {@link Governance}.</li>
	 * </ol>
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 */
	void addGovernance(String governanceName);

}