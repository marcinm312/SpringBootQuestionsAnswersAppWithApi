package pl.marcinm312.springdatasecurityex.shared.model;

import java.util.Date;

public interface CommonEntity {

	Long getId();
	Date getCreatedAt();
	Date getUpdatedAt();
}
