<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-LN+7fdVzj6u52u30Kp6M/trliBMCMKTyK833zpbD+pXdCLuTusPj697FH4R/5mcr" crossorigin="anonymous">
    <link href="${pageContext.request.contextPath}/css/signin.css" rel="stylesheet">

    <title>Zaloguj się</title>
</head>
<body>

<button class="btn btn-primary back"
        onclick="window.location.href = '../..'">Wróć do strony głównej
</button>

<div class="container">
    <form:form class="form-signin" method="post" action="${pageContext.request.contextPath}/authenticate/">

        <h1 class="h3 mb-3 fw-normal">Uzupełnij dane</h1>

        <c:if test="${param.error != null}">
            <div class="alert alert-danger" role="alert">${SPRING_SECURITY_LAST_EXCEPTION.localizedMessage}</div>
        </c:if>

        <p>
            <label for="username" class="visually-hidden">Login</label>
            <input type="text" id="username" name="username" class="form-control" placeholder="Login" required
                   autofocus/>
        </p>
        <p>
            <label for="password" class="visually-hidden">Hasło</label>
            <input type="password" id="password" name="password" class="form-control" placeholder="Hasło" required/>
        </p>

        <button type="submit" class="btn btn-lg btn-primary btn-block">Zaloguj się</button>

    </form:form>
</div>

</body>
</html>
