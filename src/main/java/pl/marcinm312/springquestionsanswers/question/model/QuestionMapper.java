package pl.marcinm312.springquestionsanswers.question.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.marcinm312.springquestionsanswers.question.model.dto.QuestionGet;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuestionMapper {

	public static QuestionGet convertQuestionEntityToQuestionGet(QuestionEntity question, boolean isCreateOrUpdate) {

		var builder = QuestionGet.builder()
				.id(question.getId())
				.title(question.getTitle())
				.description(question.getDescription())
				.user(question.getUser().getUsername());

		if (!isCreateOrUpdate) {
			builder = builder
					.createdAt(question.getCreatedAt())
					.updatedAt(question.getUpdatedAt());
		}

		return builder.build();
	}

	public static List<QuestionGet> convertQuestionEntityListToQuestionGetList(List<QuestionEntity> questionList) {
		return questionList.stream().map(question -> convertQuestionEntityToQuestionGet(question, false)).toList();
	}
}
