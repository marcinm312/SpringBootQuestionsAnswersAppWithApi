package pl.marcinm312.springdatasecurityex.user.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.marcinm312.springdatasecurityex.shared.model.AuditModel;

import javax.persistence.*;
import java.util.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity extends AuditModel implements UserDetails {

	@Id
	@GeneratedValue(generator = "user_generator")
	@SequenceGenerator(name = "user_generator", sequenceName = "user_sequence", initialValue = 1000)
	private Long id;

	@Column(unique = true)
	private String username;

	private String password;
	private String role;
	private boolean isEnabled;
	private String email;

	private Date timeOfSessionExpiration;
	private Date changePasswordDate;


	public UserEntity(String username, String password, String email) {
		this.username = username;
		this.password = password;
		this.email = email;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singleton(new SimpleGrantedAuthority(role));
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

	public Date getDateToCompareInJwt() {
		List<Date> dates = new ArrayList<>();
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
				", isEnabled=" + isEnabled +
				", timeOfSessionExpiration=" + timeOfSessionExpiration +
				", changePasswordDate=" + changePasswordDate +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof UserEntity)) return false;

		UserEntity user = (UserEntity) o;

		return getUsername() != null ? getUsername().equals(user.getUsername()) : user.getUsername() == null;
	}

	@Override
	public int hashCode() {
		return getUsername() != null ? getUsername().hashCode() : 0;
	}
}
