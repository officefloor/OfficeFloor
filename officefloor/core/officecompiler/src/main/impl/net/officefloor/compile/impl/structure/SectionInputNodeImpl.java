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
package net.officefloor.compile.impl.structure;

import net.officefloor.compile.impl.issues.FailCompilerIssues;
import net.officefloor.compile.impl.section.OfficeSectionInputTypeImpl;
import net.officefloor.compile.impl.section.SectionInputTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.ManagedFunctionNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionInputNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.ExternalServiceCleanupEscalationHandler;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.NameAwareManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link SectionInputNode} node.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionInputNodeImpl implements SectionInputNode {

	/**
	 * Name of the {@link SectionInputType}.
	 */
	private final String inputName;

	/**
	 * {@link SectionNode} containing this {@link SectionInputNode}.
	 */
	private final SectionNode section;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link InitialisedState}.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {

		/**
		 * Parameter type.
		 */
		private String parameterType;

		/**
		 * Instantiate.
		 * 
		 * @param parameterType
		 *            Parameter type.
		 */
		public InitialisedState(String parameterType) {
			this.parameterType = parameterType;
		}
	}

	/**
	 * {@link ExternalServiceProxyFunctionManager}.
	 */
	private ExternalServiceProxyFunctionManager externalFunctionManager = null;

	/**
	 * Instantiate.
	 * 
	 * @param inputName
	 *            Name of the {@link SubSectionInput} (which is the name of the
	 *            {@link SectionInputType}).
	 * @param section
	 *            {@link SectionNode} containing this {@link SectionInputNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SectionInputNodeImpl(String inputName, SectionNode section, NodeContext context) {
		this.inputName = inputName;
		this.section = section;
		this.context = context;
	}

	/*
	 * ================== Node ========================
	 */

	@Override
	public String getNodeName() {
		return this.inputName;
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public Node getParentNode() {
		return this.section;
	}

	@Override
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes();
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise(String parameterType) {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState(parameterType));
	}

	/*
	 * ===================== SectionInputNode ===========================
	 */

	@Override
	public void loadExternalServicing(Office office) throws UnknownFunctionException {

		// Determine if externally triggered
		if (this.externalFunctionManager == null) {
			return; // not externally triggered
		}

		// Obtain the function node servicing this section input
		ManagedFunctionNode functionNode = LinkUtil.retrieveTarget(this, ManagedFunctionNode.class,
				new FailCompilerIssues());
		if (functionNode == null) {
			// Compiled, so this should not occur
			return; // must have function
		}

		// Obtain the corresponding function manager
		String functionName = functionNode.getQualifiedFunctionName();
		FunctionManager functionManager = office.getFunctionManager(functionName);

		// Load the function to the proxy
		this.externalFunctionManager.delegate = functionManager;
	}

	@Override
	public SectionInputType loadSectionInputType(CompileContext compileContext) {

		// Ensure have input name
		if (CompileUtil.isBlank(this.inputName)) {
			this.context.getCompilerIssues().addIssue(this, "Null name for " + TYPE);
			return null; // must have names for inputs
		}

		// Create and return type
		return new SectionInputTypeImpl(this.inputName, this.state.parameterType);
	}

	@Override
	public OfficeSectionInputType loadOfficeSectionInputType(CompileContext compileContext) {
		return new OfficeSectionInputTypeImpl(this.inputName, this.state.parameterType);
	}

	/*
	 * ================= SectionInput =========================
	 */

	@Override
	public String getSectionInputName() {
		return this.inputName;
	}

	/*
	 * =================== SubSectionInput ========================
	 */

	@Override
	public String getSubSectionInputName() {
		return this.inputName;
	}

	/*
	 * ===================== OfficeSectionInput =====================
	 */

	@Override
	public OfficeSection getOfficeSection() {
		return this.section;
	}

	@Override
	public String getOfficeSectionInputName() {
		return this.inputName;
	}

	/*
	 * ====================== DeployedOfficeInput ====================
	 */

	@Override
	public String getDeployedOfficeInputName() {
		return this.inputName;
	}

	@Override
	public DeployedOffice getDeployedOffice() {
		return this.section.getOfficeNode();
	}

	@Override
	public FunctionManager getFunctionManager() {

		// Lazy create the function manager
		if (this.externalFunctionManager == null) {
			this.externalFunctionManager = new ExternalServiceProxyFunctionManager();
		}

		// Return the external function manager
		return this.externalFunctionManager;
	}

	@Override
	public <O, M extends ManagedObject> ExternalServiceInput<O, M> addExternalServiceInput(Class<O> objectType,
			Class<M> managedObjectType, ExternalServiceCleanupEscalationHandler<M> cleanupEscalationHandler) {

		// Create the external service input
		ExternalServiceInputManagedObjectSource<O, M> input = new ExternalServiceInputManagedObjectSource<>(objectType,
				managedObjectType, cleanupEscalationHandler);

		// Add to OfficeFloor (to make available for auto-wiring)
		OfficeNode office = this.section.getOfficeNode();
		OfficeFloorNode officeFloor = office.getOfficeFloorNode();

		// Configure the managed object source
		String inputObjectName = ExternalServiceInput.class.getSimpleName() + "_" + objectType.getName();
		OfficeFloorManagedObjectSource mos = officeFloor.addManagedObjectSource(inputObjectName, input);
		LinkUtil.linkOffice(mos.getManagingOffice(), office, this.context.getCompilerIssues(), this);
		LinkUtil.linkFlow(mos.getManagedObjectFlow(Flows.SERVICE.name()), this, this.context.getCompilerIssues(), this);

		// Configure external service input
		OfficeFloorInputManagedObject inputMo = officeFloor.addInputManagedObject(inputObjectName,
				objectType.getName());
		inputMo.addTypeQualification(null, objectType.getName());
		LinkUtil.linkManagedObjectSourceInput(mos, inputMo, this.context.getCompilerIssues(), this);

		// Return the external service input
		return input;
	}

	/*
	 * =================== LinkFlowNode ============================
	 */

	/**
	 * Linked {@link LinkFlowNode}.
	 */
	private LinkFlowNode linkedFlowNode = null;

	@Override
	public boolean linkFlowNode(LinkFlowNode node) {
		return LinkUtil.linkFlowNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedFlowNode = link);
	}

	@Override
	public LinkFlowNode getLinkedFlowNode() {
		return this.linkedFlowNode;
	}

	/**
	 * Proxy {@link FunctionManager} for external service handling.
	 */
	private static class ExternalServiceProxyFunctionManager implements FunctionManager {

		/**
		 * Delegate {@link FunctionManager}.
		 */
		private FunctionManager delegate = null;

		@Override
		public Object getDifferentiator() {
			return this.delegate.getDifferentiator();
		}

		@Override
		public Class<?> getParameterType() {
			return this.delegate.getParameterType();
		}

		@Override
		public void invokeProcess(Object parameter, FlowCallback callback) throws InvalidParameterTypeException {
			this.delegate.invokeProcess(parameter, callback);
		}
	}

	/**
	 * Flows for the {@link ExternalServiceInputManagedObjectSource}.
	 */
	private static enum Flows {
		SERVICE
	}

	/**
	 * {@link ExternalServiceInput} {@link ManagedObjectSource}.
	 * 
	 * @param <O>
	 *            {@link ExternalServiceInput} object type.
	 */
	private static class ExternalServiceInputManagedObjectSource<O, M extends ManagedObject>
			extends AbstractManagedObjectSource<None, Flows>
			implements ExternalServiceInput<O, M>, ManagedFunction<None, None> {

		/**
		 * {@link ExternalServiceInput} object type.
		 */
		private final Class<O> objectType;

		/**
		 * {@link ManagedObject} type.
		 */
		private final Class<M> managedObjectType;

		/**
		 * {@link ExternalServiceCleanupEscalationHandler}.
		 */
		private final ExternalServiceCleanupEscalationHandler<M> cleanupEscalationHandler;

		/**
		 * {@link ManagedObjectExecuteContext}.
		 */
		private ManagedObjectExecuteContext<Flows> context;

		/**
		 * Instantiate.
		 * 
		 * @param objectType
		 *            {@link ExternalServiceInput} object type.
		 * @param managedObjectType
		 *            {@link ManagedObject} type.
		 * @param cleanupEscalationHandler
		 *            {@link ExternalServiceCleanupEscalationHandler}.
		 */
		private ExternalServiceInputManagedObjectSource(Class<O> objectType, Class<M> managedObjectType,
				ExternalServiceCleanupEscalationHandler<M> cleanupEscalationHandler) {
			this.objectType = objectType;
			this.managedObjectType = managedObjectType;
			this.cleanupEscalationHandler = cleanupEscalationHandler;
		}

		/*
		 * =============== ExternalServiceInput ================
		 */

		@Override
		public void service(M managedObject, FlowCallback callback) {
			this.context.invokeProcess(Flows.SERVICE, null, managedObject, 0, callback);
		}

		/*
		 * ================ ManagedObjectSource =================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {
			context.setObjectClass(this.objectType);
			context.setManagedObjectClass(this.managedObjectType);
			context.addFlow(Flows.SERVICE, null);

			// Configure clean up escalation handling
			if (this.cleanupEscalationHandler != null) {
				context.getManagedObjectSourceContext().getRecycleFunction(new ManagedFunctionFactory<None, None>() {
					@Override
					public ManagedFunction<None, None> createManagedFunction() throws Throwable {
						return ExternalServiceInputManagedObjectSource.this;
					}
				}).linkParameter(0, RecycleManagedObjectParameter.class);
			}
		}

		@Override
		public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {
			this.context = context;
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			// Not externally servicing, so no object
			return new NullManagedObject();
		}

		/*
		 * ================ Recycle ManagedFunction ======================
		 */

		@Override
		public Object execute(ManagedFunctionContext<None, None> context) throws Throwable {

			// Obtain the recycle parameter
			RecycleManagedObjectParameter<M> parameter = RecycleManagedObjectParameter
					.getRecycleManagedObjectParameter(context);

			// Obtain the managed object
			M managedObject = parameter.getManagedObject();

			// Handle clean up escalations
			this.cleanupEscalationHandler.handleCleanupEscalations(managedObject, parameter.getCleanupEscalations());

			// Enable re-use of the object
			parameter.reuseManagedObject(managedObject);

			// Nothing further
			return null;
		}
	}

	/**
	 * {@link ManagedObject} providing <code>null</code> value. Must implement
	 * all {@link ManagedObject} interfaces to avoid {@link ClassCastException}.
	 */
	private static class NullManagedObject implements ManagedObject, NameAwareManagedObject,
			CoordinatingManagedObject<None>, AsynchronousManagedObject, ProcessAwareManagedObject {

		/*
		 * =================== ManagedObject ====================
		 */

		@Override
		public Object getObject() throws Throwable {
			return null;
		}

		@Override
		public void setBoundManagedObjectName(String boundManagedObjectName) {
			// Ignored
		}

		@Override
		public void setProcessAwareContext(ProcessAwareContext context) {
			// Ignored
		}

		@Override
		public void setAsynchronousContext(AsynchronousContext context) {
			// Ignored
		}

		@Override
		public void loadObjects(ObjectRegistry<None> registry) throws Throwable {
			// Ignored
		}

	}

}