package com.github.ljtfreitas.restify.http.client.message.form.multipart;

import com.github.ljtfreitas.restify.http.client.message.form.multipart.MultipartFormBoundaryGenerator;

class SimpleMultipartFormBoundaryGenerator implements MultipartFormBoundaryGenerator {

	private final String boundary;

	SimpleMultipartFormBoundaryGenerator(String boundary) {
		this.boundary = boundary;
	}

	@Override
	public String generate() {
		return boundary;
	}
}