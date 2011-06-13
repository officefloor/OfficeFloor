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
package net.officefloor.tutorial.gwtservice;

import net.officefloor.plugin.web.http.application.HttpSessionStateful;
import net.officefloor.tutorial.gwtservice.client.Result;
import net.officefloor.tutorial.gwtservice.client.Result.Answer;

/**
 * State of the High Low game.
 * 
 * @author Daniel Sagenschneider
 */
@HttpSessionStateful
public class GameState {

	private int number;

	private int tryCount = 0;

	public GameState() {
		this.startNewGame();
	}

	public void startNewGame() {
		this.number = (int) (Math.random() * 100);
		this.tryCount = 0;
	}

	public Result attempt(int value) {
		Answer answer;
		Integer returnNumber = null;
		if (value == this.number) {
			answer = Answer.CORRECT;
		} else if (this.tryCount >= 10) {
			answer = Answer.NO_FURTHER_ATTEMPTS;
			returnNumber = new Integer(this.number);
		} else {
			this.tryCount++;
			answer = (this.number < value ? Answer.LOWER : Answer.HIGHER);
		}
		return new Result(answer, returnNumber);
	}

}