/*******************************************************************************
 * Copyright (c) 2008 - 2021 Profactor GmbH, TU Wien ACIN, AIT, fortiss GmbH,
 *                           Johannes Kepler University,
 *                           Primetals Technologies Germany GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Gerhard Ebenhofer, Alois Zoitl, Matthias Plasch, Filip Andren,
 *   Monika Wenger
 *   - initial API and implementation and/or initial documentation
 *   Alois Zoitl - fixed copy/paste handling
 *               - extracted FBNetworkRootEditPart from FBNetworkEditor
 *               - extracted panning and selection tool added inline connection
 *                 creation
 *               - added checking code to deactivate connection creation when alt
 *                 key is not pressed anymore
 *******************************************************************************/
package org.eclipse.fordiac.ide.application.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.fordiac.ide.gef.editparts.InterfaceEditPart;
import org.eclipse.fordiac.ide.gef.tools.AdvancedPanningSelectionTool;
import org.eclipse.fordiac.ide.gef.tools.FordiacConnectionDragCreationTool;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

public class FBNetworkPanningSelectionTool extends AdvancedPanningSelectionTool {

	/**
	 * Key to indicate that connection creation mode should be activated.
	 *
	 * The current default is on most system the Alt key.
	 */
	private static final int CONNECTION_CREATION_MOD_KEY = SWT.MOD3;

	static class InlineConnectionCreationTool extends FordiacConnectionDragCreationTool {

		private final EditPart sourcePart;
		private EditPart lastConnTarget;
		private boolean startup = true;

		public InlineConnectionCreationTool(final EditPart sourcePart) {
			super();
			this.sourcePart = sourcePart;
		}

		public void startup(final Point point) {
			startup = true;
			activate();
			super.handleButtonDown(LEFT_MOUSE);
			startup = false;
			handleDragStarted();
			getCurrentInput().setMouseLocation(point.x, point.y);
			handleMove();
		}

		@Override
		public void mouseUp(final MouseEvent me, final EditPartViewer viewer) {
			super.mouseUp(me, viewer);
			lastConnTarget = getTargetEditPart();
			startup(new Point(me.x, me.y));
		}

		@Override
		protected EditPart getTargetEditPart() {
			if (startup) {
				return sourcePart;
			}
			EditPart part = super.getTargetEditPart();
			if ((null != part) && (part.equals(lastConnTarget))) {
				// don't use the last part
				part = null;
			}
			return part;
		}

	}

	static final int LEFT_MOUSE = 1;
	static final double TYPE_DISTANCE = 10.0; // the max distance the mouse may move between left click and
	// typing
	private org.eclipse.draw2d.geometry.Point lastLeftClick = new org.eclipse.draw2d.geometry.Point(0, 0);
	private InlineConnectionCreationTool connectionCreationTool;

	@Override
	public void mouseDown(final MouseEvent me, final EditPartViewer viewer) {
		checkConnCreationState(me); // check if conn creation needs to be deactivated
		if (null == connectionCreationTool) {
			super.mouseDown(me, viewer);
		}
	}

	@Override
	public void mouseUp(final MouseEvent me, final EditPartViewer viewer) {
		if (checkConnCreationState(me)) {
			connectionCreationTool.mouseUp(me, viewer);
		} else {
			if (LEFT_MOUSE == me.button) {
				lastLeftClick = getLocation();
			}
			super.mouseUp(me, viewer);
		}
	}

	@Override
	public void keyDown(final KeyEvent evt, final EditPartViewer viewer) {
		if ((Character.isLetterOrDigit(evt.character)) && (TYPE_DISTANCE > getLocation().getDistance(lastLeftClick))) {
			final EditPart editPart = viewer.findObjectAt(getLocation());
			if (null != editPart) {
				final SelectionRequest request = new SelectionRequest();
				request.setLocation(lastLeftClick);
				request.setType(RequestConstants.REQ_OPEN);
				final Map<String, String> map = new HashMap<>();
				map.put(String.valueOf(evt.character), String.valueOf(evt.character));
				request.setExtendedData(map);
				editPart.performRequest(request);
				return;
			}
		}
		if (evt.keyCode == CONNECTION_CREATION_MOD_KEY && (connectionCreationTool == null)) {
			activateConnectionCreation(viewer);
		}
		super.keyDown(evt, viewer);
	}

	@Override
	public void mouseMove(final MouseEvent me, final EditPartViewer viewer) {
		if (checkConnCreationState(me)) {
			connectionCreationTool.mouseDrag(me, viewer);
		} else {
			super.mouseMove(me, viewer);
		}
	}

	@Override
	public void keyUp(final KeyEvent evt, final EditPartViewer viewer) {
		if ((evt.keyCode == CONNECTION_CREATION_MOD_KEY) && (null != connectionCreationTool)) {
			deactivateConnectionCreation();
		}
		super.keyUp(evt, viewer);
	}

	private void activateConnectionCreation(final EditPartViewer viewer) {
		final List<Object> editParts = viewer.getSelectedEditParts();
		if ((editParts.size() == 1) && (editParts.get(0) instanceof InterfaceEditPart)) {
			connectionCreationTool = new InlineConnectionCreationTool((EditPart) editParts.get(0));
			connectionCreationTool.setViewer(viewer);
			connectionCreationTool.setEditDomain(getDomain());
			connectionCreationTool.startup(getCurrentInput().getMouseLocation());
		}
	}

	private void deactivateConnectionCreation() {
		connectionCreationTool.deactivate();
		connectionCreationTool = null;
	}

	private boolean checkConnCreationState(final MouseEvent me) {
		if (null != connectionCreationTool) {
			if ((me.stateMask & CONNECTION_CREATION_MOD_KEY) == 0) {
				// connection key not pressed anymore deactivate connection creation
				deactivateConnectionCreation();
				return false;
			}
			return true;
		}
		return false;
	}

}