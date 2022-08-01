package pl.marcinm312.springdatasecurityex.answer.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnswerMapper {

	public static AnswerGet convertAnswerEntityToAnswerGet(AnswerEntity answer) {

		return AnswerGet.builder()
				.id(answer.getId())
				.text(answer.getText())
				.createdAt(answer.getCreatedAt())
				.updatedAt(answer.getUpdatedAt())
				.user(answer.getUser().getUsername())
				.build();
	}

	public static List<AnswerGet> convertAnswerEntityListToAnswerGetList(List<AnswerEntity> answerList) {
		return answerList.stream().map(AnswerMapper::convertAnswerEntityToAnswerGet).collect(Collectors.toList());
	}
}
