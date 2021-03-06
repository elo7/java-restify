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
package com.github.ljtfreitas.restify.http;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.ljtfreitas.restify.http.client.EndpointMethodExecutor;
import com.github.ljtfreitas.restify.http.client.authentication.Authentication;
import com.github.ljtfreitas.restify.http.client.call.EndpointCallFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableProvider;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutables;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallObjectExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.HeadersEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncCallbackEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.CallableEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.CompletableFutureCallbackEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.CompletableFutureEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.FutureEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.FutureTaskEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.OptionalEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.RunnableEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JsonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.ScalarMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextHtmlMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextPlainMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.xml.JaxbXmlMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.form.FormURLEncodedFormObjectMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.form.FormURLEncodedMapMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.form.FormURLEncodedParametersMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.form.multipart.MultipartFormFileObjectMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.form.multipart.MultipartFormMapMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.form.multipart.MultipartFormObjectMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.form.multipart.MultipartFormParametersMessageWriter;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestWriter;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.RestifyEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.AcceptHeaderEndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptorStack;
import com.github.ljtfreitas.restify.http.client.request.interceptor.authentication.AuthenticationEndpoinRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.jdk.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.response.DefaultEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.http.contract.ContentType;
import com.github.ljtfreitas.restify.http.contract.DefaultRestifyContract;
import com.github.ljtfreitas.restify.http.contract.RestifyContract;
import com.github.ljtfreitas.restify.http.contract.metadata.DefaultRestifyContractReader;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointTarget;
import com.github.ljtfreitas.restify.http.contract.metadata.RestifyContractReader;

public class RestifyProxyBuilder {

	private RestifyContractReader contractReader;

	private HttpClientRequestFactory httpClientRequestFactory;

	private EndpointRequestExecutor endpointRequestExecutor;

	private HttpMessageConvertersBuilder httpMessageConvertersBuilder = new HttpMessageConvertersBuilder(this);

	private EndpointRequestInterceptorsBuilder endpointRequestInterceptorsBuilder = new EndpointRequestInterceptorsBuilder(this);

	private EndpointCallExecutablesBuilder endpointMethodExecutablesBuilder = new EndpointCallExecutablesBuilder(this);

	private EndpointResponseErrorFallbackBuilder endpointResponseErrorFallbackBuilder = new EndpointResponseErrorFallbackBuilder(this);

	public RestifyProxyBuilder client(HttpClientRequestFactory httpClientRequestFactory) {
		this.httpClientRequestFactory = httpClientRequestFactory;
		return this;
	}

	public RestifyProxyBuilder contract(RestifyContractReader contract) {
		this.contractReader = contract;
		return this;
	}

	public RestifyProxyBuilder executor(EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequestExecutor = endpointRequestExecutor;
		return this;
	}

	public HttpMessageConvertersBuilder converters() {
		return this.httpMessageConvertersBuilder;
	}

	public RestifyProxyBuilder converters(HttpMessageConverter...converters) {
		this.httpMessageConvertersBuilder.add(converters);
		return this;
	}

	public EndpointRequestInterceptorsBuilder interceptors() {
		return this.endpointRequestInterceptorsBuilder;
	}

	public RestifyProxyBuilder interceptors(EndpointRequestInterceptor...interceptors) {
		this.endpointRequestInterceptorsBuilder.add(interceptors);
		return this;
	}

	public EndpointCallExecutablesBuilder executables() {
		return this.endpointMethodExecutablesBuilder;
	}

	public RestifyProxyBuilder executables(EndpointCallExecutableFactory<?, ?>...factories) {
		this.endpointMethodExecutablesBuilder.add(factories);
		return this;
	}

	public EndpointResponseErrorFallbackBuilder error() {
		return endpointResponseErrorFallbackBuilder;
	}

	public RestifyProxyBuilder error(EndpointResponseErrorFallback fallback) {
		this.endpointResponseErrorFallbackBuilder = new EndpointResponseErrorFallbackBuilder(this, fallback);
		return this;
	}

	public <T> RestifyProxyBuilderOnTarget<T> target(Class<T> target) {
		return new RestifyProxyBuilderOnTarget<>(target, null);
	}

	public <T> RestifyProxyBuilderOnTarget<T> target(Class<T> target, String endpoint) {
		return new RestifyProxyBuilderOnTarget<>(target, endpoint);
	}

	public class RestifyProxyBuilderOnTarget<T> {
		private final Class<T> type;
		private final String endpoint;

		private RestifyProxyBuilderOnTarget(Class<T> type, String endpoint) {
			this.type = type;
			this.endpoint = endpoint;
		}

