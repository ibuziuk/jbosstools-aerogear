package org.jboss.tools.vpe.cordovasim.eclipse.launch.internal;

import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class FeedHenryLaunchConfigurationTabGroup extends CordovaSimLaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] { 
				new FeedHenryLaunchConfigurationTab(),
				new CommonTab() };
		setTabs(tabs);
	}

}
