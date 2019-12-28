/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.web.security.type;

import net.officefloor.compile.managedobject.ManagedObjectFlowType;

/**
 * {@link HttpSecurityFlowType} adapted from the {@link ManagedObjectFlowType}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityFlowTypeImpl<F extends Enum<F>> implements HttpSecurityFlowType<F> {

	/**
	 * {@link ManagedObjectFlowType}.
	 */
	private final ManagedObjectFlowType<F> flow;

	/**
	 * Initiate.
	 * 
	 * @param flow {@link ManagedObjectFlowType}.
	 */
	public HttpSecurityFlowTypeImpl(ManagedObjectFlowType<F> flow) {
		this.flow = flow;
	}

	/*
	 * ==================== HttpSecurityFlowType =======================
	 */

	@Override
	public String getFlowName() {
		return this.flow.getFlowName();
	}

	@Override
	public F getKey() {
		return this.flow.getKey();
	}

	@Override
	public int getIndex() {
		return this.flow.getIndex();
	}

	@Override
	public Class<?> getArgumentType() {
		return this.flow.getArgumentType();
	}
}
