package pl.marcinm312.springdatasecurityex.config.security.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SessionUtils {

	private final SessionRegistry sessionRegistry;

	public UserEntity expireUserSessions(UserEntity user, boolean expireCurrentSession, boolean isDeletingUser) {

		log.info("Starting expiring user sessions. user={}, expireCurrentSession={}", user, expireCurrentSession);
		List<SessionInformation> listOfSessionInformation = sessionRegistry.getAllSessions(user, true);
		log.info("listOfSessionInformation.size()={}", listOfSessionInformation.size());
		if (expireCurrentSession) {
			for (SessionInformation sessionInformation : listOfSessionInformation) {
				processSession(user.getUsername(), sessionInformation);
			}
		} else {
			String currentSessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
			for (SessionInformation sessionInformation : listOfSessionInformation) {
				if (!sessionInformation.getSessionId().equals(currentSessionId)) {
					processSession(user.getUsername(), sessionInformation);
				}
			}
		}
		if (!isDeletingUser) {
			user.setTimeOfSessionExpiration(new Date());
			log.info("User sessions expired");
		}
		return user;
	}

	private void processSession(String username, SessionInformation sessionInformation) {

		sessionInformation.expireNow();
		log.info("Session {} of user {} has expired", sessionInformation.getSessionId(), username);
	}
}
