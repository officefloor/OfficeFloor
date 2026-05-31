package net.officefloor.tutorial.springrestfunction;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

// START SNIPPET: tutorial
@Service
public class OrderService {

	private final AtomicInteger sequence = new AtomicInteger(1);

	public String createOrder(String productId, int quantity, double total) {
		return "ORD-" + sequence.getAndIncrement();
	}
}
// END SNIPPET: tutorial
