/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.frame.impl.construct.governance;

import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.spi.governance.source.GovernanceSource;
import net.officefloor.frame.spi.governance.source.GovernanceSourceContext;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.SourceProperties;

/**
 * Implementation of the {@link GovernanceSourceContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceSourceContextImpl extends SourceContextImpl implements
		GovernanceSourceContext {

	/**
	 * Initiate.
	 * 
	 * @param properties
	 *            Properties.
	 * @param sourceContext
	 *            Delegate {@link SourceContext}.
	 */
	public GovernanceSourceContextImpl(SourceProperties properties,
			SourceContext sourceContext) {
		super(sourceContext, properties);
	}

	/**
	 * Indicates that the {@link GovernanceSource#init(GovernanceSourceContext)}
	 * method has completed.
	 */
	public void flagInitOver() {
		// Disallow further configuration
	}

	/*
	 * =============== GovernanceSourceContext =====================
	 */

}