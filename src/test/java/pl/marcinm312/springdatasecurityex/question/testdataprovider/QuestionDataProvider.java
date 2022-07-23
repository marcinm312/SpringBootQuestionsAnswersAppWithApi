package pl.marcinm312.springdatasecurityex.question.testdataprovider;

import pl.marcinm312.springdatasecurityex.question.model.QuestionEntity;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionCreateUpdate;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.shared.testdataprovider.DateProvider;
import pl.marcinm312.springdatasecurityex.user.testdataprovider.UserDataProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class QuestionDataProvider {

	public static List<QuestionEntity> prepareExampleQuestionsList() {
		List<QuestionEntity> questions = new ArrayList<>();
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		questions.add(buildQuestionEntity(1002L, "bbbb", "bbbb", user,
				DateProvider.prepareDate(2019, Calendar.DECEMBER, 1, 13, 20, 0),
				DateProvider.prepareDate(2020, Calendar.SEPTEMBER, 10, 10, 25, 30)));
		questions.add(buildQuestionEntity(1001L, "aaaa", "", user,
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30)));
		questions.add(buildQuestionEntity(1000L, "cccc", "cccc", user,
				DateProvider.prepareDate(2020, Calendar.SEPTEMBER, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.SEPTEMBER, 10, 10, 25, 30)));
		return questions;
	}

	public static List<QuestionEntity> prepareExampleSearchedQuestionsList() {
		List<QuestionEntity> questions = new ArrayList<>();
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		questions.add(buildQuestionEntity(1001L, "aaaa", "", user,
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30),
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30)));
		return questions;
	}

	public static QuestionEntity prepareExampleQuestion() {
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		return buildQuestionEntity(1000L, "bbbb", "bbbb",user,
				DateProvider.prepareDate(2019, Calendar.DECEMBER, 1, 13, 20, 0),
				DateProvider.prepareDate(2020, Calendar.SEPTEMBER, 10, 10, 25, 30));
	}

	private static QuestionEntity buildQuestionEntity(Long id, String title, String description, UserEntity user, Date createdAt, Date updatedAt) {
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
