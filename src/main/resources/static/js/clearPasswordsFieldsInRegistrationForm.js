function clearPasswordsFieldsInRegistrationForm() {
    const password = document.getElementById("userPassword");
    const confirmPassword = document.getElementById("userConfirmPassword");
    password.value = "";
    confirmPassword.value = "";
}