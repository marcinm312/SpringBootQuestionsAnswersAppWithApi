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

<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.15.3/css/all.css"
    integrity="sha384-SZXxX4whJ79/gErwcOYf+zWLeJdY/qpuqC4cAa9rOGUstPomtqpuNWT9wdPEn2fk" crossorigin="anonymous">
<link rel="stylesheet"
	href="https://unpkg.com/bootstrap-table@1.18.1/dist/bootstrap-table.min.css">
<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css"
	integrity="sha384-TX8t27EcRE3e/ihU7zmQxVncDAy5uIKz4rEkgIXeMed4M0jlfIDPvg6uqKI2xXr2"
	crossorigin="anonymous">
<link href="/css/style.css" rel="stylesheet">
<title>Odpowiedzi na pytanie o id: ${question.id}</title>
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
						onclick="window.location.href = '../../../myProfile/'">Mój
						profil</button>
					<button class="btn btn-primary"
						onclick="window.location.href = '../../../../logout'">Wyloguj
						się</button>
				</div>
			</div>
		</div>
		<h1>Pytanie:</h1>
		<div class="section">
			<p>
				<span class="bold">Tytuł pytania:</span><br> ${question.title}
			</p>
			<p>
				<span class="bold">Opis:</span><br> ${question.description}
			</p>
			<p>
				<span class="bold">Użytkownik:</span><br>
				${question.user.username}
			</p>
		</div>
		<h1>Lista odpowiedzi:</h1>
		<div id="toolbar">
			<button class="btn btn-primary"
				onclick="window.location.href = '../..'">Wróć do listy
				pytań</button>
			<button class="btn btn-primary"
				onclick="window.location.href = 'new/'">Dodaj odpowiedź</button>
			<button class="btn btn-primary"
				onclick="window.location.href = 'pdf-export/'">Eksportuj do
				PDF</button>
			<button class="btn btn-primary"
				onclick="window.location.href = 'excel-export/'">Eksportuj
				do Excel</button>
		</div>
		<c:choose>
			<c:when test="${empty answerList}">
				<p class="empty">Brak odpowiedzi na pytanie</p>
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
							<th scope="col" data-sortable="true">Treść odpowiedzi</th>
							<th scope="col" data-sortable="true">Data utworzenia</th>
							<th scope="col" data-sortable="true">Data modyfikacji</th>
							<th scope="col" data-sortable="true">Użytkownik</th>
							<th scope="col">Opcje</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="answer" items="${answerList}">
							<tr>
								<td>${answer.id}</td>
								<td>${answer.text}</td>
								<td>${answer.createdAtAsString}</td>
								<td>${answer.updatedAtAsString}</td>
								<td>${answer.user.username}</td>
								<td><div class="btn-group-vertical">
										<button type="button" class="btn btn-secondary btn-sm"
											onclick="window.location.href = '${answer.id}/edit/'">Edytuj</button>
										<button type="button" class="btn btn-secondary btn-sm"
											onclick="window.location.href = '${answer.id}/delete/'">Usuń</button>
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
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.18.3/bootstrap-table.min.js"
        integrity="sha512-Wm00XTqNHcGqQgiDlZVpK4QIhO2MmMJfzNJfh8wwbBC9BR0FtdJwPqDhEYy8jCfKEhWWZe/LDB6FwY7YE9QhMg=="
        crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.18.3/locale/bootstrap-table-pl-PL.min.js"
        integrity="sha512-ep85KahosNho/4kAbIhQHli2VnBHGRZJpv3GgXiDjzOhY0VW5pdKQ0sfG+OzYjN5Aqu7sTTW6V0Q+8y5i2/VEA=="
        crossorigin="anonymous"></script>
</body>
</html>