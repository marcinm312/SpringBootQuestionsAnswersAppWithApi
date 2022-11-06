package pl.marcinm312.springdatasecurityex.question.service;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import pl.marcinm312.springdatasecurityex.question.model.QuestionEntity;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.question.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.question.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springdatasecurityex.shared.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.shared.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.shared.filter.SortField;
import pl.marcinm312.springdatasecurityex.shared.model.ListPage;
import pl.marcinm312.springdatasecurityex.shared.filter.Filter;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.testdataprovider.UserDataProvider;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

class QuestionManagerTest {

	@Mock
	private QuestionRepository questionRepository;

	@InjectMocks
	private QuestionManager questionManager;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		given(questionRepository.getPaginatedQuestions(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"))))
				.willReturn(new PageImpl<>(QuestionDataProvider.prepareExampleQuestionsList()));
		doNothing().when(questionRepository).delete(isA(QuestionEntity.class));
	}

	@Test
	void getQuestions_simpleCase_success() {
		Filter filter = new Filter(null, 0, 5, SortField.ID, Sort.Direction.DESC);
		ListPage<QuestionGet> questionsResultList = questionManager.searchPaginatedQuestions(filter);
		MatcherAssert.assertThat(questionsResultList.itemsList(), Matchers.hasSize(3));
	}

	@Test
	void getQuestion_simpleCase_success() {
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
		String expectedTitle = question.getTitle();
		String expectedDescription = question.getDescription();
		String expectedUser = question.getUser().getUsername();
		QuestionGet questionResult = questionManager.getQuestion(1000L);

		Assertions.assertEquals(expectedTitle, questionResult.getTitle());
		Assertions.assertEquals(expectedDescription, questionResult.getDescription());
		Assertions.assertEquals(expectedUser, questionResult.getUser());
	}

	@ParameterizedTest(name = "{index} ''{1}''")
	@MethodSource("successfullyDeletedQuestionData")
	void deleteQuestion_withDataFromMethod_success(UserEntity user, String nameOfTestCase) {
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
		Assertions.assertTrue(questionManager.deleteQuestion(1000L, user));
	}

	private static Stream<Arguments> successfullyDeletedQuestionData() {
		return Stream.of(
				Arguments.of(UserDataProvider.prepareExampleGoodUserWithEncodedPassword(),
						"deleteQuestion_userDeletesHisOwnQuestion_success"),
				Arguments.of(UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword(),
						"deleteQuestion_administratorDeletesAnotherUsersQuestion_success"));
	}

	@Test
	void deleteQuestion_userDeletesAnotherUsersQuestion_throwsChangeNotAllowedException() {
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
		UserEntity user = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		Throwable exception = Assertions.assertThrows(ChangeNotAllowedException.class,
				() -> questionManager.deleteQuestion(1000L, user));
		Assertions.assertEquals("Brak uprawnień do wykonania operacji!", exception.getMessage());
	}

	@Test
	void deleteQuestion_questionNotExists_throwsResourceNotFoundException() {
		given(questionRepository.findById(2000L)).willReturn(Optional.empty());
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		Throwable exception = Assertions.assertThrows(ResourceNotFoundException.class,
				() -> questionManager.deleteQuestion(2000L, user));
		Assertions.assertEquals("Nie znaleziono pytania o id: 2000", exception.getMessage());
	}
}