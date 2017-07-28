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
package net.officefloor.compile.spi.governance.source;

import net.officefloor.frame.api.governance.Governance;

/**
 * Source to obtain the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceSource<I, F extends Enum<F>> {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must
	 * be able to return the specification immediately after a default
	 * constructor instantiation.
	 * 
	 * @return Specification of this.
	 */
	GovernanceSourceSpecification getSpecification();

	/**
	 * Initialises the {@link GovernanceSource}.
	 * 
	 * @param context
	 *            {@link GovernanceSourceContext} to initialise this instance of
	 *            the {@link GovernanceSource}.
	 * @return Meta-data to describe this.
	 * @throws Exception
	 *             Should the {@link GovernanceSource} fail to configure itself
	 *             from the input properties.
	 */
	GovernanceSourceMetaData<I, F> init(GovernanceSourceContext context) throws Exception;

}