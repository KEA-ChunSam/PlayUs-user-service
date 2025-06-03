package com.playus.userservice.domain.oauth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class CookieUtils {

	public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();

		if (cookies == null) {
			return Optional.empty();
		}

		return Arrays.stream(cookies)
			.filter(cookie -> cookie.getName().equals(name))
			.findFirst();
	}

	public static void addCookie(HttpServletResponse response, String name, String value, boolean cookieSecure,
		String cookieSameSite, int maxAge, String domain) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setSecure(cookieSecure);
		cookie.setAttribute("SameSite", cookieSameSite);
		cookie.setMaxAge(maxAge);
		cookie.setDomain(domain);
		response.addCookie(cookie);
	}

	public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
		Cookie[] cookies = request.getCookies();

		if (cookies != null) {
			Arrays.stream(cookies)
				.filter(cookie -> cookie.getName().equals(name))
				.findFirst()
				.ifPresent(cookie -> {
					cookie.setValue("");
					cookie.setPath("/");
					cookie.setMaxAge(0);
					response.addCookie(cookie);
				});
		}
	}

	public static String serialize(Object obj) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
			 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(obj);
			return Base64.getUrlEncoder().encodeToString(baos.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException("Failed to serialize object", e);
		}
	}

	public static <T> T deserialize(String str, Class<T> cls) {
		byte[] data = Base64.getUrlDecoder().decode(str);
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
			Object obj = ois.readObject();
			return cls.cast(obj);
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException("Failed to deserialize object", e);
		}
	}
}
