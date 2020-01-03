package net.officefloor.plugin.section.clazz;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;

/**
 * {@link ManagedObjectSource} implementation to make the
 * {@link ClassSectionSource} object available with necessary dependency
 * injection.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class SectionClassManagedObjectSource extends ClassManagedObjectSource {

	@Override
	protected List<Field> extractDependencyFields(Class<?> objectClass) {

		// Override to obtain both dependency and managed object fields
		List<Field> dependencyFields = new LinkedList<Field>();
		Class<?> interrogateClass = objectClass;
		while ((interrogateClass != null) && (!Object.class.equals(interrogateClass))) {
			for (Field field : interrogateClass.getDeclaredFields()) {
				if ((field.getAnnotation(Dependency.class) != null)
						|| (field.getAnnotation(ManagedObject.class) != null)) {
					// Annotated as a dependency field
					dependencyFields.add(field);
				}
			}
			interrogateClass = interrogateClass.getSuperclass();
		}

		// Return the dependency fields
		return dependencyFields;
	}

}