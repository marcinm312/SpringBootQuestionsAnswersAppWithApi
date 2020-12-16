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
<title>Mój profil</title>
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
						onclick="window.location.href = '../../logout'">Wyloguj
						się</button>
				</div>
			</div>
		</div>
		<h1>Mój profil</h1>
		<div class="toolbar">
			<button class="btn btn-primary"
				onclick="window.location.href = '../questions/'">Przejdź do
				listy pytań</button>
			<button class="btn btn-primary"
				onclick="window.location.href = 'update/'">Edytuj profil</button>
			<button class="btn btn-primary"
				onclick="window.location.href = 'updatePassword/'">Zmień
				hasło</button>
			<button class="btn btn-primary"
				onclick="window.location.href = 'delete/'">Usuń moje konto</button>
			<button class="btn btn-primary"
				onclick="window.location.href = 'endOtherSessions/'">Wyloguj
				mnie z innych urządzeń</button>
		</div>
		<div class="section">
			<p>
				<span class="bold">Login:</span><br> ${user.username}
			</p>
			<p>
				<span class="bold">Email:</span><br> ${user.email}
			</p>
		</div>
	</div>
</body>
</html>