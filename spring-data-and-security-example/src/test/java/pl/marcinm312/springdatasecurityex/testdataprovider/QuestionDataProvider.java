package pl.marcinm312.springdatasecurityex.testdataprovider;

import pl.marcinm312.springdatasecurityex.model.question.Question;

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
		return new Question(1000L, "bbbb", "bbbb", UserDataProvider.prepareExampleGoodUser());
	}

	public static Question prepareGoodQuestionToRequest() {
		return new Question(null, "bbb", "bbbb", null);
	}

	public static Question prepareGoodQuestionWithNullDescriptionToRequest() {
		return new Question(null, "bbb", null, null);
	}

	public static Question prepareGoodQuestionWithEmptyDescriptionToRequest() {
		return new Question(null, "bbb", "", null);
	}

	public static Question prepareQuestionWithTooShortTitleToRequest() {
		return new Question(null, "bb", "bbbb", null);
	}

	public static Question prepareQuestionWithNullTitleToRequest() {
		return new Question(null, null, "bbbb", null);
	}

	public static Question prepareQuestionWithEmptyTitleToRequest() {
		return new Question(null, "", "bbbb", null);
	}
}
