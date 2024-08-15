package pl.marcinm312.springquestionsanswers.user.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import pl.marcinm312.springquestionsanswers.shared.model.AuditModel;
import pl.marcinm312.springquestionsanswers.shared.model.CommonEntityWithUser;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "activation_tokens")
public class ActivationTokenEntity extends AuditModel implements CommonEntityWithUser {

	@Id
	@GeneratedValue(generator = "activation_token_generator")
	@SequenceGenerator(name = "activation_token_generator", sequenceName = "activation_token_sequence",
			initialValue = 1000, allocationSize = 1)
	private Long id;

	private String value;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
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
