package net.officefloor.app.subscription.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.jwt.JwtHttpSecuritySource;

/**
 * Credentials for {@link JwtHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtClaims {

	private long userId;

	private String[] roles;
}