import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * This is not used within the application. It is only here for the
 * <code>presentation.woof</code> to give an overview screen shot of the
 * graphical configuration.
 */
public class QuestionPojo {

	@FlowInterface
	public static interface QuestionFlows {

		void nextQuestion();

		void noFurtherQuestions();
	}

	public void completeQuestion(QuestionFlows flows) {
	}

}
