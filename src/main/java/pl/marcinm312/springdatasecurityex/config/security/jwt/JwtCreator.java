package pl.marcinm312.springdatasecurityex.config.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtCreator {

	public static String createJWT(String subject, long expirationTime, byte[] secretBytes) {

		long currentTime = System.currentTimeMillis();
		return JWT.create()
				.withSubject(subject)
				.withExpiresAt(new Date(currentTime + expirationTime))
				.withIssuedAt(new Date(currentTime))
				.sign(Algorithm.HMAC256(secretBytes));
	}
}
