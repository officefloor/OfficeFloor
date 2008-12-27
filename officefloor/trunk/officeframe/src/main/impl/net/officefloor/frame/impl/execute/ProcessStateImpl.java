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
package net.officefloor.frame.impl.execute;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessCompletionListener;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Implementation of the {@link ProcessState}.
 * 
 * @author Daniel
 */
public class ProcessStateImpl implements ProcessState {

	/**
	 * {@link ManagedObjectContainer} instances for the {@link ProcessState}.
	 */
	protected final ManagedObjectContainer[] managedObjectContainers;

	/**
	 * {@link AdministratorContainer} instances for the {@link ProcessState}.
	 */
	protected final AdministratorContainer<?, ?>[] administratorContainers;

	/**
	 * {@link EscalationHandler} provided by the {@link ManagedObject} that
	 * invoked this {@link ProcessState}. May be <code>null</code>.
	 */
	protected final EscalationHandler managedObjectEscalationHandler;

	/**
	 * {@link EscalationHandler} provided by the {@link Office}. May be
	 * <code>null</code>.
	 */
	protected final EscalationHandler officeEscalationHandler;

	/**
	 * Listing of {@link ProcessCompletionListener} instances.
	 */
	protected final List<ProcessCompletionListener> completionListeners = new LinkedList<ProcessCompletionListener>();

