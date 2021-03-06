/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.contract;

import static com.github.ljtfreitas.restify.http.util.Preconditions.isTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContentType {

	private final String type;
	private final ContentTypeParameters parameters;

	private ContentType(String type, ContentTypeParameters parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public String name() {
		return type;
	}

	public Optional<String> parameter(String name) {
		return parameters.get(name);
	}

	public ContentType newParameter(String name, String value) {
		ContentTypeParameters newParameters = parameters.put(name, value);
		return new ContentType(type, newParameters);
	}

	public ContentTypeParameters parameters() {
		return parameters;
	}

	public boolean is(String contentType) {
		return this.type.equals(contentType) || this.type.startsWith(contentType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ContentType) {
			ContentType that = (ContentType) obj;
			return this.type.equals(that.type) || this.type.startsWith(that.type);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(type);

		if (!parameters.empty()) {
			sb.append("; ").append(parameters.toString());
		}

		return sb.toString();
	}

	public static ContentType of(String type) {
		String[] parts = type.split(";");

		isTrue(parts.length >= 1, "Your Content-Type source is invalid: " + type);

		String[] parameters = Arrays.copyOfRange(parts, 1, parts.length);

		return new ContentType(parts[0], ContentTypeParameters.of(parameters));
	}

	public static class ContentTypeParameters {

		private final Map<String, String> parameters;

		private ContentTypeParameters(Map<String, String> parameters) {
			this.parameters = new LinkedHashMap<>(parameters);
		}

		private Optional<String> get(String name) {
			return Optional.ofNullable(parameters.get(name));
		}

		private ContentTypeParameters put(String name, String value) {
			ContentTypeParameters newParameters = new ContentTypeParameters(parameters);
			newParameters.parameters.put(name, value);
			return newParameters;
		}

		public boolean empty() {
			return parameters.isEmpty();
		}

		@Override
		public String toString() {
			return parameters.entrySet().stream()
					.map(p -> p.getKey() + "=" + p.getValue())
						.collect(Collectors.joining("; "));
		}

		private static ContentTypeParameters of(String[] parameters) {
			Map<String, String> mapOfParameters = new LinkedHashMap<>();

			Arrays.stream(parameters).map(p -> p.split("=")).filter(p -> p.length == 2)
					.forEach(p -> mapOfParameters.put(p[0].trim(), p[1].trim()));

			return new ContentTypeParameters(mapOfParameters);
		}
	}
}