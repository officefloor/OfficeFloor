/*-
 * #%L
 * Google Signin Tutorial
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.tutorial.googlesigninhttpserver;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;

/**
 * Google Sign-in logic.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class LoginLogic {

	@HttpObject
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class LoginRequest {
		private String googleIdToken;
	}

	@Data
	@AllArgsConstructor
	public static class LoginResponse {
		private String email;
	}

	public void login(LoginRequest request, GoogleIdTokenVerifier tokenVerifier, ObjectResponse<LoginResponse> response)
			throws Exception {

		// Verify the token
		GoogleIdToken token = tokenVerifier.verify(request.getGoogleIdToken());

		// Send email response
		response.send(new LoginResponse(token.getPayload().getEmail()));
	}

}
// END SNIPPET: tutorial
