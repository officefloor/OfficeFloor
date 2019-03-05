package net.officefloor.app.subscription.store;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.jwt.authority.repository.JwtAccessKey;

/**
 * {@link Entity} for {@link JwtAccessKey}.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessKey {

	@Id
	private Long id;

	private Long startTime;

	private Long expireTime;

	private String publicKey;

	private String privateKey;
}