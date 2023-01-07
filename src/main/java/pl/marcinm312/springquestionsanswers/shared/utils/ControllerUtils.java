package pl.marcinm312.springquestionsanswers.shared.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.ui.Model;
import pl.marcinm312.springquestionsanswers.shared.exception.ChangeNotAllowedException;
import pl.marcinm312.springquestionsanswers.shared.exception.ResourceNotFoundException;

import javax.servlet.http.HttpServletResponse;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ControllerUtils {

	private static final String CHANGE_NOT_ALLOWED_VIEW = "changeNotAllowed";
	private static final String MESSAGE = "message";
	private static final String RESOURCE_NOT_FOUND_VIEW = "resourceNotFound";
	private static final String USER_LOGIN = "userLogin";

	public static String getChangeNotAllowedView(Model model, String userName, ChangeNotAllowedException e,
										   HttpServletResponse response) {

		response.setStatus(e.getHttpStatus());
		model.addAttribute(USER_LOGIN, userName);
		return CHANGE_NOT_ALLOWED_VIEW;
	}

	public static String getResourceNotFoundView(Model model, String userName, ResourceNotFoundException e,
										   HttpServletResponse response) {

		response.setStatus(e.getHttpStatus());
		model.addAttribute(USER_LOGIN, userName);
		model.addAttribute(MESSAGE, e.getMessage());
		return RESOURCE_NOT_FOUND_VIEW;
	}
}
