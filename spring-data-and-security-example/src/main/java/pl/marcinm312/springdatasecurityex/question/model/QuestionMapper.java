package pl.marcinm312.springdatasecurityex.question.model;

import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.user.model.User;

import java.util.ArrayList;
import java.util.List;

public class QuestionMapper {

	private QuestionMapper() {

	}

	public static QuestionGet convertQuestionEntityToQuestionGet(QuestionEntity question) {
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

	public static List<QuestionGet> convertQuestionEntityListToQuestionGetList(List<QuestionEntity> questionList) {
		List<QuestionGet> newQuestionList = new ArrayList<>();
		for (QuestionEntity question : questionList) {
			newQuestionList.add(convertQuestionEntityToQuestionGet(question));
		}
		return newQuestionList;
	}
}
