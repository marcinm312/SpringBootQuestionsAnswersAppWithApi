package pl.marcinm312.springdatasecurityex.question.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pl.marcinm312.springdatasecurityex.shared.model.AuditModel;
import pl.marcinm312.springdatasecurityex.shared.model.CommonEntityWithUser;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

import javax.persistence.*;

@NoArgsConstructor
@ToString
@Getter
@Setter
@SuperBuilder
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
    @ToString.Exclude
    private UserEntity user;

    public QuestionEntity(String title, String description, UserEntity user) {
        this.title = title;
        this.description = description;
        this.user = user;
    }
}
