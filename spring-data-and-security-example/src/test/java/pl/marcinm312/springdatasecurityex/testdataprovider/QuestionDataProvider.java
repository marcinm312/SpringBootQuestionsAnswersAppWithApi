package pl.marcinm312.springdatasecurityex.testdataprovider;

import pl.marcinm312.springdatasecurityex.model.Question;

import java.util.ArrayList;
import java.util.List;

public class QuestionDataProvider {

    public static List<Question> prepareExampleQuestionsList() {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question(1000L, "aaaa", "", UserDataProvider.prepareExampleGoodUser()));
        questions.add(new Question(1001L, "bbbb", "bbbb", UserDataProvider.prepareExampleGoodUser()));
        questions.add(new Question(1002L, "cccc", "cccc", UserDataProvider.prepareExampleGoodUser()));
        return questions;
    }
}
