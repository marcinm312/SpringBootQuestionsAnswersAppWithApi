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
<title>Edycja profilu</title>
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
						onclick="window.location.href = '../../../logout'">Wyloguj
						się</button>
				</div>
			</div>
		</div>
		<h1>Edycja profilu</h1>
		<p class="message">
			<span class="bold">Zmiana loginu spowoduje wylogowanie
				użytkownika!</span>
		</p>
		<form:form method="post" modelAttribute="user">
			<div class="form-group">
				<form:input path="id" type="hidden" class="form-control" />
				<form:errors path="id" class="form-text text-muted" />
			</div>
			<div class="form-group">
				<label>Login</label>
				<form:input path="username" type="text" class="form-control" />
				<form:errors path="username" class="form-text text-muted" />
			</div>
			<div class="form-group">
				<form:input path="password" type="hidden" class="form-control"
					value="password" />
				<form:errors path="password" class="form-text text-muted" />
			</div>
			<div class="form-group">
				<form:input path="confirmPassword" type="hidden"
					class="form-control" value="password" />
				<form:errors path="confirmPassword" class="form-text text-muted" />
			</div>
			<div class="form-group">
				<label>Email</label>
				<form:input path="email" type="text" class="form-control" />
				<form:errors path="email" class="form-text text-muted" />
			</div>
			<div class="formbuttons">
				<form:button type="submit" class="btn btn-success">Zapisz</form:button>
				<button type="button" class="btn btn-danger"
					onclick="window.location.href = '..'">Anuluj</button>
			</div>
		</form:form>
	</div>
</body>
</html>