package com.github.ljtfreitas.restify.http.contract;

import static org.junit.Assert.*;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.ContentType;

public class ContentTypeTest {

	@Test
	public void shouldParseContentTypeWithOneParameter() {
		String source = "application/json; charset=UTF-8";

		ContentType contentType = ContentType.of(source);

		assertEquals("application/json", contentType.name());

		assertFalse(contentType.parameters().empty());
		assertEquals("UTF-8", contentType.parameter("charset").get());

		assertEquals(source, contentType.toString());
	}

	@Test
	public void shouldParseContentTypeWithMultiplesParameters() {
		String source = "multipart/form-data; charset=UTF-8; boundary=abc1234";

		ContentType contentType = ContentType.of(source);

		assertEquals("multipart/form-data", contentType.name());

		assertFalse(contentType.parameters().empty());

		assertEquals("UTF-8", contentType.parameter("charset").get());
		assertEquals("abc1234", contentType.parameter("boundary").get());

		assertEquals(source, contentType.toString());
	}

	@Test
	public void shouldParseContentTypeWithoutParameters() {
		String source = "application/json";

		ContentType contentType = ContentType.of(source);

		assertEquals(source, contentType.name());

		assertTrue(contentType.parameters().empty());

		assertEquals(source, contentType.toString());
	}

	@Test
	public void shouldCreateNewContentTypeWhenParameterIsAdded() {
		ContentType contentType = ContentType.of("application/json");

		ContentType newContentType = contentType.newParameter("charset", "UTF-8");

		assertTrue(contentType.parameters().empty());

		assertFalse(newContentType.parameters().empty());
		assertEquals("application/json", newContentType.name());
		assertEquals("UTF-8", newContentType.parameter("charset").get());
		assertEquals("application/json; charset=UTF-8", newContentType.toString());
	}
}
