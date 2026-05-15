package net.officefloor.web.rest.build;

import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;

import java.util.List;

/**
 * {@link RestMethod} implementation.
 */
public class RestMethodImpl implements RestMethod {

    private final boolean isSecure;

    private final HttpMethod httpMethod;

    private final HttpInput httpInput;

    private final OfficeSectionInput sectionInput;

    private Object[] momentos;

    /**
     * Instantiate.
     *
     * @param isSecure     Indicates if {@link HttpMethod} requires secure connection.
     * @param httpMethod   {@link HttpMethod}.
     * @param httpInput    {@link HttpInput} to service this {@link RestMethod}.
     * @param sectionInput {@link OfficeSectionInput} to service this {@link RestMethod}.
     * @param momentos     Momentos.
     */
    public RestMethodImpl(boolean isSecure, HttpMethod httpMethod, HttpInput httpInput,
                          OfficeSectionInput sectionInput, Object[] momentos) {
        this.isSecure = isSecure;
        this.httpMethod = httpMethod;
        this.httpInput = httpInput;
        this.sectionInput = sectionInput;
        this.momentos = momentos;
    }

    /*
     * =================== RestMethod ===================
     */

    @Override
    public boolean isSecure() {
        return this.isSecure;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return this.httpMethod;
    }

    @Override
    public HttpInput getHttpInput() {
        return this.httpInput;
    }

    @Override
    public OfficeSectionInput getServiceInput() {
        return this.sectionInput;
    }

    @Override
    public <M> M getMomento(MomentoKey<M> key) {
        int momentoIndex = MomentoKeyImpl.getMomentoIndex(key);
        return (M) this.momentos[momentoIndex];
    }

}
