package pl.marcinm312.springdatasecurityex.shared.pagination;

import org.springframework.data.domain.Sort;

public class Filter {

	private final String keyword;
	private final Integer pageNo;
	private final Integer pageSize;
	private final String sortField;
	private final Sort.Direction sortDirection;

	public Filter(String keyword, Integer pageNo, Integer pageSize, String sortField, Sort.Direction sortDirection) {
		this.keyword = keyword;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.sortField = sortField;
		this.sortDirection = sortDirection;
	}

	public String getKeyword() {
		if (keyword == null) {
			return "";
		}
		return keyword.toLowerCase();
	}

	public Integer getPageNo() {
		if (pageNo == null || pageNo < 1) {
			return 1;
		}
		return pageNo;
	}

	public Integer getPageSize() {
		if (pageSize == null || pageSize < 0) {
			return 5;
		}
		return pageSize;
	}

	public String getSortField() {
		if (sortField == null || sortField.isEmpty()) {
			return "id";
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
		return keyword == null || keyword.isEmpty();
	}
}
