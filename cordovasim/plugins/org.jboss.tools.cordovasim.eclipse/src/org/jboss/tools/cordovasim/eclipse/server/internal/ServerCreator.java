/*******************************************************************************
 * Copyright (c) 2013-2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.cordovasim.eclipse.server.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.LifeCycle.Listener;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.Scheduler;
import org.jboss.tools.cordovasim.eclipse.Activator;
import org.jboss.tools.cordovasim.eclipse.internal.util.CordovaFileUtil;
import org.jboss.tools.cordovasim.eclipse.servlet.internal.ConfigServlet;
import org.jboss.tools.cordovasim.eclipse.servlet.internal.CordovaJsServlet;
import org.jboss.tools.cordovasim.eclipse.servlet.internal.CordovaPluginJsServlet;
import org.jboss.tools.cordovasim.eclipse.servlet.internal.CrossOriginProxyServlet;
import org.jboss.tools.cordovasim.eclipse.servlet.internal.FormatDataServlet;
import org.jboss.tools.cordovasim.eclipse.servlet.internal.HostFileServlet;
import org.jboss.tools.cordovasim.eclipse.servlet.internal.NotCachingResourceHandler;
import org.jboss.tools.cordovasim.eclipse.servlet.internal.PluginServlet;
import org.jboss.tools.cordovasim.eclipse.servlet.internal.StaticResponseServlet;
import org.jboss.tools.cordovasim.eclipse.servlet.internal.UploadFileServlet;
import org.jboss.tools.cordovasim.eclipse.servlet.internal.WorkspaceFileServlet;
import org.osgi.framework.Bundle;

/**
 * @author Yahor Radtsevich (yradtsevich)
 * @author Ilya Buziuk (ibuziuk)
 */
public class ServerCreator {
	private static final String LOCALHOST = "localhost"; //$NON-NLS-1$
	private static final String CORDOVASIM_RIPPLE_BUNDLE = "org.jboss.tools.cordovasim.ripple";  //$NON-NLS-1$
	
	private static final String RIPPLE_FOLDER = "ripple"; //$NON-NLS-1$
	private static final String RIPPLE_CORDOVA_FOLDER = "ripple/cordova"; //$NON-NLS-1$

