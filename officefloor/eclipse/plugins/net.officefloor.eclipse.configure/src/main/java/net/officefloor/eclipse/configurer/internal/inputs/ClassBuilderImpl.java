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
package net.officefloor.eclipse.configurer.internal.inputs;

import org.eclipse.jdt.core.IJavaProject;

import net.officefloor.eclipse.configurer.ClassBuilder;
import net.officefloor.eclipse.configurer.internal.AbstractBuilder;
import net.officefloor.eclipse.configurer.internal.ValueInput;
import net.officefloor.eclipse.configurer.internal.ValueInputContext;

/**
 * {@link ClassBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassBuilderImpl<M> extends AbstractBuilder<M, String, ValueInput, ClassBuilder<M>>
		implements ClassBuilder<M> {

	/**
	 * {@link IJavaProject}.
	 */
	private final IJavaProject javaProject;

	/**
	 * Instantiate.
	 * 
	 * @param label
	 *            Label.
	 * @param javaProject
	 *            {@link IJavaProject}.
	 */
	public ClassBuilderImpl(String label, IJavaProject javaProject) {
		super(label);
		this.javaProject = javaProject;
	}

	/*
	 * ================= AbstractBuilder ===============
	 */

	@Override
	protected ValueInput createInput(ValueInputContext<M, String> context) {
		// TODO Auto-generated method stub
		return null;
	}

}