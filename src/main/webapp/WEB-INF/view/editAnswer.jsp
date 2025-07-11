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
    <title>Edytowanie odpowiedzi o id: ${oldAnswer.id}</title>
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
                        onclick="window.location.href = '../../../../../myProfile/'">Mój profil
                </button>
                <button class="btn btn-primary"
                        onclick="window.location.href = '../../../../../../logout'">Wyloguj się
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
            <span class="bold">Użytkownik:</span><br> ${question.user}
        </p>
    </div>
    <h1>Edytowanie odpowiedzi o id: ${oldAnswer.id}</h1>
    <p class="message">Autor pytania otrzyma powiadomienie mailowe o
        każdej Twojej zaktualizowanej odpowiedzi.</p>
    <form:form method="post" modelAttribute="answer">
        <div class="mb-3">
            <label for="answerText" class="form-label">Odpowiedź:</label>
            <form:textarea id="answerText" path="text" type="text" class="form-control" rows="3"/>
            <form:errors path="text" class="form-text"/>
        </div>
        <div class="formbuttons">
            <form:button type="submit" class="btn btn-success">Zapisz</form:button>
            <button type="button" class="btn btn-danger"
                    onclick="window.location.href = '../..'">Anuluj
            </button>
        </div>
    </form:form>
</div>
</body>
</html>