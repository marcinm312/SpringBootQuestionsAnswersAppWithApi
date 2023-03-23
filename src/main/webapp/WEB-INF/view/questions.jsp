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

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.3.0/css/all.min.css"
        integrity="sha512-SzlrxWUlpfuzQ+pcUCosxcglQRNAq/DZjVsC0lE40xsADsfeQoEypE+enwcOiGjk/bSuGGKHEyjSoQ1zVisanQ=="
        crossorigin="anonymous" referrerpolicy="no-referrer" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css"
        integrity="sha384-xOolHFLEh07PJGoPkLv1IbcEPTNtaed2xpHsD9ESMhqIYd0nLMwNLD69Npy4HI+N" crossorigin="anonymous">
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
                onclick="window.location.href = 'new/'">Utwórz pytanie
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
        <form:form action="" method="GET" class="float-right">
            <input class="form-control search-input" type="search" placeholder="Szukaj" name="keyword" value="${filter.keyword}" />
            <input type="hidden" name="pageNo" value="1" />
            <input type="hidden" name="pageSize" value="${filter.pageSize}" />
            <input type="hidden" name="sortField" value="${filter.sortField}" />
            <input type="hidden" name="sortDirection" value="${filter.sortDirection}" />
        </form:form>
    </div>
    <c:choose>
        <c:when test="${empty questionList}">
            <p class="empty">Brak pytań</p>
        </c:when>
        <c:otherwise>
            <table class="table table-bordered" aria-describedby="header">

                <thead class="thead-dark">
                <tr>

                    <c:url var="sortLinkId" value="/app/questions/">
                        <c:param name="keyword" value="${filter.keyword}" />
                        <c:param name="pageNo" value="${filter.pageNo}" />
                        <c:param name="pageSize" value="${filter.pageSize}" />
                        <c:param name="sortField" value="ID" />
                        <c:param name="sortDirection" value="${reverseSortDir}" />
                    </c:url>
                    <c:url var="sortLinkTitle" value="/app/questions/">
                        <c:param name="keyword" value="${filter.keyword}" />
                        <c:param name="pageNo" value="${filter.pageNo}" />
                        <c:param name="pageSize" value="${filter.pageSize}" />
                        <c:param name="sortField" value="TITLE" />
                        <c:param name="sortDirection" value="${reverseSortDir}" />
                    </c:url>
                    <c:url var="sortLinkDescription" value="/app/questions/">
                        <c:param name="keyword" value="${filter.keyword}" />
                        <c:param name="pageNo" value="${filter.pageNo}" />
                        <c:param name="pageSize" value="${filter.pageSize}" />
                        <c:param name="sortField" value="DESCRIPTION" />
                        <c:param name="sortDirection" value="${reverseSortDir}" />
                    </c:url>
                    <c:url var="sortLinkCreatedAt" value="/app/questions/">
                        <c:param name="keyword" value="${filter.keyword}" />
                        <c:param name="pageNo" value="${filter.pageNo}" />
                        <c:param name="pageSize" value="${filter.pageSize}" />
                        <c:param name="sortField" value="CREATED_AT" />
                        <c:param name="sortDirection" value="${reverseSortDir}" />
                    </c:url>
                    <c:url var="sortLinkUpdatedAt" value="/app/questions/">
                        <c:param name="keyword" value="${filter.keyword}" />
                        <c:param name="pageNo" value="${filter.pageNo}" />
                        <c:param name="pageSize" value="${filter.pageSize}" />
                        <c:param name="sortField" value="UPDATED_AT" />
                        <c:param name="sortDirection" value="${reverseSortDir}" />
                    </c:url>
                    <c:url var="sortLinkUser" value="/app/questions/">
                        <c:param name="keyword" value="${filter.keyword}" />
                        <c:param name="pageNo" value="${filter.pageNo}" />
                        <c:param name="pageSize" value="${filter.pageSize}" />
                        <c:param name="sortField" value="USER" />
                        <c:param name="sortDirection" value="${reverseSortDir}" />
                    </c:url>

                    <th scope="col"><a class="sortablelink" href="${sortLinkId}">Id <i class="fa fa-fw fa-sort" aria-hidden="true"></i></a></th>
                    <th scope="col"><a class="sortablelink" href="${sortLinkTitle}">Tytuł <i class="fa fa-fw fa-sort" aria-hidden="true"></i></a></th>
                    <th scope="col"><a class="sortablelink" href="${sortLinkDescription}">Opis <i class="fa fa-fw fa-sort" aria-hidden="true"></i></a></th>
                    <th scope="col"><a class="sortablelink" href="${sortLinkCreatedAt}">Data utworzenia <i class="fa fa-fw fa-sort" aria-hidden="true"></i></a></th>
                    <th scope="col"><a class="sortablelink" href="${sortLinkUpdatedAt}">Data modyfikacji <i class="fa fa-fw fa-sort" aria-hidden="true"></i></a></th>
                    <th scope="col"><a class="sortablelink" href="${sortLinkUser}">Użytkownik <i class="fa fa-fw fa-sort" aria-hidden="true"></i></a></th>
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

            <div class="paginationtoolbar clearfix">
                <div class="float-left pagination-detail">
                    Liczba rekordów: <span class="bold"> ${totalItems}</span>. Strona
                        <span class="bold">${filter.pageNo} z ${totalPages}</span>.
                    <div class="page-list">
                        <form:form action="" method="GET" id="pageSizeSelectForm" >
                            <input type="hidden" name="keyword" value="${filter.keyword}" />
                            <input type="hidden" name="pageNo" value="1" />
                            <input type="hidden" name="sortField" value="${filter.sortField}" />
                            <input type="hidden" name="sortDirection" value="${filter.sortDirection}" />
                        </form:form>
                        <select class="custom-select" name="pageSize" form="pageSizeSelectForm" onchange="this.form.submit()">
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
                <div class="float-right pagination">
                    <c:url var="searchUri" value="/app/questions/">
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
