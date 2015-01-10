package org.jboss.tools.vpe.cordovasim.eclipse.launch.internal;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.jboss.tools.vpe.cordovasim.eclipse.launch.CordovaSimLaunchConstants;

public class CordovaSimLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
		String projectString = configuration.getAttribute(CordovaSimLaunchConstants.PROJECT, (String) null);
		IProject project = getProject(projectString);
		
		String rootFolderString = configuration.getAttribute(CordovaSimLaunchConstants.ROOT_FOLDER, (String) null);
		IContainer rootFolder = getFolder(project, rootFolderString);
		
		String startPage = getStartPage(configuration, project);
		
		Integer port = getPort(configuration);

		
		CordovaSimLauncher.launchCordovaSim(project, rootFolder, startPage, port);
	}

	protected String getStartPage(ILaunchConfiguration configuration, IProject project) throws CoreException {
		return CordovaSimLaunchParametersUtil.getStartPage(project);
	}
	
	private IProject getProject(String projectString) {
		return CordovaSimLaunchParametersUtil.getProject(projectString);
	}
	
	private IContainer getFolder(IProject project, String rootFolderString) {
		IContainer rootFolder = (rootFolderString != null) ? CordovaSimLaunchParametersUtil.getRootFolder(project,rootFolderString) 
														   : CordovaSimLaunchParametersUtil.getDefaultRootFolder(project);
		
		return rootFolder;
	}
	
	private Integer getPort(ILaunchConfiguration configuration) throws CoreException {
		Integer port = null;
		if (configuration.hasAttribute(CordovaSimLaunchConstants.PORT)) {
			port = configuration.getAttribute(CordovaSimLaunchConstants.PORT, 0);
		}
		return port;
	}
}	
