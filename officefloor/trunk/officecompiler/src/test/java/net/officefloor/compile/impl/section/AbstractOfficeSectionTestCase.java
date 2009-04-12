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
package net.officefloor.compile.impl.section;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.LoaderContext;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionBuilder;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.compile.spi.work.source.TaskEscalationTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskFactoryManufacturer;
import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskObjectTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkSourceSpecification;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.issues.StderrCompilerIssuesWrapper;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * Abstract functionality for testing loading the {@link OfficeSection}.
 * 
 * @author Daniel
 */
public abstract class AbstractOfficeSectionTestCase extends OfficeFrameTestCase {

	/**
	 * Location of the top level {@link OfficeSection}.
	 */
	protected static final String SECTION_LOCATION = "SECTION_LOCATION";

	/**
	 * {@link ConfigurationContext}.
	 */
	protected final ConfigurationContext configurationContext = this
			.createMock(ConfigurationContext.class);

	/**
	 * {@link PropertyList}.
	 */
	protected final PropertyList propertyList = this
			.createMock(PropertyList.class);

	/**
	 * {@link ClassLoader}.
	 */
	protected final ClassLoader classLoader = this.getClass().getClassLoader();

	/**
	 * {@link CompilerIssues}.
	 */
	protected final CompilerIssues issues = new StderrCompilerIssuesWrapper(
			this.createMock(CompilerIssues.class));

	/**
	 * Initiate for test.
	 */
	public AbstractOfficeSectionTestCase() {
		MakerManagedObjectSource.reset();
		MakerWorkSource.reset(this);
	}

	/**
	 * Loads the {@link OfficeSection}.
	 * 
	 * @param maker
	 *            {@link SectionMaker} to make the {@link OfficeSection}.
	 * @return Loaded {@link OfficeSection}.
	 */
	protected OfficeSection loadOfficeSection(SectionMaker maker) {

		// Reset for loading the office section
		MakerSectionSource.reset(maker);

		// Load the section
		this.replayMockObjects();
		SectionLoader loader = new SectionLoaderImpl(SECTION_LOCATION);
		OfficeSection section = loader.loadOfficeSection(
				MakerSectionSource.class, this.configurationContext,
				this.propertyList, this.classLoader, this.issues);
		this.verifyMockObjects();

		// Return the section
		return section;
	}

	/**
	 * Makes the {@link SubSection}.
	 */
	protected static interface SectionMaker {

		/**
		 * Test logic to make the {@link SubSection}.
		 * 
		 * @param context
		 *            {@link LoaderContext}.
		 */
		void make(SectionMakerContext context);
	}

	/**
	 * Context for the {@link SectionMaker}.
	 */
	protected static interface SectionMakerContext {

		/**
		 * Obtains the {@link SectionBuilder}.
		 * 
		 * @return {@link SectionBuilder}.
		 */
		SectionBuilder getBuilder();

		/**
		 * Obtains the {@link SectionSourceContext}.
		 * 
		 * @return {@link SectionSourceContext}.
		 */
		SectionSourceContext getContext();

		/**
		 * Adds a {@link SubSection}.
		 * 
		 * @param subSectionName
		 *            Name of the {@link SubSection} which is also used as the
		 *            {@link SubSection} location.
		 * @param maker
		 *            {@link SectionMaker}.
		 * @return Added {@link SubSection}.
		 */
		SubSection addSubSection(String subSectionName, SectionMaker maker);

		/**
		 * Adds a {@link SectionManagedObject}.
		 * 
		 * @param managedObjectName
		 *            Name of the {@link SectionManagedObject}.
		 * @param maker
		 *            {@link ManagedObjectMaker}.
		 * @return Added {@link SectionManagedObject}.
		 */
		SectionManagedObject addManagedObject(String managedObjectName,
				ManagedObjectMaker maker);

		/**
		 * Adds a {@link SectionWork}.
		 * 
		 * @param workName
		 *            Name of the {@link SectionWork}.
		 * @param maker
		 *            {@link WorkMaker}.
		 * @return Added {@link SectionWork}.
		 */
		SectionWork addWork(String workName, WorkMaker maker);

		/**
		 * Adds a {@link SectionTask}.
		 * 
		 * @param workName
		 *            Name of the {@link SectionWork} for the
		 *            {@link SectionTask}.
		 * @param taskName
		 *            Name of the {@link SectionTask}.
		 * @param taskMaker
		 *            {@link TaskMaker} for the {@link SectionTask}.
		 * @return {@link SectionTask}.
		 */
		SectionTask addTask(String workName, String taskName,
				TaskMaker taskMaker);

