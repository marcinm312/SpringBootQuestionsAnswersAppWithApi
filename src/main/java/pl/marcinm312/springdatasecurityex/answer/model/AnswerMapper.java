package pl.marcinm312.springdatasecurityex.answer.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnswerMapper {

	public static AnswerGet convertAnswerEntityToAnswerGet(AnswerEntity answer, boolean isCreateOrUpdate) {

		var builder = AnswerGet.builder()
				.id(answer.getId())
				.text(answer.getText())
				.user(answer.getUser().getUsername());

		if (!isCreateOrUpdate) {
			builder = builder
					.createdAt(answer.getCreatedAt())
					.updatedAt(answer.getUpdatedAt());
		}

		return builder.build();
	}

	public static List<AnswerGet> convertAnswerEntityListToAnswerGetList(List<AnswerEntity> answerList) {
		return answerList.stream().map(answer -> convertAnswerEntityToAnswerGet(answer, false)).toList();
	}
}
