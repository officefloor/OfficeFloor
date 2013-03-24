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
package net.officefloor.eclipse.desk;

import org.eclipse.gef.requests.CreationFactory;

/**
 * Implementation of the {@link CreationFactory} that returns the tag from the
 * initiation.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowInstigationTagFactory implements CreationFactory {

	/**
	 * Tag.
	 */
	private final Object tag;

	/**
	 * Initiate.
	 * 
	 * @param tag
	 *            Tag to be returned from {@link #getNewObject()}.
	 */
	public FlowInstigationTagFactory(Object tag) {
		this.tag = tag;
	}

	/*
	 * ==================== CreationFactory ===========================
	 */
	
	@Override
	public Object getNewObject() {
		return this.tag;
	}

	@Override
	public Object getObjectType() {
		return this.tag.getClass();
	}

}