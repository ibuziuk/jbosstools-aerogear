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

public class FeedHenryLaunchShortcut extends CordovaSimLaunchShortcut {
	
	protected void launch(IProject project, String mode) {
		try {
			ILaunchConfigurationType cordovaSimLaunchConfiguraionType = DebugPlugin.getDefault().getLaunchManager()
					.getLaunchConfigurationType(CordovaSimLaunchConstants.LAUNCH_CONFIGURATION_ID); 
			ILaunchConfiguration[] configurations = DebugPlugin.getDefault()
					.getLaunchManager().getLaunchConfigurations(cordovaSimLaunchConfiguraionType);
			
			ILaunchConfiguration existingConfiguraion = CordovaSimLaunchConfigurationAutofillUtil.chooseLaunchConfiguration(configurations, project);
			if (existingConfiguraion != null) {
				DebugUITools.launch(existingConfiguraion, mode);
			} else {
				ILaunchConfigurationWorkingCopy newConfiguration = createEmptyLaunchConfiguration(project.getName());
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
			if (FeedHenryUtil.isFeedHenryProject(project)) {
				String startPage = CordovaSimLaunchParametersUtil.getDefaultStartPageFromConfigXml(project);
				if (startPage != null) {
					startPage = FeedHenryUtil.addDefaultParameer(startPage);
					launchConfiguration.setAttribute(CordovaSimLaunchConstants.START_PAGE, startPage); 
				}
			}
		}
	}

}
