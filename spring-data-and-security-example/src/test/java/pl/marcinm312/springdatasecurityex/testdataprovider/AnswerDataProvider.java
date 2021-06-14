package pl.marcinm312.springdatasecurityex.testdataprovider;

import pl.marcinm312.springdatasecurityex.model.answer.Answer;
import pl.marcinm312.springdatasecurityex.model.question.Question;
import pl.marcinm312.springdatasecurityex.model.user.User;

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
		answers.add(new Answer(1001L, "answer1", question, user,
				DateProvider.prepareDate(2020, Calendar.FEBRUARY, 20, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.FEBRUARY, 21, 10, 30, 30)));
		answers.add(new Answer(1002L, "answer1", question, user,
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
}
