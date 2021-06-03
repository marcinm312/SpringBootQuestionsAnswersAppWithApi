package pl.marcinm312.springdatasecurityex.model.question;

import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.model.user.User;

import java.util.ArrayList;
import java.util.List;

public class QuestionMapper {

	private QuestionMapper() {

	}

	public static QuestionGet convertQuestionToQuestionGet(Question question) {
		QuestionGet questionGet = new QuestionGet();
		questionGet.setId(question.getId());
		questionGet.setTitle(question.getTitle());
		questionGet.setDescription(question.getDescription());
		questionGet.setCreatedAt(question.getCreatedAt());
		questionGet.setUpdatedAt(question.getUpdatedAt());
		User user = question.getUser();
		if (user != null) {
			questionGet.setUser(user.getUsername());
		}
		return questionGet;
	}

	public static List<QuestionGet> convertQuestionListToQuestionGetList(List<Question> questionList) {
		List<QuestionGet> newQuestionList = new ArrayList<>();
		for (Question question : questionList) {
			newQuestionList.add(convertQuestionToQuestionGet(question));
		}
		return newQuestionList;
	}
}
