package net.officefloor.web.rest.build;

import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;

/**
 * REST supported {@link HttpMethod}.
 */
public interface RestMethod {

    /**
     * Indicates if the {@link RestMethod} is secure (e.g. HTTPS).
     *
     * @return <code>true</code> if {@link RestMethod} is secure.
     */
    boolean isSecure();

    /**
     * Obtains the {@link HttpMethod}.
     *
     * @return {@link HttpMethod}.
     */
    HttpMethod getHttpMethod();

    /**
     * Obtains the {@link HttpInput}.
     *
     * @return {@link HttpInput}.
     */
    HttpInput getHttpInput();

    /**
     * Obtains the {@link OfficeSectionInput} to service the {@link RestMethod}.
     *
     * @return {@link OfficeSectionInput} to service the {@link RestMethod}.
     */
    OfficeSectionInput getServiceInput();

    /**
     * Obtains the Momento for the {@link MomentoKey}.
     *
     * @param key {@link MomentoKey}.
     * @param <M> Type of Momento.
     * @return Momento or <code>null</code> if no Momento specified by the respective {@link RestMethodDecorator}.
     */
    <M> M getMomento(MomentoKey<M> key);

}