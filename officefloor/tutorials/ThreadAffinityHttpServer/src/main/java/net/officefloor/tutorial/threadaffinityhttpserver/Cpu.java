/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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