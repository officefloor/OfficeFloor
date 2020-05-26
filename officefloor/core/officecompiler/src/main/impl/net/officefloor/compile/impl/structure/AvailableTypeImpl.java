/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.impl.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.frame.api.source.SourceContext;

/**
 * {@link AvailableType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AvailableTypeImpl implements AvailableType {

	/**
	 * Extracts the {@link AvailableType} instances.
	 * 
	 * @param managedObjects {@link ManagedObjectNode} instances by name.
	 * @param compileContext {@link CompileContext}.
	 * @return Extracted {@link AvailableType} instances.
	 */
	public static AvailableType[] extractAvailableTypes(Map<String, ManagedObjectNode> managedObjects,
			CompileContext compileContext, SourceContext sourceContext) {

		// Extract the available types
		List<AvailableType> availableTypes = new ArrayList<>(managedObjects.size());
		CompileUtil.source(managedObjects, (mo) -> mo.getBoundManagedObjectName(), (mo) -> {

			// Do not include supplied managed objects
			if (mo.getManagedObjectSourceNode().isSupplied()) {
				return true; // successfully not included
			}

			// Extract the type qualifications and load as available type
			TypeQualification[] typeQualifications = mo.getTypeQualifications(compileContext);
			if (typeQualifications != null) {
				for (TypeQualification typeQualification : typeQualifications) {

					// Load the qualified type
					String qualifier = typeQualification.getQualifier();
					String typeName = typeQualification.getType();
					Class<?> type = sourceContext.loadClass(typeName);
					AvailableType availableType = new AvailableTypeImpl(qualifier, type);

					// Include the available type
					availableTypes.add(availableType);
				}
			}

			// Successful
			return true;
		});

		// Return the extract available types
		return availableTypes.toArray(new AvailableType[availableTypes.size()]);
	}

	/**
	 * Qualifier. May be <code>null</code>.
	 */
	private final String qualifier;

	/**
	 * Type.
	 */
	private final Class<?> type;

	/**
	 * Instantiate.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param type      Type.
	 */
	public AvailableTypeImpl(String qualifier, Class<?> type) {
		this.qualifier = qualifier;
		this.type = type;
	}

	/*
	 * ================= AvailableType ==================
	 */

	@Override
	public String getQualifier() {
		return this.qualifier;
	}

	@Override
	public Class<?> getType() {
		return this.type;
	}

}
