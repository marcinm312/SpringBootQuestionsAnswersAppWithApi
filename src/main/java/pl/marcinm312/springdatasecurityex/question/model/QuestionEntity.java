package pl.marcinm312.springdatasecurityex.question.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pl.marcinm312.springdatasecurityex.shared.model.AuditModel;
import pl.marcinm312.springdatasecurityex.shared.model.CommonEntityWithUser;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

import javax.persistence.*;
import java.util.Date;

@NoArgsConstructor
@ToString
@Getter
@Setter
@Entity
@Table(name = "questions")
public class QuestionEntity extends AuditModel implements CommonEntityWithUser {

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
}
