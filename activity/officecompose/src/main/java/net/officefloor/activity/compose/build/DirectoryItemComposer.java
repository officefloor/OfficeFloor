/*-
 * #%L
 * Composition
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
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

package net.officefloor.activity.compose.build;

/**
 * Enables different {@link ComposeSource} implementations per directory items.
 */
public interface DirectoryItemComposer<T> {

    /**
     * Undertakes composition for the directory item.
     *
     * @param context  {@link DirectoryItemComposerContext} to handle the directory item.
     * @param listener {@link ComposeListener} to receive compositions.
     * @throws Exception If fails to handle directory item.
     */
    void compose(DirectoryItemComposerContext context, ComposeListener<T> listener) throws Exception;

}
