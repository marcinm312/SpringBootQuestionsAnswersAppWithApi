<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="paginator" uri="/WEB-INF/tlds/Paginator" %>
<!DOCTYPE html>
<html lang="pl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css"
        integrity="sha512-Evv84Mr4kqVGRNSgIGL/F/aIDqQb7xQ2vcrdIwxfjThSH8CSR7PBEakCr51Ck+w+/U6swU2Im1vVX0SVk9ABhg=="
        crossorigin="anonymous" referrerpolicy="no-referrer" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-LN+7fdVzj6u52u30Kp6M/trliBMCMKTyK833zpbD+pXdCLuTusPj697FH4R/5mcr" crossorigin="anonymous">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
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
                        onclick="window.location.href = '../../../myProfile/'">Mój profil
                </button>
                <button class="btn btn-primary"
                        onclick="window.location.href = '../../../../logout'">Wyloguj się
                </button>
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
            <span class="bold">Użytkownik:</span><br> ${question.user}
        </p>
    </div>
    <h1 id="header">Lista odpowiedzi:</h1>
    <div id="toolbar" class="clearfix">
        <button class="btn btn-primary"
                onclick="window.location.href = '../..'">Wróć do listy pytań
        </button>
        <button class="btn btn-primary"
                onclick="window.location.href = 'new/'">Dodaj odpowiedź
        </button>
        <button class="btn btn-primary"
                onclick="window.location.href =
                        'file-export/?fileType=PDF&keyword=${filter.keyword}&sortField=${filter.sortField}&sortDirection=${filter.sortDirection}&pageNo=${filter.pageNo}&pageSize=${filter.pageSize}'">
                        Eksportuj do PDF
        </button>
        <button class="btn btn-primary"
                onclick="window.location.href =
                        'file-export/?fileType=EXCEL&keyword=${filter.keyword}&sortField=${filter.sortField}&sortDirection=${filter.sortDirection}&pageNo=${filter.pageNo}&pageSize=${filter.pageSize}'">
                        Eksportuj do Excel
        </button>
        <form:form action="" method="GET" class="float-end">
            <input id="search-input" class="form-control search-input" type="search" placeholder="Szukaj" name="keyword" value="${filter.keyword}" />
            <input type="hidden" name="pageNo" value="1" />
            <input type="hidden" name="pageSize" value="${filter.pageSize}" />
            <input type="hidden" name="sortField" value="${filter.sortField}" />
            <input type="hidden" name="sortDirection" value="${filter.sortDirection}" />
        </form:form>
    </div>
    <c:choose>
        <c:when test="${empty answerList}">
            <p class="empty">Brak odpowiedzi na pytanie</p>
        </c:when>
        <c:otherwise>
            <table class="table table-bordered table-hover" aria-describedby="header">

                <thead class="table-dark">
                <tr>

                    <c:url var="sortLinkId" value="/app/questions/${questionId}/answers/">
                        <c:param name="keyword" value="${filter.keyword}" />
                        <c:param name="pageNo" value="${filter.pageNo}" />
                        <c:param name="pageSize" value="${filter.pageSize}" />
                        <c:param name="sortField" value="ID" />
                        <c:param name="sortDirection" value="${reverseSortDir}" />
                    </c:url>
                    <c:url var="sortLinkText" value="/app/questions/${questionId}/answers/">
                        <c:param name="keyword" value="${filter.keyword}" />
                        <c:param name="pageNo" value="${filter.pageNo}" />
                        <c:param name="pageSize" value="${filter.pageSize}" />
                        <c:param name="sortField" value="TEXT" />
                        <c:param name="sortDirection" value="${reverseSortDir}" />
                    </c:url>
                    <c:url var="sortLinkCreatedAt" value="/app/questions/${questionId}/answers/">
                        <c:param name="keyword" value="${filter.keyword}" />
                        <c:param name="pageNo" value="${filter.pageNo}" />
                        <c:param name="pageSize" value="${filter.pageSize}" />
                        <c:param name="sortField" value="CREATED_AT" />
                        <c:param name="sortDirection" value="${reverseSortDir}" />
                    </c:url>
                    <c:url var="sortLinkUpdatedAt" value="/app/questions/${questionId}/answers/">
                        <c:param name="keyword" value="${filter.keyword}" />
                        <c:param name="pageNo" value="${filter.pageNo}" />
                        <c:param name="pageSize" value="${filter.pageSize}" />
                        <c:param name="sortField" value="UPDATED_AT" />
                        <c:param name="sortDirection" value="${reverseSortDir}" />
                    </c:url>
                    <c:url var="sortLinkUser" value="/app/questions/${questionId}/answers/">
                        <c:param name="keyword" value="${filter.keyword}" />
                        <c:param name="pageNo" value="${filter.pageNo}" />
                        <c:param name="pageSize" value="${filter.pageSize}" />
                        <c:param name="sortField" value="USER" />
                        <c:param name="sortDirection" value="${reverseSortDir}" />
                    </c:url>

                    <th scope="col"><a class="sortablelink" href="${sortLinkId}">Id <i class="fa fa-fw fa-sort" aria-hidden="true"></i></a></th>
                    <th class="content-column" scope="col"><a class="sortablelink" href="${sortLinkText}">Treść odpowiedzi <i class="fa fa-fw fa-sort" aria-hidden="true"></i></a></th>
                    <th scope="col"><a class="sortablelink" href="${sortLinkCreatedAt}">Data utworzenia <i class="fa fa-fw fa-sort" aria-hidden="true"></i></a></th>
                    <th scope="col"><a class="sortablelink" href="${sortLinkUpdatedAt}">Data modyfikacji <i class="fa fa-fw fa-sort" aria-hidden="true"></i></a></th>
                    <th scope="col"><a class="sortablelink" href="${sortLinkUser}">Użytkownik <i class="fa fa-fw fa-sort" aria-hidden="true"></i></a></th>
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
                        <td>${answer.user}</td>
                        <td>
                            <div class="btn-group-vertical">
                                <button type="button" class="btn btn-secondary btn-sm"
                                        onclick="window.location.href = '${answer.id}/edit/'">Edytuj
                                </button>
                                <button type="button" class="btn btn-secondary btn-sm"
                                        onclick="window.location.href = '${answer.id}/delete/'">Usuń
                                </button>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

            <div class="paginationtoolbar clearfix">
                <div class="float-start pagination-detail">
                    Liczba rekordów: <span class="bold"> ${totalItems}</span>. Strona
                        <span class="bold">${filter.pageNo} z ${totalPages}</span>.
                    <div class="page-list">
                        <form:form action="" method="GET" id="pageSizeSelectForm" >
                            <input type="hidden" name="keyword" value="${filter.keyword}" />
                            <input type="hidden" name="pageNo" value="1" />
                            <input type="hidden" name="sortField" value="${filter.sortField}" />
                            <input type="hidden" name="sortDirection" value="${filter.sortDirection}" />
                        </form:form>
                        <select class="form-select" name="pageSize" form="pageSizeSelectForm" onchange="this.form.submit()"
                                aria-label="Page size">
                            <option selected hidden value="${filter.pageSize}">${filter.pageSize}</option>
                            <option value="3">3</option>
                            <option value="5">5</option>
                            <option value="10">10</option>
                            <option value="20">20</option>
                            <option value="50">50</option>
                            <option value="100">100</option>
                            <option value="500">500</option>
                            <option value="1000">1000</option>
                            <option value="5000">5000</option>
                        </select>
                        rekordów na stronę
                    </div>
                </div>
                <div class="float-end pagination">
                    <c:url var="searchUri" value="/app/questions/${questionId}/answers/">
                        <c:param name="keyword" value="${filter.keyword}" />
                        <c:param name="pageNo" value="xxx" />
                        <c:param name="pageSize" value="${filter.pageSize}" />
                        <c:param name="sortField" value="${filter.sortField}" />
                        <c:param name="sortDirection" value="${filter.sortDirection}" />
                    </c:url>
                    <paginator:display maxLinks="5" currPage="${filter.pageNo}" totalPages="${totalPages}" uri="${searchUri}" />
                </div>
            </div>

        </c:otherwise>
    </c:choose>
</div>

</body>
</html>
