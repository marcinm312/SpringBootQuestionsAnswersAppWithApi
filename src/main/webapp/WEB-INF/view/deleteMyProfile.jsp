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
    <title>Usuwanie konta</title>
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
                        onclick="window.location.href = '../../../logout'">Wyloguj się
                </button>
            </div>
        </div>
    </div>
    <h1>Usuwanie konta</h1>
    <div class="deleteuser">
        <p>Czy na pewno chcesz usunąć swoje konto użytkownika?</p>
        <p>
				<span class="bold">Czynność jest nieodwracalna i spowoduje
					usunięcie wszystkich Twoich wpisów!</span>
        </p>
    </div>
    <form:form method="post" modelAttribute="user3">
        <div class="formbuttons">
            <form:button type="submit" class="btn btn-success">Usuń</form:button>
            <button type="button" class="btn btn-danger"
                    onclick="window.location.href = '..'">Anuluj
            </button>
        </div>
    </form:form>
</div>
</body>
</html>