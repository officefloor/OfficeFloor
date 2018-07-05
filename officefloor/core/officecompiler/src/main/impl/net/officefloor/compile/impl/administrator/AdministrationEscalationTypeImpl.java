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
package net.officefloor.compile.impl.administrator;

import net.officefloor.compile.administration.AdministrationEscalationType;
import net.officefloor.frame.api.escalate.Escalation;

/**
 * {@link AdministrationEscalationType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationEscalationTypeImpl implements AdministrationEscalationType {

	/**
	 * {@link Escalation} name.
	 */
	private final String escalationName;

	/**
	 * {@link Escalation} type.
	 */
	private final Class<? extends Throwable> escalationType;

	/**
	 * Instantiate.
	 * 
	 * @param escalationName
	 *            {@link Escalation} name.
	 * @param escalationType
	 *            {@link Escalation} type.
	 */
	public AdministrationEscalationTypeImpl(String escalationName, Class<? extends Throwable> escalationType) {
		this.escalationName = escalationName;
		this.escalationType = escalationType;
	}

	/*
	 * ================= AdministrationEscalationType ===============
	 */

	@Override
	public String getEscalationName() {
		return this.escalationName;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends Throwable> Class<E> getEscalationType() {
		return (Class<E>) this.escalationType;
	}

}
