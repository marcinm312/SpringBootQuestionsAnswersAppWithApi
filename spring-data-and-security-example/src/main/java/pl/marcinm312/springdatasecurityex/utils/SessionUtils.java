package pl.marcinm312.springdatasecurityex.utils;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;

@Service
public class SessionUtils {

	private final SessionRegistry sessionRegistry;

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	public SessionUtils(SessionRegistry sessionRegistry) {
		this.sessionRegistry = sessionRegistry;
	}

	public void expireUserSessions(String username, boolean expireCurrentSession) {
		log.info("Starting expiring user sessions");
		for (Object principal : sessionRegistry.getAllPrincipals()) {
			if (principal instanceof UserDetails) {
				UserDetails userDetails = (UserDetails) principal;
				if (userDetails.getUsername().equals(username)) {
					List<SessionInformation> listOfSessionInformation = sessionRegistry.getAllSessions(userDetails, true);
					log.info("listOfSessionInformation.size()={}", listOfSessionInformation.size());
					for (SessionInformation sessionInformation : listOfSessionInformation) {
						processSession(username, expireCurrentSession, sessionInformation);
					}
				}
			}
		}
		log.info("User sessions expired");
	}

	private void processSession(String username, boolean expireCurrentSession, SessionInformation sessionInformation) {
		if (expireCurrentSession) {
			sessionInformation.expireNow();
			log.info("Session {} of user {} has expired", sessionInformation.getSessionId(), username);
		} else {
			String currentSessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
			if (!sessionInformation.getSessionId().equals(currentSessionId)) {
				sessionInformation.expireNow();
				log.info("Session {} of user {} has expired", sessionInformation.getSessionId(), username);
			}
		}
	}
}