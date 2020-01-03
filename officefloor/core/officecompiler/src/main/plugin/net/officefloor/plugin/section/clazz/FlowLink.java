package net.officefloor.plugin.section.clazz;

import java.lang.annotation.Documented;

import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link FlowLink} of a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
public @interface FlowLink {

	/**
	 * Obtains the name of the {@link ManagedObjectFlow}.
	 * 
	 * @return Name of the {@link ManagedObjectFlow}.
	 */
	String name();

	/**
	 * Obtains the name of the method to link the {@link FlowLink}.
	 * 
	 * @return Name of the method to link the {@link FlowLink}.
	 */
	String method();

}