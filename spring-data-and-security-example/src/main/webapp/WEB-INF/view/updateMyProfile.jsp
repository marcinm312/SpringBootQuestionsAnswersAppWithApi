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
<body>
	<div style="margin: 30px">
		<span style='float:right'>
			<b>Zalogowany jako:</b> ${userlogin} &nbsp;
			<button class="btn btn-primary"
				onclick="window.location.href = '../../../logout'">Wyloguj się</button>
		</span><br/>
		<h1>Edycja profilu</h1>
		<br />
		<form:form method="post" modelAttribute="user">
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
				<form:input path="password" placeholder="Hasło" type="hidden"
					class="form-control" value="password"/>
				<form:errors path="password" style="color:red"
					class="form-text text-muted" />
			</div>
			<div class="form-group">
				<form:input path="confirmPassword" placeholder="Potwierdź hasło" type="hidden"
					class="form-control" value="password"/>
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
			<form:button type="submit" class="btn btn-success">Zapisz</form:button>
		</form:form>
		<br />
		<button class="btn btn-danger" onclick="window.location.href = '..'">Anuluj</button>
	</div>
</body>
</html>