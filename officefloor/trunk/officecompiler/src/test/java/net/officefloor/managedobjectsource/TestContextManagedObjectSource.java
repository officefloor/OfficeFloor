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
package net.officefloor.managedobjectsource;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlersBuilder;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} to provide testing of
 * {@link ManagedObjectSourceLoader}.
 * 
 * @author Daniel
 */
public class TestContextManagedObjectSource extends AbstractManagedObjectSource {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadSpecification(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty("test");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadMetaData(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext)
	 */
	@Override
	protected void loadMetaData(MetaDataContext context) throws Exception {

		// Obtain the managed object source context for additional configuration
		ManagedObjectSourceContext sourceContext = context
				.getManagedObjectSourceContext();
		
		// Register handler with flows
		ManagedObjectHandlersBuilder<HandlerKey> managedObjectHandlerBuilder = sourceContext
				.getHandlerBuilder(HandlerKey.class);
		ManagedObjectHandlerBuilder indirectHandlerBuilder = managedObjectHandlerBuilder
				.registerHandler(HandlerKey.INDIRECT_HANDLER);
		indirectHandlerBuilder.setHandlerType(Handler.class);
		ManagedObjectHandlerBuilder addedHandlerBuilder = managedObjectHandlerBuilder
				.registerHandler(HandlerKey.ADDED_HANDLER);
		HandlerBuilder<Indexed> addedHandler = addedHandlerBuilder
				.getHandlerBuilder();
		addedHandler.setHandlerFactory(new HandlerFactory<Indexed>() {
			@Override
			public Handler<Indexed> createHandler() {
				TestCase.fail("Should not create Handler");
				return null;
			}
		});
		addedHandler.linkProcess(0, null, null);
		addedHandler.linkProcess(1, "handler-work", "handler-task");

		// Register a task with the office
		ManagedObjectWorkBuilder<Work> workBuilder = sourceContext.addWork(
				"WORK", Work.class);
		ManagedObjectTaskBuilder<Indexed> taskBuilder = workBuilder.addTask(
				"TASK", Object.class, null);
		taskBuilder.linkFlow(0, null, FlowInstigationStrategyEnum.SEQUENTIAL);
		taskBuilder.linkFlow(1, null, FlowInstigationStrategyEnum.PARALLEL);

		// Register recycle work
		ManagedObjectWorkBuilder<Work> recycleWorkBuilder = sourceContext
				.getRecycleWork(Work.class);
		ManagedObjectTaskBuilder<Indexed> recycleOneTaskBuilder = recycleWorkBuilder
				.addTask("RECYCLE TASK ONE", Object.class, null);
		recycleOneTaskBuilder.linkFlow(0, "RECYCLE TASK TWO",
				FlowInstigationStrategyEnum.PARALLEL);
		recycleOneTaskBuilder.linkFlow(1, null,
				FlowInstigationStrategyEnum.SEQUENTIAL);
		ManagedObjectTaskBuilder<Indexed> recycleTwoTaskBuilder = recycleWorkBuilder
				.addTask("RECYCLE TASK TWO", Object.class, null);
		recycleTwoTaskBuilder.linkFlow(0, "RECYCLE TASK ONE",
				FlowInstigationStrategyEnum.SEQUENTIAL);
		recycleTwoTaskBuilder.setNextTaskInFlow("RECYCLE TASK ONE");
		recycleTwoTaskBuilder.setTeam("Recycle Team");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource#getManagedObject()
	 */
	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		Assert.fail("getManagedObject should not be invoked");
		return null;
	}

	/**
	 * Handler keys.
	 */
	public static enum HandlerKey {
		INDIRECT_HANDLER, ADDED_HANDLER
	}

	/**
	 * Handler flow keys.
	 */
	public static enum HandlerFlowKey {
		FLOW_ONE, FLOW_TWO
	}
}
