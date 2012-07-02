package net.officefloor.test.ria.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * GWT {@link EntryPoint}.
 */
public class RiaEntryPoint implements EntryPoint {

	private final CounterServiceAsync service = GWT
			.create(CounterService.class);

	public void onModuleLoad() {

		// Obtain the div to include RIA functionality
		RootPanel rootPanel = RootPanel.get("gwtria");

		// Provide Rich Internet Application functionality
		VerticalPanel panel = new VerticalPanel();
		panel.setSpacing(20);
		rootPanel.add(panel);

		// Provide indicating of count
		final Label countLabel = new Label("Count is 0");
		panel.add(countLabel);

		// Provide means to update count
		HorizontalPanel updatePanel = new HorizontalPanel();
		panel.add(updatePanel);
		updatePanel.add(new Label("Increment count by"));
		final TextBox incrementText = new TextBox();
		updatePanel.add(incrementText);
		Button incrementButton = new Button("increment");
		updatePanel.add(incrementButton);
		final Label issueLabel = new Label("");
		updatePanel.add(issueLabel);

		// Allow incrementing the count
		incrementButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				// Obtain the increment value
				Integer incrementValue;
				try {
					incrementValue = Integer.parseInt(incrementText.getText());
				} catch (NumberFormatException ex) {
					issueLabel.setText("Must be an integer");
					return;
				}

				// Clear possible issue
				issueLabel.setText("");

				// AJAX call to server to increment count
				RiaEntryPoint.this.service.updateCount(incrementValue,
						new AsyncCallback<Integer>() {

							public void onSuccess(Integer result) {
								countLabel.setText("Count is " + result);
							}

							public void onFailure(Throwable caught) {
								issueLabel.setText("AJAX failure: "
										+ caught.getMessage());
							}
						});
			}
		});
	}

}