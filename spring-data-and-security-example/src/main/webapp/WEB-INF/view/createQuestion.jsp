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
    <title>Tworzenie nowego pytania</title>
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
                        onclick="window.location.href = '../../myProfile/'">Mój
                    profil
                </button>
                <button class="btn btn-primary"
                        onclick="window.location.href = '../../../logout'">Wyloguj
                    się
                </button>
            </div>
        </div>
    </div>
    <h1>Tworzenie nowego pytania</h1>
    <p class="message">Za każdym razem, kiedy ktoś opublikuje lub
        zmieni odpowiedź na Twoje pytanie, zostaniesz powiadomiony o tym
        mailowo.</p>
    <form:form method="post" modelAttribute="question">
        <div class="form-group">
            <label>Tytuł pytania:</label>
            <form:input path="title" type="text" class="form-control"/>
            <form:errors path="title" class="form-text"/>
        </div>
        <div class="form-group">
            <label>Opis pytania:</label>
            <form:textarea path="description" type="text" class="form-control"
                           rows="3"/>
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
</html>