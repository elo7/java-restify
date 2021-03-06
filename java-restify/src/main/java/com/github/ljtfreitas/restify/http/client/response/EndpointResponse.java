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
package com.github.ljtfreitas.restify.http.client.response;

import com.github.ljtfreitas.restify.http.client.Headers;

public class EndpointResponse<T> {

	private final StatusCode statusCode;
	private final Headers headers;
	private final T body;

	public EndpointResponse(StatusCode statusCode, Headers headers, T body) {
		this.statusCode = statusCode;
		this.headers = headers;
		this.body = body;
	}

	public Headers headers() {
		return headers;
	}

	public StatusCode code() {
		return statusCode;
	}

	public T body() {
		return body;
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("EndpointResponse: [")
				.append("HTTP Status code: ")
					.append(statusCode)
				.append(", ")
				.append("Headers: ")
					.append(headers)
				.append(", ")
				.append("Body: ")
					.append(body)
			.append("]");

		return report.toString();
	}

	public static <T> EndpointResponse<T> empty(StatusCode statusCode, Headers headers) {
		return new EndpointResponse<T>(statusCode, headers, null);
	}
}
