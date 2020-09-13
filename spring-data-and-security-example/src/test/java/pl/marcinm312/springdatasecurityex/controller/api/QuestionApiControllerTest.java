package pl.marcinm312.springdatasecurityex.controller.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;

import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class QuestionApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    QuestionRepository questionRepository;

    @Mock
    UserManager userManager;

    @InjectMocks
    QuestionManager questionManager;

    @Mock
    Authentication authentication;

    private final ObjectMapper mapper = new ObjectMapper();


    @Before
    public void setup() {
        given(questionRepository.findAll()).willReturn(QuestionDataProvider.prepareExampleQuestionsList());
        doNothing().when(questionRepository).delete(isA(Question.class));
        this.mockMvc = MockMvcBuilders.standaloneSetup(new QuestionApiController(questionManager, userManager))
                .alwaysDo(print()).build();
    }

    @Test
    public void getQuestions_simpleCase_success() throws Exception {
        String response = mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Question[] responseQuestionList = mapper.readValue(response, Question[].class);
        int arrayExpectedSize = 3;
        int arrayResultSize = responseQuestionList.length;
        Assert.assertEquals(arrayExpectedSize, arrayResultSize);
    }

    @Test
    public void getQuestion_simpleCase_success() throws Exception {
        Question question = QuestionDataProvider.prepareExampleQuestion();
        given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
        String expectedTitle = question.getTitle();
        String expectedDescription = question.getDescription();
        String response = mockMvc.perform(get("/api/questions/1000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Question responseQuestion = mapper.readValue(response, Question.class);

        Assert.assertEquals(expectedTitle, responseQuestion.getTitle());
        Assert.assertEquals(expectedDescription, responseQuestion.getDescription());
    }

    @Test
    public void getQuestion_questionNotExists_notFound() throws Exception {
        given(questionRepository.findById(2000L)).willReturn(Optional.empty());
        String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(get("/api/questions/2000"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResolvedException())
                .getMessage();

        String expectedErrorMessage = "Question not found with id 2000";
        Assert.assertEquals(expectedErrorMessage, receivedErrorMessage);
    }

    @Test
    public void createQuestion_simpleCase_success() throws Exception {
        given(userManager.getUserByAuthentication(any(Authentication.class))).willReturn(UserDataProvider.prepareExampleGoodUser());
        Question questionToRequestBody = QuestionDataProvider.prepareGoodQuestionToRequest();
        given(questionRepository.save(any(Question.class))).willReturn(questionToRequestBody);
        String response = mockMvc.perform(post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(questionToRequestBody))
                .characterEncoding("utf-8")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Question responseQuestion = mapper.readValue(response, Question.class);
        Assert.assertEquals(questionToRequestBody.getTitle(), responseQuestion.getTitle());
        Assert.assertEquals(questionToRequestBody.getDescription(), responseQuestion.getDescription());
    }

    @Test
    public void createQuestion_nullDescription_success() throws Exception {
        given(userManager.getUserByAuthentication(any(Authentication.class))).willReturn(UserDataProvider.prepareExampleGoodUser());
        Question questionToRequestBody = QuestionDataProvider.prepareGoodQuestionWithNullDescriptionToRequest();
        given(questionRepository.save(any(Question.class))).willReturn(questionToRequestBody);
        String response = mockMvc.perform(post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(questionToRequestBody))
                .characterEncoding("utf-8")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Question responseQuestion = mapper.readValue(response, Question.class);
        Assert.assertEquals(questionToRequestBody.getTitle(), responseQuestion.getTitle());
        Assert.assertEquals(questionToRequestBody.getDescription(), responseQuestion.getDescription());
    }

    @Test
    public void createQuestion_tooShortTitle_badRequest() throws Exception {
        given(userManager.getUserByAuthentication(any(Authentication.class))).willReturn(UserDataProvider.prepareExampleGoodUser());
        mockMvc.perform(post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(QuestionDataProvider.prepareQuestionWithTooShortTitleToRequest()))
                .characterEncoding("utf-8")
                .principal(authentication))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createQuestion_nullTitle_badRequest() throws Exception {
        given(userManager.getUserByAuthentication(any(Authentication.class))).willReturn(UserDataProvider.prepareExampleGoodUser());
        mockMvc.perform(post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(QuestionDataProvider.prepareQuestionWithNullTitleToRequest()))
                .characterEncoding("utf-8")
                .principal(authentication))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteQuestion_userDeletesHisOwnQuestion_success() throws Exception {
        Question question = QuestionDataProvider.prepareExampleQuestion();
        given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
        User user = UserDataProvider.prepareExampleGoodUser();
        given(userManager.getUserByAuthentication(any(Authentication.class))).willReturn(user);
        String response = mockMvc.perform(delete("/api/questions/1000")
                .principal(authentication))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assert.assertEquals("true", response);
    }

    @Test
    public void deleteQuestion_administratorDeletesAnotherUsersQuestion_success() throws Exception {
        Question question = QuestionDataProvider.prepareExampleQuestion();
        given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
        User user = UserDataProvider.prepareExampleGoodAdministrator();
        given(userManager.getUserByAuthentication(any(Authentication.class))).willReturn(user);
        String response = mockMvc.perform(delete("/api/questions/1000")
                .principal(authentication))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assert.assertEquals("true", response);
    }

    @Test
    public void deleteQuestion_userDeletesAnotherUsersQuestion_forbidden() throws Exception {
        Question question = QuestionDataProvider.prepareExampleQuestion();
        given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
        User user = UserDataProvider.prepareExampleSecondGoodUser();
        given(userManager.getUserByAuthentication(any(Authentication.class))).willReturn(user);
        String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(delete("/api/questions/1000")
                .principal(authentication))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResolvedException())
                .getMessage();

        String expectedErrorMessage = "Change not allowed!";
        Assert.assertEquals(expectedErrorMessage, receivedErrorMessage);
    }

    @Test
    public void deleteQuestion_questionNotExists_notFound() throws Exception {
        given(questionRepository.findById(2000L)).willReturn(Optional.empty());
        User user = UserDataProvider.prepareExampleGoodUser();
        given(userManager.getUserByAuthentication(any(Authentication.class))).willReturn(user);
        String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(delete("/api/questions/2000")
                .principal(authentication))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResolvedException())
                .getMessage();

        String expectedErrorMessage = "Question not found with id 2000";
        Assert.assertEquals(expectedErrorMessage, receivedErrorMessage);
    }
}