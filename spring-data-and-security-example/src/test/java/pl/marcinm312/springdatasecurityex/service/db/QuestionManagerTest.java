package pl.marcinm312.springdatasecurityex.service.db;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.testdataprovider.QuestionDataProvider;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class QuestionManagerTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    QuestionManager questionManager;

    @Before
    public void setUp() {
        given(questionRepository.findAll()).willReturn(QuestionDataProvider.prepareExampleQuestionsList());
    }

    @Test
    public void getQuestions_simpleCase_success() {
        List<Question> questionsResultList = questionManager.getQuestions();
        MatcherAssert.assertThat(questionsResultList, Matchers.hasSize(3));
    }


    @Test
    public void getQuestion_simpleCase_success() {
        Question question = QuestionDataProvider.prepareExampleQuestion();
        given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
        String expectedTitle = question.getTitle();
        String expectedDescription = question.getDescription();
        Question questionResult = questionManager.getQuestion(1000L);

        Assert.assertEquals(expectedTitle, questionResult.getTitle());
        Assert.assertEquals(expectedDescription, questionResult.getDescription());
    }
}