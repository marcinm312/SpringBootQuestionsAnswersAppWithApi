package pl.marcinm312.springdatasecurityex.question.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuestionMapper {

	public static QuestionGet convertQuestionEntityToQuestionGet(QuestionEntity question) {
		QuestionGet questionGet = new QuestionGet();
		questionGet.setId(question.getId());
		questionGet.setTitle(question.getTitle());
		questionGet.setDescription(question.getDescription());
		questionGet.setCreatedAt(question.getCreatedAt());
		questionGet.setUpdatedAt(question.getUpdatedAt());
		UserEntity user = question.getUser();
		if (user != null) {
			questionGet.setUser(user.getUsername());
		}
		return questionGet;
	}

	public static List<QuestionGet> convertQuestionEntityListToQuestionGetList(List<QuestionEntity> questionList) {
		return questionList.stream().map(QuestionMapper::convertQuestionEntityToQuestionGet).collect(Collectors.toList());
	}
}
