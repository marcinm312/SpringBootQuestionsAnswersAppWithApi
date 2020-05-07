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
<title>Usuwanie pytania o id: ${question.id}</title>
</head>
<body>
	<div style="margin: 30px">
		<span style='float:right'>
			<b>Zalogowany jako:</b> ${userlogin} &nbsp;
			<button class="btn btn-primary"
				onclick="window.location.href = '../../../../logout'">Wyloguj się</button>
		</span><br/>
		<h1>Usuwanie pytania o id: ${question.id}</h1>
		<br /> <strong>Tytuł pytania:</strong> <br /> ${question.title} <br />
		<br /> <strong>Opis:</strong> <br /> ${question.description} <br />
		<br />
		<form:form method="post" modelAttribute="question">
			<form:button type="submit" class="btn btn-success">Usuń</form:button>
		</form:form>
		<br />
		<button class="btn btn-danger"
			onclick="window.location.href = '../..'">Anuluj</button>
	</div>

</body>
</html>