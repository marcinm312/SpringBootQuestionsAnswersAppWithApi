package pl.marcinm312.springdatasecurityex.answer.model;

import lombok.experimental.UtilityClass;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class AnswerMapper {

	public static AnswerGet convertAnswerEntityToAnswerGet(AnswerEntity answer) {
		AnswerGet answerGet = new AnswerGet();
		answerGet.setId(answer.getId());
		answerGet.setText(answer.getText());
		answerGet.setCreatedAt(answer.getCreatedAt());
		answerGet.setUpdatedAt(answer.getUpdatedAt());
		UserEntity user = answer.getUser();
		if (user != null) {
			answerGet.setUser(user.getUsername());
		}
		return answerGet;
	}

	public static List<AnswerGet> convertAnswerEntityListToAnswerGetList(List<AnswerEntity> answerList) {
		return answerList.stream().map(AnswerMapper::convertAnswerEntityToAnswerGet).collect(Collectors.toList());
	}
}
