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
    <title>Strona główna</title>
</head>
<body>
<div class="wrapper">
    <button class="btn btn-primary"
            onclick="window.location.href = 'app/questions/'">Przejdź do
        listy pytań
    </button>
    <button class="btn btn-primary"
            onclick="window.location.href = 'register'">Zarejestruj się
    </button>
    <p class="message">
        Aby przejść do listy pytań, niezbędne będzie zalogowanie się. Jeżeli
        nie posiadasz konta, kliknij przycisk <span class="bold">Zarejestruj
				się</span>
    </p>
</div>
</body>
</html>