package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.office.AdministerableManagedObject;
import net.officefloor.compile.spi.office.GovernerableManagedObject;

/**
 * Extension of {@link ManagedObjectNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExtensionNode extends Node, AdministerableManagedObject, GovernerableManagedObject {
}