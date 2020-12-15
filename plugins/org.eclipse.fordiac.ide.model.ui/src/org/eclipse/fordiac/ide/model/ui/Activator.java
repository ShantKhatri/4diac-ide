/********************************************************************************
 * Copyright (c) 2020 Johannes Kepler University Linz
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Bianca Wiesmayr
 *    - initial API and implementation and/or initial documentation
 ********************************************************************************/

package org.eclipse.fordiac.ide.model.ui;

import org.eclipse.fordiac.ide.ui.Abstract4DIACUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Abstract4DIACUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.fordiac.ide.model.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		setPlugin(this);
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		super.stop(context);
		setPlugin(null);
	}

	private static synchronized void setPlugin(final Activator newPlugin) {
		plugin = newPlugin;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
