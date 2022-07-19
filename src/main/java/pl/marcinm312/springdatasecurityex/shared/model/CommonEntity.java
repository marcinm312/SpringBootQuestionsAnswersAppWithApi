package pl.marcinm312.springdatasecurityex.shared.model;

import java.util.Date;

public interface CommonEntity {

	Long getId();
	void setId(Long id);
	Date getCreatedAt();
	void setCreatedAt(Date createdAt);
	Date getUpdatedAt();
	void setUpdatedAt(Date updatedAt);
}
