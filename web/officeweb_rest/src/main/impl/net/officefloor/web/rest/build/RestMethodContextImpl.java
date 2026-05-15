package net.officefloor.web.rest.build;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;
import net.officefloor.web.build.WebArchitect;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link RestMethodDecoratorContext} implementation.
 */
public class RestMethodContextImpl<M> {

    private boolean isSecure;

    private final HttpMethod httpMethod;

    private final RestPathContext path;

    private final OfficeSectionInput sectionInput;

    private final RestConfiguration configuration;

    private final List<HttpInputInterceptor> interceptors = new LinkedList<>();

    private Object[] momentos;

    public RestMethodContextImpl(boolean isSecure, HttpMethod httpMethod,
                                 RestPathContext path, OfficeSectionInput sectionInput,
                                 RestConfiguration configuration) {
        this.isSecure = isSecure;
        this.httpMethod = httpMethod;
        this.path = path;
        this.sectionInput = sectionInput;
        this.configuration = configuration;
    }

    public void decorateRestMethod(List<RestMethodDecorator<?>> decorators) {

        // Create momento array to allow each decorator momento
        this.momentos = new Object[decorators.size()];

        // Decorate the REST method
        for (int i = 0; i < decorators.size(); i++) {
            final int momentoIndex = i;
            RestMethodDecorator<M> decorator = (RestMethodDecorator<M>) decorators.get(i);
            decorator.decorateRestMethod(new RestMethodDecoratorContext<M>() {

                @Override
                public boolean isSecure() {
                    return RestMethodContextImpl.this.isSecure;
                }

                @Override
                public void setSecure(boolean isSecure) {
                    RestMethodContextImpl.this.isSecure = isSecure;
                }

                @Override
                public RestPathContext getPath() {
                    return RestMethodContextImpl.this.path;
                }

                @Override
                public HttpMethod getHttpMethod() {
                    return RestMethodContextImpl.this.httpMethod;
                }

                @Override
                public <T> T getConfiguration(String itemName, Class<T> type) {
                    return RestMethodContextImpl.this.configuration.getConfiguration(itemName, type);
                }

                @Override
                public void addHttpInputInterceptor(HttpInputInterceptor interceptor) {
                    RestMethodContextImpl.this.interceptors.add(interceptor);
                }

                @Override
                public void setMomento(M momento) {
                    RestMethodContextImpl.this.momentos[momentoIndex] = momento;
                }
            });
        }
    }

    public RestMethod buildRestMethod(WebArchitect webArchitect, OfficeArchitect officeArchitect, OfficeSourceContext sourceContext) {

        // Obtain the REST input
        HttpInput httpInput = webArchitect.getHttpInput(this.isSecure, this.httpMethod.getName(), this.path.getPath());

        // Configure in interceptors
        OfficeFlowSourceNode[] flowSource = new OfficeFlowSourceNode[] { httpInput.getInput() };
        for (HttpInputInterceptor interceptor : this.interceptors) {
            interceptor.intercept(new HttpInputInterceptorContext() {

                @Override
                public void link(OfficeFlowSinkNode input, OfficeFlowSourceNode output) {
                    // Link intercepting
                    officeArchitect.link(flowSource[0], input);

                    // Setup for link next intercepting (or handling of REST method)
                    flowSource[0] = output;
                }

                @Override
                public OfficeArchitect getOfficeArchitect() {
                    return officeArchitect;
                }

                @Override
                public OfficeSourceContext getOfficeSourceContext() {
                    return sourceContext;
                }
            });
        }

        // Configure REST method handling after interception
        officeArchitect.link(flowSource[0], this.sectionInput);

        // Create and return rest method
        return new RestMethodImpl(this.isSecure, this.httpMethod, httpInput, this.sectionInput, this.momentos);
    }

}
