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

<script src="https://kit.fontawesome.com/5fab29723f.js"
	crossorigin="anonymous"></script>
<link rel="stylesheet"
	href="https://unpkg.com/bootstrap-table@1.18.1/dist/bootstrap-table.min.css">
<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css"
	integrity="sha384-TX8t27EcRE3e/ihU7zmQxVncDAy5uIKz4rEkgIXeMed4M0jlfIDPvg6uqKI2xXr2"
	crossorigin="anonymous">
<link href="/css/style.css" rel="stylesheet">
<title>Lista pytań</title>
</head>
<body>
	<div style="margin: 30px">
		<span style='float: right'> <b>Zalogowany jako:</b>
			${userLogin} &nbsp;
			<button class="btn btn-primary"
				onclick="window.location.href = '../myProfile/'">Mój profil</button>
			<button class="btn btn-primary"
				onclick="window.location.href = '../../logout'">Wyloguj się</button>
		</span><br />
		<h1>Lista pytań:</h1>
		<br />
		<div id="toolbar">
			<button class="btn btn-primary"
				onclick="window.location.href = 'new/'">Utwórz nowe pytanie</button>
			<button class="btn btn-primary"
				onclick="window.location.href = 'pdf-export/'">Eksportuj do
				PDF</button>
			<button class="btn btn-primary"
				onclick="window.location.href = 'excel-export/'">Eksportuj
				do Excel</button>
		</div>
		<c:choose>
			<c:when test="${empty questionList}">
				<br />
				<h5>
					<font color="red">Brak pytań</font>
				</h5>
			</c:when>
			<c:otherwise>
				<table data-toggle="table" data-page-size="5" data-pagination="true"
					data-toolbar="#toolbar" data-page-list="[5,10,20,50]"
					data-search="true" data-show-columns="true" data-locale="pl-PL"
					data-show-columns-toggle-all="true"
					data-show-pagination-switch="true">

					<thead class="thead-dark">
						<tr>
							<th scope="col" data-sortable="true">Id</th>
							<th scope="col" data-sortable="true">Tytuł</th>
							<th scope="col" data-sortable="true">Opis</th>
							<th scope="col" data-sortable="true">Data utworzenia</th>
							<th scope="col" data-sortable="true">Data modyfikacji</th>
							<th scope="col" data-sortable="true">Użytkownik</th>
							<th scope="col">Opcje</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="question" items="${questionList}">
							<tr>
								<td>${question.id}</td>
								<td>${question.title}</td>
								<td>${question.description}</td>
								<td>${question.createdAtAsString}</td>
								<td>${question.updatedAtAsString}</td>
								<td>${question.user.username}</td>
								<td><div class="btn-group-vertical">
										<button type="button" class="btn btn-secondary btn-sm"
											onclick="window.location.href = '${question.id}/answers/'">Odpowiedzi</button>
										<button type="button" class="btn btn-secondary btn-sm"
											onclick="window.location.href = '${question.id}/edit/'">Edytuj</button>
										<button type="button" class="btn btn-secondary btn-sm"
											onclick="window.location.href = '${question.id}/delete/'">Usuń</button>
									</div></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>

			</c:otherwise>
		</c:choose>
	</div>

	<script src="https://code.jquery.com/jquery-3.3.1.slim.min.js"
		integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo"
		crossorigin="anonymous"></script>
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js"
		integrity="sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49"
		crossorigin="anonymous"></script>
	<script
		src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js"
		integrity="sha384-ChfqqxuZUCnJSK3+MXmPNIyE6ZbWh2IMqE241rYiqJxyMiZ6OW/JmZQ5stwEULTy"
		crossorigin="anonymous"></script>
	<script
		src="https://unpkg.com/bootstrap-table@1.18.1/dist/bootstrap-table.min.js"></script>
	<script
		src="https://unpkg.com/bootstrap-table@1.18.1/dist/bootstrap-table-locale-all.min.js"></script>
</body>
</html>