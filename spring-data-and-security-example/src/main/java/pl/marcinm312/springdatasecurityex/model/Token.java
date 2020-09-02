package pl.marcinm312.springdatasecurityex.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "tokens")
public class Token extends AuditModel {

	@Id
	@GeneratedValue(generator = "token_id_generator")
	@SequenceGenerator(name = "token_id_generator", sequenceName = "token_id_sequence", initialValue = 1000)
	private Long id;

	@NotBlank
	private String value;

	@OneToOne
	private User user;

	public Token() {
	}

	public Token(Long id, String value, User user) {
		this.id = id;
		this.value = value;
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Token [id=" + id + ", value=" + value + ", user=" + user + "]";
	}
}
