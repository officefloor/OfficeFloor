package net.officefloor.tutorial.springwebfluxhttpserver;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import reactor.core.publisher.Flux;
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
		model.addAttribute("names", new ReactiveDataDriverContextVariable(Flux.just(name), 1));
		return Mono.just("simple");
	}
}
// END SNIPPET: tutorial