/*******************************************************************************
 * Copyright (c) 2007-2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.cordovasim.eclipse.launch.internal;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.cordavasim.eclipse.callbacks.CordovaSimRestartCallback;
import org.jboss.tools.cordavasim.eclipse.callbacks.StopServerCallback;
import org.jboss.tools.vpe.browsersim.eclipse.callbacks.OpenFileCallback;
import org.jboss.tools.vpe.browsersim.eclipse.callbacks.ViewSourceCallback;
import org.jboss.tools.vpe.browsersim.eclipse.launcher.BrowserSimLauncher;
import org.jboss.tools.vpe.browsersim.eclipse.launcher.ExternalProcessCallback;
import org.jboss.tools.vpe.browsersim.eclipse.launcher.ExternalProcessLauncher;
import org.jboss.tools.vpe.browsersim.ui.ExceptionNotifier;
import org.jboss.tools.vpe.browsersim.util.BrowserSimUtil;
import org.jboss.tools.vpe.cordovasim.eclipse.Activator;
import org.jboss.tools.vpe.cordovasim.eclipse.internal.util.ServerUtil;
import org.jboss.tools.vpe.cordovasim.eclipse.server.internal.ServerCreator;
import org.jboss.tools.vpe.cordovasim.eclipse.server.internal.ServerStorage;

/**
 * @author "Yahor Radtsevich (yradtsevich)"
 * @author "Ilya Buziuk (ibuziuk)"
 */
public class CordovaSimLauncher {

	public static final String CORDOVASIM_CLASS_NAME = "org.jboss.tools.vpe.cordovasim.CordovaSimRunner"; //$NON-NLS-1$
	private static final List<ExternalProcessCallback> CORDOVASIM_CALLBACKS = Arrays.asList(
			new ViewSourceCallback(),
			new OpenFileCallback(),
			new CordovaSimRestartCallback(),
			new StopServerCallback()
		);


	private static final List<String> RESOURCES_BUNDLES = BrowserSimLauncher.RESOURCES_BUNDLES;
	
	//if you change this parameter, see also @org.jbosstools.browsersim.ui.BrowserSim
	private static final String NOT_STANDALONE = BrowserSimLauncher.NOT_STANDALONE;	
	
	public static void launchCordovaSim(IProject project, IContainer rootFolder, String startPage,
			Integer port) {
		List<String> parameters = new ArrayList<String>();
		parameters.add(NOT_STANDALONE);

		String cordovaEngineLocation = null;
		String cordovaVersion = null;
			
		if (project != null) {
			cordovaEngineLocation = CordovaSimLaunchParametersUtil.getCordovaEngineLocation(project);
			cordovaVersion = CordovaSimLaunchParametersUtil.getCordovaVersion(project);
		}
					
		if (rootFolder != null && startPage!= null) {		
			try {
				if (!ServerStorage.getStorage().containsKey(port) && ServerUtil.isPortAvailable(port)) {
					Server server = ServerCreator.createServer(project, rootFolder, cordovaEngineLocation, port);
					server.start();
										
					Connector connector = server.getConnectors()[0];
					port = connector.getLocalPort(); // for the case if port equals 0 is requested (any free port)
					
					ServerStorage.getStorage().put(port, server); // Adding server to the ServerStorage
					
					parameters.add(rootFolder.getRawLocation().makeAbsolute().toString());
					parameters.add("http://localhost:" + port + "/" + startPage); //$NON-NLS-1$ //$NON-NLS-2$
					
					if (cordovaVersion != null) {
						parameters.add("-version"); //$NON-NLS-1$
						parameters.add(cordovaVersion);
					}
					launchCordovaSim(parameters);
				} else {
					showPortInUseMessage(port);
				}
			} catch (Exception e) {
				Activator.logError(e.getMessage(), e);
			}
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					Shell parentShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
					if (parentShell == null) {
						parentShell = PlatformUI.getWorkbench().getDisplay().getShells()[0]; // Hot fix for gtk3
					}	
					MessageDialog.openError(parentShell,
							Messages.CordovaSimLauncher_CANNOT_RUN_CORDOVASIM, Messages.CordovaSimLauncher_CANNOT_FIND_ROOT_FOLDER);					
				}
			});
		}
	}
	
	public static List<String> getBundles() {
		List<String> bundles = BrowserSimLauncher.getBundles();
		bundles.addAll(Arrays.asList(
				"org.jboss.tools.vpe.cordovasim", //$NON-NLS-1$
				"org.jboss.tools.vpe.cordovasim.ripple", //$NON-NLS-1$
				"org.jboss.tools.vpe.browsersim.debugger", //$NON-NLS-1$
				"org.eclipse.jetty.continuation", //$NON-NLS-1$
				"org.eclipse.jetty.continuation", //$NON-NLS-1$
				"org.eclipse.jetty.http", //$NON-NLS-1$
				"org.eclipse.jetty.io", //$NON-NLS-1$
				"org.eclipse.jetty.security", //$NON-NLS-1$
				"org.eclipse.jetty.server", //$NON-NLS-1$
				"org.eclipse.jetty.servlet", //$NON-NLS-1$
				"org.eclipse.jetty.util", //$NON-NLS-1$
				"org.eclipse.jetty.client", //$NON-NLS-1$
				"org.eclipse.jetty.servlets", //$NON-NLS-1$
				"org.eclipse.jetty.rewrite", //$NON-NLS-1$
				"javax.servlet" //$NON-NLS-1$
		));

		return bundles;
	}
	
	public static void launchCordovaSim(List<String> parameters) {
		Activator.getDefault().countLaunchEvent();
		IVMInstall jvm = BrowserSimLauncher.getSelectedVM();
		if (jvm == null) {// no suitable vm
			ExternalProcessLauncher.showErrorDialog(Messages.CordovaSimLauncher_CORDOVASIM);
		} else {
			String jvmPath = jvm.getInstallLocation().getAbsolutePath();
			String jrePath = jvm.getInstallLocation().getAbsolutePath() + File.separator + "jre"; //$NON-NLS-1$
			
			List<String> bundles = getBundles();
			
			if (!ExternalProcessLauncher.isGTK2()
					|| (!BrowserSimUtil.isJavaFxAvailable(jvmPath) && !BrowserSimUtil.isJavaFxAvailable(jrePath))) {
				bundles.add("org.jboss.tools.vpe.browsersim.javafx.mock"); //$NON-NLS-1$
			}
			
			ExternalProcessLauncher.launchAsExternalProcess(bundles, RESOURCES_BUNDLES,
					CORDOVASIM_CALLBACKS, CORDOVASIM_CLASS_NAME, parameters, Messages.CordovaSimLauncher_CORDOVASIM, jvm);
		}
	}
	
	private static void showPortInUseMessage(final Integer port) throws Exception {
		final Display display = Display.getDefault();
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = new Shell(display);
				String message = MessageFormat.format(Messages.ExceptionNotifier_PORT_IN_USE, port);
				ExceptionNotifier.showErrorMessage(shell, message);
				shell.dispose();
			}
		});
	}
	
}
