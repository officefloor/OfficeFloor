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
package net.officefloor.plugin.administrator.clazz;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.ClassFlowMethodMetaData;
import net.officefloor.plugin.clazz.ClassFlowParameterFactory;

/**
 * {@link AdministrationParameterFactory} to obtain the {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationFlowParameterFactory implements AdministrationParameterFactory {

	/**
	 * {@link ClassFlowParameterFactory}.
	 */
	private final ClassFlowParameterFactory flowParameterFactory;

	/**
	 * Initiate.
	 * 
	 * @param flowParameterFactory
	 *            {@link ClassFlowParameterFactory}.
	 */
	public AdministrationFlowParameterFactory(ClassFlowParameterFactory flowParameterFactory) {
		this.flowParameterFactory = flowParameterFactory;
	}

	/**
	 * Obtains the {@link ClassFlowMethodMetaData}.
	 * 
	 * @return {@link ClassFlowMethodMetaData} instances.
	 */
	public ClassFlowMethodMetaData[] getFlowMethodMetaData() {
		return this.flowParameterFactory.getFlowMethodMetaData();
	}

	/*
	 * ==================== ParameterFactory ========================
	 */

	@Override
	public Object createParameter(AdministrationContext<?, ?, ?> context) throws Exception {
		return this.flowParameterFactory.createParameter(context);
	}

}