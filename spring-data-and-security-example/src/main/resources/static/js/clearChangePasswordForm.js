function clearChangePasswordForm() {
    const currentPassword = document.getElementById("currentPassword");
    const password = document.getElementById("password");
    const confirmPassword = document.getElementById("confirmPassword");
    currentPassword.value = "";
    password.value = "";
    confirmPassword.value = "";
}