package net.officefloor.tutorial.springresthttpserver;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// START SNIPPET: tutorial
@Service
public class AuditService {

	private final List<String> entries = new ArrayList<>();

	public void record(String message) {
		entries.add(message);
	}

	public List<String> getEntries() {
		return Collections.unmodifiableList(entries);
	}
}
// END SNIPPET: tutorial
