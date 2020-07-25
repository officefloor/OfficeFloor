package net.officefloor.plugin.section.clazz.loader;

import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Decoration of {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectDecoration {

	/**
	 * Decorates the {@link SectionManagedObject}.
	 * 
	 * @param objectContext {@link ObjectClassSectionLoaderContext}.
	 */
	public void decorateObject(ObjectClassSectionLoaderContext objectContext) {
		// No decoration by default
	}

}