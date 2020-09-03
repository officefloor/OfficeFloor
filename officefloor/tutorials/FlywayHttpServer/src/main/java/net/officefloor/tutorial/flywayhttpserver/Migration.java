package net.officefloor.tutorial.flywayhttpserver;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.flywaydb.core.Flyway;

import com.sun.istack.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link Flyway} migration {@link Entity}.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Migration {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	private String message;
}