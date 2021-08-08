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
 * <p>
 * Latch on an {@link Asset} to only allow the {@link ThreadState} instances
 * proceed when {@link Asset} is ready.
 * <p>
 * May be used as a {@link LinkedListSetEntry} in a list of {@link AssetLatch}
 * instances for an {@link AssetManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AssetLatch {

	/**
	 * Obtains the {@link Asset} for this {@link AssetLatch}.
	 * 
	 * @return {@link Asset} for this {@link AssetLatch}.
	 */
	Asset getAsset();

	/**
	 * <p>
	 * Flags for the {@link FunctionState} (and more specifically the
	 * {@link ThreadState} of the {@link FunctionState}) to wait until the
	 * {@link Asset} is ready.
	 * <p>
	 * This is typically because the {@link Asset} is doing some processing that the
	 * {@link FunctionState} requires completed before proceeding.
	 * 
	 * @param function {@link FunctionState} to be released when the {@link Asset}
	 *                 is ready.
	 * @return Optional {@link FunctionState} to execute to wait on the
	 *         {@link Asset}.
	 */
	FunctionState awaitOnAsset(FunctionState function);

	/**
	 * Releases the {@link FunctionState} instances waiting on the {@link Asset}.
	 * 
	 * @param isPermanent <code>true</code> indicates that all {@link FunctionState}
	 *                    instances added to the {@link AssetLatch} from now on are
	 *                    activated immediately. It is useful to flag an
	 *                    {@link AssetLatch} in this state when the {@link Asset} is
	 *                    no longer being used to stop a {@link FunctionState} from
	 *                    waiting forever.
	 */
	void releaseFunctions(boolean isPermanent);

	/**
	 * Releases the {@link FunctionState} instances waiting on the {@link Asset}.
	 * 
	 * @param isPermanent   <code>true</code> indicates that all
	 *                      {@link FunctionState} instances added to the
	 *                      {@link AssetLatch} from now on are activated
	 *                      immediately. It is useful to flag an {@link AssetLatch}
	 *                      in this state when the {@link Asset} is no longer being
	 *                      used to stop a {@link FunctionState} from waiting
	 *                      forever.
	 * @param functionState {@link FunctionState} to be executed first before each
	 *                      currently waiting {@link FunctionState}.
	 */
	void releaseFunctions(boolean isPermanent, FunctionState functionState);

	/**
	 * Fails the {@link FunctionState} instances waiting on this {@link Asset}.
	 * 
	 * @param failure     Failure to propagate to the {@link ThreadState} of the
	 *                    {@link FunctionState} instances waiting on the
	 *                    {@link Asset}.
	 * @param isPermanent <code>true</code> indicates that all {@link FunctionState}
	 *                    instances added to the {@link AssetLatch} from now on are
	 *                    activated immediately with the input failure. It is useful
	 *                    to flag an {@link AssetLatch} in this state when the
	 *                    {@link Asset} is in a failed state that can not be
	 *                    recovered from.
	 */
	void failFunctions(Throwable failure, boolean isPermanent);

}
