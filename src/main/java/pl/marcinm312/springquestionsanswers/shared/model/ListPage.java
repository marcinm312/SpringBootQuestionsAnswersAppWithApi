package pl.marcinm312.springquestionsanswers.shared.model;

import java.util.List;

public record ListPage<T extends CommonsDTOFields>(List<T> itemsList, int totalPages, long totalElements) {

}
