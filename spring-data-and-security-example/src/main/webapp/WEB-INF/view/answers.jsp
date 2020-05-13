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
<link
	href="//maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css"
	rel="stylesheet">
<title>Odpowiedzi na pytanie o id: ${question.id}</title>
</head>
<body>
	<div style="margin: 30px">
		<span style='float:right'>
			<b>Zalogowany jako:</b> ${userlogin} &nbsp;
			<button class="btn btn-primary"
				onclick="window.location.href = '../../../myprofile/'">Mój profil</button>
			<button class="btn btn-primary"
				onclick="window.location.href = '../../../../logout'">Wyloguj się</button>
		</span><br/>
		<button class="btn btn-primary"
			onclick="window.location.href = '../..'">Wróć do listy pytań</button>
		<button class="btn btn-primary"
			onclick="window.location.href = 'new/'">Dodaj odpowiedź</button>
		<button class="btn btn-primary"
			onclick="window.location.href = 'pdf-export/'">Eksportuj do pliku PDF</button>
		<button class="btn btn-primary"
			onclick="window.location.href = 'excel-export/'">Eksportuj do pliku Excel</button>
		<br /> <br />
		<h1>Pytanie:</h1>
		<br /> <b>Tytuł pytania:</b> <br /> ${question.title} <br />
		<br /> <b>Opis:</b> <br /> ${question.description} <br />
		<br /> <b>Użytkownik:</b> <br /> ${question.user.username} <br />
		<br />
		<h1>Lista odpowiedzi na pytanie:</h1>
		<br />
		<c:choose>
			<c:when test="${empty answerList}">
				<h5>
					<font color="red">Brak odpowiedzi na pytanie</font>
				</h5>
			</c:when>
			<c:otherwise>
				<table id="answersTable" class="table table-bordered">
					<thead class="thead-dark">
						<tr>
							<th scope="col" onclick="sortTable(0)">Id <i
								class="fa fa-fw fa-sort"></i></th>
							<th scope="col" onclick="sortTable(1)">Treść odpowiedzi <i
								class="fa fa-fw fa-sort"></i></th>
							<th scope="col" onclick="sortTable(2)">Data utworzenia <i
								class="fa fa-fw fa-sort"></i></th>
							<th scope="col" onclick="sortTable(3)">Data modyfikacji <i
								class="fa fa-fw fa-sort"></i></th>
							<th scope="col" onclick="sortTable(4)">Użytkownik <i
								class="fa fa-fw fa-sort"></i></th>
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
								<td><div class="btn-group btn-group-sm" role="group"
										aria-label="...">
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
</body>
<script type="text/javascript">
	function sortTable(n) {
		var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;
		table = document.getElementById("answersTable");
		switching = true;
		dir = "asc";
		while (switching) {
			switching = false;
			rows = table.rows;
			for (i = 1; i < (rows.length - 1); i++) {
				shouldSwitch = false;
				x = rows[i].getElementsByTagName("TD")[n];
				y = rows[i + 1].getElementsByTagName("TD")[n];
				if (dir == "asc") {
					if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
						shouldSwitch = true;
						break;
					}
				} else if (dir == "desc") {
					if (x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase()) {
						shouldSwitch = true;
						break;
					}
				}
			}
			if (shouldSwitch) {
				rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
				switching = true;
				switchcount++;
			} else {
				if (switchcount == 0 && dir == "asc") {
					dir = "desc";
					switching = true;
				}
			}
		}
	}
</script>
</html>