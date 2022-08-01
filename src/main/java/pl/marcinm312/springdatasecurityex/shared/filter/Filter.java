package pl.marcinm312.springdatasecurityex.shared.filter;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;

@AllArgsConstructor
public class Filter {

	private final String keyword;
	private final Integer pageNo;
	private final Integer pageSize;
	private final SortField sortField;
	private final Sort.Direction sortDirection;


	public Filter(String keyword, SortField sortField, Sort.Direction sortDirection) {
		this.keyword = keyword;
		this.sortField = sortField;
		this.sortDirection = sortDirection;
		this.pageNo = null;
		this.pageSize = null;
	}

	public String getKeyword() {
		if (keyword == null) {
			return "";
		}
		return keyword.toLowerCase().trim();
	}

	public Integer getPageNo() {
		if (pageNo == null || pageNo < 1) {
			return 1;
		}
		return pageNo;
	}

	public Integer getPageSize() {
		if (pageSize == null || pageSize < 1) {
			return 5;
		}
		return pageSize;
	}

	public SortField getSortField() {
		if (sortField == null) {
			return SortField.ID;
		}
		return sortField;
	}

	public Sort.Direction getSortDirection() {
		if (sortDirection == null) {
			return Sort.Direction.DESC;
		}
		return sortDirection;
	}

	public boolean isKeywordEmpty() {
		return getKeyword() == null || getKeyword().isEmpty();
	}

	public static SortField checkQuestionsSortField(SortField sortField) {
		if (sortField == SortField.TEXT) {
			sortField = SortField.ID;
		}
		return sortField;
	}

	public static SortField checkAnswersSortField(SortField sortField) {
		if (sortField == SortField.TITLE || sortField == SortField.DESCRIPTION) {
			sortField = SortField.ID;
		}
		return sortField;
	}
}
