package net.officefloor.compile.internal.structure;

import net.officefloor.compile.managedobject.ManagedObjectType;

/**
 * Visits each {@link ManagedObjectSourceNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceVisitor {

	/**
	 * Visits the {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectType       {@link ManagedObjectType}.
	 * @param managedObjectSourceNode {@link ManagedObjectSourceNode}.
	 * @param compileContext          {@link CompileContext}.
	 */
	void visit(ManagedObjectType<?> managedObjectType, ManagedObjectSourceNode managedObjectSourceNode,
			CompileContext compileContext);

}