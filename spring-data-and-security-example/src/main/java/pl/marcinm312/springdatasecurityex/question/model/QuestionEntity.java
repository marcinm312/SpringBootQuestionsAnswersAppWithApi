package pl.marcinm312.springdatasecurityex.question.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pl.marcinm312.springdatasecurityex.shared.model.AuditModel;
import pl.marcinm312.springdatasecurityex.shared.model.EntityWithUser;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "questions")
public class QuestionEntity extends AuditModel implements EntityWithUser {

    @Id
    @GeneratedValue(generator = "question_generator")
    @SequenceGenerator(name = "question_generator", sequenceName = "question_sequence", initialValue = 1000)
    private Long id;

    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    public QuestionEntity() {
    }

    public QuestionEntity(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public QuestionEntity(Long id, String title, String description, UserEntity user, Date createdAt, Date updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "QuestionEntity [id=" + id + ", title=" + title + ", description=" + description + ", user=" + user + "]";
    }
}
