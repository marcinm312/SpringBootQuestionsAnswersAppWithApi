package pl.marcinm312.springdatasecurityex.config.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

public class JwtCreator {

	private JwtCreator() {

	}

	public static String createJWT(String subject, long expirationTime, byte[] secretBytes) {

		return JWT.create()
				.withSubject(subject)
				.withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
				.sign(Algorithm.HMAC256(secretBytes));
	}
}
