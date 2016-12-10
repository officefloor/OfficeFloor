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
package net.officefloor.tutorial.transactionhttpserver;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import net.officefloor.plugin.section.clazz.NextTask;

/**
 * Logic for the users page.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("unchecked")
// START SNIPPET: tutorial
public class UsersLogic {

	public UserProperties[] getUsers(EntityManager entityManager) {

		// Obtain the users
		Query query = entityManager.createQuery("SELECT U FROM User U");
		List<User> list = query.getResultList();
		List<UserProperties> users = new ArrayList<UserProperties>();
		for (User user : list) {
			users.add(new UserProperties(user.getUserName(), user.getPerson()
					.getFullName()));
		}

		// Return the users
		return users.toArray(new UserProperties[list.size()]);
	}

	@NextTask("createUser")
	public UserProperties create(UserProperties user) {
		return user;
	}

}
// END SNIPPET: tutorial
