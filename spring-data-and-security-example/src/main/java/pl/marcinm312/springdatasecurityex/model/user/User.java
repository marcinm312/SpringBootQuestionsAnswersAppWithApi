package pl.marcinm312.springdatasecurityex.model.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.marcinm312.springdatasecurityex.model.AuditModel;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "users")
public class User extends AuditModel implements UserDetails {

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

	public User() {

	}

	public User(String username, String password, String email) {
		this.username = username;
		this.password = password;
		this.email = email;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singleton(new SimpleGrantedAuthority(role));
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
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

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	public Date getTimeOfSessionExpiration() {
		return timeOfSessionExpiration;
	}

	public void setTimeOfSessionExpiration(Date timeOfSessionExpiration) {
		this.timeOfSessionExpiration = timeOfSessionExpiration;
	}

	public Date getChangePasswordDate() {
		return changePasswordDate;
	}

	public void setChangePasswordDate(Date changePasswordDate) {
		this.changePasswordDate = changePasswordDate;
	}

	public Date getDateToCompareInJwt() {
		List<Date> dates = new ArrayList<>();
		dates.add(getTimeOfSessionExpiration());
		dates.add(getChangePasswordDate());
		return Collections.max(dates);
	}

	@Override
	public String toString() {
		return "User{" +
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
		if (!(o instanceof User)) return false;

		User user = (User) o;

		return getUsername() != null ? getUsername().equals(user.getUsername()) : user.getUsername() == null;
	}

	@Override
	public int hashCode() {
		return getUsername() != null ? getUsername().hashCode() : 0;
	}
}
