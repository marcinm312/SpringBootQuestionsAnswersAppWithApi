package pl.marcinm312.springdatasecurityex.controller.web;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.service.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.service.file.PdfGenerator;
import pl.marcinm312.springdatasecurityex.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
public class QuestionWebControllerTest {

    private MockMvc mockMvc;

    @Mock
    QuestionRepository questionRepository;

    @Mock
    UserManager userManager;

    @InjectMocks
    QuestionManager questionManager;

    @Mock
    Authentication authentication;


    @BeforeEach
    public void setup() {
        User user = UserDataProvider.prepareExampleGoodUser();
        given(questionRepository.findAll()).willReturn(QuestionDataProvider.prepareExampleQuestionsList());
        given(userManager.getUserByAuthentication(any(Authentication.class))).willReturn(user);
        given(authentication.getName()).willReturn(user.getUsername());

        ExcelGenerator excelGenerator = new ExcelGenerator();
        PdfGenerator pdfGenerator = new PdfGenerator();
        this.mockMvc = MockMvcBuilders.standaloneSetup(new QuestionWebController(questionManager, pdfGenerator, excelGenerator, userManager))
                .alwaysDo(print()).build();
    }

    @Test
    public void createQuestion_simpleCase_success() throws Exception {
        Question expectedQuestion = QuestionDataProvider.prepareGoodQuestionToRequest();
        User expectedUser = UserDataProvider.prepareExampleGoodUser();
        Question receivedQuestion = (Question) Objects.requireNonNull(mockMvc.perform(post("/app/questions/new")
                .param("title", expectedQuestion.getTitle())
                .param("description", expectedQuestion.getDescription())
                .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(".."))
                .andExpect(view().name("redirect:.."))
                .andExpect(model().hasNoErrors())
                .andReturn()
                .getModelAndView())
                .getModelMap()
                .getAttribute("question");
        assert receivedQuestion != null;
        Assert.assertEquals(expectedUser, receivedQuestion.getUser());
        Assert.assertEquals(expectedQuestion.getTitle(), receivedQuestion.getTitle());
        Assert.assertEquals(expectedQuestion.getDescription(), receivedQuestion.getDescription());
    }

    @Test
    public void createQuestion_emptyDescription_success() throws Exception {
        Question expectedQuestion = QuestionDataProvider.prepareGoodQuestionWithEmptyDescriptionToRequest();
        User expectedUser = UserDataProvider.prepareExampleGoodUser();
        Question receivedQuestion = (Question) Objects.requireNonNull(mockMvc.perform(post("/app/questions/new")
                .param("title", expectedQuestion.getTitle())
                .param("description", expectedQuestion.getDescription())
                .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(".."))
                .andExpect(view().name("redirect:.."))
                .andExpect(model().hasNoErrors())
                .andReturn()
                .getModelAndView())
                .getModelMap()
                .getAttribute("question");
        assert receivedQuestion != null;
        Assert.assertEquals(expectedUser, receivedQuestion.getUser());
        Assert.assertEquals(expectedQuestion.getTitle(), receivedQuestion.getTitle());
        Assert.assertEquals(expectedQuestion.getDescription(), receivedQuestion.getDescription());
    }

    @Test
    public void createQuestion_tooShortTitle_validationErrors() throws Exception {
        Question question = QuestionDataProvider.prepareQuestionWithTooShortTitleToRequest();
        mockMvc.perform(post("/app/questions/new")
                .param("title",question.getTitle())
                .param("description", question.getDescription())
                .principal(authentication))
                .andExpect(view().name("createQuestion"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("question", "title"));
    }

    @Test
    public void createQuestion_emptyTitle_validationErrors() throws Exception {
        Question question = QuestionDataProvider.prepareQuestionWithEmptyTitleToRequest();
        mockMvc.perform(post("/app/questions/new")
                .param("title",question.getTitle())
                .param("description", question.getDescription())
                .principal(authentication))
                .andExpect(view().name("createQuestion"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("question", "title"));
    }
}