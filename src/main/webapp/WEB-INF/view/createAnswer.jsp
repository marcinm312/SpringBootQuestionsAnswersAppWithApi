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
    <title>Dodawanie odpowiedzi na pytanie o id: ${question.id}</title>
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
                        onclick="window.location.href = '../../../../myProfile/'">Mój
                    profil
                </button>
                <button class="btn btn-primary"
                        onclick="window.location.href = '../../../../../logout'">Wyloguj
                    się
                </button>
            </div>
        </div>
    </div>
    <h1>Pytanie:</h1>
    <div class="section">
        <p>
            <span class="bold">Tytuł pytania:</span><br> ${question.title}
        </p>
        <p>
            <span class="bold">Opis:</span><br> ${question.description}
        </p>
        <p>
            <span class="bold">Użytkownik:</span><br>
            ${question.user}
        </p>
    </div>
    <h1>Twoja odpowiedź:</h1>
    <p class="message">Autor pytania otrzyma powiadomienie mailowe o
        każdej Twojej opublikowanej odpowiedzi.</p>
    <form:form method="post" modelAttribute="answer">
        <div class="form-group">
            <label for="answerText">Odpowiedź:</label>
            <form:textarea id="answerText" path="text" type="text" class="form-control" rows="3"/>
            <form:errors path="text" class="form-text"/>
        </div>
        <div class="formbuttons">
            <form:button type="submit" class="btn btn-success">Dodaj</form:button>
            <button type="button" class="btn btn-danger"
                    onclick="window.location.href = '..'">Anuluj
            </button>
        </div>
    </form:form>
</div>
</body>
</html>