		/**
		 * Adds a {@link TaskFlow}.
		 * 
		 * @param workName
		 *            Name of the {@link SectionWork} for the
		 *            {@link SectionTask}.
		 * @param taskName
		 *            Name of the {@link SectionTask}.
		 * @param flowName
		 *            Name of the {@link TaskFlowType}.
		 * @param argumentType
		 *            Argument type.
		 * @return {@link TaskFlow}.
		 */
		TaskFlow addTaskFlow(String workName, String taskName, String flowName,
				Class<?> argumentType);

		/**
		 * Adds a {@link TaskObject}.
		 * 
		 * @param workName
		 *            Name of the {@link SectionWork} for the
		 *            {@link SectionTask}.
		 * @param taskName
		 *            Name of the {@link SectionTask}.
		 * @param objectName
		 *            Name of the {@link TaskObjectType}.
		 * @param objectType
		 *            Object type.
		 * @return {@link TaskObject}.
		 */
		TaskObject addTaskObject(String workName, String taskName,
				String objectName, Class<?> objectType);

		/**
		 * Adds a {@link TaskFlow} for a {@link TaskEscalationType}.
		 * 
		 * @param workName
		 *            Name of the {@link SectionWork} for the
		 *            {@link SectionTask}.
		 * @param taskName
		 *            Name of the {@link SectionTask}.
		 * @param escalationName
		 *            Name of the {@link TaskEscalationType}.
		 * @param escalationType
		 *            Escalation type.
		 * @return {@link TaskFlow}.
		 */
		<E extends Throwable> TaskFlow addTaskEscalation(String workName,
				String taskName, String escalationName, Class<E> escalationType);
	}

	/**
	 * Maker {@link SectionSource}.
	 */
	public static class MakerSectionSource implements SectionSource,
			SectionMakerContext {

		/**
		 * Initial {@link SectionMaker}.
		 */
		public static SectionMaker initialMaker = null;

		/**
		 * Reset for the next test.
		 * 
		 * @param initialMaker
		 *            Initial {@link SectionMaker} for the top level
		 *            {@link OfficeSection}.
		 */
		public static void reset(SectionMaker initialMaker) {
			MakerSectionSource.initialMaker = initialMaker;
		}

		/**
		 * {@link SectionMaker}.
		 */
		private final SectionMaker maker;

		/**
		 * {@link SectionBuilder}.
		 */
		private SectionBuilder builder;

		/**
		 * {@link SectionSourceContext}.
		 */
		private SectionSourceContext context;

		/**
		 * Default constructor for initial {@link OfficeSection}.
		 */
		public MakerSectionSource() {
			this.maker = initialMaker;
		}

		/**
		 * Initiate for {@link SubSection} instances.
		 * 
		 * @param maker
		 *            {@link SectionMaker}.
		 */
		private MakerSectionSource(SectionMaker maker) {
			this.maker = maker;
		}

		/*
		 * ===================== SectionSource =============================
		 */

		@Override
		public SectionSourceSpecification getSpecification() {
			fail("Should not require specification");
			return null;
		}

		@Override
		public void sourceSection(SectionBuilder sectionBuilder,
				SectionSourceContext context) throws Exception {

			// Store details to load
			this.builder = sectionBuilder;
			this.context = context;

			// Make the section (if required)
			if (this.maker != null) {
				this.maker.make(this);
			}
		}

		/*
		 * ======================= SectionMakerContext ====================
		 */

		@Override
		public SectionBuilder getBuilder() {
			return this.builder;
		}

		@Override
		public SectionSourceContext getContext() {
			return this.context;
		}

		@Override
		public SubSection addSubSection(String subSectionName,
				SectionMaker maker) {
			// Create the section source
			SectionSource sectionSource = new MakerSectionSource(maker);

			// Return the created sub section (using name as location)
			return this.builder.addSubSection(subSectionName, sectionSource,
					subSectionName);
		}

		@Override
		public SectionManagedObject addManagedObject(String managedObjectName,
				ManagedObjectMaker maker) {
			// Register (and add) the managed object
			return MakerManagedObjectSource.register(managedObjectName,
					this.builder, maker);
		}

		@Override
		public SectionWork addWork(String workName, WorkMaker maker) {
			// Create the work source
			WorkSource<?> workSource = new MakerWorkSource(maker);

			// Return the created work
			return this.builder.addWork(workName, workSource);
		}

		@Override
		public SectionTask addTask(String workName, final String taskName,
				final TaskMaker taskMaker) {
			// Create the section work containing the single task type
			WorkMaker workMaker = new WorkMaker() {
				@Override
				public void make(WorkMakerContext context) {
					// Create the task type and make if required
					TaskTypeMaker maker = context.addTask(taskName);
					if (taskMaker != null) {
						taskMaker.make(maker);
					}
				}
			};
			SectionWork work = this.addWork(workName, workMaker);

			// Return the section task
			return work.addTask(taskName, taskName);
		}

		@Override
		public TaskFlow addTaskFlow(String workName, String taskName,
				final String flowName, final Class<?> argumentType) {
			// Create the section task containing of a single flow
			TaskMaker taskMaker = new TaskMaker() {
				@Override
				public void make(TaskTypeMaker maker) {
					// Create the flow
					maker.addFlow(flowName, argumentType);
				}
			};
			SectionTask task = this.addTask(workName, taskName, taskMaker);

			// Return the task flow
			return task.getTaskFlow(flowName);
		}

		@Override
		public TaskObject addTaskObject(String workName, String taskName,
				final String objectName, final Class<?> objectType) {
			// Create the section task containing of a single object
			TaskMaker taskMaker = new TaskMaker() {
				@Override
				public void make(TaskTypeMaker maker) {
					// Create the object
					maker.addObject(objectName, objectType);
				}
			};
			SectionTask task = this.addTask(workName, taskName, taskMaker);

			// Return the task object
			return task.getTaskObject(objectName);
		}

		@Override
		public <E extends Throwable> TaskFlow addTaskEscalation(
				String workName, String taskName, final String escalationName,
				final Class<E> escalationType) {
			// Create the section task containing of a single escalation
			TaskMaker taskMaker = new TaskMaker() {
				@Override
				public void make(TaskTypeMaker maker) {
					// Create the escalation
					maker.addEscalation(escalationName, escalationType);
				}
			};
			SectionTask task = this.addTask(workName, taskName, taskMaker);

			// Return the task escalation
			return task.getTaskEscalation(escalationName);
		}
	}

