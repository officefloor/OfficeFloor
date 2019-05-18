package net.officefloor.app.subscription.store;

import java.util.Date;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Payment {@link Entity}.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Payment {

	@Id
	private Long id;

	@Index
	@NonNull
	private Ref<User> user;

	@Index
	@NonNull
	private Ref<Invoice> invoice;

	/**
	 * Amount in cents.
	 */
	private int amount;

	private String receipt;

	private Date timestamp = new Date(System.currentTimeMillis());
}