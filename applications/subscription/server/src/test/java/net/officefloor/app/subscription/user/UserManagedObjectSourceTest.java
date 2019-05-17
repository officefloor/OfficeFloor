/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.app.subscription.user;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.app.subscription.store.User;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.identity.google.mock.GoogleIdTokenRule;
import net.officefloor.nosql.objectify.mock.ObjectifyRule;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link UserManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class UserManagedObjectSourceTest {

	private static final ObjectMapper mapper = new ObjectMapper();

	private final ObjectifyRule obectify = new ObjectifyRule();

	private final GoogleIdTokenRule google = new GoogleIdTokenRule();

	private final MockWoofServerRule server = new MockWoofServerRule((loadContext, compiler) -> {
		loadContext.extend((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();
			OfficeSection section = office.addOfficeSection("TEST_USER_MOS", ClassSectionSource.class.getName(),
					UserService.class.getName());
			office.link(web.getHttpInput(false, "/test").getInput(), section.getOfficeSectionInput("service"));
		});
	});

	public static class UserService {
		public static void service(User user, ObjectResponse<User> response) {
			response.send(user);
		}
	}

	@Rule
	public final RuleChain order = RuleChain.outerRule(this.obectify).around(this.google).around(this.server);

	/**
	 * Ensure can create the {@link User}.
	 */
	@Test
	public void testCreateUser() throws Exception {
		this.google.getMockIdToken("GOOGLE_ID-test", "email");
		
		this.server.send(MockWoofServer.mockRequest("/test")).assertResponse(200,
				mapper.writeValueAsString(new User("test@officefloor.net")));
	}

}