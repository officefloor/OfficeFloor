package net.officefloor.app.subscription.store;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Google sign-in {@link Entity}.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class GoogleSignin {

	@Id
	private Long id;

	@Load
	private Ref<User> user;

	@Index
	@NonNull
	private String googleId;

	@Index
	@NonNull
	private String email;

	private String name;

	private String photoUrl;

}