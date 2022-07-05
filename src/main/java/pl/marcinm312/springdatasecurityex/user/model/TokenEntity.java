package pl.marcinm312.springdatasecurityex.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.marcinm312.springdatasecurityex.shared.model.AuditModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tokens")
public class TokenEntity extends AuditModel {

	@Id
	@GeneratedValue(generator = "token_id_generator")
	@SequenceGenerator(name = "token_id_generator", sequenceName = "token_id_sequence", initialValue = 1000)
	private Long id;

	@NotBlank
	private String value;

	@OneToOne
	private UserEntity user;


	@Override
	public String toString() {
		return "TokenEntity [id=" + id + ", value=" + value + ", user=" + user + "]";
	}
}
