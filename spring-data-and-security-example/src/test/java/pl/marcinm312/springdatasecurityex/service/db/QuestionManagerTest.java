package pl.marcinm312.springdatasecurityex.service.db;

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
import pl.marcinm312.springdatasecurityex.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

class QuestionManagerTest {

	@Mock
	QuestionRepository questionRepository;

	@InjectMocks
	QuestionManager questionManager;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		given(questionRepository.findAll()).willReturn(QuestionDataProvider.prepareExampleQuestionsList());
		doNothing().when(questionRepository).delete(isA(Question.class));
	}

	@Test
	void getQuestions_simpleCase_success() {
		List<Question> questionsResultList = questionManager.getQuestions();
		MatcherAssert.assertThat(questionsResultList, Matchers.hasSize(3));
	}

	@Test
	void getQuestion_simpleCase_success() {
		Question question = QuestionDataProvider.prepareExampleQuestion();
		given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
		String expectedTitle = question.getTitle();
		String expectedDescription = question.getDescription();
		Question questionResult = questionManager.getQuestion(1000L);

		Assertions.assertEquals(expectedTitle, questionResult.getTitle());
		Assertions.assertEquals(expectedDescription, questionResult.getDescription());
	}

	@ParameterizedTest(name = "{index} ''{1}''")
	@MethodSource("successfullyDeletedQuestionData")
	void deleteQuestion_withDataFromMethod_success(User user, String nameOfTestCase) {
		Question question = QuestionDataProvider.prepareExampleQuestion();
		given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
		Assertions.assertTrue(questionManager.deleteQuestion(1000L, user));
	}

	private static Stream<Arguments> successfullyDeletedQuestionData() {
		return Stream.of(Arguments.of(UserDataProvider.prepareExampleGoodUser(), "deleteQuestion_userDeletesHisOwnQuestion_success"),
				Arguments.of(UserDataProvider.prepareExampleGoodAdministrator(), "deleteQuestion_administratorDeletesAnotherUsersQuestion_success"));
	}

	@Test
	void deleteQuestion_userDeletesAnotherUsersQuestion_throwsChangeNotAllowedException() {
		Question question = QuestionDataProvider.prepareExampleQuestion();
		given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
		User user = UserDataProvider.prepareExampleSecondGoodUser();
		Throwable exception = Assertions.assertThrows(ChangeNotAllowedException.class, () -> questionManager.deleteQuestion(1000L, user));
		Assertions.assertEquals("Change not allowed!", exception.getMessage());
	}

	@Test
	void deleteQuestion_questionNotExists_throwsResourceNotFoundException() {
		given(questionRepository.findById(2000L)).willReturn(Optional.empty());
		User user = UserDataProvider.prepareExampleGoodUser();
		Throwable exception = Assertions.assertThrows(ResourceNotFoundException.class, () -> questionManager.deleteQuestion(2000L, user));
		Assertions.assertEquals("Question not found with id 2000", exception.getMessage());
	}
}