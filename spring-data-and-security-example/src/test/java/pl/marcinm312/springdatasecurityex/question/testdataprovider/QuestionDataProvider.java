package pl.marcinm312.springdatasecurityex.question.testdataprovider;

import pl.marcinm312.springdatasecurityex.question.model.Question;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionCreateUpdate;
import pl.marcinm312.springdatasecurityex.user.model.User;
import pl.marcinm312.springdatasecurityex.shared.testdataprovider.DateProvider;
import pl.marcinm312.springdatasecurityex.user.testdataprovider.UserDataProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class QuestionDataProvider {

	public static List<Question> prepareExampleQuestionsList() {
		List<Question> questions = new ArrayList<>();
		User user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		questions.add(new Question(1002L, "bbbb", "bbbb", user,
				DateProvider.prepareDate(2019, Calendar.DECEMBER, 1, 13, 20, 0),
				DateProvider.prepareDate(2020, Calendar.SEPTEMBER, 10, 10, 25, 30)));
		questions.add(new Question(1001L, "aaaa", "", user,
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30)));
		questions.add(new Question(1000L, "cccc", "cccc", user,
				DateProvider.prepareDate(2020, Calendar.SEPTEMBER, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.SEPTEMBER, 10, 10, 25, 30)));
		return questions;
	}

	public static Question prepareExampleQuestion() {
		User user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		return new Question(1000L, "bbbb", "bbbb",user,
				DateProvider.prepareDate(2019, Calendar.DECEMBER, 1, 13, 20, 0),
				DateProvider.prepareDate(2020, Calendar.SEPTEMBER, 10, 10, 25, 30));
	}

	public static QuestionCreateUpdate prepareGoodQuestionToRequest() {
		return new QuestionCreateUpdate("ccc", "cccc");
	}

	public static QuestionCreateUpdate prepareGoodQuestionWithNullDescriptionToRequest() {
		return new QuestionCreateUpdate("ccc", null);
	}

	public static QuestionCreateUpdate prepareGoodQuestionWithEmptyDescriptionToRequest() {
		return new QuestionCreateUpdate("ccc", "");
	}

	public static QuestionCreateUpdate prepareQuestionWithTooShortTitleToRequest() {
		return new QuestionCreateUpdate("cc", "cccc");
	}

	public static QuestionCreateUpdate prepareQuestionWithTooShortTitleAfterTrimToRequest() {
		return new QuestionCreateUpdate(" c ", "cccc");
	}

	public static QuestionCreateUpdate prepareQuestionWithNullTitleToRequest() {
		return new QuestionCreateUpdate(null, "cccc");
	}

	public static QuestionCreateUpdate prepareQuestionWithEmptyTitleToRequest() {
		return new QuestionCreateUpdate("", "cccc");
	}
}
