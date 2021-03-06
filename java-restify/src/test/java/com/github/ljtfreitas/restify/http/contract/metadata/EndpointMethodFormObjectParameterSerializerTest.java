package com.github.ljtfreitas.restify.http.contract.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.Form;
import com.github.ljtfreitas.restify.http.contract.Form.Field;

public class EndpointMethodFormObjectParameterSerializerTest {

	private EndpointMethodFormObjectParameterSerializer serializer;

	@Before
	public void setup() {
		serializer = new EndpointMethodFormObjectParameterSerializer();
	}

	@Test
	public void shouldSerializeFormObjectToQueryParametersFormat() {
		MyFormObject myFormObject = new MyFormObject();
		myFormObject.param1 = "value1";
		myFormObject.param2 = "value2";

		String result = serializer.serialize("name", MyFormObject.class, myFormObject);

		assertEquals("param1=value1&customParamName=value2", result);
	}

	@Test
	public void shouldReturnNullWhenFormObjectSourceIsNull() {
		MyFormObject myFormObject = null;

		String result = serializer.serialize("name", MyFormObject.class, myFormObject);

		assertNull(result);
	}

	@Form
	private class MyFormObject {

		@Field
		private String param1;

		@Field("customParamName")
		private String param2;
	}
}
