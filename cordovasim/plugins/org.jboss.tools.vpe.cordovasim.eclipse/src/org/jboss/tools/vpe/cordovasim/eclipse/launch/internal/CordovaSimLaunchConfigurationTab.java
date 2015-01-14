/*******************************************************************************
 * Copyright (c) 2007-2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.cordovasim.eclipse.launch.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.WorkingDirectoryBlock;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.jboss.tools.vpe.cordovasim.eclipse.Activator;
import org.jboss.tools.vpe.cordovasim.eclipse.launch.CordovaSimLaunchConstants;
import static org.jboss.tools.vpe.cordovasim.eclipse.launch.internal.CordovaSimLaunchParametersUtil.AEROGEAR_HYBRID_NATURE_ID;
import static org.jboss.tools.vpe.cordovasim.eclipse.launch.internal.CordovaSimLaunchParametersUtil.ANDROID_NATURE_ID;;


/**
 * @author "Yahor Radtsevich (yradtsevich)"
 * @author "Ilya Buziuk(ibuziuk)"
 */
@SuppressWarnings("restriction")
public class CordovaSimLaunchConfigurationTab extends
		AbstractLaunchConfigurationTab {

	private Image image = Activator.getImageDescriptor("icons/cordovasim_16.png").createImage(); //$NON-NLS-1$
	private WidgetListener defaultListener = new WidgetListener();
	private Text projectText;
	private Button useDefaultPortCheckbox;
	private Text portText;
	
	public CordovaSimLaunchConfigurationTab() {
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
		((GridLayout) comp.getLayout()).verticalSpacing = 0;
		
		createProjectEditor(comp);
		createServerEditor(comp);
				
		setControl(comp);
	}


	private void createProjectEditor(Composite parent) {
		Group group = SWTFactory.createGroup(parent, Messages.CordovaSimLaunchConfigurationTab_PROJECT, 2, 1, GridData.FILL_HORIZONTAL);
		projectText = SWTFactory.createSingleText(group, 1);
		projectText.addModifyListener(defaultListener);
		Button peojectButton = createPushButton(group, Messages.CordovaSimLaunchConfigurationTab_BROWSE, null); 
		peojectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					handleProjectButtonSelected();
				} catch (CoreException e) {
					Activator.logError(e.getMessage(), e);
				}				
			}
		});
	}
	
	private void createServerEditor(Composite parent) {
		Group group = SWTFactory.createGroup(parent, Messages.CordovaSimLaunchConfigurationTab_SERVER_PORT, 2, 1, GridData.FILL_HORIZONTAL);
		useDefaultPortCheckbox = SWTFactory.createCheckButton(group, Messages.CordovaSimLaunchConfigurationTab_USE_DEFAULT, null, true, 2); 
		SWTFactory.createLabel(group, Messages.CordovaSimLaunchConfigurationTab_PORT, 1);
		portText = SWTFactory.createSingleText(group, 1);
		portText.addModifyListener(defaultListener);
		useDefaultPortCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean useDefaultPort = ((Button) e.widget).getSelection();
				setSelectedPort(useDefaultPort ? null : 4400);
			}
		});
	}
	
	protected void handleProjectButtonSelected() throws CoreException {
		IProject project = getSelectedProject();     

		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(),
				WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		dialog.setTitle(Messages.CordovaSimLaunchConfigurationTab_PROJECT_SELECTION);
		dialog.setMessage(Messages.CordovaSimLaunchConfigurationTab_SELECT_PROJECT);
		List<IProject> openPojects = new ArrayList<IProject>();
		for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (p.isOpen() && (p.hasNature(AEROGEAR_HYBRID_NATURE_ID) || p.hasNature(ANDROID_NATURE_ID))) {
				openPojects.add(p);
			}
		}
		dialog.setElements(openPojects.toArray(new IProject[0]));
		if (project != null) {
			dialog.setInitialSelections(new IProject[] {project});
		}
		
		dialog.setDialogBoundsSettings(getDialogBoundsSettings(Activator.PLUGIN_ID + ".PROJECT_DIALOG"), //$NON-NLS-1$
				Dialog.DIALOG_PERSISTSIZE);
		
		dialog.open();
		IProject newProject = (IProject) dialog.getFirstResult();
		if (newProject != null) {
			setSelectedProject(newProject);
		}
	}
	
	/**
	 * Returns the {@link IDialogSettings} for the given id
	 * 
	 * @param id the id of the dialog settings to get
	 * @return the {@link IDialogSettings} to pass into the {@link ContainerSelectionDialog}
	 */
	IDialogSettings getDialogBoundsSettings(String id) {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(id);
		if (section == null) {
			section = settings.addNewSection(id);
		} 
		return section;
	}
	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		IWorkbenchWindow workbenchWindow = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (workbenchWindow != null) {
			IWorkbenchPage activePage = workbenchWindow.getActivePage();
			if (activePage != null) {
				ISelection selection = activePage.getSelection();
				
				IProject project = CordovaSimLaunchConfigurationAutofillUtil.getProjectToRun(selection);
				CordovaSimLaunchConfigurationAutofillUtil.fillLaunchConfiguraion(configuration, project);
			}
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		IProject project = null;
		try {
			String projectString = configuration.getAttribute(CordovaSimLaunchConstants.PROJECT, (String) null);
			project = CordovaSimLaunchParametersUtil.getProject(projectString);
		} catch (CoreException e) {
		}
		setSelectedProject(project);
				
		Integer port = null;
		try {
			if (configuration.hasAttribute(CordovaSimLaunchConstants.PORT)) {
				port = configuration.getAttribute(CordovaSimLaunchConstants.PORT, 0);				
			}
		} catch (CoreException e) {
		}
		setSelectedPort(port);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setMappedResources(new IResource[]{getSelectedProject()});
		configuration.setAttribute(CordovaSimLaunchConstants.PROJECT, projectText.getText());		
		Integer port = getSelectedPort();
		if (port == null) {
			configuration.removeAttribute(CordovaSimLaunchConstants.PORT);
		} else {
			configuration.setAttribute(CordovaSimLaunchConstants.PORT, port);			
		}
	}

	@Override
	public String getName() {
		return Messages.CordovaSimLaunchConfigurationTab_MAIN;
	}
	
	@Override
	public Image getImage() {
		return image;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		image.dispose();
	}
	
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		
		String projectString = projectText.getText();
		IProject project;
		try {
			project = CordovaSimLaunchParametersUtil.validateAndGetProject(projectString);
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
			return false;
		}
		
		IContainer rootFolder;
		rootFolder = CordovaSimLaunchParametersUtil.getRootFolder(project);
		if (rootFolder == null) {
			setErrorMessage(Messages.CordovaSimLaunchConfigurationTab_NO_ROOT_FOLDER);
			return false;
		}
					
		if (!useDefaultPortCheckbox.getSelection()) { 
			String portString = portText.getText();
			try {
				CordovaSimLaunchParametersUtil.validatePortNumber(portString);
			} catch (CoreException e) {
				setErrorMessage(e.getStatus().getMessage());
				return false;
			}
		}
		
		return true;
	}
	
	private IProject getSelectedProject() {
		String projectString = projectText.getText();
		return CordovaSimLaunchParametersUtil.getProject(projectString);
	}
	
	private void setSelectedProject(IProject project) {
		projectText.setText(project != null ? project.getName() : ""); //$NON-NLS-1$
//		IContainer rootFolder = getSelectedRootFolder(project);
//		if (rootFolder == null) {
//			setSelectedRootFolder(project, null); // restore defaults
//			rootFolder = CordovaSimLaunchParametersUtil.getRootFolder(project);
//		}
//		if (getSelectedStartPage(project, rootFolder) == null) {
//			setSelectedStartPage(project, rootFolder, null, null);
//		}
	}
	
