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
<title>Brak uprawnień</title>
</head>
<body>
	<div style="margin: 30px">
		<span style='float: right'><b>Zalogowany jako:</b> ${userLogin}</span><br />
		<br />
		<button class="btn btn-primary"
			onclick="window.location.href = '../..'">Wróć</button>
		<br /> <br />
		<h1>Brak uprawnień do wykonania operacji</h1>
		<br />Użytkownik może modyfikować lub usuwać tylko swoje wpisy
	</div>
</body>
</html>