	/**
	 * Makes the {@link SectionManagedObject}.
	 */
	protected static interface ManagedObjectMaker {

		/**
		 * Makes the {@link SectionManagedObject}.
		 * 
		 * @param context
		 *            {@link ManagedObjectMakerContext}.
		 */
		void make(ManagedObjectMakerContext context);
	}

	/**
	 * Context for the {@link ManagedObjectMaker}.
	 */
	protected static interface ManagedObjectMakerContext {

		/**
		 * Obtains the {@link MetaDataContext}.
		 * 
		 * @return {@link MetaDataContext}.
		 */
		MetaDataContext<Indexed, Indexed> getContext();

		/**
		 * Adds an extension interface supported by the {@link ManagedObject}.
		 * 
		 * @param extensionInterface
		 *            Extension interface supported.
		 */
		void addExtensionInterface(Class<?> extensionInterface);
	}

	/**
	 * Maker {@link ManagedObjectSource}.
	 */
	public static class MakerManagedObjectSource extends
			AbstractManagedObjectSource<Indexed, Indexed> implements
			ManagedObjectMakerContext {

		/**
		 * Name of property holding the identifier for the
		 * {@link ManagedObjectMaker}.
		 */
		private static final String MAKER_IDENTIFIER_PROPERTY_NAME = "managed.object.maker";

		/**
		 * {@link ManagedObjectMaker} instances by their identifiers.
		 */
		private static Map<String, ManagedObjectMaker> managedObjectMakers;

		/**
		 * Clears the {@link ManagedObjectMaker} instances for the next test.
		 */
		public static void reset() {
			managedObjectMakers = new HashMap<String, ManagedObjectMaker>();
		}

		/**
		 * Registers a {@link ManagedObjectMaker}.
		 * 
		 * @param managedObjectName
		 *            Name of the {@link ManagedObject}.
		 * @param sectionBuilder
		 *            {@link SectionBuilder}.
		 * @param maker
		 *            {@link ManagedObjectMaker}.
		 * @return {@link SectionManagedObject}.
		 */
		public static SectionManagedObject register(String managedObjectName,
				SectionBuilder sectionBuilder, ManagedObjectMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new ManagedObjectMaker() {
					@Override
					public void make(ManagedObjectMakerContext context) {
						// Empty managed object
					}
				};
			}

			// Register the managed object maker
			String identifier = String.valueOf(managedObjectMakers.size());
			managedObjectMakers.put(identifier, maker);

			// Create the section managed object
			SectionManagedObject mo = sectionBuilder
					.addManagedObject(managedObjectName,
							MakerManagedObjectSource.class.getName());
			mo.addProperty(MAKER_IDENTIFIER_PROPERTY_NAME, identifier);

			// Return the section managed object
			return mo;
		}

