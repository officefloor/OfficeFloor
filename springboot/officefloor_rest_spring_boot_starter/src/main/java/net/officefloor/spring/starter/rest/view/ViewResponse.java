package net.officefloor.spring.starter.rest.view;

/**
 * Renders a view (typically Thyme Leaf).
 */
public interface ViewResponse {

    /**
     * Sends the view response.
     *
     * @param view Name of the view.
     */
    void send(String view);
}