	public static Server createServer(final IProject project, final IContainer resourceBase, String cordovaEngineLocation, Integer port) {
		QueuedThreadPool threadPool = new QueuedThreadPool(100, 10);
		Server server = new Server(threadPool);
		server.manage(threadPool);
		
		ServerConnector connector = new ServerConnector(server);
		connector.setReuseAddress(false);
		connector.setSoLingerTime(0);  // Linux keeps the port blocked without this line
		
		port = (port != null) ? port : 0; // If port is undefined use any free port
		connector.setHost(LOCALHOST); 
		connector.setPort(port);
	
		server.setConnectors(new Connector[] {connector});
		
		// Basic application context (Handler Tree)
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/"); //$NON-NLS-1$
		
		// "www" folder location
		String wwwLocation = resourceBase.getRawLocation().makeAbsolute().toOSString();
		
		// User-Agent request must return 200
		ServletHolder userAgentServletHolder = new ServletHolder(new StaticResponseServlet("OK")); 
		context.addServlet(userAgentServletHolder, "/ripple/user-agent");
		
		// Hosting ripple/assets folder
		String ripplePath = getResoursePathFromBundle(RIPPLE_FOLDER, CORDOVASIM_RIPPLE_BUNDLE); 
		ServletHolder rippleHome = new ServletHolder("ripple-home", DefaultServlet.class);
		rippleHome.setInitParameter("resourceBase", ripplePath);
		rippleHome.setInitParameter("pathInfoOnly","true");
		rippleHome.setInitParameter("dirAllowed", "true");
		context.addServlet(rippleHome, "/ripple/assets/*");
		
		ServletHolder wwwHome = new ServletHolder("www-home", DefaultServlet.class);
		wwwHome.setInitParameter("resourceBase", wwwLocation);
		wwwHome.setInitParameter("pathInfoOnly","true");
		wwwHome.setInitParameter("dirAllowed", "true");
		context.addServlet(wwwHome, "/");
				
		// Local Proxy servlet
		ServletHolder proxyServletHolder = new ServletHolder(new CrossOriginProxyServlet("tinyhippos_rurl")); 
		proxyServletHolder.setAsyncSupported(true);
		proxyServletHolder.setInitParameter("maxThreads", "10");
		context.addServlet(proxyServletHolder, "/ripple/xhr_proxy");
		
		// File Upload Servlet
		ServletHolder uploadFileServletHolder = new ServletHolder(new UploadFileServlet());
		uploadFileServletHolder.getRegistration().setMultipartConfig(new MultipartConfigElement(null, 1048576, 1048576, 262144));
		context.addServlet(uploadFileServletHolder, "/ripple/fileUpload"); 
		
		// Temp Photo Servlet
		ServletHolder hostFileServletHolder = new ServletHolder(new HostFileServlet());
		context.addServlet(hostFileServletHolder, "/temp-photo/*");
		
		// Data Format Servlet
		ServletHolder formatDataServletHolder = new ServletHolder(new FormatDataServlet());
		context.addServlet(formatDataServletHolder, "/ripple/formatData"); 
				
		// Processing cordova.js request
		ServletHolder cordovaJsHolder = new ServletHolder(new CordovaJsServlet(cordovaEngineLocation));
		context.addServlet(cordovaJsHolder, "/cordova.js");
		
		String cordovaLocation = getResoursePathFromBundle(RIPPLE_CORDOVA_FOLDER, CORDOVASIM_RIPPLE_BUNDLE);
		ServletHolder cordovaHome = new ServletHolder("cordova-home", DefaultServlet.class);
		cordovaHome.setInitParameter("resourceBase", cordovaLocation);
		cordovaHome.setInitParameter("dirAllowed", "true");
		cordovaHome.setInitParameter("pathInfoOnly","true");
		context.addServlet(cordovaHome, "/ripple/cordova/*");
		
		ServletHolder configHolder = new ServletHolder(new ConfigServlet(project));
		context.addServlet(configHolder, "/config.xml");
		
		File pluginDir = CordovaFileUtil.getPluginDir(wwwLocation); 
		ServletHolder cordovaPluginJsServletHolder = new ServletHolder(new CordovaPluginJsServlet(pluginDir));
		context.addServlet(cordovaPluginJsServletHolder, "/cordova_plugins.js"); 
		
		ServletHolder pluginServletHolder = new ServletHolder(new PluginServlet(pluginDir));
		context.addServlet(pluginServletHolder, "/plugins/*"); 
				
		// ? linked folders
//		String workspaceResoureLocation = "/" + project.getName() + "/" + resourceBase.getProjectRelativePath().toOSString(); //$NON-NLS-1$ //$NON-NLS-2$
//		ServletHolder workspaceServletHolder = new ServletHolder(new WorkspaceFileServlet(workspaceResoureLocation));
//		context.addServlet(workspaceServletHolder, "/"); //$NON-NLS-1$


		RewriteHandler rippleRewriteHandler = new RewriteHandler();
		rippleRewriteHandler.setRewriteRequestURI(true);
		rippleRewriteHandler.setRewritePathInfo(true);
		rippleRewriteHandler.setHandler(context);
		rippleRewriteHandler.addRule(new Rule() {
			@Override
			public String matchAndApply(String target, HttpServletRequest request,
					HttpServletResponse response) throws IOException {
				if (request.getParameter("enableripple") != null) { 
					return "/ripple/assets/index.html"; 
				} else {
					return null;
				}
			}
		});
		
		server.setHandler(rippleRewriteHandler);
						
//		HandlerList handlers = new HandlerList();
//		handlers.setHandlers(new Handler[] {
//				userAgentServletHandler,
//				rippleRewriteHandler,
//				wwwResourceHandler,
//				cordovaJsServetHandler,
//				configHandler,
//				cordovaPluginJsServetHandler,
//				cordovaContextHandler,
//				pluginServletHandler,
//				proxyServletHandler,
//				fileUploadContextHandler,
//				hostFileServletHandler,
//				formatDataServletHandler,
//				workspaceServletHandler,
//				new DefaultHandler()
//			});
//		server.setHandler(handlers);
		return server;
	}
	
	private static String getResoursePathFromBundle(final String path, final String bundleName) {
		String resourcePath = null;
		Bundle bundle = Platform.getBundle(bundleName); 
		URL fileURL = bundle.getEntry(path);
		try {
			URL resolvedFileURL = FileLocator.toFileURL(fileURL);
			// We need to use the 3-arg constructor of URI in order to properly escape file system chars
			URI resolvedURI = new URI(resolvedFileURL.getProtocol(), resolvedFileURL.getPath(), null);
			File file = new File(resolvedURI);
			if (file != null && file.exists()) {
				resourcePath = file.getAbsolutePath();
			}
		} catch (URISyntaxException e) {
			Activator.logError(e.getMessage(), e);
		} catch (IOException e) {
			Activator.logError(e.getMessage(), e);
		}
		return resourcePath;
	}
	
}

