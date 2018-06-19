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
package net.officefloor.eclipse.conform.figures;

import org.eclipse.draw2d.IFigure;

import net.officefloor.model.conform.TargetItemModel;
import net.officefloor.model.conform.TargetModel;

/**
 * Context for the {@link TargetConformModelItemFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TargetConformModelItemFigureContext {

	/**
	 * Obtains the {@link TargetItemModel} name.
	 * 
	 * @return {@link TargetItemModel} name.
	 */
	String getTargetItemName();

	/**
	 * Indicates if the {@link TargetItemModel} configuration is inheritable.
	 * 
	 * @return <code>true</code> if may inherit the {@link TargetItemModel}
	 *         configuration.
	 */
	boolean isInheritable();

	/**
	 * Indicates whether inheriting the {@link TargetModel} configuration.
	 * 
	 * @return <code>true</code> if inheriting the {@link TargetItemModel}
	 *         configuration.
	 */
	boolean isInherit();

	/**
	 * Allows an action of the {@link TargetConformModelItemFigure} to indicate
	 * whether the {@link TargetItemModel} is to be inherited.
	 * 
	 * @param isInherit
	 *            <code>true</code> to action inheriting the
	 *            {@link TargetItemModel} configuration.
	 */
	void setInherit(boolean isInherit);

	/**
	 * Specifies the layout constraint for the
	 * {@link TargetConformModelItemFigure}.
	 * 
	 * @param figure
	 *            {@link IFigure} to have constraint.
	 * @param layoutConstraint
	 *            Layout constraint.
	 */
	void setLayoutConstraint(IFigure figure, Object layoutConstraint);

}