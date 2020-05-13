<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<!-- Bootstrap CSS -->
<link rel="stylesheet"
	href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
	integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T"
	crossorigin="anonymous">
<link
	href="//maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css"
	rel="stylesheet">
<title>Mój profil</title>
</head>
<body>
	<div style="margin: 30px">
		<span style='float:right'>
			<b>Zalogowany jako:</b> ${userlogin} &nbsp;
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
			onclick="window.location.href = 'endOtherSessions/'">Wyloguj mnie z innych urządzeń</button>
		<button class="btn btn-primary"
			onclick="window.location.href = 'delete/'">Usuń moje konto</button>
		<br /> <br />
		<h1>Mój profil:</h1>
		<br /> <strong>Login:</strong> <br /> ${user.username} <br />
		<br /> <strong>Imię:</strong> <br /> ${user.firstName} <br />
		<br /> <strong>Nazwisko:</strong> <br /> ${user.lastName} <br />
		<br /> <strong>Email:</strong> <br /> ${user.email} <br />
	</div>
</body>
</html>