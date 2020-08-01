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
<title>Rejestracja użytkownika</title>
</head>
<body>
	<div style="margin: 30px">
		<h1>Utwórz nowe konto</h1>
		<br />
		Po rejestracji, otrzymasz maila, dzięki któremu będziesz mógł aktywować swoje konto. Bez aktywacji konta, zalogowanie się na nowe konto nie jest możliwe.
		<br /><br />
		<form:form method="post" modelAttribute="user">
			<div class="form-group">
				<label>Login</label>
				<form:input path="username" placeholder="Login" type="text"
					class="form-control" />
				<form:errors path="username" style="color:red"
					class="form-text text-muted" />
			</div>
			<div class="form-group">
				<label>Hasło</label>
				<form:input path="password" placeholder="Hasło" type="password"
					class="form-control" />
				<form:errors path="password" style="color:red"
					class="form-text text-muted" />
			</div>
			<div class="form-group">
				<label>Potwierdź hasło</label>
				<form:input path="confirmPassword" placeholder="Potwierdź hasło" type="password"
					class="form-control" />
				<form:errors path="confirmPassword" style="color:red"
					class="form-text text-muted" />
			</div>
			<div class="form-group">
				<label>Imię</label>
				<form:input path="firstName" placeholder="Imię" type="text"
					class="form-control" />
				<form:errors path="firstName" style="color:red"
					class="form-text text-muted" />
			</div>
			<div class="form-group">
				<label>Nazwisko</label>
				<form:input path="lastName" placeholder="Nazwisko" type="text"
					class="form-control" />
				<form:errors path="lastName" style="color:red"
					class="form-text text-muted" />
			</div>
			<div class="form-group">
				<label>Email</label>
				<form:input path="email" placeholder="Email" type="text"
					class="form-control" />
				<form:errors path="email" style="color:red"
					class="form-text text-muted" />
			</div>
			<br />
			<form:button type="submit" class="btn btn-success">Utwórz</form:button>
		</form:form>
		<br />
		<button class="btn btn-danger" onclick="window.location.href = '..'">Anuluj</button>
	</div>
</body>
</html>