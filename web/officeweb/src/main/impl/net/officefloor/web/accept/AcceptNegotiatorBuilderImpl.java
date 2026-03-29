/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.accept;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.web.accept.AcceptNegotiatorImpl.AcceptHandler;
import net.officefloor.web.build.AcceptNegotiatorBuilder;
import net.officefloor.web.build.NoAcceptHandlersException;

/**
 * {@link AcceptNegotiatorBuilder} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class AcceptNegotiatorBuilderImpl<H> implements AcceptNegotiatorBuilder<H> {

    /**
     * {@link Class} of accept handler to allow creating an array.
     */
    private final Class<H> handlerType;

    /**
     * {@link ContentTypeAcceptHandler} instances.
     */
    private final List<ContentTypeAcceptHandler> contentTypeAcceptHandlers = new LinkedList<>();

    /**
     * Instantiate.
     *
     * @param handlerType {@link Class} of accept handler to allow creating an array.
     */
    public AcceptNegotiatorBuilderImpl(Class<H> handlerType) {
        this.handlerType = handlerType;
    }

    /*
     * =================== AcceptNegotiatorBuilder =============================
     */

    @Override
    public void addHandler(String contentType, H handler) {

        // Determine if content type already added
        for (ContentTypeAcceptHandler acceptHandler : this.contentTypeAcceptHandlers) {
            if (acceptHandler.contentType.equals(contentType)) {
                acceptHandler.handlers.add(handler);
                return; // added to the existing content type
            }
        }

        // New content type being added
        ContentTypeAcceptHandler acceptHandler = new ContentTypeAcceptHandler(contentType);
        acceptHandler.handlers.add(handler);
        this.contentTypeAcceptHandlers.add(acceptHandler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AcceptNegotiator<H> build() throws NoAcceptHandlersException {

        // Ensure have at least one accept handler
        if (this.contentTypeAcceptHandlers.isEmpty()) {
            throw new NoAcceptHandlersException("Must have at least one " + AcceptHandler.class.getSimpleName()
                    + " configured for the " + AcceptNegotiator.class.getSimpleName());
        }

        // Create the listing of accept handlers
        List<AcceptHandler<H>> acceptHandlers = new LinkedList<>();
        for (ContentTypeAcceptHandler acceptHandler : this.contentTypeAcceptHandlers) {
            H[] handlers = acceptHandler.handlers.toArray((H[]) Array.newInstance(this.handlerType, acceptHandler.handlers.size()));
            acceptHandlers.add(AcceptNegotiatorImpl.createAcceptHandler(acceptHandler.contentType, handlers));
        }

        // Return the negotiator
        return new AcceptNegotiatorImpl<>(acceptHandlers.toArray(new AcceptHandler[acceptHandlers.size()]));
    }

    /**
     * Accept handler for specific <code>Content-Type</code>.
     */
    private class ContentTypeAcceptHandler {

        private final String contentType;

        private final List<H> handlers = new LinkedList<>();

        public ContentTypeAcceptHandler(String contentType) {
            this.contentType = contentType;
        }
    }

}
