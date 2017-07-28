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
package net.officefloor.eclipse.util;

import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.configuration.project.ProjectConfigurationContext;

/**
 * Utility methods for working with Java.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaUtil {

	/**
	 * Convenience method.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 * @param typeName
	 *            Type.
	 * @return Listing of {@link IType} that represent the classes of the input
	 *         type.
	 * @throws JavaModelException
	 *             If fails to obtain sub types.
	 */
	public static IType[] getSubTypes(AbstractOfficeFloorEditPart<?, ?, ?> editPart, String typeName)
			throws JavaModelException {

		// Obtain the project
		IProject project = ProjectConfigurationContext.getProject(editPart.getEditor().getEditorInput());

		// Obtain the sub types
		return getSubTypes(project, typeName);
	}

	/**
	 * Obtains the listing of {@link IType} that represent the classes of the
	 * input type.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @param typeName
	 *            Type name.
	 * @return Listing of {@link IType} that represent the classes of the input
	 *         type.
	 * @throws JavaModelException
	 *             If fails to obtain sub types.
	 */
	public static IType[] getSubTypes(IProject project, String typeName) throws JavaModelException {

		// Obtain the type on the class path
		IJavaProject javaProject = JavaCore.create(project);
		IType javaType = javaProject.findType(typeName);
		if (javaType == null) {
			// Type not on class path, so no sub types
			return new IType[0];
		}

		// Obtain the list of implementations
		ITypeHierarchy hierarchy = javaType.newTypeHierarchy(javaProject, null);
		IType[] allTypes = hierarchy.getAllClasses();

		// Trim the list to non-abstract implementations
		LinkedList<IType> subTypes = new LinkedList<IType>();
		for (IType subType : allTypes) {

			// Ignore object
			if (Object.class.getName().equals(subType.getFullyQualifiedName())) {
				continue;
			}

			// Ignore abstract classes
			if (Flags.isAbstract(subType.getFlags())) {
				continue;
			}

			// Include type
			subTypes.add(subType);
		}

		// Return the sub types
		return subTypes.toArray(new IType[0]);
	}

	/**
	 * All access via static methods.
	 */
	private JavaUtil() {
	}
}
