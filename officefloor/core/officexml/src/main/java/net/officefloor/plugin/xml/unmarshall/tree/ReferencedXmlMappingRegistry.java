/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.xml.unmarshall.tree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Registry of {@link ReferenceXmlMapping} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ReferencedXmlMappingRegistry {

	/**
	 * Registry of {@link ReferenceXmlMapping} instances.
	 */
	protected final Map<String, List<ReferenceXmlMapping>> registry = new HashMap<String, List<ReferenceXmlMapping>>();

	/**
	 * Obtains the {@link ElementXmlMapping} instance identified by its id for a
	 * specific target object.
	 * 
	 * @param id
	 *            Id of the {@link XmlMapping}.
	 * @param targetObjectType
	 *            Class of the target object.
	 * @return {@link ElementXmlMapping} by id for the target object. If no
	 *         match is found, then will return <code>null</code>.
	 */
	public ElementXmlMapping getElementXmlMapping(String id,
			Class<?> targetObjectType) {
		// Obtain recursive xml mappings for id
		List<ReferenceXmlMapping> recursiveMappings = this.registry.get(id);

		// Find mapping corresponding to target object
		for (ReferenceXmlMapping recursiveMapping : recursiveMappings) {
			if (recursiveMapping.getTargetObjectType().equals(targetObjectType)) {
				return recursiveMapping.getElementXmlMapping();
			}
		}

		// No mapping found
		return null;
	}

	/**
	 * Obtains the meta-data of the {@link ElementXmlMapping} by its id.
	 * 
	 * @param id
	 *            Id of the {@link ElementXmlMapping}.
	 * @return {@link XmlMappingMetaData} for the id.
	 */
	public XmlMappingMetaData getXmlMappingMetaData(String id) {

		// Obtain recursive xml mappings for id
		List<ReferenceXmlMapping> recursiveMappings = this.registry.get(id);

		// Obtain the first meta-data (as will be the same for the id)
		if (recursiveMappings != null) {
			for (ReferenceXmlMapping recursiveMapping : recursiveMappings) {
				return recursiveMapping.getXmlMappingMetaData();
			}
		}

		// No mapping found
		return null;
	}

	/**
	 * Registers a reference {@link XmlMapping}.
	 * 
	 * @param id
	 *            Id of the {@link XmlMapping}.
	 * @param targetObjectType
	 *            Class of the target object for the {@link XmlMapping}.
	 * @param mapping
	 *            The {@link ElementXmlMapping} itself.
	 * @param mappingMetaData
	 *            Meta-data of the {@link XmlMapping}.
	 */
	protected void registerReferenceXmlMapping(String id,
			Class<?> targetObjectType, ElementXmlMapping mapping,
			XmlMappingMetaData mappingMetaData) {

		// Ensure obtain reference xml mapping by lazy register
		List<ReferenceXmlMapping> referenceMappings = this.registry.get(id);
		if (referenceMappings == null) {
			referenceMappings = new LinkedList<ReferenceXmlMapping>();
			this.registry.put(id, referenceMappings);
		}

		// Add the reference mapping
		referenceMappings.add(new ReferenceXmlMapping(targetObjectType,
				mapping, mappingMetaData));
	}
}

class ReferenceXmlMapping {

	/**
	 * Class of target object for reference mapping.
	 */
	protected final Class<?> targetObjectType;

	/**
	 * {@link ElementXmlMapping} to reference.
	 */
	protected final ElementXmlMapping mapping;

	/**
	 * Meta-data of the {@link ElementXmlMapping} to enable creation of a
	 * {@link ElementXmlMapping} specific to target object.
	 */
	protected final XmlMappingMetaData mappingMetaData;

	/**
	 * Initiate with details to enable recursive xml mapping.
	 * 
	 * @param targetObjectType
	 *            Class of the target object for recursive mapping.
	 * @param mapping
	 *            {@link ElementXmlMapping} to reference.
	 * @param mappingMetaData
	 *            Meta-data of the {@link XmlMapping}.
	 */
	public ReferenceXmlMapping(Class<?> targetObjectType,
			ElementXmlMapping mapping, XmlMappingMetaData mappingMetaData) {
		// Store state
		this.targetObjectType = targetObjectType;
		this.mapping = mapping;
		this.mappingMetaData = mappingMetaData;
	}

	/**
	 * Obtains the class of the target object for this mapping.
	 * 
	 * @return Class of the target object.
	 */
	public Class<?> getTargetObjectType() {
		return this.targetObjectType;
	}

	/**
	 * Obtains the {@link ElementXmlMapping} of this reference mapping.
	 * 
	 * @return {@link ElementXmlMapping}.
	 */
	public ElementXmlMapping getElementXmlMapping() {
		return this.mapping;
	}

	/**
	 * Obtains the meta-data of the {@link XmlMapping}.
	 * 
	 * @return Meta-data of the {@link XmlMapping}.
	 */
	public XmlMappingMetaData getXmlMappingMetaData() {
		return this.mappingMetaData;
	}
}
