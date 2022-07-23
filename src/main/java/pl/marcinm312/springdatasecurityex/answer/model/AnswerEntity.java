package pl.marcinm312.springdatasecurityex.answer.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pl.marcinm312.springdatasecurityex.question.model.QuestionEntity;
import pl.marcinm312.springdatasecurityex.shared.model.AuditModel;
import pl.marcinm312.springdatasecurityex.shared.model.CommonEntityWithUser;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "answers")
public class AnswerEntity extends AuditModel implements CommonEntityWithUser {

	@Id
	@GeneratedValue(generator = "answer_generator")
	@SequenceGenerator(name = "answer_generator", sequenceName = "answer_sequence", initialValue = 1000)
	private Long id;

	@Column(columnDefinition = "text")
	private String text;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@ToString.Exclude
	private QuestionEntity question;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@ToString.Exclude
	private UserEntity user;

	public AnswerEntity(String text, QuestionEntity question, UserEntity user) {
		this.text = text;
		this.question = question;
		this.user = user;
	}
}
