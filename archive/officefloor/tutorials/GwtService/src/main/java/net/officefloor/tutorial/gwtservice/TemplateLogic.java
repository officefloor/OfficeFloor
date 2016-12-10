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
package net.officefloor.tutorial.gwtservice;

import java.io.Serializable;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.web.http.application.HttpSessionStateful;
import net.officefloor.tutorial.gwtservice.client.Result;
import net.officefloor.tutorial.gwtservice.client.Result.Answer;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
@HttpSessionStateful
public class TemplateLogic implements Serializable {

	private Integer number;

	private int tryCount;

	public TemplateLogic() {
		this.newGame(); // assigns number for first game
	}

	public void newGame() {
		this.number = Integer.valueOf((int) (Math.random() * 100));
		this.tryCount = 0;
	}

	public void attempt(@Parameter Integer value, AsyncCallback<Result> callback) {
		Answer answer;
		Integer returnNumber = null;
		
		if (value.equals(this.number)) {
			answer = Answer.CORRECT;
			
		} else if (this.tryCount >= 10) {
			answer = Answer.NO_FURTHER_ATTEMPTS;
			returnNumber = this.number;
			
		} else {
			this.tryCount++;
			answer = (this.number < value ? Answer.LOWER : Answer.HIGHER);
		}
		
		Result result = new Result(answer, returnNumber);
		callback.onSuccess(result);
	}

}
// END SNIPPET: example