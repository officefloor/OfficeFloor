package net.officefloor.spring.starter.rest.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderFactory;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

/**
 * {@link HttpObjectResponderFactory} for the {@link ResponseEntity}.
 */
public class RequestEntityHttpObjectResponderFactory implements HttpObjectResponderFactory, HttpObjectResponder<ResponseEntity> {

    /**
     * {@link ObjectMapper}.
     */
    private final ObjectMapper mapper;

    /**
     * Instantiate.
     *
     * @param mapper {@link ObjectMapper}.
     */
    public RequestEntityHttpObjectResponderFactory(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /*
     * ===================== HttpObjectResponderFactory ================
     */

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public <T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType) {
        return (ResponseEntity.class.isAssignableFrom(objectType)) ? (HttpObjectResponder<T>) this : null;
    }

    @Override
    public <E extends Throwable> HttpObjectResponder<E> createHttpEscalationResponder(Class<E> escalationType) {
        return null; // don't handle escalations
    }

    /*
     * ====================== HttpObjectResponder ========================
     */

    @Override
    public void send(ResponseEntity entity, ServerHttpConnection connection) throws IOException {

        // Load response from response entity
        HttpResponse response = connection.getResponse();
        response.setStatus(HttpStatus.getHttpStatus(entity.getStatusCode().value()));
        entity.getHeaders().asSingleValueMap().forEach((name, value) -> {
            response.getHeaders().addHeader(name, value);
        });
        if (entity.hasBody()) {
            this.mapper.writeValue(response.getEntity(), entity.getBody());
        }
    }

}
