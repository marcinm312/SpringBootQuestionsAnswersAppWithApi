<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
<link href="/css/style.css" rel="stylesheet">
<title>Usuwanie odpowiedzi o id: ${answer.id}</title>
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
						onclick="window.location.href = '../../../../../myProfile/'">Mój
						profil</button>
					<button class="btn btn-primary"
						onclick="window.location.href = '../../../../../../logout'">Wyloguj
						się</button>
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
				${question.user.username}
			</p>
		</div>
		<h1>Usuwanie odpowiedzi o id: ${answer.id}</h1>
		<p>
			<span class="bold">Odpowiedź:</span><br> ${answer.text}
		</p>
		<form:form method="post" modelAttribute="answer">
			<div class="formbuttons">
				<form:button type="submit" class="btn btn-success">Usuń</form:button>
				<button type="button" class="btn btn-danger"
					onclick="window.location.href = '../..'">Anuluj</button>
			</div>
		</form:form>
	</div>
</body>
</html>