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
<title>Lista pytań</title>
</head>
<body onload="sortTableDesc()">
	<div style="margin: 30px">
		<span style='float:right'><b>Zalogowany jako:</b> ${userlogin}</span><br/>
		<button class="btn btn-primary"
			onclick="window.location.href = 'new/'">Utwórz nowe pytanie</button>
		<button class="btn btn-primary"
			onclick="window.location.href = 'pdf-export/'">Eksportuj do pliku PDF</button>
		<button class="btn btn-primary"
			onclick="window.location.href = 'excel-export/'">Eksportuj do pliku Excel</button>
		<button class="btn btn-primary"
			onclick="window.location.href = '../../logout'">Wyloguj się</button>
		<br /> <br />
		<h1>Lista pytań:</h1>
		<br />
		<c:choose>
			<c:when test="${empty questionList}">
				<h5>
					<font color="red">Brak pytań</font>
				</h5>
			</c:when>
			<c:otherwise>
				<table id="questionsTable" class="table table-bordered">
					<thead class="thead-dark">
						<tr>
							<th scope="col" onclick="sortTable(0)">Id <i
								class="fa fa-fw fa-sort"></i></th>
							<th scope="col" onclick="sortTable(1)">Tytuł <i
								class="fa fa-fw fa-sort"></i></th>
							<th scope="col" onclick="sortTable(2)">Opis <i
								class="fa fa-fw fa-sort"></i></th>
							<th scope="col" onclick="sortTable(3)">Data utworzenia <i
								class="fa fa-fw fa-sort"></i></th>
							<th scope="col" onclick="sortTable(4)">Data modyfikacji <i
								class="fa fa-fw fa-sort"></i></th>
							<th scope="col" onclick="sortTable(5)">Użytkownik <i
								class="fa fa-fw fa-sort"></i></th>
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
								<td><div class="btn-group btn-group-sm" role="group"
										aria-label="...">
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
</body>
<script type="text/javascript">
	function sortTable(n) {
		var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;
		table = document.getElementById("questionsTable");
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
<script type="text/javascript">
	function sortTableDesc() {
		var table, rows, switching, i, x, y, shouldSwitch;
		table = document.getElementById("questionsTable");
		if (typeof(table) != 'undefined' && table != null){
			switching = true;
			while (switching) {
				switching = false;
				rows = table.rows;
				for (i = 1; i < (rows.length - 1); i++) {
					shouldSwitch = false;
					x = rows[i].getElementsByTagName("TD")[0];
					y = rows[i + 1].getElementsByTagName("TD")[0];
					if (x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase()) {
						shouldSwitch = true;
						break;
					}
				}
				if (shouldSwitch) {
					rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
					switching = true;
				}
			}
		}
	}
</script>
</html>