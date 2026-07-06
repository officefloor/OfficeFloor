package net.officefloor.tutorial.springwebmvchttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpObject;

@HttpObject
// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestModel {

	private String input;
}
// END SNIPPET: tutorial