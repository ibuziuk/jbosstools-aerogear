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
					.getLaunchConfigurationType(CordovaSimLaunchConstants.FH_LAUNCH_CONFIGURATION_ID);
			ILaunchConfiguration[] configurations = DebugPlugin.getDefault().getLaunchManager()
					.getLaunchConfigurations(cordovaSimLaunchConfiguraionType);

			ILaunchConfiguration existingConfiguraion = CordovaSimLaunchConfigurationAutofillUtil
					.chooseLaunchConfiguration(configurations, project);
			
			if (existingConfiguraion != null) {
				DebugUITools.launch(existingConfiguraion, mode);
			}
			
			ILaunchConfigurationWorkingCopy newConfiguration = createEmptyLaunchConfiguration(project.getName());
			CordovaSimLaunchConfigurationAutofillUtil.fillLaunchConfiguraion(newConfiguration, project);
			newConfiguration.doSave();
			DebugUITools.launch(newConfiguration, mode);	
			
		} catch (CoreException e) {
			Activator.logError(e.getMessage(), e);
		}
	}
	
}
