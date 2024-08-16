package pl.marcinm312.springquestionsanswers.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.model.UserMapper;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserGet;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserAdminManager {

	private final UserRepo userRepo;

	@Value("${user.expiration.days}")
	private Integer userExpirationDays;

	public List<UserGet> getNonEnabledOldUsers() {

		log.info("Checking expired users. userExpirationDays={}", userExpirationDays);
		List<UserEntity> users = userRepo.getNonEnabledOldUsers(LocalDate.now().minusDays(userExpirationDays));
		log.info("Returned expired users: {}", users.size());
		return UserMapper.convertUserEntityListToUserGetList(users);
	}

	public void deleteNonEnabledOldUsers() {

		log.info("Deleting expired users. userExpirationDays={}", userExpirationDays);
		List<UserEntity> users = userRepo.getNonEnabledOldUsers(LocalDate.now().minusDays(userExpirationDays));
		log.info("Returned expired users: {}", users.size());
		for (UserEntity userEntity : users) {
			try {
				userRepo.delete(userEntity);
				log.info("Deleted user: {}", UserMapper.convertUserToUserGet(userEntity, false));
			} catch (Exception e) {
				String errorMessage = String.format("Error deleting user %s: %s", userEntity.getUsername(), e.getMessage());
				log.error(errorMessage, e);
			}
		}
		log.info("Expired users deleted");
	}
}
