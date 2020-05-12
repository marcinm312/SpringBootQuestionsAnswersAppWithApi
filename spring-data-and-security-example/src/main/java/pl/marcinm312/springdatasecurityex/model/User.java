package pl.marcinm312.springdatasecurityex.model;

import java.util.Collection;
import java.util.Collections;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
public class User extends AuditModel implements UserDetails {

	@Id
	@GeneratedValue(generator = "user_generator")
	@SequenceGenerator(name = "user_generator", sequenceName = "user_sequence", initialValue = 1000)
	private Long id;

	@Column(unique = true)
	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 3, max = 50, message = "Pole to musi zawierać od 3 do 50 znaków")
	private String username;

	@Transient
	private String currentPassword;

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 6, message = "Pole to musi zawierać minimum 6 znaków")
	private String password;

	@Transient
	private String confirmPassword;

	private String role;
	private boolean isEnabled;

	@NotBlank(message = "Pole to musi być wypełnione!")
	private String firstName;

	@NotBlank(message = "Pole to musi być wypełnione!")
	private String lastName;

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Email(message = "Niepoprawny adres email!")
	private String email;

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

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
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

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

}
