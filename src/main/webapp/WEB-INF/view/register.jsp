<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-LN+7fdVzj6u52u30Kp6M/trliBMCMKTyK833zpbD+pXdCLuTusPj697FH4R/5mcr" crossorigin="anonymous">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
    <title>Rejestracja użytkownika</title>
</head>
<body onload="clearPasswordsFieldsInRegistrationForm()">
<div class="wrapper">
    <h1>Rejestracja użytkownika</h1>
    <p class="message">Po rejestracji, otrzymasz maila, dzięki któremu
        będziesz mógł aktywować swoje konto. Bez aktywacji konta, zalogowanie
        się na nowe konto nie jest możliwe.</p>
    <form:form method="post" modelAttribute="user">
        <div class="mb-3">
            <label for="userLogin" class="form-label">Login</label>
            <form:input id="userLogin" path="username" type="text" class="form-control"/>
            <form:errors path="username" class="form-text"/>
        </div>
        <div class="mb-3">
            <label for="userPassword" class="form-label">Hasło</label>
            <form:input id="userPassword" path="password" type="password" class="form-control"/>
            <form:errors path="password" class="form-text"/>
        </div>
        <div class="mb-3">
            <label for="userConfirmPassword" class="form-label">Potwierdź hasło</label>
            <form:input id="userConfirmPassword" path="confirmPassword" type="password" class="form-control"/>
            <form:errors path="confirmPassword" class="form-text"/>
        </div>
        <div class="mb-3">
            <label for="userEmail" class="form-label">Email</label>
            <form:input id="userEmail" path="email" type="email" class="form-control"/>
            <form:errors path="email" class="form-text"/>
        </div>
        <div class="formbuttons">
            <form:button type="submit" class="btn btn-success">Utwórz</form:button>
            <button type="button" class="btn btn-danger"
                    onclick="window.location.href = '..'">Anuluj
            </button>
        </div>
    </form:form>
</div>
</body>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/clearPasswordsFieldsInRegistrationForm.js"></script>
</html>