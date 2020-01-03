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

package net.officefloor.compile.impl.managedfunction;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionEscalationTypeBuilder;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * {@link ManagedFunctionEscalationType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionEscalationTypeImpl
		implements ManagedFunctionEscalationType, ManagedFunctionEscalationTypeBuilder {

	/**
	 * Type of the {@link EscalationFlow}.
	 */
	private final Class<?> escalationType;

	/**
	 * Label of the {@link EscalationFlow}.
	 */
	private String label = null;

	/**
	 * Initiate.
	 * 
	 * @param escalationType
	 *            Type of the {@link EscalationFlow}.
	 */
	public ManagedFunctionEscalationTypeImpl(Class<?> escalationType) {
		this.escalationType = escalationType;
	}

	/*
	 * =================== ManagedFunctionEscalationTypeBuilder ====================
	 */

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	/*
	 * =================== ManagedFunctionEscalationType ==========================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public <E extends Throwable> Class<E> getEscalationType() {
		return (Class<E>) this.escalationType;
	}

	@Override
	public String getEscalationName() {
		// Obtain name by priorities
		if (!CompileUtil.isBlank(this.label)) {
			return this.label;
		} else if (this.escalationType != null) {
			return this.escalationType.getName();
		} else {
			return "escalation";
		}
	}

}
