package net.officefloor.tutorial.springwebmvchttpserver;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Spring {@link Controller}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@Controller
public class SpringController {

	@GetMapping("/html")
	public String html(@RequestParam String name, Model model) {
		model.addAttribute("name", name);
		return "simple";
	}
}
// END SNIPPET: tutorial