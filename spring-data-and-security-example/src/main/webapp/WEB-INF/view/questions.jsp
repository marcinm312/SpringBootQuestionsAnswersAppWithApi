<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.15.3/css/all.css"
          integrity="sha384-SZXxX4whJ79/gErwcOYf+zWLeJdY/qpuqC4cAa9rOGUstPomtqpuNWT9wdPEn2fk" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.18.3/bootstrap-table.min.css"
          integrity="sha512-5RNDl2gYvm6wpoVAU4J2+cMGZQeE2o4/AksK/bi355p/C31aRibC93EYxXczXq3ja2PJj60uifzcocu2Ca2FBg=="
          crossorigin="anonymous" referrerpolicy="no-referrer"/>
    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css"
          integrity="sha384-TX8t27EcRE3e/ihU7zmQxVncDAy5uIKz4rEkgIXeMed4M0jlfIDPvg6uqKI2xXr2"
          crossorigin="anonymous">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
    <title>Lista pytań</title>
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
                        onclick="window.location.href = '../myProfile/'">Mój
                    profil
                </button>
                <button class="btn btn-primary"
                        onclick="window.location.href = '../../logout'">Wyloguj
                    się
                </button>
            </div>
        </div>
    </div>
    <h1 id="header">Lista pytań:</h1>
    <div id="toolbar">
        <button class="btn btn-primary"
                onclick="window.location.href = 'new/'">Utwórz nowe pytanie
        </button>
        <button class="btn btn-primary"
                onclick="window.location.href = 'pdf-export/'">Eksportuj do
            PDF
        </button>
        <button class="btn btn-primary"
                onclick="window.location.href = 'excel-export/'">Eksportuj
            do Excel
        </button>
        <form:form action="" method="GET" class="float-right">
            <input class="form-control search-input" type="search" placeholder="Szukaj" name="keyword" />
        </form:form>
    </div>
    <c:choose>
        <c:when test="${empty questionList}">
            <p class="empty">Brak pytań</p>
        </c:when>
        <c:otherwise>
            <table data-toggle="table" data-page-size="5" data-pagination="true"
                   data-toolbar="#toolbar" data-page-list="[5,10,20,50]" data-locale="pl-PL"
                   aria-describedby="header">

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
                        <td>${question.user}</td>
                        <td>
                            <div class="btn-group-vertical">
                                <button type="button" class="btn btn-secondary btn-sm"
                                        onclick="window.location.href = '${question.id}/answers/'">Odpowiedzi
                                </button>
                                <button type="button" class="btn btn-secondary btn-sm"
                                        onclick="window.location.href = '${question.id}/edit/'">Edytuj
                                </button>
                                <button type="button" class="btn btn-secondary btn-sm"
                                        onclick="window.location.href = '${question.id}/delete/'">Usuń
                                </button>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

        </c:otherwise>
    </c:choose>
</div>

<script
        src="https://code.jquery.com/jquery-3.6.0.slim.min.js"
        integrity="sha256-u7e5khyithlIdTpu22PHhENmPcRdFiHRjhAuHcs05RI="
        crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.1/umd/popper.min.js"
        integrity="sha512-ubuT8Z88WxezgSqf3RLuNi5lmjstiJcyezx34yIU2gAHonIi27Na7atqzUZCOoY4CExaoFumzOsFQ2Ch+I/HCw=="
        crossorigin="anonymous" referrerpolicy="no-referrer"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"
        integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM"
        crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.18.3/bootstrap-table.min.js"
        integrity="sha512-Wm00XTqNHcGqQgiDlZVpK4QIhO2MmMJfzNJfh8wwbBC9BR0FtdJwPqDhEYy8jCfKEhWWZe/LDB6FwY7YE9QhMg=="
        crossorigin="anonymous" referrerpolicy="no-referrer"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.18.3/locale/bootstrap-table-pl-PL.min.js"
        integrity="sha512-ep85KahosNho/4kAbIhQHli2VnBHGRZJpv3GgXiDjzOhY0VW5pdKQ0sfG+OzYjN5Aqu7sTTW6V0Q+8y5i2/VEA=="
        crossorigin="anonymous"></script>
</body>
</html>