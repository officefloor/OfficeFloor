package net.officefloor.tutorial.springwebfluxhttpserver;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import reactor.core.publisher.Mono;

/**
 * Spring {@link Controller}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@Controller
public class SpringController {

	@GetMapping("/html")
	public Mono<String> html(@RequestParam String name, Model model) {
		model.addAttribute("name", name);
		return Mono.just("simple");
	}
}
// END SNIPPET: tutorial