package org.jboss.tools.vpe.cordovasim.eclipse.launch.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.cordavasim.eclipse.feedhenry.internal.FeedHenryUtil;
import org.jboss.tools.vpe.cordovasim.eclipse.launch.CordovaSimLaunchConstants;

public class FeedHenryLaunchConfigurationTab extends CordovaSimLaunchConfigurationTab {
	
	private Button useLocalCloudServer;
	private Text localCloudUrText;
	
	@Override
	protected void initControls(Composite comp) {
		createProjectEditor(comp);
		createRootFolderEditor(comp);
		createServerEditor(comp);
		createFeedHenryCloudEditor(comp);
				
		setControl(comp);
	}
	
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		
		// init FeedHenry cloud
		
		String localCloudUrl = null;
		try {
			localCloudUrl = configuration.getAttribute(CordovaSimLaunchConstants.FH_LOCAL_SERVER_URL, (String) null);
		} catch (CoreException e) {
		}
		setSelectedLocalCloudUrl(localCloudUrl);
	}
	
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		
		// apply FeedHenry cloud
		
		String localCloudUrl = null;
		if (useLocalCloudServer.getSelection()) {
			localCloudUrl = getSelectedLocalServerUrl();
		}
		configuration.setAttribute(CordovaSimLaunchConstants.FH_LOCAL_SERVER_URL, localCloudUrl);
	}
	
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		super.isValid(launchConfig);
		
		// validate FeedHenry cloud
		
		return true;
	}

	private void createFeedHenryCloudEditor(Composite parent) {
		Group group = SWTFactory.createGroup(parent, Messages.CordovaSimLaunchConfigurationTab_FEED_HENRY, 2, 1, GridData.FILL_HORIZONTAL);
		useLocalCloudServer = SWTFactory.createCheckButton(group, Messages.CordovaSimLaunchConfigurationTab_USE_LOCAL_FH_SERVER, null, true, 2); 
		SWTFactory.createLabel(group, Messages.CordovaSimLaunchConfigurationTab_LOCAL_FH_SERVER_URL, 1);
		localCloudUrText = SWTFactory.createSingleText(group, 1);
		localCloudUrText.addModifyListener(defaultListener);
		useLocalCloudServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean useLocalServer = ((Button) e.widget).getSelection();
				setSelectedLocalCloudUrl(useLocalServer ? FeedHenryUtil.DEFAULT_FH_PARAMETER : null);
			}
		});
	}
	
	private void setSelectedLocalCloudUrl(String url) {
		boolean useLocalServer = (url != null);
		
		useLocalCloudServer.setSelection(useLocalServer);
		localCloudUrText.setEnabled(useLocalServer);
		localCloudUrText.setText(url != null ? url : ""); //$NON-NLS-1$
	}
	
	private String getSelectedLocalServerUrl() {
		return localCloudUrText.getText();
	}
}
