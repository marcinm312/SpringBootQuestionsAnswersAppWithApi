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
<title>Edycja profilu</title>
</head>
<body onload="clearForm()">
	<div style="margin: 30px">
		<span style='float:right'>
			<b>Zalogowany jako:</b> ${userlogin} &nbsp;
			<button class="btn btn-primary"
				onclick="window.location.href = '../../../logout'">Wyloguj się</button>
		</span><br/>
		<h1>Edycja profilu</h1>
		<br />
		<form:form method="post" modelAttribute="user2">
			<div class="form-group">
				<form:input path="id" placeholder="Id" type="hidden"
					class="form-control" />
				<form:errors path="id" style="color:red"
					class="form-text text-muted" />
			</div>
			<div class="form-group">
				<form:input path="username" placeholder="Login" type="hidden"
					class="form-control" />
				<form:errors path="username" style="color:red"
					class="form-text text-muted" />
			</div>
			<div class="form-group">
				<label>Obecne hasło</label>
				<form:input id="currentPassword" path="currentPassword" placeholder="Obecne hasło" type="password"
					class="form-control" />
				<form:errors path="currentPassword" style="color:red"
					class="form-text text-muted" />
			</div>
			<div class="form-group">
				<label>Hasło</label>
				<form:input id="password" path="password" placeholder="Hasło" type="password"
					class="form-control" />
				<form:errors path="password" style="color:red"
					class="form-text text-muted" />
			</div>
			<div class="form-group">
				<label>Potwierdź hasło</label>
				<form:input id="confirmPassword" path="confirmPassword" placeholder="Potwierdź hasło" type="password"
					class="form-control" />
				<form:errors path="confirmPassword" style="color:red"
					class="form-text text-muted" />
			</div>
			<div class="form-group">
				<form:input path="firstName" placeholder="Imię" type="hidden"
					class="form-control" />
				<form:errors path="firstName" style="color:red"
					class="form-text text-muted" />
			</div>
			<div class="form-group">
				<form:input path="lastName" placeholder="Nazwisko" type="hidden"
					class="form-control" />
				<form:errors path="lastName" style="color:red"
					class="form-text text-muted" />
			</div>
			<div class="form-group">
				<form:input path="email" placeholder="Email" type="hidden"
					class="form-control" />
				<form:errors path="email" style="color:red"
					class="form-text text-muted" />
			</div>
			<br />
			<form:button type="submit" class="btn btn-success">Zapisz</form:button>
		</form:form>
		<br />
		<button class="btn btn-danger" onclick="window.location.href = '..'">Anuluj</button>
	</div>
</body>
<script type="text/javascript">
	function clearForm(){
		var currentPassword = document.getElementById("currentPassword");
		var password = document.getElementById("password");
		var confirmPassword = document.getElementById("confirmPassword");
		currentPassword.value = "";
		password.value = "";
		confirmPassword.value = "";
	}
</script>
</html>