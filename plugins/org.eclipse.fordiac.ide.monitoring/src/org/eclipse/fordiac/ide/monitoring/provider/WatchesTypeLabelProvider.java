/* Copyright (c) 2022 Primetals Technologies Austria GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Fabio Gandolfi - initial API and implementation and/or initial documentation
 *
 *******************************************************************************/
package org.eclipse.fordiac.ide.monitoring.provider;

import org.eclipse.fordiac.ide.model.libraryElement.IInterfaceElement;
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration;
import org.eclipse.fordiac.ide.monitoring.views.WatchValueTreeNode;
import org.eclipse.fordiac.ide.ui.FordiacMessages;
import org.eclipse.jface.viewers.ColumnLabelProvider;

public class WatchesTypeLabelProvider extends ColumnLabelProvider {
	@Override
	public String getText(final Object element) {

		if (element instanceof WatchValueTreeNode) {
			final WatchValueTreeNode tn = (WatchValueTreeNode) element;
			final IInterfaceElement ie = tn.getMonitoringBaseElement().getPort().getInterfaceElement();
			if (ie instanceof VarDeclaration) {
				return ie.getTypeName();
			}
			return FordiacMessages.Event.toUpperCase();
		}

		return ""; //$NON-NLS-1$
	}
}
