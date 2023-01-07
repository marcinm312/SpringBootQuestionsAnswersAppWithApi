package pl.marcinm312.springquestionsanswers.shared.model;

import pl.marcinm312.springquestionsanswers.user.model.UserEntity;

public interface CommonEntityWithUser extends CommonEntity {

	UserEntity getUser();
}
