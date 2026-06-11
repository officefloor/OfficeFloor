package net.officefloor.tutorial.teamhttpserver;

import lombok.Value;

public class ExampleService {

	@Value
	public static class ExampleData {
		LetterEncryption display;
		String cacheThreadName;
		String databaseThreadName;
	}

	public ExampleData getTemplateData(Template template) {
		return new ExampleData(
				template.getTemplate(),
				template.getCacheThreadName(),
				template.getDatabaseThreadName());
	}
}
