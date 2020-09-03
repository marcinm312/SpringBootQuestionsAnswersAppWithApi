package pl.marcinm312.springdatasecurityex.testdataprovider;

import pl.marcinm312.springdatasecurityex.model.Question;

import java.util.ArrayList;
import java.util.List;

public class QuestionDataProvider {

    public static List<Question> prepareExampleQuestionsList() {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question(1000L, "bbbb", "bbbb", UserDataProvider.prepareExampleGoodUser()));
        questions.add(new Question(1001L, "aaaa", "", UserDataProvider.prepareExampleGoodUser()));
        questions.add(new Question(1002L, "cccc", "cccc", UserDataProvider.prepareExampleGoodUser()));
        return questions;
    }

    public static Question prepareExampleQuestion() {
        return new Question(1000L, "bbbb", "bbbb", UserDataProvider.prepareExampleGoodUser());
    }

    public static Question prepareGoodQuestionToRequestBody() {
        return new Question(null, "bbb", "bbbb", null);
    }

    public static Question prepareGoodQuestionWithEmptyDescriptionToRequestBody() {
        return new Question(null, "bbb", null, null);
    }

    public static Question prepareQuestionWithTooShortTitleToRequestBody() {
        return new Question(null, "bb", "bbbb", null);
    }

    public static Question prepareQuestionWithNullTitleToRequestBody() {
        return new Question(null, null, "bbbb", null);
    }
}
