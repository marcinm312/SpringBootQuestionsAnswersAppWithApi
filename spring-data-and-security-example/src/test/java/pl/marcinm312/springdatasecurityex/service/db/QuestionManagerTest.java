package pl.marcinm312.springdatasecurityex.service.db;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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
    public void getQuestions() {
        List<Question> questions = questionManager.getQuestions();
        MatcherAssert.assertThat(questions, Matchers.hasSize(3));
    }
}