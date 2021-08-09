/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
	 * Obtains the parallel owner {@link BlockState}.
	 * 
	 * @return Parallel owner {@link BlockState}.
	 */
	BlockState getParallelOwner();

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
	 * Specifies the sequential {@link BlockState}.
	 * 
	 * @param sequentialBlock Sequential {@link BlockState}.
	 */
	void setSequentialBlock(BlockState sequentialBlock);

	/**
	 * Obtains the sequential {@link BlockState}.
	 * 
	 * @return Sequential {@link BlockState}.
	 */
	BlockState getSequentialBlock();

	/**
	 * Loads a parallel {@link BlockState} relative to this {@link BlockState}
	 * within the tree of {@link BlockState} instances.
	 * 
	 * @param parallelBlock {@link BlockState} to load to tree.
	 */
	default void loadParallelBlock(BlockState parallelBlock) {

		// Move possible next parallel block out
		BlockState currentParallelBlock = this.getParallelBlock();
		if (currentParallelBlock != null) {
			parallelBlock.setParallelBlock(currentParallelBlock);
			loadParallelOwner(currentParallelBlock, parallelBlock);
		}

		// Set next parallel block
		this.setParallelBlock(parallelBlock);
		loadParallelOwner(parallelBlock, this);
	}

	/**
	 * Loads the parallel owner to the parallel {@link BlockState}.
	 * 
	 * @param parallelBlock Parallel {@link BlockState}.
	 * @param parallelOwner Parallel owner.
	 */
	static void loadParallelOwner(BlockState parallelBlock, BlockState parallelOwner) {
		while (parallelBlock != null) {
			parallelBlock.setParallelOwner(parallelOwner);
			parallelBlock = parallelBlock.getSequentialBlock();
		}
	}

	/**
	 * Loads a sequential {@link BlockState} relative to this {@link BlockState}
	 * within the tree of {@link BlockState} instances.
	 * 
	 * @param sequentialBlock {@link BlockState} to load to tree.
	 */
	default void loadSequentialBlock(BlockState sequentialBlock) {

		// Obtain the next sequential block
		BlockState currentSequentialBlock = this.getSequentialBlock();
		if (currentSequentialBlock != null) {
			// Move current sequential block to parallel block
			this.loadParallelBlock(currentSequentialBlock);
		}

		// Set next sequential block
		this.setSequentialBlock(sequentialBlock);
		sequentialBlock.setParallelOwner(this.getParallelOwner());
	}

	/**
	 * <p>
	 * Obtains the parallel {@link BlockState} to execute.
	 * <p>
	 * This will not include the sequential {@link BlockState} instances. Therefore,
	 * this provides ability to check if a {@link BlockState} should be executed
	 * before this {@link BlockState}.
	 * 
	 * @return Parallel {@link BlockState} to execute. May be <code>null</code> if
	 *         no parallel {@link BlockState} to execute.
	 */
	default BlockState getParallelBlockToExecute() {

		// Determine furthest parallel node
		BlockState currentFunction = this;
		BlockState nextFunction = null;
		while ((nextFunction = currentFunction.getParallelBlock()) != null) {
			currentFunction = nextFunction;
		}

		// Determine if a parallel function
		if (currentFunction == this) {
			// No parallel function
			return null;
		} else {
			// Return the furthest parallel function
			return currentFunction;
		}
	}

	/**
	 * Obtains the next {@link FunctionState} to execute.
	 * 
	 * @return Next {@link FunctionState} to execute. May be <code>null</code> if no
	 *         further {@link BlockState} instances to execute.
	 */
	default BlockState getNextBlockToExecute() {

		// Determine if have parallel block
		BlockState nextBlock = this.getParallelBlockToExecute();
		if (nextBlock != null) {
			return nextBlock;
		}

		// Determine if have sequential block
		nextBlock = this.getSequentialBlock();
		if (nextBlock != null) {
			// Moving to next sequential block, so avoid moving again (stops infinite loop)
			this.setSequentialBlock(null);
			return nextBlock;
		}

		// Determine if have parallel owner
		nextBlock = this.getParallelOwner();
		if (nextBlock != null) {
			// Returning to owner, therefore unlink to this block
			nextBlock.setParallelBlock(null);

			// Parallel owner
			return nextBlock;
		}

		// No further blocks
		return null;
	}

}
