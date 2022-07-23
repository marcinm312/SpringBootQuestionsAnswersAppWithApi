package pl.marcinm312.springdatasecurityex.question.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuestionMapper {

	public static QuestionGet convertQuestionEntityToQuestionGet(QuestionEntity question) {
		return QuestionGet.builder()
				.id(question.getId())
				.title(question.getTitle())
				.description(question.getDescription())
				.createdAt(question.getCreatedAt())
				.updatedAt(question.getUpdatedAt())
				.user(question.getUser().getUsername())
				.build();
	}

	public static List<QuestionGet> convertQuestionEntityListToQuestionGetList(List<QuestionEntity> questionList) {
		return questionList.stream().map(QuestionMapper::convertQuestionEntityToQuestionGet).collect(Collectors.toList());
	}
}
