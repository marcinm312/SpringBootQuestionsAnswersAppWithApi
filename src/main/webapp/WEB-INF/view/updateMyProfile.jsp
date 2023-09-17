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

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css"
            integrity="sha384-xOolHFLEh07PJGoPkLv1IbcEPTNtaed2xpHsD9ESMhqIYd0nLMwNLD69Npy4HI+N" crossorigin="anonymous">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
    <title>Edycja profilu</title>
</head>
<body>
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
    <h1>Edycja profilu</h1>
    <p class="message">
			<span class="bold">Po zmianie loginu, konieczne będzie ponowne zalogowanie się.</span>
			<br><br>
			<span class="bold">Po zmianie adresu email, otrzymasz email na stary adres mailowy.
			Zmiana emaila nastąpi dopiero po kliknięciu w link w otrzymanym mailu.</span>
    </p>
    <form:form method="post" modelAttribute="user">
        <div class="form-group">
            <label>Login</label>
            <form:input path="username" type="text" class="form-control"/>
            <form:errors path="username" class="form-text"/>
        </div>
        <div class="form-group">
            <label>Email</label>
            <form:input path="email" type="email" class="form-control"/>
            <form:errors path="email" class="form-text"/>
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
</html>