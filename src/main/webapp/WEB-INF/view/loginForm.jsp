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

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css"
            integrity="sha384-xOolHFLEh07PJGoPkLv1IbcEPTNtaed2xpHsD9ESMhqIYd0nLMwNLD69Npy4HI+N" crossorigin="anonymous">
    <link href="${pageContext.request.contextPath}/css/signin.css" rel="stylesheet">

    <title>Zaloguj się</title>
</head>
<body>

<button class="btn btn-primary back"
        onclick="window.location.href = '../..'">Wróć do strony głównej
</button>

<div class="container">
    <form:form class="form-signin" method="post" action="${pageContext.request.contextPath}/authenticate">

        <h1 class="h3 mb-3 font-weight-normal">Uzupełnij dane</h1>

        <c:if test="${param.error != null}">
            <div class="alert alert-danger" role="alert">${SPRING_SECURITY_LAST_EXCEPTION.localizedMessage}</div>
        </c:if>

        <p>
            <label for="username" class="sr-only">Login</label>
            <input type="text" id="username" name="username" class="form-control" placeholder="Login" required
                   autofocus/>
        </p>
        <p>
            <label for="password" class="sr-only">Hasło</label>
            <input type="password" id="password" name="password" class="form-control" placeholder="Hasło" required/>
        </p>

        <button type="submit" class="btn btn-lg btn-primary btn-block">Zaloguj się</button>

    </form:form>
</div>

</body>
</html>
