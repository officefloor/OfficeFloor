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
package net.officefloor.tutorial.transactionhttpserver;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Handles exception logic.
 * 
 * @author Daniel Sagenschneider
 */
public class RollbackExceptionHandler {

	public void handle(@Parameter IllegalArgumentException exception, ServerHttpConnection connection)
			throws Exception {
		HttpResponse response = connection.getResponse();
		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		response.getEntityWriter().write(exception.getMessage());
	}

}