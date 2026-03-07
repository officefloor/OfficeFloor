package net.officefloor.spring.starter.rest;

import jakarta.servlet.http.HttpServletRequest;

/**
 * {@link net.officefloor.frame.api.managedobject.source.ManagedObjectSource} for the {@link HttpServletRequest}.
 */
public class HttpServletRequestManagedObjectSource extends AbstractSpringManagedObjectSource<HttpServletRequest> {

    public HttpServletRequestManagedObjectSource() {
        super(HttpServletRequest.class, SpringServerHttpConnection::getHttpServletRequest);
    }
}
