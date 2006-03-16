package org.eclipse.jdt.internal.debug.ui.monitors;

import org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousContentAdapter;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public abstract class AsyncMonitorAdapter extends AsynchronousContentAdapter implements IPropertyChangeListener {

	private boolean fDisplayMonitors;

	public AsyncMonitorAdapter() {
        IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		preferenceStore.addPropertyChangeListener(this);
		fDisplayMonitors= preferenceStore.getBoolean(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO);
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO)) {
			fDisplayMonitors= ((Boolean)event.getNewValue()).booleanValue();
		}
	}

	protected boolean isDisplayMonitors() {
	    return fDisplayMonitors;
	}
	
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousContentAdapter#supportsPartId(java.lang.String)
     */
	protected boolean supportsPartId(String id) {
		return IDebugUIConstants.ID_DEBUG_VIEW.equals(id);
	}	
}