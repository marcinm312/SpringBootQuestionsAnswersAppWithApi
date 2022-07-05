function clearPasswordsFieldsInRegistrationForm() {
    const password = document.getElementById("password");
    const confirmPassword = document.getElementById("confirmPassword");
    password.value = "";
    confirmPassword.value = "";
}