package net.officefloor.tutorial.springrestthymeleaf;

import net.officefloor.spring.starter.rest.view.ViewResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

// START SNIPPET: tutorial
public class GreetingViewService {

	public void service(
			@RequestParam(name = "name", required = false, defaultValue = "World") String name,
			Model model,
			ViewResponse response) {
		model.addAttribute("name", name);
		response.send("greeting");
	}
}
// END SNIPPET: tutorial
