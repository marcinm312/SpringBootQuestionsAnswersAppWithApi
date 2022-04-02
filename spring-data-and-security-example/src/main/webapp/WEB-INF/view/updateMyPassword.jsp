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

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.1/dist/css/bootstrap.min.css"
          integrity="sha384-zCbKRCUGaJDkqS1kPbPd7TveP5iyJE0EjAuZQTgFLD2ylzuqKfdKlfG/eSrtxUkn" crossorigin="anonymous">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
    <title>Zmiana hasła</title>
</head>
<body onload="clearChangePasswordForm()">
<div class="wrapper">
    <div class="usertoolbar clearfix">
        <div class="right">
            <p>
                <span class="bold">Zalogowany jako:</span> ${userLogin}
            </p>

            <div class="group">
                <button class="btn btn-primary"
                        onclick="window.location.href = '../../../logout'">Wyloguj
                    się
                </button>
            </div>
        </div>
    </div>
    <h1>Zmiana hasła</h1>
    <form:form method="post" modelAttribute="user2">
        <div class="form-group">
            <label>Obecne hasło</label>
            <form:input id="currentPassword" path="currentPassword"
                        type="password" class="form-control"/>
            <form:errors path="currentPassword" class="form-text"/>
        </div>
        <div class="form-group">
            <label>Hasło</label>
            <form:input id="password" path="password" type="password"
                        class="form-control" value=""/>
            <form:errors path="password" class="form-text"/>
        </div>
        <div class="form-group">
            <label>Potwierdź hasło</label>
            <form:input id="confirmPassword" path="confirmPassword"
                        type="password" class="form-control"/>
            <form:errors path="confirmPassword" class="form-text"/>
        </div>
        <div class="formbuttons">
            <form:button type="submit" class="btn btn-success">Zapisz</form:button>
            <button type="button" class="btn btn-danger"
                    onclick="window.location.href = '..'">Anuluj
            </button>
        </div>
    </form:form>
</div>
</body>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/clearChangePasswordForm.js"></script>
</html>