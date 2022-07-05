package pl.marcinm312.springdatasecurityex.answer.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pl.marcinm312.springdatasecurityex.question.model.QuestionEntity;
import pl.marcinm312.springdatasecurityex.shared.model.AuditModel;
import pl.marcinm312.springdatasecurityex.shared.model.EntityWithUser;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "answers")
public class AnswerEntity extends AuditModel implements EntityWithUser {

	@Id
	@GeneratedValue(generator = "answer_generator")
	@SequenceGenerator(name = "answer_generator", sequenceName = "answer_sequence", initialValue = 1000)
	private Long id;

	@Column(columnDefinition = "text")
	private String text;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private QuestionEntity question;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private UserEntity user;

	public AnswerEntity() {
	}

	public AnswerEntity(String text) {
		this.text = text;
	}

	public AnswerEntity(String text, UserEntity user) {
		this.text = text;
		this.user = user;
	}

	public AnswerEntity(Long id, String text, QuestionEntity question, UserEntity user, Date createdAt, Date updatedAt) {
		this.id = id;
		this.text = text;
		this.question = question;
		this.user = user;
		this.setCreatedAt(createdAt);
		this.setUpdatedAt(updatedAt);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public QuestionEntity getQuestion() {
		return question;
	}

	public void setQuestion(QuestionEntity question) {
		this.question = question;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "AnswerEntity [id=" + id + ", text=" + text + ", question=" + question + ", user=" + user + "]";
	}
}
