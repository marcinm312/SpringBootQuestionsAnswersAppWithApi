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
@Table(name = "activation_tokens")
public class ActivationTokenEntity extends AuditModel implements CommonEntityWithUser {

	@Id
	@GeneratedValue(generator = "activation_token_generator")
	@SequenceGenerator(name = "activation_token_generator", sequenceName = "activation_token_sequence", initialValue = 1000)
	private Long id;

	@NotBlank
	private String value;

	@OneToOne
	private UserEntity user;

	public ActivationTokenEntity(String value, UserEntity user) {
		this.value = value;
		this.user = user;
	}

	@Override
	public String toString() {
		return "ActivationTokenEntity [id=" + id + ", value=" + value + ", user=" + user + "]";
	}
}
