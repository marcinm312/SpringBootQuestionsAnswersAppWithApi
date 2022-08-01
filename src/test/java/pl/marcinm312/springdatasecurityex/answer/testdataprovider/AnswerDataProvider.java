package pl.marcinm312.springdatasecurityex.answer.testdataprovider;

import pl.marcinm312.springdatasecurityex.answer.model.AnswerEntity;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerCreateUpdate;
import pl.marcinm312.springdatasecurityex.question.model.QuestionEntity;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.shared.testdataprovider.DateProvider;
import pl.marcinm312.springdatasecurityex.question.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springdatasecurityex.user.testdataprovider.UserDataProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AnswerDataProvider {

	public static List<AnswerEntity> prepareExampleAnswersList() {
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		UserEntity user = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		List<AnswerEntity> answers = new ArrayList<>();
		answers.add(buildAnswerEntity(1002L, "answer3", question, user,
				DateProvider.prepareDate(2020, Calendar.MARCH, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.MARCH, 10, 10, 30, 30)));
		answers.add(buildAnswerEntity(1001L, "answer2", question, user,
				DateProvider.prepareDate(2020, Calendar.FEBRUARY, 20, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.FEBRUARY, 21, 10, 30, 30)));
		answers.add(buildAnswerEntity(1000L, "answer1", question, user,
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.JANUARY, 15, 10, 30, 30)));
		return answers;
	}

	public static List<AnswerEntity> prepareExampleSearchedAnswersList() {
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		UserEntity user = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		List<AnswerEntity> answers = new ArrayList<>();
		answers.add(buildAnswerEntity(1000L, "answer1", question, user,
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.JANUARY, 15, 10, 30, 30)));
		return answers;
	}

	public static AnswerEntity prepareExampleAnswer() {
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		UserEntity user = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		return buildAnswerEntity(1000L, "answer1", question, user,
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.JANUARY, 15, 10, 30, 30));
	}

	private static AnswerEntity buildAnswerEntity(Long id, String text, QuestionEntity question, UserEntity user, Date createdAt, Date updatedAt) {
		return AnswerEntity.builder()
				.id(id)
				.text(text)
				.question(question)
				.user(user)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.build();
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
