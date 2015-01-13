/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.cordovasim.eclipse.launch.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.jboss.tools.cordavasim.eclipse.feedhenry.internal.FeedHenryUtil;
import org.jboss.tools.vpe.cordovasim.eclipse.Activator;
import org.jboss.tools.vpe.cordovasim.eclipse.launch.CordovaSimLaunchConstants;

/**
 * @author Ilya Buziuk (ibuziuk)
 */
public class FeedHenryLaunchShortcut extends CordovaSimLaunchShortcut {
	private static final String FH_PREFIX = " w/FeedHenry"; //$NON-NLS-1$

	@Override
	protected void launch(IProject project, String mode) {
		try {
			ILaunchConfigurationType cordovaSimLaunchConfiguraionType = DebugPlugin.getDefault().getLaunchManager()
					.getLaunchConfigurationType(CordovaSimLaunchConstants.LAUNCH_CONFIGURATION_ID);
			ILaunchConfiguration[] configurations = DebugPlugin.getDefault().getLaunchManager()
					.getLaunchConfigurations(cordovaSimLaunchConfiguraionType);

			ILaunchConfiguration existingConfiguraion = chooseLaunchConfiguration(configurations, project);
			if (existingConfiguraion != null) {
				DebugUITools.launch(existingConfiguraion, mode);
			} else if (project != null) {
				ILaunchConfigurationWorkingCopy newConfiguration = createEmptyLaunchConfiguration(project.getName() + FH_PREFIX);
				setConfigAttributes(newConfiguration, project);
				newConfiguration.doSave();
				DebugUITools.launch(newConfiguration, mode);
			}
		} catch (CoreException e) {
			Activator.logError(e.getMessage(), e);
		}
	}

	private void setConfigAttributes(ILaunchConfigurationWorkingCopy launchConfiguration, IProject project) {
		if (project != null) {
			launchConfiguration.setAttribute(CordovaSimLaunchConstants.PROJECT, project.getName());
			launchConfiguration.setAttribute(CordovaSimLaunchConstants.FH, FH_PREFIX);
			String startPage = CordovaSimLaunchParametersUtil.getDefaultStartPageFromConfigXml(project);
			startPage = FeedHenryUtil.addDefaultServerParameter(startPage);
			launchConfiguration.setAttribute(CordovaSimLaunchConstants.START_PAGE, startPage);
		}
	}
	
	private ILaunchConfiguration chooseLaunchConfiguration(ILaunchConfiguration[] configurations, IProject project) {
		try {
			for (ILaunchConfiguration configuration : configurations) {				
				String projectName = configuration.getAttribute(CordovaSimLaunchConstants.PROJECT, (String) null);
				String fh = configuration.getAttribute(CordovaSimLaunchConstants.FH, (String) null);
				if (fh != null && projectName != null && projectName.equals(project.getName())) {
					return configuration;
				}
			}
		} catch (CoreException e) {
			Activator.logError(e.getMessage(), e);
		}
		return null;
	}

}