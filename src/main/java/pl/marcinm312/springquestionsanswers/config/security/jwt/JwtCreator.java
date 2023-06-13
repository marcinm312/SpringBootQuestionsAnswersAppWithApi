package pl.marcinm312.springquestionsanswers.config.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtCreator {

	public static String createJWT(String subject, long minutesToExpire, byte[] secretBytes) {

		Instant currentDate = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant();
		Instant expirationDate = LocalDateTime.now().plusMinutes(minutesToExpire).atZone(ZoneId.systemDefault()).toInstant();

		return JWT.create()
				.withSubject(subject)
				.withExpiresAt(expirationDate)
				.withIssuedAt(currentDate)
				.sign(Algorithm.HMAC256(secretBytes));
	}
}
