package pl.marcinm312.springquestionsanswers.user.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import pl.marcinm312.springquestionsanswers.shared.model.AuditModel;
import pl.marcinm312.springquestionsanswers.shared.model.CommonEntityWithUser;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "tokens")
public class TokenEntity extends AuditModel implements CommonEntityWithUser {

	@Id
	@GeneratedValue(generator = "token_id_generator")
	@SequenceGenerator(name = "token_id_generator", sequenceName = "token_id_sequence", initialValue = 1000)
	private Long id;

	@NotBlank
	private String value;

	@OneToOne
	private UserEntity user;

	public TokenEntity(String value, UserEntity user) {
		this.value = value;
		this.user = user;
	}

	@Override
	public String toString() {
		return "TokenEntity [id=" + id + ", value=" + value + ", user=" + user + "]";
	}
}
