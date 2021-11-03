package pl.marcinm312.springdatasecurityex.answer.testdataprovider;

import pl.marcinm312.springdatasecurityex.answer.model.Answer;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerCreateUpdate;
import pl.marcinm312.springdatasecurityex.question.model.Question;
import pl.marcinm312.springdatasecurityex.user.model.User;
import pl.marcinm312.springdatasecurityex.shared.testdataprovider.DateProvider;
import pl.marcinm312.springdatasecurityex.question.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springdatasecurityex.user.testdataprovider.UserDataProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AnswerDataProvider {

	public static List<Answer> prepareExampleAnswersList() {
		Question question = QuestionDataProvider.prepareExampleQuestion();
		User user = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		List<Answer> answers = new ArrayList<>();
		answers.add(new Answer(1000L, "answer1", question, user,
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.JANUARY, 15, 10, 30, 30)));
		answers.add(new Answer(1001L, "answer2", question, user,
				DateProvider.prepareDate(2020, Calendar.FEBRUARY, 20, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.FEBRUARY, 21, 10, 30, 30)));
		answers.add(new Answer(1002L, "answer3", question, user,
				DateProvider.prepareDate(2020, Calendar.MARCH, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.MARCH, 10, 10, 30, 30)));
		return answers;
	}

	public static Answer prepareExampleAnswer() {
		Question question = QuestionDataProvider.prepareExampleQuestion();
		User user = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		return new Answer(1000L, "answer1", question, user,
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.JANUARY, 15, 10, 30, 30));
	}

	public static AnswerCreateUpdate prepareGoodAnswerToRequest() {
		return new AnswerCreateUpdate("aaa");
	}

	public static AnswerCreateUpdate prepareAnswerWithTooShortTextToRequest() {
		return new AnswerCreateUpdate("aa");
	}

	public static AnswerCreateUpdate prepareAnswerWithTooShortTextAfterTrimToRequest() {
		return new AnswerCreateUpdate(" a ");
	}

	public static AnswerCreateUpdate prepareAnswerWithEmptyTextToRequest() {
		return new AnswerCreateUpdate("");
	}

	public static AnswerCreateUpdate prepareAnswerWithNullTextToRequest() {
		return new AnswerCreateUpdate(null);
	}
}
