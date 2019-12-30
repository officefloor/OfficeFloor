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
package net.officefloor.frame.internal.structure;

/**
 * Representative of blocks of code to be executed.
 * 
 * @author Daniel Sagenschneider
 */
public interface BlockState extends FunctionState {

	/**
	 * Specifies the parallel owning {@link BlockState}.
	 * 
	 * @param parallelOwner Parallel owning {@link BlockState}.
	 */
	void setParallelOwner(BlockState parallelOwner);

	/**
	 * Specifies the parallel {@link BlockState}.
	 * 
	 * @param parallelBlock Parallel {@link BlockState}.
	 */
	void setParallelBlock(BlockState parallelBlock);

	/**
	 * Obtains the parallel {@link BlockState}.
	 * 
	 * @return Parallel {@link BlockState}.
	 */
	BlockState getParallelBlock();

	/**
	 * Obtains the sequential {@link BlockState}.
	 * 
	 * @return Sequential {@link BlockState}.
	 */
	BlockState getSequentialBlock();

}