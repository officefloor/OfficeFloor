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
package net.officefloor.tutorial.gwtservice.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Result of High Low guess.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class Result implements IsSerializable {

	public static enum Answer implements IsSerializable {
		HIGHER, LOWER, CORRECT, NO_FURTHER_ATTEMPTS
	}

	private Answer answer;

	private Integer number;

	public Result() {
	}

	public Result(Answer answer, Integer number) {
		this.answer = answer;
		this.number = number;
	}

	public Answer getAnswer() {
		return this.answer;
	}

	public Integer getNumber() {
		return this.number;
	}

}
// END SNIPPET: example