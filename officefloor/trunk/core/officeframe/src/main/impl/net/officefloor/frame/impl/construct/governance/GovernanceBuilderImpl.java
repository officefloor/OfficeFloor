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

import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.spi.governance.source.GovernanceSource;
import net.officefloor.frame.spi.source.SourceProperties;

/**
 * {@link GovernanceBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceBuilderImpl<I, F extends Enum<F>, GS extends GovernanceSource<I, F>>
		implements GovernanceBuilder, GovernanceConfiguration<I, F, GS> {

	/**
	 * {@link SourceProperties} for the {@link GovernanceSource}.
	 */
	private final SourcePropertiesImpl properties = new SourcePropertiesImpl();

	/*
	 * ================= GovernanceBuilder =======================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name, value);
	}

	/*
	 * =============== GovernanceConfiguration ====================
	 */

	@Override
	public String getGovernanceName() {
		// TODO implement GovernanceConfiguration.getGovernanceName
		throw new UnsupportedOperationException(
				"TODO implement GovernanceConfiguration.getGovernanceName");
	}

	@Override
	public GS getGovernanceSource() {
		// TODO implement GovernanceConfiguration<I,F,GS>.getGovernanceSource
		throw new UnsupportedOperationException(
				"TODO implement GovernanceConfiguration<I,F,GS>.getGovernanceSource");
	}

	@Override
	public Class<GS> getGovernanceSourceClass() {
		// TODO implement
		// GovernanceConfiguration<I,F,GS>.getGovernanceSourceClass
		throw new UnsupportedOperationException(
				"TODO implement GovernanceConfiguration<I,F,GS>.getGovernanceSourceClass");
	}

	@Override
	public SourceProperties getProperties() {
		// TODO implement GovernanceConfiguration<I,F,GS>.getProperties
		throw new UnsupportedOperationException(
				"TODO implement GovernanceConfiguration<I,F,GS>.getProperties");
	}

}