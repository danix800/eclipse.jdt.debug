/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

public class LaunchListener implements ILaunchListener, IDebugEventSetListener {
	private ArrayList<ILaunch> managedLaunches;

	private static final String RESTART = "restart"; //$NON-NLS-1$

	public LaunchListener() {
		managedLaunches = new ArrayList<>();
	}

	public void manage(ILaunch launch) {
		if (managedLaunches.isEmpty()) {
			hookListener(true);
		}
		if (!managedLaunches.contains(launch)) {
			managedLaunches.add(launch);
		}
	}

	@Override
	public void launchRemoved(ILaunch launch) {
		update(launch, true);
	}

	@Override
	public void launchAdded(ILaunch launch) {
	}

	@Override
	public void launchChanged(ILaunch launch) {
	}

	private void update(ILaunch launch, boolean remove) {
		if (managedLaunches.contains(launch)) {
			if (remove || launch.isTerminated()) {
				managedLaunches.remove(launch);
				if (managedLaunches.isEmpty()) {
					hookListener(false);
				}
			}
		}
	}

	private void hookListener(boolean add) {
		DebugPlugin debugPlugin = DebugPlugin.getDefault();
		ILaunchManager launchManager = debugPlugin.getLaunchManager();
		if (add) {
			launchManager.addLaunchListener(this);
			debugPlugin.addDebugEventListener(this);
		} else {
			launchManager.removeLaunchListener(this);
			debugPlugin.removeDebugEventListener(this);
		}
	}

	private void doRestart(ILaunch launch) {
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		try {
			ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
			copy.setAttribute(RESTART, true);
			copy.launch(launch.getLaunchMode(), new NullProgressMonitor());
		} catch (CoreException e) {
			Status status = new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, 42, null, e);
			IStatusHandler statusHandler = DebugPlugin.getDefault().getStatusHandler(status);
			if (statusHandler == null) {
				LaunchingPlugin.log(e);
			} else {
				try {
					statusHandler.handleStatus(status, null);
				} catch (CoreException e1) {
					// status handler failed to log the original exception
					// log it ourselves
					LaunchingPlugin.log(e);
				}
			}
		}
	}

	public void shutdown() {
		hookListener(false);
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for (DebugEvent event : events) {
			Object source = event.getSource();
			if (source instanceof IProcess && event.getKind() == DebugEvent.TERMINATE) {
				IProcess process = (IProcess) source;
				ILaunch launch = process.getLaunch();
				if (launch != null) {
					try {
						launchTerminated(launch, process.getExitValue());
					} catch (DebugException e) {
					} catch (CoreException e) {
						LaunchingPlugin.log(e);
					}
				}
			}
		}
	}

	private void launchTerminated(final ILaunch launch, int returnValue) throws CoreException {
		if (managedLaunches.contains(launch)) {
			update(launch, true);
			ILaunchConfiguration config = launch.getLaunchConfiguration();
			int relaunchExitCode = config.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_RELAUNCH_EXIT_CODE,
					IJavaLaunchConfigurationConstants.DEFAULT_RELAUNCH_EXIT_CODE);
			if (returnValue == relaunchExitCode) {
				doRestart(launch);
			}
		}
	}

}