//	private IContainer getSelectedRootFolder(IProject project) {
//		if (!useDefaultRootFolderCheckbox.getSelection()) {
//			String rootFolderString = rootFolderText.getText();
//			return CordovaSimLaunchParametersUtil.getRootFolder(project, rootFolderString);
//		} else {
//			return null;
//		}
//	}
	
//	private void setSelectedRootFolder(IProject project, IContainer rootFolder) {
//		boolean useDefaultRootFolder = (rootFolder == null);
//		useDefaultRootFolderCheckbox.setSelection(useDefaultRootFolder);
//		rootFolderText.setEnabled(!useDefaultRootFolder);
//		rootFolderButton.setEnabled(!useDefaultRootFolder);
//		
//		IContainer actualRootFolder;
//		if (useDefaultRootFolder) {
//			actualRootFolder = CordovaSimLaunchParametersUtil.getRootFolder(project);
//		} else {
//			actualRootFolder = rootFolder;
//		}
//		IPath rootFolderRelative = CordovaSimLaunchParametersUtil.getRelativePath(project, actualRootFolder);
//		rootFolderText.setText(rootFolderRelative != null ? rootFolderRelative.toString() : ""); //$NON-NLS-1$
//	}
	
//	private IResource getSelectedStartPage(IProject project, IContainer rootFolder) {
//		if (useDefaultStartPageCheckbox.getSelection()) {
//			return null;
//		} else {
//			String startPageString = startPageText.getText();
//			return CordovaSimLaunchParametersUtil.getStartPage(rootFolder, startPageString);
//		}
//	}
	
