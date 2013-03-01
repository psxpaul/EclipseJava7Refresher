package com.pr.jdkseven;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.refresh.IRefreshMonitor;
import org.eclipse.core.resources.refresh.IRefreshResult;
import org.eclipse.core.resources.refresh.RefreshProvider;

public class Java7RefreshProvider  extends RefreshProvider {
	private Java7RefreshMonitor refreshMonitor;
	
	@Override
	public IRefreshMonitor installMonitor(IResource resource, IRefreshResult result) {
		try {
			if(refreshMonitor == null) {
				refreshMonitor = new Java7RefreshMonitor();
			}
			
			refreshMonitor.monitor(resource, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return refreshMonitor;
	}
}
