/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.building.process;

import java.io.Serializable;

/**
 * Request on the {@link Process}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessRequest implements Serializable {

	/**
	 * Stop {@link ProcessRequest}.
	 */
	public static ProcessRequest STOP_REQUEST = new ProcessRequest();

	/**
	 * Flag indicate if requesting to stop the {@link Process}.
	 */
	private final boolean isStop;

	/**
	 * Id for this {@link ProcessRequest}.
	 */
	private final long requestId;

	/**
	 * Command for this {@link ProcessRequest}.
	 */
	private final Object command;

	/**
	 * Initiate the stop request.
	 * 
	 * @param isStop
	 *            Flags if a stop request.
	 * @param requestId
	 *            Id for this {@link ProcessRequest}.
	 * @param command
	 *            Command.
	 */
	private ProcessRequest() {
		this.isStop = true;
		this.requestId = -1;
		this.command = null;
	}

	/**
	 * Initiate a command request.
	 * 
	 * @param requestId
	 *            Id of this {@link ProcessRequest}.
	 * @param command
	 *            Command.
	 */
	public ProcessRequest(long requestId, Object command) {
		this.isStop = false;
		this.requestId = requestId;
		this.command = command;
	}

	/**
	 * Flags if to stop the {@link Process}.
	 * 
	 * @return <code>true</code> to stop the {@link Process}.
	 */
	public boolean isStop() {
		return this.isStop;
	}

	/**
	 * Obtains the id for this {@link ProcessRequest}.
	 * 
	 * @return Id for this {@link ProcessRequest}.
	 */
	public long getRequestId() {
		return this.requestId;
	}

	/**
	 * Obtains the command for this {@link ProcessRequest}.
	 * 
	 * @return Command.
	 */
	public Object getCommand() {
		return this.command;
	}

}