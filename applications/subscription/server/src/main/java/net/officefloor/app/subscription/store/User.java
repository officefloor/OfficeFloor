package net.officefloor.app.subscription.store;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * User {@link Entity}.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class User {

	@Id
	Long id;

	@Index
	@NonNull
	String email;

	String name;

	String photoUrl;

}