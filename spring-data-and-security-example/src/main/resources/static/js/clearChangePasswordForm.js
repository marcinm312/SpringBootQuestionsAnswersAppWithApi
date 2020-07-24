function clearChangePasswordForm() {
	var currentPassword = document.getElementById("currentPassword");
	var password = document.getElementById("password");
	var confirmPassword = document.getElementById("confirmPassword");
	currentPassword.value = "";
	password.value = "";
	confirmPassword.value = "";
}