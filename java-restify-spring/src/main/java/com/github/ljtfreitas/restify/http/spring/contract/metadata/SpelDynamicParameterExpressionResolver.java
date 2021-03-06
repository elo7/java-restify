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
package com.github.ljtfreitas.restify.http.spring.contract.metadata;

import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.StandardBeanExpressionResolver;

public class SpelDynamicParameterExpressionResolver implements SpringDynamicParameterExpressionResolver {

	private final ConfigurableBeanFactory beanFactory;
	private final BeanExpressionResolver resolver;
	private final BeanExpressionContext context;

	public SpelDynamicParameterExpressionResolver(ConfigurableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		this.resolver = beanFactory.getBeanExpressionResolver();
		this.context = new BeanExpressionContext(beanFactory, null);
	}

	@Override
	public String resolve(String expression) {
		if (bean(expression)) {
			return resolver.evaluate(expression, context).toString();
		} else {
			return beanFactory.resolveEmbeddedValue(expression);
		}
	}

	private boolean bean(String expression) {
		return expression.startsWith(StandardBeanExpressionResolver.DEFAULT_EXPRESSION_PREFIX)
				&& expression.endsWith(StandardBeanExpressionResolver.DEFAULT_EXPRESSION_SUFFIX);
	}
}
