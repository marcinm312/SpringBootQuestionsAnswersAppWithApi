function clearChangePasswordForm() {
    let currentPassword = document.getElementById("currentPassword");
    let password = document.getElementById("password");
    let confirmPassword = document.getElementById("confirmPassword");
    currentPassword.value = "";
    password.value = "";
    confirmPassword.value = "";
}