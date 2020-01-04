package net.officefloor.tutorial.threadaffinityhttpserver;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * CPU.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
@Data
@NamedQuery(name = "AllCpus", query = "SELECT C FROM Cpu C")
@NoArgsConstructor
public class Cpu {

	@Id
	private Integer id;

	@NonNull
	@Column(name = "CPU_NUMBER")
	private Integer cpuNumber;

}