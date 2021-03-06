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
package com.github.ljtfreitas.restify.http.client.request.okhttp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.github.ljtfreitas.restify.http.RestifyHttpException;
import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpClientRequest implements HttpClientRequest {

	private final OkHttpClient okHttpClient;
	private final EndpointRequest endpointRequest;
	private final Charset charset;

	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

	public OkHttpClientRequest(OkHttpClient okHttpClient, EndpointRequest endpointRequest, Charset charset) {
		this.okHttpClient = okHttpClient;
		this.endpointRequest = endpointRequest;
		this.charset = charset;
	}

	@Override
	public OutputStream output() {
		return outputStream;
	}

	@Override
	public Charset charset() {
		return charset;
	}

	@Override
	public Headers headers() {
		return endpointRequest.headers();
	}

	@Override
	public HttpResponseMessage execute() throws RestifyHttpException {
		MediaType contentType = endpointRequest.headers().get("Content-Type").map(header -> MediaType.parse(header.value()))
				.orElse(null);

		byte[] content = outputStream.toByteArray();

		try {
			RequestBody body = (content.length > 0 ? RequestBody.create(contentType, content) : null);

			Request.Builder builder = new Request.Builder();

			builder.url(endpointRequest.endpoint().toURL())
				.method(endpointRequest.method(), body);

			endpointRequest.headers().all().forEach(h -> builder.addHeader(h.name(), h.value()));

			Request request = builder.build();

			return responseOf(okHttpClient.newCall(request).execute());

		} catch (IOException e) {
			throw new RestifyHttpException(e);
		}
	}

	private OkHttpClientResponse responseOf(Response response) {
		StatusCode statusCode = StatusCode.of(response.code());

		Headers headers = new Headers();
		response.headers().names().forEach(name -> headers.put(name, response.headers(name)));

		InputStream stream = response.body().byteStream();

		return new OkHttpClientResponse(statusCode, headers, stream, response);
	}

}