		/**
		 * {@link MetaDataContext}.
		 */
		private MetaDataContext<Indexed, Indexed> context;

		/*
		 * ================= AbstractManagedObjectSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not require specification");
		}

		@Override
		protected void loadMetaData(MetaDataContext<Indexed, Indexed> context)
				throws Exception {

			// Provide default object type
			context.setObjectClass(Object.class);

			// Store details to load
			this.context = context;

			// Obtain the managed object maker
			String identifier = context.getManagedObjectSourceContext()
					.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			ManagedObjectMaker managedObjectMaker = managedObjectMakers
					.get(identifier);

			// Make the managed object
			managedObjectMaker.make(this);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not require to source managed object");
			return null;
		}

		/*
		 * ==================== ManagedObjectMakerContext ====================
		 */

		@Override
		public MetaDataContext<Indexed, Indexed> getContext() {
			return this.context;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void addExtensionInterface(Class<?> extensionInterface) {
			// Add the extension interface
			this.context.addManagedObjectExtensionInterface(extensionInterface,
					new ExtensionInterfaceFactory() {
						@Override
						public Object createExtensionInterface(
								ManagedObject managedObject) {
							fail("Should not require to create extension interface");
							return null;
						}
					});
		}
	}

	/**
	 * Makes the {@link SectionWork}.
	 */
	protected static interface WorkMaker {

		/**
		 * Makes the {@link SectionWork}.
		 * 
		 * @param context
		 *            {@link WorkMakerContext}.
		 */
		void make(WorkMakerContext context);
	}

	/**
	 * Context for the {@link WorkMaker}.
	 */
	protected static interface WorkMakerContext {

		/**
		 * Obtains the {@link WorkTypeBuilder}.
		 * 
		 * @return {@link WorkTypeBuilder}.
		 */
		WorkTypeBuilder<Work> getBuilder();

		/**
		 * Obtains the {@link WorkSourceContext}.
		 * 
		 * @return {@link WorkSourceContext}.
		 */
		WorkSourceContext getContext();

		/**
		 * Obtains the {@link WorkFactory} used for the {@link SectionWork}.
		 * 
		 * @return {@link WorkFactory} used for the {@link SectionWork}.
		 */
		WorkFactory<Work> getWorkFactory();

		/**
		 * Adds a {@link TaskType}.
		 * 
		 * @param taskTypeName
		 *            Name of the {@link TaskType}.
		 * @return {@link TaskTypeMaker} to make the {@link TaskType}.
		 */
		TaskTypeMaker addTask(String taskTypeName);
	}

	/**
	 * Makes the {@link TaskType}.
	 */
	protected static interface TaskMaker {

		/**
		 * Makes the {@link TaskType}.
		 * 
		 * @param maker
		 *            {@link TaskTypeMaker}.
		 */
		void make(TaskTypeMaker maker);
	}

	/**
	 * Context for the {@link TaskMaker}.
	 */
	protected static interface TaskTypeMaker {

		/**
		 * Adds a {@link TaskFlowType}.
		 * 
		 * @param flowName
		 *            Name of the {@link TaskFlowType}.
		 * @param argumentType
		 *            Argument type.
		 * @return {@link TaskFlowTypeBuilder}.
		 */
		TaskFlowTypeBuilder<?> addFlow(String flowName, Class<?> argumentType);

		/**
		 * Adds a {@link TaskObjectType}.
		 * 
		 * @param objectName
		 *            Name of the {@link TaskObjectType}.
		 * @param objectType
		 *            Object type.
		 * @return {@link TaskObjectTypeBuilder}.
		 */
		TaskObjectTypeBuilder<?> addObject(String objectName,
				Class<?> objectType);

		/**
		 * Adds a {@link TaskEscalationTypeBuilder}.
		 * 
		 * @param escalationName
		 *            Name of the {@link TaskEscalationType}.
		 * @param escalationType
		 *            Escalation type.
		 * @return {@link TaskEscalationTypeBuilder}.
		 */
		<E extends Throwable> TaskEscalationTypeBuilder addEscalation(
				String escalationName, Class<E> escalationType);
	}

