package pl.marcinm312.springdatasecurityex.shared.model;

import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

public interface CommonEntityWithUser extends CommonEntity {

	UserEntity getUser();
	void setUser(UserEntity user);
}
