package pl.marcinm312.springdatasecurityex.answer.model;

import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.user.model.User;

import java.util.ArrayList;
import java.util.List;

public class AnswerMapper {

	private AnswerMapper() {

	}

	public static AnswerGet convertAnswerToAnswerGet(Answer answer) {
		AnswerGet answerGet = new AnswerGet();
		answerGet.setId(answer.getId());
		answerGet.setText(answer.getText());
		answerGet.setCreatedAt(answer.getCreatedAt());
		answerGet.setUpdatedAt(answer.getUpdatedAt());
		User user = answer.getUser();
		if (user != null) {
			answerGet.setUser(user.getUsername());
		}
		return answerGet;
	}

	public static List<AnswerGet> convertAnswerListToAnswerGetList(List<Answer> answerList) {
		List<AnswerGet> newAnswerList = new ArrayList<>();
		for (Answer answer : answerList) {
			newAnswerList.add(convertAnswerToAnswerGet(answer));
		}
		return newAnswerList;
	}
}
