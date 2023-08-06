package pl.marcinm312.springquestionsanswers.user.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.marcinm312.springquestionsanswers.shared.model.AuditModel;
import pl.marcinm312.springquestionsanswers.shared.model.CommonEntity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "users")
public class UserEntity extends AuditModel implements UserDetails, CommonEntity {

	@Id
	@GeneratedValue(generator = "user_generator")
	@SequenceGenerator(name = "user_generator", sequenceName = "user_sequence", initialValue = 1000)
	private Long id;

	@Column(unique = true)
	private String username;

	private String password;

	@Enumerated(EnumType.STRING)
	private Role role;

	@Column(name = "is_enabled")
	private boolean enabled;

	private String email;

	private LocalDateTime timeOfSessionExpiration;
	private LocalDateTime changePasswordDate;


	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singleton(new SimpleGrantedAuthority(role.name()));
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	public LocalDateTime getDateToCompareInJwt() {
		List<LocalDateTime> dates = new ArrayList<>();
		dates.add(getTimeOfSessionExpiration());
		dates.add(getChangePasswordDate());
		return Collections.max(dates);
	}

	@Override
	public String toString() {
		return "UserEntity{" +
				"id=" + id +
				", username='" + username + '\'' +
				", role='" + role + '\'' +
				", enabled=" + enabled +
				", timeOfSessionExpiration=" + timeOfSessionExpiration +
				", changePasswordDate=" + changePasswordDate +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof UserEntity user)) return false;

		return getUsername() != null ? getUsername().equals(user.getUsername()) : user.getUsername() == null;
	}

	@Override
	public int hashCode() {
		return getUsername() != null ? getUsername().hashCode() : 0;
	}
}
