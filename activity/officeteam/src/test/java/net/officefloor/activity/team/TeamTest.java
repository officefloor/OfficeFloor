package net.officefloor.activity.team;

import net.officefloor.activity.team.build.TeamDeployer;
import net.officefloor.activity.team.build.TeamEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.test.officefloor.CompileOfficeExtension;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.clazz.Qualified;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TeamTest {

    @Test
    public void simple() throws Throwable {
        SimpleSection.invokedThread = null;
        this.doTest(SimpleSection.class,
                (office) -> office.addManagedObject("simple", SimpleObject.class, ManagedObjectScope.THREAD),
                (deployer, properties) -> deployer.addTeam("simple", "officefloor/teams/simple.yml", properties));
        this.assertSimple();
    }

    private void assertSimple() {
        assertNotSame(Thread.currentThread(), SimpleSection.invokedThread, "Should be different thread");
        assertEquals("thread_simple", SimpleSection.invokedThread.getName(), "Incorrect thread invoked");
    }

    public static class SimpleSection {
        public static volatile Thread invokedThread = null;

        public void service(SimpleObject dependency) {
            SimpleSection.invokedThread = Thread.currentThread();
        }
    }

    public static class SimpleObject {
    }

    @Test
    public void qualified() throws Throwable {
        QualifiedSection.invokedThread = null;
        this.doTest(QualifiedSection.class,
                (office) -> office.addManagedObject("qualified", QualifiedObject.class, ManagedObjectScope.THREAD).addTypeQualification("qualified", QualifiedObject.class.getName()),
                (deployer, properties) -> deployer.addTeam("qualified", "officefloor/teams/qualified.yml", properties));
        assertNotSame(Thread.currentThread(), QualifiedSection.invokedThread, "Should be different thread");
        assertEquals("thread_qualified", QualifiedSection.invokedThread.getName(), "Incorrect thread invoked");
    }

    public static class QualifiedSection {
        public static volatile Thread invokedThread = null;

        public void service(@Qualified("qualified") QualifiedObject dependency) {
            QualifiedSection.invokedThread = Thread.currentThread();
        }
    }

    public static class QualifiedObject {
    }

    @Test
    public void directory() throws Throwable {
        this.doTest(SimpleSection.class,
                (office) -> office.addManagedObject("simple", SimpleObject.class, ManagedObjectScope.THREAD),
                (deployer, properties) -> deployer.addTeams("officefloor/teams", properties));
        this.assertSimple();
    }

    @FunctionalInterface
    protected interface SetupOfficeFloorTeams {
        void setup(TeamDeployer deployer, PropertyList properties) throws Exception;
    }

    private void doTest(Class<?> sectionClass, CompileOfficeExtension setupObjects, SetupOfficeFloorTeams officeFloorSetup) throws Throwable {

        // Undertake test
        CompileOfficeFloor compile = new CompileOfficeFloor();
        compile.office((office) -> {
            office.addSection("SECTION", sectionClass);

            office.getOfficeArchitect().enableAutoWireTeams();
        });
        compile.office(setupObjects);
        compile.officeFloor((officeFloor) -> {
            OfficeFloorDeployer deployer = officeFloor.getOfficeFloorDeployer();
            OfficeFloorSourceContext sourceContext = officeFloor.getOfficeFloorSourceContext();
            DeployedOffice deployedOffice = officeFloor.getDeployedOffice();

            TeamDeployer teamDeployer = TeamEmployer.employTeamDeployer(deployer, sourceContext, deployedOffice);

            // Setup
            PropertyList properties = sourceContext.createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());
            officeFloorSetup.setup(teamDeployer, properties);
        });

        try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

            // Invoke the function for testing
            CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", null);
        }
    }

    @TestSource
    public static class MockTeamSource extends AbstractTeamSource {

        @Override
        protected void loadSpecification(SpecificationContext context) {
        }

        @Override
        public Team createTeam(TeamSourceContext context) throws Exception {
            String threadName = context.getProperty("thread.name");
            return new Team() {
                @Override
                public void startWorking() {
                }

                @Override
                public void assignJob(Job job) throws Exception {
                    new Thread(job::run, threadName).start();
                }

                @Override
                public void stopWorking() {
                }
            };
        }
    }

}
