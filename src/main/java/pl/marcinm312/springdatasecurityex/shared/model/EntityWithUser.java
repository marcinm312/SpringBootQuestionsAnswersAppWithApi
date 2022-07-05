package pl.marcinm312.springdatasecurityex.shared.model;

import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

public interface EntityWithUser {

	UserEntity getUser();

	void setUser(UserEntity user);
}
