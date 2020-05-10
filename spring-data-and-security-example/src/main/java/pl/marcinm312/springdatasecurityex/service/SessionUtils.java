package pl.marcinm312.springdatasecurityex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

@Service
public class SessionUtils {

	private SessionRegistry sessionRegistry;

	@Autowired
	public SessionUtils(SessionRegistry sessionRegistry) {
		this.sessionRegistry = sessionRegistry;
	}

	public void expireUserSessionsExceptTheCurrentOne(String username) {
		for (Object principal : sessionRegistry.getAllPrincipals()) {
			if (principal instanceof UserDetails) {
				UserDetails userDetails = (UserDetails) principal;
				if (userDetails.getUsername().equals(username)) {
					for (SessionInformation sessionInformation : sessionRegistry.getAllSessions(userDetails, true)) {
						String currentSessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
						if (!sessionInformation.getSessionId().equals(currentSessionId)) {
							sessionInformation.expireNow();
						}
					}
				}
			}
		}
	}

	public void expireAllUserSessions(String username) {
		for (Object principal : sessionRegistry.getAllPrincipals()) {
			if (principal instanceof UserDetails) {
				UserDetails userDetails = (UserDetails) principal;
				if (userDetails.getUsername().equals(username)) {
					for (SessionInformation sessionInformation : sessionRegistry.getAllSessions(userDetails, true)) {
						sessionInformation.expireNow();
					}
				}
			}
		}
	}
}