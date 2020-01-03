package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.ExecutionManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Node representing an instance use of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectNode extends DependentObjectNode, BoundManagedObjectNode, ManagedObjectExtensionNode,
		SectionManagedObject, OfficeSectionManagedObject, OfficeManagedObject, OfficeFloorManagedObject {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Managed Object";

	/**
	 * Initialises the {@link ManagedObjectNode}.
	 * 
	 * @param managedObjectScope      {@link ManagedObjectScope} for the
	 *                                {@link ManagedObject}.
	 * @param managedObjectSourceNode {@link ManagedObjectSourceNode} for the
	 *                                {@link ManagedObjectNode}.
	 */
	void initialise(ManagedObjectScope managedObjectScope, ManagedObjectSourceNode managedObjectSourceNode);

	/**
	 * Sources the {@link ManagedObject}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced the {@link ManagedObject}.
	 *         <code>false</code> if failed to source, with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceManagedObject(CompileContext compileContext);

	/**
	 * Obtains the {@link ManagedObjectSourceNode} for this
	 * {@link ManagedObjectNode}.
	 * 
	 * @return {@link ManagedObjectSourceNode} for this {@link ManagedObjectNode}.
	 */
	ManagedObjectSourceNode getManagedObjectSourceNode();

	/**
	 * Obtains the {@link TypeQualification} instances for the
	 * {@link ManagedObject}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link TypeQualification} instances for the {@link ManagedObject}.
	 */
	TypeQualification[] getTypeQualifications(CompileContext compileContext);

	/**
	 * Obtains the {@link ManagedObjectDependencyNode} instances.
	 * 
	 * @return {@link ManagedObjectDependencyNode} instances.
	 */
	ManagedObjectDependencyNode[] getManagedObjectDepdendencies();

	/**
	 * Auto-wires the dependencies for the {@link ManagedObject}.
	 * 
	 * @param autoWirer      {@link AutoWirer}.
	 * @param office         {@link OfficeNode} requiring the auto-wiring.
	 * @param compileContext {@link CompileContext}.
	 */
	void autoWireDependencies(AutoWirer<LinkObjectNode> autoWirer, OfficeNode office, CompileContext compileContext);

	/**
	 * Creates the {@link ExecutionManagedObject} for this
	 * {@link ManagedObjectNode}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link ExecutionManagedObject} for this {@link ManagedObjectNode}.
	 */
	ExecutionManagedObject createExecutionManagedObject(CompileContext compileContext);

	/**
	 * Loads the {@link OfficeSectionManagedObjectType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link OfficeSectionManagedObjectType} or <code>null</code> with
	 *         issues reported to the {@link CompilerIssues}.
	 */
	OfficeSectionManagedObjectType loadOfficeSectionManagedObjectType(CompileContext compileContext);

}