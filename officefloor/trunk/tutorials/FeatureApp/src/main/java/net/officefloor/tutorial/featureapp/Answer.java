/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.tutorial.featureapp;

/**
 * {@link Answer} to a {@link Question}.
 * 
 * @author Daniel Sagenschneider
 */
public class Answer {

	private final int index;
	
	private final String text;

	public Answer(int index, String text) {
		this.index = index;
		this.text = text;
	}
	
	public int getIndex() {
		return this.index;
	}

	public String getText() {
		return this.text;
	}

}