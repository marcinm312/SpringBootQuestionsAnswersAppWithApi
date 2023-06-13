package pl.marcinm312.springquestionsanswers.answer.testdataprovider;

import pl.marcinm312.springquestionsanswers.answer.model.AnswerEntity;
import pl.marcinm312.springquestionsanswers.answer.model.dto.AnswerCreateUpdate;
import pl.marcinm312.springquestionsanswers.question.model.QuestionEntity;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.shared.testdataprovider.DateProvider;
import pl.marcinm312.springquestionsanswers.question.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class AnswerDataProvider {

	public static List<AnswerEntity> prepareExampleAnswersList() {
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		UserEntity user = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		List<AnswerEntity> answers = new ArrayList<>();
		answers.add(buildAnswerEntity(1002L, "answer3", question, user,
				DateProvider.prepareDate(2020, Month.MARCH, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Month.MARCH, 10, 10, 30, 30)));
		answers.add(buildAnswerEntity(1001L, "answer2", question, user,
				DateProvider.prepareDate(2020, Month.FEBRUARY, 20, 10, 25, 30),
				DateProvider.prepareDate(2020, Month.FEBRUARY, 21, 10, 30, 30)));
		answers.add(buildAnswerEntity(1000L, "answer1", question, user,
				DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Month.JANUARY, 15, 10, 30, 30)));
		return answers;
	}

	public static List<AnswerEntity> prepareExampleSearchedAnswersList() {
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		UserEntity user = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		List<AnswerEntity> answers = new ArrayList<>();
		answers.add(buildAnswerEntity(1000L, "answer1", question, user,
				DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Month.JANUARY, 15, 10, 30, 30)));
		return answers;
	}

	public static AnswerEntity prepareExampleAnswer() {
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		UserEntity user = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		return buildAnswerEntity(1000L, "answer1", question, user,
				DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Month.JANUARY, 15, 10, 30, 30));
	}

	private static AnswerEntity buildAnswerEntity(Long id, String text, QuestionEntity question, UserEntity user, LocalDateTime createdAt, LocalDateTime updatedAt) {
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