		@SuppressWarnings("unchecked")
		public T build() {
			RestifyProxyHandler restifyProxyHandler = doBuild();

			return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{type}, restifyProxyHandler);
		}

		private RestifyProxyHandler doBuild() {
			EndpointTarget target = new EndpointTarget(type, endpoint);

			EndpointMethodExecutor endpointMethodExecutor = new EndpointMethodExecutor(endpointCallExecutables(), endpointMethodCallFactory()); 

			RestifyContract restifyContract = contract();

			return new RestifyProxyHandler(restifyContract.read(target), endpointMethodExecutor);
		}

		private EndpointCallExecutables endpointCallExecutables() {
			return endpointMethodExecutablesBuilder.build();
		}

		private EndpointCallFactory endpointMethodCallFactory() {
			return new EndpointCallFactory(endpointRequestFactory(), endpointRequestExecutor());
		}

		private EndpointRequestFactory endpointRequestFactory() {
			return new EndpointRequestFactory(endpointRequestInterceptorsBuilder.build());
		}

		private EndpointRequestExecutor endpointRequestExecutor() {
			HttpMessageConverters messageConverters = httpMessageConvertersBuilder.build();
			return Optional.ofNullable(endpointRequestExecutor)
					.orElseGet(() -> new RestifyEndpointRequestExecutor(httpClientRequestFactory(), 
							new EndpointRequestWriter(messageConverters),
							new EndpointResponseReader(messageConverters, endpointResponseErrorFallbackBuilder())));
		}

		private EndpointResponseErrorFallback endpointResponseErrorFallbackBuilder() {
			return endpointResponseErrorFallbackBuilder.build();
		}

		private HttpClientRequestFactory httpClientRequestFactory() {
			return Optional.ofNullable(httpClientRequestFactory)
					.orElseGet(() -> new JdkHttpClientRequestFactory());
		}

