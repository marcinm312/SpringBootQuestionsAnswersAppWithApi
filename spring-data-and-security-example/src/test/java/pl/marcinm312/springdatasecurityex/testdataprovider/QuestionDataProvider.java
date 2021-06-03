package pl.marcinm312.springdatasecurityex.testdataprovider;

import pl.marcinm312.springdatasecurityex.model.question.Question;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionCreateUpdate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class QuestionDataProvider {

	public static List<Question> prepareExampleQuestionsList() {
		List<Question> questions = new ArrayList<>();
		questions.add(new Question(1002L, "bbbb", "bbbb", UserDataProvider.prepareExampleGoodUser(),
				DateProvider.prepareDate(2019, Calendar.DECEMBER, 1, 13, 20, 0),
				DateProvider.prepareDate(2020, Calendar.SEPTEMBER, 10, 10, 25, 30)));
		questions.add(new Question(1001L, "aaaa", "", UserDataProvider.prepareExampleGoodUser(),
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30)));
		questions.add(new Question(1000L, "cccc", "cccc", UserDataProvider.prepareExampleGoodUser(),
				DateProvider.prepareDate(2020, Calendar.SEPTEMBER, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.SEPTEMBER, 10, 10, 25, 30)));
		return questions;
	}

	public static Question prepareExampleQuestion() {
		return new Question(1000L, "bbbb", "bbbb", UserDataProvider.prepareExampleGoodUser(),
				DateProvider.prepareDate(2019, Calendar.DECEMBER, 1, 13, 20, 0),
				DateProvider.prepareDate(2020, Calendar.SEPTEMBER, 10, 10, 25, 30));
	}

	public static QuestionCreateUpdate prepareGoodQuestionToRequest() {
		return new QuestionCreateUpdate("bbb", "bbbb");
	}

	public static QuestionCreateUpdate prepareGoodQuestionWithNullDescriptionToRequest() {
		return new QuestionCreateUpdate("bbb", null);
	}

	public static QuestionCreateUpdate prepareGoodQuestionWithEmptyDescriptionToRequest() {
		return new QuestionCreateUpdate("bbb", "");
	}

	public static QuestionCreateUpdate prepareQuestionWithTooShortTitleToRequest() {
		return new QuestionCreateUpdate("bb", "bbbb");
	}

	public static QuestionCreateUpdate prepareQuestionWithNullTitleToRequest() {
		return new QuestionCreateUpdate(null, "bbbb");
	}

	public static QuestionCreateUpdate prepareQuestionWithEmptyTitleToRequest() {
		return new QuestionCreateUpdate("", "bbbb");
	}
}
