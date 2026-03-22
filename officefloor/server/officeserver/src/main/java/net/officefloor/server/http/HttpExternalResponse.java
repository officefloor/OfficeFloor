package net.officefloor.server.http;

/**
 * Enables flagging that the {@link HttpResponse} is externally sent.
 */
public interface HttpExternalResponse {

    /**
     * Obtains the {@link HttpExternalResponse} from the {@link HttpResponse}.
     *
     * @param response {@link HttpResponse}.
     * @return {@link HttpExternalResponse}.
     */
    static HttpExternalResponse of(HttpResponse response) {
        return (HttpExternalResponse) response;
    }

    /**
     * Flags that {@link HttpResponse} is externally sent.
     */
    void externalSend();

}
