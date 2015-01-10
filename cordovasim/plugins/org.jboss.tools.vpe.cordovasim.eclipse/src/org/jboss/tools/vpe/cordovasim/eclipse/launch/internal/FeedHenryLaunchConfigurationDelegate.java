package org.jboss.tools.vpe.cordovasim.eclipse.launch.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.jboss.tools.cordavasim.eclipse.feedhenry.internal.FeedHenryUtil;
import org.jboss.tools.vpe.cordovasim.eclipse.launch.CordovaSimLaunchConstants;

public class FeedHenryLaunchConfigurationDelegate extends CordovaSimLaunchConfigurationDelegate {
	
	@Override
	protected String getStartPage(ILaunchConfiguration configuration, IProject project) throws CoreException {
		String startPage = super.getStartPage(configuration, project);
		String localServerUrl = configuration.getAttribute(CordovaSimLaunchConstants.FH_LOCAL_SERVER_URL, (String) null);
		if (localServerUrl != null) {
			startPage += "?" + localServerUrl; //$NON-NLS-1$
		}
		
		return startPage;
	}
}
