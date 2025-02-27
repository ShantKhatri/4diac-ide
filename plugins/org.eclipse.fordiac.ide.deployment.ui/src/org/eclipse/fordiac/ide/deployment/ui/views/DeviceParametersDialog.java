/*******************************************************************************
 * Copyright (c) 2009, 2011 Profactor GbmH, TU Wien ACIN
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Gerhard Ebenhofer, Alois Zoitl
 *     - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.deployment.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.fordiac.ide.deployment.DeploymentCoordinator;
import org.eclipse.fordiac.ide.model.libraryElement.Device;
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * The Class DeviceParametersDialog.
 */
public class DeviceParametersDialog extends org.eclipse.jface.dialogs.Dialog {

	private Device device;

	/**
	 * Instantiates a new device parameters dialog.
	 *
	 * @param parent the parent
	 */
	public DeviceParametersDialog(final Shell parent) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/**
	 * Sets the device.
	 *
	 * @param device the new device
	 */
	public void setDevice(final Device device) {
		this.device = device;
	}

	private List<VarDeclaration> selectedProperties = new ArrayList<>();

	/**
	 * Gets the selected properties.
	 *
	 * @return the selected properties
	 */
	public List<VarDeclaration> getSelectedProperties() {
		return selectedProperties;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		final Composite main = new Composite(composite, SWT.NONE);
		main.setLayout(new GridLayout(1, false));
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		if (device != null) {
			selectedProperties = DeploymentCoordinator.INSTANCE.getSelectedDeviceProperties(device);

			for (final VarDeclaration varDecl : device.getVarDeclarations()) {
				final Button bt = new Button(main, SWT.CHECK);
				bt.setData(varDecl);
				bt.setText(varDecl.getName());
				if (varDecl.getName().equals("MGR_ID")) { //$NON-NLS-1$
					bt.setEnabled(false);
				}
				if (selectedProperties != null) {
					bt.setSelection(selectedProperties.contains(varDecl));
				} else {
					selectedProperties = new ArrayList<>();
					bt.setSelection(false);
				}

				bt.addListener(SWT.Selection, e -> {
					if (bt.getSelection()) {
						selectedProperties.add((VarDeclaration) bt.getData());
					} else {
						selectedProperties.remove(bt.getData());
					}

				});

			}
		}
		return composite;
	}

	/**
	 * Gets the device.
	 *
	 * @return the device
	 */
	public Device getDevice() {
		return device;
	}
}
