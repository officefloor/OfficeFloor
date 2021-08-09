/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.structure;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.AdministrationNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeBindings;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OptionalThreadLocalReceiver;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link InputManagedObjectNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class InputManagedObjectNodeImpl implements InputManagedObjectNode {

	/**
	 * Name of this {@link InputManagedObjectNode}.
	 */
	private final String inputManagedObjectName;

	/**
	 * Input object type.
	 */
	private final String inputObjectType;

	/**
	 * Parent {@link OfficeFloor}.
	 */
	private final OfficeFloorNode officeFloor;

	/**
	 * Initialised state.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {
	}

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Bound {@link ManagedObjectSourceNode}.
	 */
	private ManagedObjectSourceNode boundManagedObjectSource = null;

	/**
	 * {@link TypeQualification} instances for this {@link ManagedObjectNode}.
	 */
	private final List<TypeQualification> typeQualifications = new LinkedList<TypeQualification>();

	/**
	 * Listing of {@link GovernanceNode} instances for the particular
	 * {@link OfficeNode}.
	 */
	private final Map<OfficeNode, List<GovernanceNode>> governancesPerOffice = new HashMap<>();

	/**
	 * Listing of pre-load {@link AdministrationNode} instances for the particular
	 * {@link OfficeNode}.
	 */
	private final Map<OfficeNode, List<AdministrationNode>> preLoadAdministrationsPerOffice = new HashMap<>();

	/**
	 * {@link OptionalThreadLocalInputLinker}.
	 */
	private final OptionalThreadLocalInputLinker optionalThreadLocalInputLinker = new OptionalThreadLocalInputLinker();

	/**
	 * Initiate.
	 * 
	 * @param inputManagedObjectName Name of this {@link InputManagedObjectNode}.
	 * @param inputObjectType        Input object type.
	 * @param officeFloor            {@link OfficeFloorNode}.
	 * @param context                {@link NodeContext}.
	 */
	public InputManagedObjectNodeImpl(String inputManagedObjectName, String inputObjectType,
			OfficeFloorNode officeFloor, NodeContext context) {
		this.inputManagedObjectName = inputManagedObjectName;
		this.inputObjectType = inputObjectType;
		this.officeFloor = officeFloor;
		this.context = context;
	}

	/*
	 * =========================== Node =============================
	 */

	@Override
	public String getNodeName() {
		return this.inputManagedObjectName;
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
		return this.officeFloor;
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
	public void initialise() {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState());
	}

	/*
	 * ======================= InputManagedObjectNode =========================
	 */

	@Override
	public String getInputObjectType() {
		return this.inputObjectType;
	}

	@Override
	public ManagedObjectSourceNode getBoundManagedObjectSourceNode() {
		return this.boundManagedObjectSource;
	}

	/*
	 * ======================= BoundManagedObjectNode =========================
	 */

	@Override
	public String getBoundManagedObjectName() {
		return this.inputManagedObjectName;
	}

	@Override
	public void addGovernance(GovernanceNode governance, OfficeNode office) {

		// Obtain the listing of governances for the office
		List<GovernanceNode> governances = this.governancesPerOffice.get(office);
		if (governances == null) {
			governances = new LinkedList<>();
			this.governancesPerOffice.put(office, governances);
		}

		// Add the governance
		governances.add(governance);
	}

	@Override
	public void addPreLoadAdministration(AdministrationNode preLoadAdministration, OfficeNode office) {

		// Obtain the listing of pre-load administrations for the office
		List<AdministrationNode> preLoadAdmins = this.preLoadAdministrationsPerOffice.get(office);
		if (preLoadAdmins == null) {
			preLoadAdmins = new LinkedList<>();
			this.preLoadAdministrationsPerOffice.put(office, preLoadAdmins);
		}

		// Add the pre-load administration
		preLoadAdmins.add(preLoadAdministration);
	}

	@Override
	public ManagedObjectSourceNode getManagedObjectSourceNode() {
		return this.boundManagedObjectSource;
	}

	@Override
	public void buildOfficeManagedObject(OfficeNode office, OfficeBuilder officeBuilder, OfficeBindings officeBindings,
			CompileContext compileContext) {

		// Provide binding to managed object source if specified
		if (this.boundManagedObjectSource != null) {
			officeBindings.buildInputManagedObjectIntoOffice(this);
		}
	}

	@Override
	public void buildSupplierThreadLocal(OptionalThreadLocalReceiver optionalThreadLocalReceiver) {
		this.optionalThreadLocalInputLinker.addOptionalThreadLocalReceiver(optionalThreadLocalReceiver);
	}

	@Override
	public GovernanceNode[] getGovernances(OfficeNode managingOffice) {

		// Obtain the governances
		List<GovernanceNode> governances = this.governancesPerOffice.get(managingOffice);

		// Return the governances
		return (governances == null ? new GovernanceNode[0]
				: governances.toArray(new GovernanceNode[governances.size()]));
	}

	@Override
	public AdministrationNode[] getPreLoadAdministrations(OfficeNode managingOffice) {

		// Obtain the pre-load administrations
		List<AdministrationNode> preLoadAdmins = this.preLoadAdministrationsPerOffice.get(managingOffice);

		// Return the pre-load administrations
		return (preLoadAdmins == null ? new AdministrationNode[0]
				: preLoadAdmins.toArray(new AdministrationNode[preLoadAdmins.size()]));
	}

	@Override
	public TypeQualification[] getTypeQualifications(CompileContext compileContext) {

		// Obtain the type qualifications
		TypeQualification[] qualifications = this.typeQualifications.stream().toArray(TypeQualification[]::new);
		if (qualifications.length == 0) {

			// No qualifications, so use managed object type
			if (this.boundManagedObjectSource != null) {
				ManagedObjectType<?> managedObjectType = compileContext
						.getOrLoadManagedObjectType(this.boundManagedObjectSource);
				if (managedObjectType == null) {
					return null; // must have type
				}

				// Use the managed object type
				qualifications = new TypeQualification[] {
						new TypeQualificationImpl(null, managedObjectType.getObjectType().getName()) };
			}

			// Still no qualifications, so use input object type
			if ((qualifications.length == 0) && (this.inputObjectType != null)) {
				qualifications = new TypeQualification[] { new TypeQualificationImpl(null, this.inputObjectType) };
			}
		}
		return qualifications;
	}

	/*
	 * ================== OfficeFloorInputManagedObject =======================
	 */

	@Override
	public String getOfficeFloorInputManagedObjectName() {
		return this.inputManagedObjectName;
	}

	@Override
	public void addTypeQualification(String qualifier, String type) {
		this.typeQualifications.add(new TypeQualificationImpl(qualifier, type));
	}

	@Override
	public void setBoundOfficeFloorManagedObjectSource(OfficeFloorManagedObjectSource managedObjectSource) {

		// Ensure is a Managed Object Source Node
		if (!(managedObjectSource instanceof ManagedObjectSourceNode)) {
			this.context.getCompilerIssues().addIssue(this,
					"Invalid managed object source node: " + managedObjectSource + " ["
							+ (managedObjectSource == null ? null : managedObjectSource.getClass().getName())
							+ ", required " + ManagedObjectSourceNode.class.getName() + "]");
			return; // can not bind
		}

		// Ensure not already bound
		if (this.boundManagedObjectSource != null) {
			this.context.getCompilerIssues().addIssue(this,
					"Managed Object Source already bound for Input Managed Object '" + this.inputManagedObjectName
							+ "'");
			return; // already bound
		}

		// Bind the managed object source
		this.boundManagedObjectSource = (ManagedObjectSourceNode) managedObjectSource;

		// Allow linking optional thread local
		this.optionalThreadLocalInputLinker.setManagedObjectSourceNode(this.boundManagedObjectSource);
	}

	/*
	 * =================== LinkObjectNode ======================================
	 */

	/**
	 * Linked {@link LinkObjectNode}.
	 */
	private LinkObjectNode linkedObjectNode;

	@Override
	public boolean linkObjectNode(LinkObjectNode node) {
		return LinkUtil.linkObjectNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedObjectNode = link);
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectNode;
	}

}
