package pl.marcinm312.springquestionsanswers.shared.model;

import java.util.Date;

public interface CommonEntity {

	Long getId();
	Date getCreatedAt();
	Date getUpdatedAt();
}
