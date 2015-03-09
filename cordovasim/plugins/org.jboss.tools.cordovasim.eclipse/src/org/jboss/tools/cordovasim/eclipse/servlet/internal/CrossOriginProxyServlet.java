/*******************************************************************************
 * Copyright (c) 2007-2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.cordovasim.eclipse.servlet.internal;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.proxy.ProxyServlet;

/**
 * @author Yahor Radtsevich (yradtsevich)
 * @author Ilya Buziuk (ibuziuk)
 */
public class CrossOriginProxyServlet extends ProxyServlet {
	private static final long serialVersionUID = 1L;
	private String urlParameterName;

	public CrossOriginProxyServlet(String urlParameterName) {
		this.urlParameterName = urlParameterName;
	}
	
	// TODO Do we really need "Host" header ? I would probably rewrite this method
	@Override
	protected URI rewriteURI(HttpServletRequest request) {
		String proxyTo = getProxyTo(request);
		if (proxyTo == null) {
			return null;
		}
		String path = request.getRequestURI();
		String query = request.getQueryString();
		if (query != null) {
			path += "?" + query; //$NON-NLS-1$
		}
		return URI.create(proxyTo + "/" + path).normalize(); //$NON-NLS-1$
	}
	
	private String getProxyTo(HttpServletRequest request) {
		return request.getParameter(urlParameterName);
	}
	
//	protected HttpURI proxyHttpURI(HttpServletRequest request, String uri)
//			throws MalformedURLException {;
//		String proxiedUrl = request.getParameter(urlParameterName);
//		if (proxiedUrl != null) {
//			return new HttpURI(proxiedUrl);				
//		} else {
//			return null;
//		}
//	}
	
	
//	@Override
//	protected void customizeExchange(HttpExchange exchange,
//			HttpServletRequest request) {
//		HttpURI url = new HttpURI(exchange.getRequestURI());
//		exchange.setRequestHeader("Host", url.getHost()); //$NON-NLS-1$
//	}
}
