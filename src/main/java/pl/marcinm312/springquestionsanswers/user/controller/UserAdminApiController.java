package pl.marcinm312.springquestionsanswers.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserGet;
import pl.marcinm312.springquestionsanswers.user.service.UserAdminManager;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/users")
public class UserAdminApiController {

	private final UserAdminManager userAdminManager;

	@GetMapping("/getNonEnabledOldUsers")
	public List<UserGet> getNonEnabledOldUsers() {
		return userAdminManager.getNonEnabledOldUsers();
	}

	@DeleteMapping("/deleteNonEnabledOldUsers")
	public void deleteNonEnabledOldUsers() {
		userAdminManager.deleteNonEnabledOldUsers();
	}
}
