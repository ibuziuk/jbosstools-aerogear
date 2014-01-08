/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.aerogear.hybrid.core.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.tools.aerogear.hybrid.engine.internal.cordova.AndroidTemplateResolver;
import org.jboss.tools.aerogear.hybrid.engine.internal.cordova.CordovaEngineProvider;
import org.jboss.tools.aerogear.hybrid.engine.internal.cordova.IosTemplateResolver;


public class HybridMobileEngine{
	
	private String id;
	private String name;
	private String version;
	private ArrayList<String> platforms = new ArrayList<String>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public HybridMobileTemplateResolver getPlatformTemplateResolver(String platformId){
        Assert.isNotNull(platformId);
		if(platformId.equals("ios")){
			IPath libraryRoot = new Path(CordovaEngineProvider.getLibFolder().toString());
			libraryRoot = libraryRoot.append("ios").append(getId()).append(getVersion());
			return new IosTemplateResolver(libraryRoot, getVersion());
		}
		if(platformId.equals("android")){
			IPath libraryRoot = new Path(CordovaEngineProvider.getLibFolder().toString());
			libraryRoot = libraryRoot.append("android").append(getId()).append(getVersion());
			return new AndroidTemplateResolver(libraryRoot);
		}
		return null;
	}
	
	public void addPlatform(String platform) {
		if(!platforms.contains(platform)){
			platforms.add(platform);
		}
	}
	
	public List<String> getPlatforms(){
		return Collections.unmodifiableList(platforms);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof HybridMobileEngine) ){
			return false;
		}
		HybridMobileEngine that = (HybridMobileEngine) obj;
		if(this.getId().equals(that.getId()) 
				&& this.getVersion().equals(that.getVersion())){
			return true;
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		if(this.getId() != null && this.getVersion() != null ){
			return this.getId().hashCode()+this.getVersion().hashCode();
		}
		return super.hashCode();
	}
	
}