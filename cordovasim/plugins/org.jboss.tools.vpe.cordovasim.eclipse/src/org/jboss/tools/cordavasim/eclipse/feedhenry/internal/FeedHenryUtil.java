/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.cordavasim.eclipse.feedhenry.internal;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.jboss.tools.vpe.cordovasim.eclipse.launch.internal.CordovaSimLaunchParametersUtil;

/**
 * @author Ilya Buziuk (ibuziuk)
 */
public final class FeedHenryUtil {
	private static final String FH_CONFIG_JSON = "www/fhconfig.json"; //$NON-NLS-1$
	private static final String DEFAULT_FH_PARAMETER = "http://localhost:8001"; //$NON-NLS-1$
	private static final String URL = "url"; //$NON-NLS-1$
//	private static final String PATTERN = "(?<=[?&;])url=.*?($|[&;])"; //$NON-NLS-1$
	
	private FeedHenryUtil() {
	}

	public static boolean isFeedHenryProject(final IProject project) {
		if (project != null) {
			IFile file = project.getFile(FH_CONFIG_JSON);
			if (file != null && file.exists()) {
				return true;
			}
		}
		return false;
	}
	
	public static String addDefaultParameer(final String startPage) {
		return startPage + "?" + URL + "=" +  DEFAULT_FH_PARAMETER;	 //$NON-NLS-1$ //$NON-NLS-2$
	}
	
//	private static boolean isLocalServerAvailable(final String serverUrl) {
//		try {
//			HttpURLConnection.setFollowRedirects(false);
//			URL url = new URL(serverUrl); 
//			HttpURLConnection con = (HttpURLConnection) url.openConnection();
//			con.setConnectTimeout(1000);
//			con.setRequestMethod("GET"); //$NON-NLS-1$
//			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
//		} catch (Exception e) {
//			return false;
//		}
//	}
		
//	public static String validateURL(final String url) {
//		String parameters = CordovaSimLaunchParametersUtil.getStartPageParameters(url);
//		String serverUrl = getLocalServerUrl(parameters.substring(1, parameters.length()));
//		if (serverUrl != null && !isLocalServerAvailable(serverUrl)) {
//			String validatedUrl = url.replaceAll(PATTERN, "");  //$NON-NLS-1$
//			return validatedUrl;
//		}
//		return url;
//	}
	
//	private static String getLocalServerUrl(String parameterString) {
//		if (parameterString != null) {
//			Map<String, String> queryMap = getQueryMap(parameterString);
//			if (queryMap != null) {
//				String localServerUrl = queryMap.get(URL);
//				return localServerUrl;
//			}
//		}
//		return null;
//	}
//		
//	private static Map<String, String> getQueryMap(final String parameterString) {
//		String[] params = parameterString.split("&"); //$NON-NLS-1$
//		Map<String, String> map = new HashMap<String, String>();
//		for (String param : params) {
//			String name = param.split("=")[0]; //$NON-NLS-1$
//			String value = param.split("=")[1]; //$NON-NLS-1$
//			map.put(name, value);
//		}
//		return map;
//	}
	
}