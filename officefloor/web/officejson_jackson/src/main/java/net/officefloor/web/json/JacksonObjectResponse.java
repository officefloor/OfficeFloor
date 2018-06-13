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
package net.officefloor.web.json;

import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderFactory;

/**
 * Jackson {@link ObjectResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class JacksonObjectResponse implements HttpObjectResponderFactory {

	/*
	 * =============== HttpObjectResponderFactory ==================
	 */

	@Override
	public String getContentType() {
		return "application/json";
	}

	@Override
	public <T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Throwable> HttpObjectResponder<E> createHttpEscalationResponder(Class<E> escalationType) {
		// TODO Auto-generated method stub
		return null;
	}

}