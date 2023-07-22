package pl.marcinm312.springquestionsanswers.question.testdataprovider;

import pl.marcinm312.springquestionsanswers.question.model.QuestionEntity;
import pl.marcinm312.springquestionsanswers.question.model.dto.QuestionCreateUpdate;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.shared.testdataprovider.DateProvider;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class QuestionDataProvider {

	public static List<QuestionEntity> prepareExampleQuestionsList() {

		List<QuestionEntity> questions = new ArrayList<>();
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		questions.add(buildQuestionEntity(1002L, "bbbb", "bbbb", user,
				DateProvider.prepareDate(2019, Month.DECEMBER, 1, 13, 20, 0),
				DateProvider.prepareDate(2020, Month.SEPTEMBER, 10, 10, 25, 30)));
		questions.add(buildQuestionEntity(1001L, "aaaa", "", user,
				DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30)));
		questions.add(buildQuestionEntity(1000L, "cccc", "cccc", user,
				DateProvider.prepareDate(2020, Month.SEPTEMBER, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Month.SEPTEMBER, 10, 10, 25, 30)));
		return questions;
	}

	public static List<QuestionEntity> prepareExampleSearchedQuestionsList() {

		List<QuestionEntity> questions = new ArrayList<>();
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		questions.add(buildQuestionEntity(1001L, "aaaa", "", user,
				DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30)));
		return questions;
	}

	public static QuestionEntity prepareExampleQuestion() {

		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		return buildQuestionEntity(1000L, "bbbb", "bbbb",user,
				DateProvider.prepareDate(2019, Month.DECEMBER, 1, 13, 20, 0),
				DateProvider.prepareDate(2020, Month.SEPTEMBER, 10, 10, 25, 30));
	}

	private static QuestionEntity buildQuestionEntity(Long id, String title, String description, UserEntity user,
													  LocalDateTime createdAt, LocalDateTime updatedAt) {

		return QuestionEntity.builder()
				.id(id)
				.title(title)
				.description(description)
				.user(user)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.build();
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
