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

    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css"
          integrity="sha384-TX8t27EcRE3e/ihU7zmQxVncDAy5uIKz4rEkgIXeMed4M0jlfIDPvg6uqKI2xXr2"
          crossorigin="anonymous">
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
        <div class="form-group">
            <label>Login</label>
            <form:input path="username" type="text" class="form-control"/>
            <form:errors path="username" class="form-text"/>
        </div>
        <div class="form-group">
            <label>Hasło</label>
            <form:input path="password" type="password" class="form-control"/>
            <form:errors path="password" class="form-text"/>
        </div>
        <div class="form-group">
            <label>Potwierdź hasło</label>
            <form:input path="confirmPassword" type="password"
                        class="form-control"/>
            <form:errors path="confirmPassword" class="form-text"/>
        </div>
        <div class="form-group">
            <label>Email</label>
            <form:input path="email" type="text" class="form-control"/>
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