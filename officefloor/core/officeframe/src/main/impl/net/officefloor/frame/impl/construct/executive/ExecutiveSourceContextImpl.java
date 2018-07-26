/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.executive;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link ExecutiveSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveSourceContextImpl extends SourceContextImpl implements ExecutiveSourceContext {

	
	public ExecutiveSourceContextImpl(boolean isLoadingType, SourceContext delegate,
			SourceProperties sourceProperties) {
		super(isLoadingType, delegate, sourceProperties);
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * ================ ExecutiveSourceContext ===========================
	 */


	@Override
	public ThreadFactory createThreadFactory(String executionStrategyName) {
		// TODO Auto-generated method stub
		return null;
	}

}