		private RestifyContract contract() {
			return Optional.ofNullable(contractReader)
					.map(c -> new DefaultRestifyContract(c))
					.orElseGet(() -> new DefaultRestifyContract(new DefaultRestifyContractReader()));
		}
	}

	public class HttpMessageConvertersBuilder {

		private final RestifyProxyBuilder context;
		private final Collection<HttpMessageConverter> converters = new ArrayList<>();

		private HttpMessageConvertersBuilder(RestifyProxyBuilder context) {
			this.context = context;
		}

		public HttpMessageConvertersBuilder json() {
			converters.add(JsonMessageConverter.available());
			return this;
		}

		public HttpMessageConvertersBuilder xml() {
			converters.add(new JaxbXmlMessageConverter<Object>());
			return this;
		}

		public HttpMessageConvertersBuilder text() {
			converters.add(new TextPlainMessageConverter());
			converters.add(new TextHtmlMessageConverter());
			converters.add(new ScalarMessageConverter());
			return this;
		}

		public HttpMessageConvertersBuilder form() {
			converters.add(new FormURLEncodedParametersMessageConverter());
			converters.add(new FormURLEncodedFormObjectMessageConverter());
			converters.add(new FormURLEncodedMapMessageConverter());
			converters.add(new MultipartFormParametersMessageWriter());
			converters.add(new MultipartFormObjectMessageWriter());
			converters.add(new MultipartFormFileObjectMessageWriter());
			converters.add(new MultipartFormMapMessageWriter());
			return this;
		}

		public HttpMessageConvertersBuilder all() {
			return json().xml().text().form();
		}

		public HttpMessageConvertersBuilder add(HttpMessageConverter...converters) {
			this.converters.addAll(Arrays.asList(converters));
			return this;
		}

		public RestifyProxyBuilder and() {
			return context;
		}

		private HttpMessageConverters build() {
			return converters.isEmpty() ? all().build() : new HttpMessageConverters(converters);
		}
	}

	public class EndpointRequestInterceptorsBuilder {

		private final RestifyProxyBuilder context;
		private final Collection<EndpointRequestInterceptor> interceptors = new ArrayList<>();

		private EndpointRequestInterceptorsBuilder(RestifyProxyBuilder context) {
			this.context = context;
		}

		public EndpointRequestInterceptorsBuilder authentication(Authentication authentication) {
			interceptors.add(new AuthenticationEndpoinRequestInterceptor(authentication));
			return this;
		}

		public EndpointRequestInterceptorsBuilder accept(String... contentTypes) {
			interceptors.add(new AcceptHeaderEndpointRequestInterceptor(contentTypes));
			return this;
		}

		public EndpointRequestInterceptorsBuilder accept(ContentType... contentTypes) {
			interceptors.add(new AcceptHeaderEndpointRequestInterceptor(contentTypes));
			return this;
		}

		public EndpointRequestInterceptorsBuilder add(EndpointRequestInterceptor...interceptors) {
			this.interceptors.addAll(Arrays.asList(interceptors));
			return this;
		}

		public RestifyProxyBuilder and() {
			return context;
		}

		private EndpointRequestInterceptorStack build() {
			return new EndpointRequestInterceptorStack(interceptors);
		}
	}

	public class EndpointCallExecutablesBuilder {

		private final RestifyProxyBuilder context;
		private final AsyncEndpointCallExecutablesBuilder async = new AsyncEndpointCallExecutablesBuilder();

		private final Collection<EndpointCallExecutableProvider> providers = new ArrayList<>();

		private EndpointCallExecutablesBuilder(RestifyProxyBuilder context) {
			this.context = context;
			this.providers.add(new OptionalEndpointCallExecutableFactory<Object>());
			this.providers.add(new CallableEndpointCallExecutableFactory<Object, Object>());
			this.providers.add(new RunnableEndpointCallExecutableFactory());
			this.providers.add(new EndpointCallObjectExecutableFactory<Object, Object>());
			this.providers.add(new HeadersEndpointCallExecutableFactory());
		}

		public EndpointCallExecutablesBuilder async() {
			async.all();
			return this;
		}

		public EndpointCallExecutablesBuilder async(Executor executor) {
			async.with(executor);
			return this;
		}

		public EndpointCallExecutablesBuilder async(ExecutorService executorService) {
			async.with(executorService);
			return this;
		}

		public EndpointCallExecutablesBuilder add(EndpointCallExecutableProvider endpointCallExecutableProvider) {
			providers.add(endpointCallExecutableProvider);
			return this;
		}

		public EndpointCallExecutablesBuilder add(EndpointCallExecutableProvider...providers) {
			this.providers.addAll(Arrays.asList(providers));
			return this;
		}

		public RestifyProxyBuilder and() {
			return context;
		}

		private EndpointCallExecutables build() {
			providers.addAll(async.build());
			return new EndpointCallExecutables(providers);
		}
	}

	private class AsyncEndpointCallExecutablesBuilder {

		private final Collection<EndpointCallExecutableProvider> providers = new ArrayList<>();

		private AsyncEndpointCallExecutablesBuilder all() {
			with(Executors.newCachedThreadPool());
			return this;
		}

		private AsyncEndpointCallExecutablesBuilder with(ExecutorService executor) {
			with((Executor) executor);
			providers.add(new FutureEndpointCallExecutableFactory<Object, Object>(executor));
			providers.add(new FutureTaskEndpointCallExecutableFactory<Object, Object>(executor));
			return this;
		}

		private AsyncEndpointCallExecutablesBuilder with(Executor executor) {
			providers.add(new CompletableFutureEndpointCallExecutableFactory<Object, Object>(executor));
			providers.add(new CompletableFutureCallbackEndpointCallExecutableFactory<Object, Object>(executor));
			providers.add(new AsyncEndpointCallExecutableFactory<Object, Object>(executor));
			providers.add(new AsyncCallbackEndpointCallExecutableFactory<Object, Object>(executor));
			return this;
		}

		private Collection<EndpointCallExecutableProvider> build() {
			return providers.isEmpty() ? all().build() : providers;
		}
	}

	public class EndpointResponseErrorFallbackBuilder {

		private final RestifyProxyBuilder context;

		private EndpointResponseErrorFallback fallback = null;
		private boolean emptyOnNotFound = false;

		private EndpointResponseErrorFallbackBuilder(RestifyProxyBuilder context) {
			this.context = context;
		}

		private EndpointResponseErrorFallbackBuilder(RestifyProxyBuilder context, EndpointResponseErrorFallback fallback) {
			this.context = context;
			this.fallback = fallback;
		}

		public RestifyProxyBuilder emptyOnNotFound() {
			this.emptyOnNotFound = true;
			return context;
		}

		public RestifyProxyBuilder using(EndpointResponseErrorFallback fallback) {
			this.fallback = fallback;
			return context;
		}

		private EndpointResponseErrorFallback build() {
			return Optional.ofNullable(fallback)
					.orElseGet(() -> emptyOnNotFound ? DefaultEndpointResponseErrorFallback.emptyOnNotFound() : new DefaultEndpointResponseErrorFallback());
		}
	}
}
