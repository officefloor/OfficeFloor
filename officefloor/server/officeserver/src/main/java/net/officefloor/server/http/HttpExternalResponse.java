package net.officefloor.server.http;

/**
 * Enables flagging that the {@link HttpResponse} is externally sent.
 */
public interface HttpExternalResponse {

    /**
     * Flags that {@link HttpResponse} is externally sent.
     */
    void externalSend();

}
