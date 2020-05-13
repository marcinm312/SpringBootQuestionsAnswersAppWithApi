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
<title>Strona główna</title>
</head>
<body>
	<div style="margin: 30px">
		<button class="btn btn-primary" onclick="window.location.href = 'app/questions/'">Przejdź do listy pytań</button>
		<button class="btn btn-primary" onclick="window.location.href = 'register'">Zarejestruj się</button>
		<br /><br />
		Aby przejść do listy pytań, niezbędne będzie zalogowanie się. Jeżeli nie posiadasz konta, kliknij przycisk <b>Zarejestruj się</b>
	</div>
</body>
</html>