//	private void setSelectedStartPage(IProject project, IContainer rootFolder, IResource startPage, String startPageString) {
//		boolean useDefaultStartPage = (startPage == null);
//		useDefaultStartPageCheckbox.setSelection(useDefaultStartPage);
//		startPageText.setEnabled(!useDefaultStartPage);
//		startPageButton.setEnabled(!useDefaultStartPage);
//		
//		IResource actualStartPage;
//		if (useDefaultStartPage) {
//			actualStartPage = CordovaSimLaunchParametersUtil.getDefaultStartPage(project, rootFolder);
//		} else {
//			actualStartPage = startPage;
//		}
//		IPath startPagePath = CordovaSimLaunchParametersUtil.getRelativePath(rootFolder, actualStartPage);
//		String queryParameters = null; 
//		
//		if (startPageString != null) {
//			queryParameters = CordovaSimLaunchParametersUtil.getStartPageParameters(startPageString);
//		} else {
//			String startPageFromConfigXml = CordovaSimLaunchParametersUtil.getDefaultStartPageFromConfigXml(project);
//			queryParameters = CordovaSimLaunchParametersUtil.getStartPageParameters(startPageFromConfigXml);
//		} 
//
//		queryParameters = (queryParameters == null) ? "" : queryParameters; //$NON-NLS-1$		
//		startPageText.setText(startPagePath != null ? startPagePath.toString() + queryParameters : ""); //$NON-NLS-1$
//	}
	
//	private IResource getActualStartPage(IProject project, IContainer rootFolder) {
//		IResource startPage = getSelectedStartPage(project, rootFolder);
//		if (startPage == null) {
//			startPage = CordovaSimLaunchParametersUtil.getDefaultStartPage(project, rootFolder);
//		}
//		return startPage;
//	}

//	private IContainer getActualRootFolder(IProject project) {
//		IContainer rootFolder = getSelectedRootFolder(project);
//		if (rootFolder == null) {
//			rootFolder = CordovaSimLaunchParametersUtil.getRootFolder(project);
//		}
//		return rootFolder;
//	}
	
	private Integer getSelectedPort() {
		if (useDefaultPortCheckbox.getSelection()) {
			return null;
		} else {
			int port = 0;
			try {
				port = Integer.parseInt(portText.getText());
			} catch (NumberFormatException e) {
			}
			return port;
		}
	}
	
	private void setSelectedPort(Integer port) {
		boolean useDefaultPort = (port == null);
		
		useDefaultPortCheckbox.setSelection(useDefaultPort);
		portText.setEnabled(!useDefaultPort);
		portText.setText(port != null ? port.toString() : ""); //$NON-NLS-1$
	}
	
	private class WidgetListener implements ModifyListener, SelectionListener {
		
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}
		
		public void widgetDefaultSelected(SelectionEvent e) {/*do nothing*/}
		
		public void widgetSelected(SelectionEvent e) {
			updateLaunchConfigurationDialog();
		}
	}
}

class CordovaSimWorkingDirectoryBlock extends WorkingDirectoryBlock {

	protected CordovaSimWorkingDirectoryBlock() {
		super("org.jboss.tools.vpe.cordovasim.eclipse.launch.internal.rootFolder"); //$NON-NLS-1$
	}

	@Override
	protected IProject getProject(ILaunchConfiguration configuration)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}
}
