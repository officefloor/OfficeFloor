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

package net.officefloor.plugin.section.clazz;

import java.lang.reflect.Method;
import java.util.Arrays;

import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link SectionInterface} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionInterfaceAnnotation {

	/**
	 * Name of the {@link Flow}.
	 */
	private final String flowName;

	/**
	 * Index of the {@link Flow} to invoke for this {@link Method}.
	 */
	private final int flowIndex;

	/**
	 * Flags whether to spawn {@link Flow} in another {@link ThreadState}.
	 */
	private final boolean isSpawn;

	/**
	 * Type for parameter of the {@link Flow}. Will be <code>null</code> if no
	 * parameter.
	 */
	private final Class<?> parameterType;

	/**
	 * Flag indicating if {@link FlowCallback} for the {@link Flow}.
	 */
	private final boolean isFlowCallback;

	/**
	 * Name of the section.
	 */
	private final String sectionName;

	/**
	 * {@link SectionSource} class for this section.
	 */
	private final Class<? extends SectionSource> source;

	/**
	 * Location of the section.
	 */
	private final String location;

	/**
	 * {@link PropertyValue} instances for the section.
	 */
	private final PropertyValueAnnotation[] properties;

	/**
	 * {@link FlowLink} instances for the {@link SectionOutput} instances.
	 */
	private final FlowLinkAnnotation[] outputs;

	/**
	 * Instantiate.
	 * 
	 * @param flowName       Name of the {@link Flow}.
	 * @param flowIndex      Index of {@link Flow} to invoked for this
	 *                       {@link Method}.
	 * @param isSpawn        Flags whether to spawn {@link Flow} in another
	 *                       {@link ThreadState}.
	 * @param parameterType  Type for parameter of the {@link Flow}. Will be
	 *                       <code>null</code> if no parameter.
	 * @param isFlowCallback Flag indicating if {@link FlowCallback} for the
	 *                       {@link Flow}.
	 * @param sectionName    Name of the section.
	 * @param source         {@link SectionSource} class for this section.
	 * @param location       Location of the section.
	 * @param properties     {@link PropertyValue} instances for the section.
	 * @param outputs        {@link FlowLink} instances for the
	 *                       {@link SectionOutput} instances.
	 */
	public SectionInterfaceAnnotation(String flowName, int flowIndex, boolean isSpawn, Class<?> parameterType,
			boolean isFlowCallback, String sectionName, Class<? extends SectionSource> source, String location,
			PropertyValueAnnotation[] properties, FlowLinkAnnotation[] outputs) {
		this.flowName = flowName;
		this.flowIndex = flowIndex;
		this.isSpawn = isSpawn;
		this.parameterType = parameterType;
		this.isFlowCallback = isFlowCallback;
		this.sectionName = sectionName;
		this.source = source;
		this.location = location;
		this.properties = properties;
		this.outputs = outputs;
	}

	/**
	 * Instantiate.
	 * 
	 * @param flowName       Name of the {@link Flow}.
	 * @param flowIndex      Index of {@link Flow} to invoked for this
	 *                       {@link Method}.
	 * @param isSpawn        Flags whether to spawn {@link Flow} in another
	 *                       {@link ThreadState}.
	 * @param parameterType  Type for parameter of the {@link Flow}. Will be
	 *                       <code>null</code> if no parameter.
	 * @param isFlowCallback Flag indicating if {@link FlowCallback} for the
	 *                       {@link Flow}.
	 * @param sectionName    Name of the section.
	 * @param section        {@link SectionInterface}.
	 */
	public SectionInterfaceAnnotation(String flowName, int flowIndex, boolean isSpawn, Class<?> parameterType,
			boolean isFlowCallback, String sectionName, SectionInterface section) {
		this.flowName = flowName;
		this.flowIndex = flowIndex;
		this.isSpawn = isSpawn;
		this.parameterType = parameterType;
		this.isFlowCallback = isFlowCallback;
		this.sectionName = sectionName;
		this.source = section.source();
		this.location = (!Void.class.equals(section.locationClass()) ? section.locationClass().getName()
				: section.location());
		this.properties = Arrays.asList(section.properties()).stream()
				.map((property) -> new PropertyValueAnnotation(property)).toArray(PropertyValueAnnotation[]::new);
		this.outputs = Arrays.asList(section.outputs()).stream().map((output) -> new FlowLinkAnnotation(output))
				.toArray(FlowLinkAnnotation[]::new);
	}

	/**
	 * Obtains the name of the {@link Flow}.
	 * 
	 * @return Name of the {@link Flow}.
	 */
	public String getFlowName() {
		return this.flowName;
	}

	/**
	 * Obtains the index of the {@link Flow} to invoke for this {@link Method}.
	 * 
	 * @return Index of the {@link Flow} to invoke for this {@link Method}.
	 */
	public int getFlowIndex() {
		return flowIndex;
	}

	/**
	 * Indicates whether to spawn {@link Flow} in another {@link ThreadState}.
	 * 
	 * @return <code>true</code> to spawn {@link Flow} in another
	 *         {@link ThreadState}.
	 */
	public boolean isSpawn() {
		return isSpawn;
	}

	/**
	 * Obtains the parameter type.
	 * 
	 * @return Type for parameter of the {@link Flow}. Will be <code>null</code> if
	 *         no parameter.
	 */
	public Class<?> getParameterType() {
		return parameterType;
	}

	/**
	 * Indicates if {@link FlowCallback} for the {@link Flow}.
	 * 
	 * @return <code>true</code> if {@link FlowCallback} for the {@link Flow}.
	 */
	public boolean isFlowCallback() {
		return isFlowCallback;
	}

	/**
	 * Obtains the name of the section.
	 * 
	 * @return Name of the section.
	 */
	public String getSectionName() {
		return this.sectionName;
	}

	/**
	 * Obtains the {@link SectionSource} class for this section.
	 * 
	 * @return {@link SectionSource} class.
	 */
	public Class<? extends SectionSource> getSource() {
		return this.source;
	}

	/**
	 * Obtains the location of the section.
	 * 
	 * @return Location of the section.
	 */
	public String getLocation() {
		return this.location;
	}

	/**
	 * Obtains the {@link PropertyValue} instances for the section.
	 * 
	 * @return {@link PropertyValue} instances for the section.
	 */
	public PropertyValueAnnotation[] getProperties() {
		return this.properties;
	}

	/**
	 * Obtains the {@link FlowLink} instances for the {@link SectionOutput}
	 * instances.
	 * 
	 * @return {@link FlowLink} instances for the {@link SectionOutput} instances.
	 */
	public FlowLinkAnnotation[] getOutputs() {
		return this.outputs;
	}

}
