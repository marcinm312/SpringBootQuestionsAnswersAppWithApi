package pl.marcinm312.springdatasecurityex.shared.model;

import java.util.List;

public class ListPage<T extends CommonsDTOFields> {

	private final List<T> itemsList;
	private final int totalPages;
	private final long totalElements;

	public ListPage(List<T> itemsList, int totalPages, long totalElements) {
		this.itemsList = itemsList;
		this.totalPages = totalPages;
		this.totalElements = totalElements;
	}

	public List<T> getItemsList() {
		return itemsList;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public long getTotalElements() {
		return totalElements;
	}
}
