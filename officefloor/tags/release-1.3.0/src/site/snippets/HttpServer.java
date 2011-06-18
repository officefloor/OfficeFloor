// OfficeFloor source to create an in process HTTP Server (useful for testing).
// Note that applications are normally configured using the WoOF graphical configuration.
HttpServerAutoWireOfficeFloorSource server = new HttpServerAutoWireOfficeFloorSource();

// Add a dynamic web page (PageLogic is just a POJO)
server.addHttpTemplate("page.html", PageLogic.class);

// Add configured DataSource for dependency injection
server.addManagedObject(DataSourceManagedObjectSource.class, null, DataSource.class).loadProperties("datasource.properties");

// Assign Team (specific thread pool) responsible for executing the methods with a DataSource dependency
server.assignTeam(LeaderFollowerTeamSource.class, DataSource.class).addProperty("size", "10");

// Start the HTTP Server
server.openOfficeFloor();