	/**
	 * Maker {@link WorkSource}.
	 */
	private static class MakerWorkSource implements WorkSource<Work>,
			WorkMakerContext {

		/**
		 * {@link AbstractOfficeSectionTestCase} currently running.
		 */
		private static AbstractOfficeSectionTestCase testCase;

		/**
		 * Resets for the next load.
		 * 
		 * @param testCase
		 *            {@link AbstractOfficeSectionTestCase} currently running.
		 */
		public static void reset(AbstractOfficeSectionTestCase testCase) {
			MakerWorkSource.testCase = testCase;
		}

		/**
		 * {@link WorkMaker}.
		 */
		private final WorkMaker workMaker;

		/**
		 * {@link WorkFactory}.
		 */
		private WorkFactory<Work> workFactory;

		/**
		 * {@link WorkTypeBuilder}.
		 */
		private WorkTypeBuilder<Work> builder;

		/**
		 * {@link WorkSourceContext}.
		 */
		private WorkSourceContext context;

		/**
		 * Initiate.
		 * 
		 * @param maker
		 *            {@link WorkMaker}.
		 */
		public MakerWorkSource(WorkMaker maker) {
			this.workMaker = maker;
		}

		/*
		 * ==================== WorkSource ==============================
		 */

		@Override
		public WorkSourceSpecification getSpecification() {
			fail("Should not require specification");
			return null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void sourceWork(WorkTypeBuilder<Work> workTypeBuilder,
				WorkSourceContext context) throws Exception {

			// Create and load the work factory
			this.workFactory = testCase.createMock(WorkFactory.class);
			workTypeBuilder.setWorkFactory(this.workFactory);

			// Store details to load
			this.builder = workTypeBuilder;
			this.context = context;

			// Make the work
			this.workMaker.make(this);
		}

		/*
		 * ================== WorkMakerContext ===========================
		 */

		@Override
		public WorkTypeBuilder<Work> getBuilder() {
			return this.builder;
		}

		@Override
		public WorkSourceContext getContext() {
			return this.context;
		}

		@Override
		public WorkFactory<Work> getWorkFactory() {
			return this.workFactory;
		}

		@Override
		public TaskTypeMaker addTask(String taskName) {
			// Create and return the task type maker
			return new TaskTypeMakerImpl(taskName, this.builder);
		}

		/**
		 * {@link TaskTypeMaker} implementation.
		 */
		private class TaskTypeMakerImpl implements TaskTypeMaker {

			/**
			 * {@link TaskFactoryManufacturer}.
			 */
			@SuppressWarnings("unchecked")
			private final TaskFactoryManufacturer<Work, ?, ?> manufacturer = MakerWorkSource.testCase
					.createMock(TaskFactoryManufacturer.class);

			/**
			 * {@link TaskTypeBuilder}.
			 */
			private final TaskTypeBuilder<?, ?> taskTypeBuilder;

			/**
			 * Initiate.
			 * 
			 * @param taskName
			 *            Name of the {@link SectionTask}.
			 * @param workTypeBuilder
			 *            {@link WorkTypeBuilder}.
			 */
			public TaskTypeMakerImpl(String taskName,
					WorkTypeBuilder<Work> workTypeBuilder) {

				// Create the task type builder
				this.taskTypeBuilder = workTypeBuilder.addTaskType(taskName,
						this.manufacturer, null, null);
			}

			/*
			 * ================ TaskMake =================================
			 */

			@Override
			public TaskFlowTypeBuilder<?> addFlow(String flowName,
					Class<?> argumentType) {
				// Create and return the task flow type builder
				TaskFlowTypeBuilder<?> flowBuilder = this.taskTypeBuilder
						.addFlow();
				flowBuilder.setArgumentType(argumentType);
				flowBuilder.setLabel(flowName);
				return flowBuilder;
			}

			@Override
			public TaskObjectTypeBuilder<?> addObject(String objectName,
					Class<?> objectType) {
				// Create and return the task object type builder
				TaskObjectTypeBuilder<?> objectBuilder = this.taskTypeBuilder
						.addObject(objectType);
				objectBuilder.setLabel(objectName);
				return objectBuilder;
			}

			@Override
			public <E extends Throwable> TaskEscalationTypeBuilder addEscalation(
					String escalationName, Class<E> escalationType) {
				// Create and return the task escalation type builder
				TaskEscalationTypeBuilder escalationBuilder = this.taskTypeBuilder
						.addEscalation(escalationType);
				escalationBuilder.setLabel(escalationName);
				return escalationBuilder;
			}
		}
	}

}