package net.officefloor.web.template.section;

import java.lang.reflect.Field;

import net.officefloor.plugin.managedobject.clazz.DependencyMetaData;
import net.officefloor.web.HttpSessionStateful;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.session.object.HttpSessionObjectManagedObject.Dependencies;

/**
 * {@link DependencyMetaData} for {@link HttpSessionStateful}.
 * 
 * @author Daniel Sagenschneider
 */
public class StatefulDependencyMetaData extends DependencyMetaData {

	/**
	 * {@link HttpSession} for providing {@link Field}.
	 */
	public HttpSession httpSession;

	/**
	 * Initiate.
	 * 
	 * @throws Exception
	 *             Should not occur but required for compiling.
	 */
	public StatefulDependencyMetaData() throws Exception {
		super(Dependencies.HTTP_SESSION.name(), Dependencies.HTTP_SESSION
				.ordinal(), StatefulDependencyMetaData.class
				.getField("httpSession"));
	}

}