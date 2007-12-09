/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.test;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeScope;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.escalate.EscalationContext;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.OfficeFrameImpl;
import net.officefloor.frame.internal.structure.ParentEscalationProcedure;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.team.Team;

/**
 * Abstract {@link junit.framework.TestCase} for construction testing of an
 * Office.
 * 
 * @author Daniel
 */
public abstract class AbstractOfficeConstructTestCase extends
		OfficeFrameTestCase implements ParentEscalationProcedure {

	/**
	 * {@link OfficeFloorBuilder}.
	 */
	private OfficeFloorBuilder officeFloorBuilder;

	/**
	 * {@link OfficeBuilder}.
	 */
	private OfficeBuilder officeBuilder;

	/**
	 * {@link WorkBuilder}.
	 */
	private WorkBuilder<?> workBuilder;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// Clear the office
		((OfficeFrameImpl) OfficeFrameImpl.getInstance()).clearOfficeFloors();

		// Initiate for constructing office
		this.officeFloorBuilder = OfficeFrame.getInstance()
				.getBuilderFactory().createOfficeFloorBuilder();
		this.officeBuilder = OfficeFrame.getInstance().getBuilderFactory()
				.createOfficeBuilder();
	}

	/**
	 * Handle failure.
	 */
	public <E extends Throwable> void escalate(EscalationContext<E> context) {

		// Obtain the failure
		Throwable cause = context.getException();

		// Ensure have failure
		assertNotNull("Must have the cause on escalation", cause);

		// Fail
		fail("Escalated [" + cause.getClass().getSimpleName() + "] "
				+ cause.getMessage());
	}

	/**
	 * Obtains the {@link OfficeBuilder}.
	 * 
	 * @return {@link OfficeBuilder}.
	 */
	protected OfficeBuilder getOfficeBuilder() {
		return this.officeBuilder;
	}

	/**
	 * Facade method to register a
	 * {@link net.officefloor.frame.api.execute.Work}.
	 * 
	 * @return {@link WorkBuilder} for the
	 *         {@link net.officefloor.frame.api.execute.Work}.
	 */
	@SuppressWarnings("unchecked")
	protected <W extends Work> WorkBuilder<W> constructWork(String workName,
			WorkFactory<W> workFactory, Class<W> typeOfWork,
			String initialTaskName) throws BuildException {

		// Create the Work Builder
		WorkBuilder<W> workBuilder = OfficeFrame.getInstance()
				.getBuilderFactory().createWorkBuilder(typeOfWork);

		// Construct the work
		workBuilder.setWorkFactory(workFactory);
		workBuilder.setInitialTask(initialTaskName);

		// Register the work
		this.officeBuilder.addWork(workName, workBuilder);

		// Make current work builder
		this.workBuilder = workBuilder;

		// Return the work builder
		return workBuilder;
	}

	/**
	 * Facade method to register a
	 * {@link net.officefloor.frame.api.execute.Work}.
	 * 
	 * @return {@link WorkBuilder} for the
	 *         {@link net.officefloor.frame.api.execute.Work}.
	 */
	@SuppressWarnings("unchecked")
	protected <W extends Work> WorkBuilder<W> constructWork(String workName,
			final W work, String initialTaskName) throws BuildException {

		// Obtain the type of work
		Class typeOfWork = work.getClass();

		// Create the Work Factory
		WorkFactory<W> workFactory = new WorkFactory<W>() {
			public W createWork() {
				return work;
			}
		};

		// Return the constructed work
		return this.constructWork(workName, workFactory, typeOfWork,
				initialTaskName);
	}

	/**
	 * Facade method to register a
	 * {@link net.officefloor.frame.api.execute.Task}.
	 * 
	 * @return {@link TaskBuilder} for the
	 *         {@link net.officefloor.frame.api.execute.Task}.
	 */
	@SuppressWarnings("unchecked")
	protected <P extends Object, W extends Work, M extends Enum<M>, F extends Enum<F>> TaskBuilder<P, W, M, F> constructTask(
			String taskName, Class parameterType,
			TaskFactory<P, W, M, F> taskFactory, String teamName,
			String moName, String nextTaskName) throws BuildException {

		// Create the Task Builder
		TaskBuilder taskBuilder = this.workBuilder.addTask(taskName,
				parameterType);

		// Construct the task
		taskBuilder.setTaskFactory(taskFactory);
		taskBuilder.setTeam(teamName);
		if (nextTaskName != null) {
			taskBuilder.setNextTaskInFlow(nextTaskName);
		}
		if (moName != null) {
			taskBuilder.linkManagedObject(0, moName);
		}

		// Return the task builder
		return taskBuilder;
	}

	/**
	 * Facade method to register a
	 * {@link net.officefloor.frame.api.execute.Task}.
	 * 
	 * @return {@link TaskBuilder} for the
	 *         {@link net.officefloor.frame.api.execute.Task}.
	 */
	@SuppressWarnings("unchecked")
	protected <P extends Object, W extends Work, M extends Enum<M>, F extends Enum<F>> TaskBuilder constructTask(
			String taskName, Class parameterType, final Task<P, W, M, F> task,
			String teamName, String nextTaskName) throws BuildException {

		// Create the Task Factory
		TaskFactory<P, W, M, F> taskFactory = new TaskFactory<P, W, M, F>() {

			public Task<P, W, M, F> createTask(W work) {
				return task;
			}
		};

		// Construct and return the Task
		return this.constructTask(taskName, parameterType, taskFactory,
				teamName, null, nextTaskName);
	}

	/**
	 * Facade method to register a
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	protected <S extends ManagedObjectSource> ManagedObjectBuilder<?> constructManagedObject(
			String managedObjectName, Class<S> managedObjectSourceClass,
			String managingOffice) throws BuildException {

		// Create the Managed Object Builder
		ManagedObjectBuilder<?> managedObjectBuilder = OfficeFrame
				.getInstance().getBuilderFactory()
				.createManagedObjectBuilder();

		// Register the Managed Object Source class
		managedObjectBuilder
				.setManagedObjectSourceClass(managedObjectSourceClass);

		// Flag managing office
		managedObjectBuilder.setManagingOffice(managingOffice);

		// Obtain office floor id for managed object
		String managedObjectId = "of-" + managedObjectName;

		// Register the Managed Object with the current Office
		this.officeFloorBuilder.addManagedObject(managedObjectId,
				managedObjectBuilder);

		// Link into the Office
		this.officeBuilder.registerManagedObject(managedObjectName,
				managedObjectId);

		// Return the Managed Object Builder
		return managedObjectBuilder;
	}

	/**
	 * Facade method to register a
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	protected void constructManagedObject(String managedObjectName,
			ManagedObjectSourceMetaData<?, ?> metaData,
			ManagedObject managedObject, String managingOffice)
			throws BuildException {

		// Create the Managed Object Builder
		ManagedObjectBuilder<?> managedObjectBuilder = OfficeFrame
				.getInstance().getBuilderFactory()
				.createManagedObjectBuilder();

		// Bind Managed Object
		MockManagedObjectSource.bindManagedObject(managedObjectBuilder,
				managedObjectName, managedObject, metaData);

		// Flag managing office
		managedObjectBuilder.setManagingOffice(managingOffice);

		// Obtain office floor id for managed object
		String managedObjectId = "of-" + managedObjectName;

		// Register the Managed Object with the current Office
		this.officeFloorBuilder.addManagedObject(managedObjectId,
				managedObjectBuilder);

		// Link into the Office
		this.officeBuilder.registerManagedObject(managedObjectName,
				managedObjectId);
	}

	/**
	 * Facade method to register a
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	protected void constructManagedObject(String managedObjectName,
			ManagedObject managedObject, String managingOffice)
			throws BuildException {

		// Create the mock Managed Object Source meta-data
		ManagedObjectSourceMetaData<?, ?> metaData = new MockManagedObjectSourceMetaData(
				managedObject);

		// Register the Managed Object
		this.constructManagedObject(managedObjectName, metaData, managedObject,
				managingOffice);
	}

	/**
	 * Facade method to register a
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	protected void constructManagedObject(final Object object,
			String managedObjectName, String managingOffice)
			throws BuildException {

		// Create the wrapping Managed Object
		ManagedObject managedObject = new ManagedObject() {
			public Object getObject() {
				return object;
			}
		};

		// Register the managed object
		this.constructManagedObject(managedObjectName, managedObject,
				managingOffice);
	}

	/**
	 * Facade method to create a {@link Team}.
	 */
	protected void constructTeam(String teamName, Team team)
			throws BuildException {

		// Obtain the office floor Id for the team
		String teamId = "of-" + teamName;

		// Add the team
		this.officeFloorBuilder.addTeam(teamId, team);

		// Link into the Office
		this.officeBuilder.registerTeam(teamName, teamId);
	}

	/**
	 * Facade method to create a {@link Administrator}.
	 * 
	 * @param adminName
	 *            Name of the {@link Administrator}.
	 * @param adminOne
	 *            {@link Administrator}.
	 * @param adminOneMetaData
	 *            Meta-data for the {@link AdministratorSourceMetaData}.
	 * @return {@link AdministratorBuilder}.
	 */
	@SuppressWarnings("unchecked")
	protected <I extends Object, A extends Enum<A>> AdministratorBuilder<A> constructAdministrator(
			String adminName, Administrator<I, A> adminOne,
			AdministratorSourceMetaData<I, A> adminOneMetaData,
			OfficeScope adminScope) throws BuildException {

		// Create the Administrator Builder
		AdministratorBuilder<A> adminBuilder = (AdministratorBuilder<A>) OfficeFrame
				.getInstance().getBuilderFactory()
				.createAdministratorBuilder();

		// Bind the Administrator
		MockAdministratorSource.bindAdministrator(adminBuilder, adminName,
				adminOne, adminOneMetaData);

		// Configure the administrator
		adminBuilder.setAdministratorScope(adminScope);

		// Register the Administrator with the current Office
		this.officeBuilder.addAdministrator(adminName, adminBuilder);

		// Return the administrator builder
		return adminBuilder;
	}

	/**
	 * Facade method to construct an {@link Administrator}.
	 * 
	 * @param adminName
	 *            Name of the {@link Administrator}.
	 * @param adminSource
	 *            {@link AdministratorSource} {@link Class}.
	 * @param adminScope
	 *            {@link OfficeScope} of the {@link Administrator}.
	 * @return {@link AdministratorBuilder}.
	 */
	@SuppressWarnings("unchecked")
	protected <I extends Object, A extends Enum<A>, AS extends AdministratorSource<I, A>> AdministratorBuilder<A> constructAdministrator(
			String adminName, Class<AS> adminSource, OfficeScope adminScope)
			throws BuildException {

		// Create the Administrator Builder
		AdministratorBuilder<A> adminBuilder = (AdministratorBuilder<A>) OfficeFrame
				.getInstance().getBuilderFactory()
				.createAdministratorBuilder();

		// Configure the administrator
		adminBuilder.setAdministratorSourceClass(adminSource);
		adminBuilder.setAdministratorScope(adminScope);

		// Register the Administrator with the current Office
		this.officeBuilder.addAdministrator(adminName, adminBuilder);

		// Return the administrator builder
		return adminBuilder;
	}

	/**
	 * Facade method to create the
	 * {@link net.officefloor.frame.api.manage.OfficeFloor}.
	 * 
	 * @return {@link net.officefloor.frame.api.manage.OfficeFloor}.
	 */
	protected OfficeFloor constructOfficeFloor(String officeFloorName)
			throws Exception {

		// Construct the Office
		this.officeFloorBuilder.addOffice(officeFloorName, this.officeBuilder);

		// Construct the Office Floor
		OfficeFloor officeFloor = OfficeFrame.getInstance()
				.registerOfficeFloor("of-" + officeFloorName,
						this.officeFloorBuilder);

		// Initiate for constructing another office
		this.officeFloorBuilder = OfficeFrame.getInstance()
				.getBuilderFactory().createOfficeFloorBuilder();
		this.officeBuilder = OfficeFrame.getInstance().getBuilderFactory()
				.createOfficeBuilder();

		// Return the Office Floor
		return officeFloor;
	}

}
