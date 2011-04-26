// OfficeFloor source to create an inline HTTP Server (useful for testing a single web page).
// Note that applications would be configured using the WoOF graphical configuration.
HttpServerAutoWireOfficeFloorSource server = new HttpServerAutoWireOfficeFloorSource();

// Add a dynamic web page (ExampleBean provides the page logic and is just a POJO)
server.addHttpTemplate("example.html", ExampleBean.class);

// Add configured DataSource for dependency injection
server.addManagedObject(DataSourceManagedObjectSource.class, null, DataSource.class).loadProperties("datasource.properties");

// Assign Team (specific thread pool) responsible for executing the methods with a DataSource dependency
server.assignTeam(LeaderFollowerTeamSource.class, DataSource.class).addProperty("size", "10");

// Start the HTTP Server
server.openOfficeFloor();