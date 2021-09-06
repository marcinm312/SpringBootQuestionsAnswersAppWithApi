function clearPasswordsFieldsInRegistrationForm() {
    let password = document.getElementById("password");
    let confirmPassword = document.getElementById("confirmPassword");
    password.value = "";
    confirmPassword.value = "";
}