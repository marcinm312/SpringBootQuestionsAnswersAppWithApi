<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<!-- Bootstrap CSS -->
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css" integrity="sha384-9aIt2nRpC12Uk9gS9baDl411NQApFmC26EwAOH8WgZl5MYYxFfc+NcPb1dKGj7Sk" crossorigin="anonymous">
<link href="/css/custom.css" rel="stylesheet">
<title>Mój profil</title>
</head>
<body>
	<div style="margin: 30px">
		<span style='float:right'>
			<b>Zalogowany jako:</b> ${userLogin} &nbsp;
			<button class="btn btn-primary"
				onclick="window.location.href = '../../logout'">Wyloguj się</button>
		</span><br/>
		<button class="btn btn-primary"
			onclick="window.location.href = '../questions/'">Przejdź do listy pytań</button>
		<button class="btn btn-primary"
			onclick="window.location.href = 'update/'">Edytuj profil</button>
		<button class="btn btn-primary"
			onclick="window.location.href = 'updatePassword/'">Zmień hasło</button>
		<button class="btn btn-primary"
            onclick="window.location.href = 'delete/'">Usuń moje konto</button>
		<button class="btn btn-primary"
			onclick="window.location.href = 'endOtherSessions/'">Wyloguj mnie z innych urządzeń</button>
		<br /> <br />
		<h1>Mój profil:</h1>
		<br /> <b>Login:</b> <br /> ${user.username} <br />
		<br /> <b>Imię:</b> <br /> ${user.firstName} <br />
		<br /> <b>Nazwisko:</b> <br /> ${user.lastName} <br />
		<br /> <b>Email:</b> <br /> ${user.email} <br />
	</div>
</body>
</html>