	/**
	 * Count of active {@link ThreadState} instances for this
	 * {@link ProcessState}.
	 */
	protected int activeThreadCount = 0;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectMetaData
	 *            Meta-data of the {@link ManagedObject} instances for the
	 *            {@link ProcessState}.
	 * @param administratorMetaData
	 *            Meta-data of the {@link Administrator} instances for the
	 *            {@link ProcessState}.
	 * @param managedObjectEscalationHandler
	 *            {@link EscalationHandler} provided by the
	 *            {@link ManagedObject} that invoked this {@link ProcessState}.
	 * @param officeEscalationHandler
	 *            {@link EscalationHandler} provided by the {@link Office}.
	 */
	@SuppressWarnings("unchecked")
	public ProcessStateImpl(ManagedObjectMetaData[] managedObjectMetaData,
			AdministratorMetaData[] administratorMetaData,
			EscalationHandler managedObjectEscalationHandler,
			EscalationHandler officeEscalationHandler) {
		// Managed Objects
		this.managedObjectContainers = new ManagedObjectContainer[managedObjectMetaData.length];
		for (int i = 0; i < this.managedObjectContainers.length; i++) {
			this.managedObjectContainers[i] = new ManagedObjectContainerImpl(
					managedObjectMetaData[i], this.getProcessLock());
		}

		// Administrators
		this.administratorContainers = new AdministratorContainer[administratorMetaData.length];
		for (int i = 0; i < this.administratorContainers.length; i++) {
			this.administratorContainers[i] = new AdministratorContainerImpl(
					administratorMetaData[i]);
		}

		// Escalation
		this.managedObjectEscalationHandler = managedObjectEscalationHandler;
		this.officeEscalationHandler = officeEscalationHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.ProcessState#getProcessLock()
	 */
	public Object getProcessLock() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ProcessState#createThread()
	 */
	public <W extends Work> Flow createThread(FlowMetaData<W> flowMetaData) {
		// Create the new thread
		ThreadState threadState = new ThreadStateImpl(this, flowMetaData);

		// Increment the number of threads
		synchronized (this.getProcessLock()) {
			this.activeThreadCount++;
		}

		// Return the flow for the new thread
		return threadState.createFlow(flowMetaData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.ProcessState#getCatchAllEscalation
	 * ()
	 */
	@Override
	public Escalation getCatchAllEscalation() {

		// Create the catch all escalation
		TaskMetaDataImpl<Throwable, Work, None, None> catchAllTask = new TaskMetaDataImpl<Throwable, Work, None, None>(
				new CatchAllEscalationTaskFactory(), new PassiveTeam(),
				new int[0], new int[0], new TaskDutyAssociation[0],
				new TaskDutyAssociation[0]);
		FlowMetaData<Work> catchAllFlow = new FlowMetaDataImpl<Work>(
				FlowInstigationStrategyEnum.SEQUENTIAL, catchAllTask, null);
		WorkMetaData<Work> workMetaData = new WorkMetaDataImpl<Work>(-1,
				new CatchAllEscalationWorkFactory(),
				new ManagedObjectMetaData[0], new AdministratorMetaData[0],
				catchAllFlow);
		catchAllTask.loadRemainingState(workMetaData, new FlowMetaData[0],
				null, null);
		Escalation catchAllEscalation = new EscalationImpl(Throwable.class,
				true, catchAllFlow);

		// Return the escalation
		return catchAllEscalation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.ProcessState#threadComplete(
	 * net.officefloor.frame.internal.structure.ThreadState)
	 */
	public void threadComplete(ThreadState thread) {
		// Decrement the number of threads
		synchronized (this.getProcessLock()) {
			this.activeThreadCount--;

			// Determine if clean process
			if (this.activeThreadCount == 0) {
				// No further threads therefore unload managed objects
				for (int i = 0; i < this.managedObjectContainers.length; i++) {

					// Unload the managed objects
					this.managedObjectContainers[i].unloadManagedObject();

					// Notify process complete
					for (ProcessCompletionListener listener : this.completionListeners) {
						listener.processComplete();
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ProcessState#
	 * getManagedObjectContainer(int)
	 */
	public ManagedObjectContainer getManagedObjectContainer(int index) {
		return this.managedObjectContainers[index];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ProcessState#
	 * getAdministratorContainer(int)
	 */
	public AdministratorContainer<?, ?> getAdministratorContainer(int index) {
		return this.administratorContainers[index];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ProcessState#
	 * registerProcessCompletionListener
	 * (net.officefloor.frame.internal.structure.ProcessCompletionListener)
	 */
	public void registerProcessCompletionListener(
			ProcessCompletionListener listener) {
		this.completionListeners.add(listener);
	}

	/**
	 * {@link TaskFactory} for the top level {@link Escalation}.
	 */
	private class CatchAllEscalationTaskFactory implements
			TaskFactory<Throwable, Work, None, None>,
			Task<Throwable, Work, None, None> {

		/*
		 * ================== TaskFactory =============================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.frame.api.build.TaskFactory#createTask(net.officefloor
		 * .frame.api.execute.Work)
		 */
		@Override
		public Task<Throwable, Work, None, None> createTask(Work work) {
			return this;
		}

		/*
		 * =================== Task ====================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame
		 * .api.execute.TaskContext)
		 */
		@Override
		public Object doTask(TaskContext<Throwable, Work, None, None> context) {

			// Obtain the escalation handling
			EscalationHandler moEh = ProcessStateImpl.this.managedObjectEscalationHandler;
			EscalationHandler ofEh = ProcessStateImpl.this.officeEscalationHandler;

			// Obtain the escalation
			Throwable initialEscalation = context.getParameter();
			Throwable escalation = initialEscalation;

			// Handle escalation first from managed object
			try {
				// Determine if provided escalation handler
				if (moEh != null) {
					// Allow escalation handling
					moEh.handleEscalation(escalation);

					// Escalation handled
					return null;
				}
			} catch (Throwable ex) {
				// Continue on to process the failed escalation
				escalation = ex;
			}

			// Handle escalation next from office
			try {
				// Determine if provided escalation handler
				if (ofEh != null) {
					// Allow escalation handling
					ofEh.handleEscalation(escalation);

					// Escalation handled
					return null;
				}
			} catch (Throwable ex) {
				// Continue on to process the failed escalation
				escalation = ex;
			}

			// Log escalation to stderr as last resort
			StringWriter msg = new StringWriter();
			msg.write("Office Floor top level failure: ");
			escalation.printStackTrace(new PrintWriter(msg));
			if (initialEscalation != escalation) {
				msg.write("\n\nCause by: ");
				initialEscalation.printStackTrace(new PrintWriter(msg));
			}
			System.err.println(msg.toString());

			// Escalation handled
			return null;
		}
	}

	/**
	 * {@link WorkFactory} for the top level {@link Escalation}.
	 */
	private static class CatchAllEscalationWorkFactory implements
			WorkFactory<Work>, Work {

		/*
		 * ==================== WorkFactory =======================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.WorkFactory#createWork()
		 */
		@Override
		public Work createWork() {
			return this;
		}
	}

}
