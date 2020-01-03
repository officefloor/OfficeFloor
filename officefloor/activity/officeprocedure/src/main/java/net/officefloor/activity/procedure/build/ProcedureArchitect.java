package net.officefloor.activity.procedure.build;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Builds the {@link Procedure} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureArchitect<S> {

	/**
	 * {@link SectionInput} name to invoke the {@link Procedure}.
	 */
	public static final String INPUT_NAME = "procedure";

	/**
	 * {@link SectionOutput} name for next {@link Flow}.
	 */
	public static final String NEXT_OUTPUT_NAME = "NEXT";

	/**
	 * Adds a {@link Procedure}.
	 * 
	 * @param sectionName   Name to uniquely identify the use of the
	 *                      {@link Procedure}. It is possible to configure the same
	 *                      {@link Procedure}, so need to name them each uniquely.
	 * @param resource      Resource.
	 * @param sourceName    {@link ProcedureSource} name.
	 * @param procedureName Name of {@link Procedure} to be provided to
	 *                      {@link ProcedureSource}.
	 * @param isNext        Indicates if next {@link Flow} configured.
	 * @param properties    {@link PropertyList} for extra configuration.
	 * @return {@link OfficeSection}/{@link SubSection} for the {@link Procedure}.
	 */
	S addProcedure(String sectionName, String resource, String sourceName, String procedureName, boolean isNext,
			PropertyList properties);

}