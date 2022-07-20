package pl.marcinm312.springdatasecurityex.shared.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class ListPage<T extends CommonsDTOFields> {

	private final List<T> itemsList;
	private final int totalPages;
	private final long totalElements;
}
