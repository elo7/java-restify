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
package com.github.ljtfreitas.restify.http.client.request.jdk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Collection;

import com.github.ljtfreitas.restify.http.RestifyHttpException;
import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;

public class JdkHttpClientRequest implements HttpClientRequest {

	private final HttpURLConnection connection;
	private final Charset charset;
	private final Headers headers;

	public JdkHttpClientRequest(HttpURLConnection connection, Charset charset, Headers headers) {
		this.connection = connection;
		this.charset = charset;
		this.headers = new JdkHttpClientHeadersDecorator(connection, headers);
	}

	@Override
	public HttpResponseMessage execute() {
		try {
			connection.connect();

			return responseOf(connection);

		} catch (IOException e) {
			throw new RestifyHttpException(e);
		}
	}

	private JdkHttpClientResponse responseOf(HttpURLConnection connection) throws IOException {
		StatusCode statusCode = StatusCode.of(connection.getResponseCode());

		Headers headers = new Headers();

		connection.getHeaderFields().entrySet().stream()
			.filter(e -> e.getKey() != null && !e.getKey().equals(""))
				.forEach(e -> headers.put(e.getKey(), e.getValue()));

		InputStream stream = connection.getErrorStream() == null ? connection.getInputStream() : connection.getErrorStream();

		return new JdkHttpClientResponse(statusCode, headers, stream, connection);
	}

	@Override
	public OutputStream output() {
		try {
			return connection.getOutputStream();
		} catch (IOException e) {
			throw new RestifyHttpException(e);
		}
	}

	@Override
	public Charset charset() {
		return charset;
	}

	@Override
	public Headers headers() {
		return headers;
	}

	private class JdkHttpClientHeadersDecorator extends Headers {

		private final Headers headers;
		private final HttpURLConnection connection;

		public JdkHttpClientHeadersDecorator(HttpURLConnection connection, Headers headers) {
			super(headers);
			this.connection = connection;
			this.headers = headers;
			apply();
		}

		private void apply() {
			headers.all().forEach(h -> connection.setRequestProperty(h.name(), h.value()));
		}

		@Override
		public void put(String name, String value) {
			super.put(name, value);
			connection.setRequestProperty(name, value);
		}

		@Override
		public void put(String name, Collection<String> values) {
			super.put(name, values);
			values.forEach(value -> connection.setRequestProperty(name, value));
		}

		@Override
		public void replace(String name, String value) {
			super.replace(name, value);
			connection.setRequestProperty(name, value);
		}
	}
}
