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
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css" integrity="sha384-9aIt2nRpC12Uk9gS9baDl411NQApFmC26EwAOH8WgZl5MYYxFfc+NcPb1dKGj7Sk" crossorigin="anonymous">
<title>Edytowanie pytania o id: ${oldQuestion.id}</title>
</head>
<body>
	<div style="margin: 30px">
		<span style='float:right'>
			<b>Zalogowany jako:</b> ${userLogin} &nbsp;
			<button class="btn btn-primary"
				onclick="window.location.href = '../../../myProfile/'">Mój profil</button>
			<button class="btn btn-primary"
				onclick="window.location.href = '../../../../logout'">Wyloguj się</button>
		</span><br/>
		<h1>Edytowanie pytania o id: ${oldQuestion.id}</h1>
		<br />
		Za każdym razem, kiedy ktoś opublikuje lub zmieni odpowiedź na Twoje pytanie, zostaniesz powiadomiony o tym mailowo.
		<br /><br />
		<form:form method="post" modelAttribute="question">
			<div class="form-group">
				<label>Tytuł pytania:</label>
				<form:input path="title" placeholder="Tytuł pytania" type="text"
					class="form-control" />
				<form:errors path="title" style="color:red"
					class="form-text text-muted" />
			</div>
			<div class="form-group">
				<label>Opis pytania:</label>
				<form:textarea path="description" placeholder="Opis pytania"
					type="text" class="form-control" rows="3" />
			</div>
			<br />
			<form:button type="submit" class="btn btn-success">Zapisz</form:button>
		</form:form>
		<br />
		<button class="btn btn-danger"
			onclick="window.location.href = '../..'">Anuluj</button>
	</div>
</body>
</html>