/*******************************************************************************
 * Copyright (c) 2013-2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *       Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.vpe.cordovasim.eclipse.launch.internal;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.jboss.tools.vpe.cordovasim.eclipse.launch.CordovaSimLaunchConstants;

/**
 * @author "Yahor Radtsevich (yradtsevich)"
 * @author "Ilya Buziuk" (ibuziuk)
 */
public class CordovaSimLaunchConfigurationDelegate implements
		ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {	
		IProject project = getProject(configuration);
		IContainer rootFolder = getRootFolder(configuration, project);
		String startPage = getStartPage(project, configuration);
		Integer port = getPort(configuration);
		CordovaSimLauncher.launchCordovaSim(project, rootFolder, startPage, port);
	}
	
	private IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		String projectString = configuration.getAttribute(CordovaSimLaunchConstants.PROJECT, (String) null);
		return CordovaSimLaunchParametersUtil.getProject(projectString);
	}

	private IContainer getRootFolder(ILaunchConfiguration configuration, IProject project) throws CoreException {
		return CordovaSimLaunchParametersUtil.getRootFolder(project);
	}

	private String getStartPage(IProject project, ILaunchConfiguration configuration) throws CoreException {
		String startPage = CordovaSimLaunchParametersUtil.getDefaultStartPageFromConfigXml(project);
		String parameters = getStartPageParameters(configuration);
		if (parameters != null) {
			int index = startPage.lastIndexOf("?"); //$NON-NLS-1$
			if (index == -1) {
				startPage += "?" + parameters; //$NON-NLS-1$
			} else {
				startPage += "&" + parameters; //$NON-NLS-1$
			}
		}
		return startPage;
	}

	private String getStartPageParameters(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(CordovaSimLaunchConstants.START_PAGE_PARAMETERS, (String) null);
	}

	private Integer getPort(ILaunchConfiguration configuration) throws CoreException {
		Integer port = null;
		if (configuration.hasAttribute(CordovaSimLaunchConstants.PORT)) {
			port = configuration.getAttribute(CordovaSimLaunchConstants.PORT, 0);
		}
		return port;
	}
	
}
