/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.vpe.cordovasim.eclipse.launch.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.jboss.tools.cordavasim.eclipse.feedhenry.internal.FeedHenryUtil;

/**
 * @author Ilya Buziuk (ibuziuk)
 */
public class FeedHenryTester extends PropertyTester {
	private static final String IS_FEED_HENRY = "isFeedHenry"; //$NON-NLS-1$
	
	public FeedHenryTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (IS_FEED_HENRY.equals(property) && receiver instanceof IResource) {
			IProject project = ((IResource) receiver).getProject(); 
			return FeedHenryUtil.isFeedHenryProject(project);
		}
		return false;
	}
	
}