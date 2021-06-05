package pl.marcinm312.springdatasecurityex.model.answer;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pl.marcinm312.springdatasecurityex.model.AuditModel;
import pl.marcinm312.springdatasecurityex.model.question.Question;
import pl.marcinm312.springdatasecurityex.model.user.User;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "answers")
public class Answer extends AuditModel {

	@Id
	@GeneratedValue(generator = "answer_generator")
	@SequenceGenerator(name = "answer_generator", sequenceName = "answer_sequence", initialValue = 1000)
	private Long id;

	@Column(columnDefinition = "text")
	private String text;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Question question;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User user;

	public Answer() {
	}

	public Answer(String text) {
		this.text = text;
	}

	public Answer(Long id, String text, Question question, User user, Date createdAt, Date updatedAt) {
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

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Answer [id=" + id + ", text=" + text + ", question=" + question + ", user=" + user + "]";
	}
}
