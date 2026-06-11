/*-
 * #%L
 * Web REST
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

package net.officefloor.web.rest.build;

/**
 * {@link RestMethodDecorator} to handle HTTPS.
 */
public class SecureRestMethodDecorator implements RestMethodDecorator<Void> {

    @Override
    public void decorateRestMethod(RestMethodDecoratorContext<Void> context) {

        // Attempt to determine if method is secure
        Boolean isSecure = context.getConfiguration("secure", Boolean.class);
        if (isSecure != null) {
            context.setSecure(isSecure);
            return; // flag secure
        }

        // Attempt to determine from parents
        RestPathContext path = context.getPath();
        while (path != null) {

            // Determine if secure
            isSecure = path.getConfiguration("secure", Boolean.class);
            if (isSecure != null) {
                context.setSecure(isSecure);
                return; // flag secure
            }

            // Attempt next parent
            path = path.getParentPath();
        }
    }

}
