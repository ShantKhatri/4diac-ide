/*******************************************************************************
 * Copyright (c) 2019, 2021 Johannes Kepler University Linz,
 * 				            Primetals Technologies Germany GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alois Zoitl - initial API and implementation and/or initial documentation
 *               - added check if subapp interface is selected and mark that in
 *                 parent
 *   Daniel Lindhuber - MoveElementsFromSubappCommand integration
 *   				  - adjusted for unfolded subapps
 *   Bianca Wiesmayr, Alois Zoitl, Michael Oberlehner - refactor for breadcrumb
 ********************************************************************************/
package org.eclipse.fordiac.ide.application.handlers;

import static org.eclipse.fordiac.ide.model.ui.editors.HandlerHelper.getCommandStack;
import static org.eclipse.fordiac.ide.model.ui.editors.HandlerHelper.getFBNetwork;
import static org.eclipse.fordiac.ide.model.ui.editors.HandlerHelper.openEditor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.fordiac.ide.application.commands.MoveElementsFromSubAppCommand;
import org.eclipse.fordiac.ide.application.editparts.AbstractFBNElementEditPart;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetwork;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.SubApp;
import org.eclipse.fordiac.ide.model.ui.editors.HandlerHelper;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;

public class MoveToParentHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IEditorPart editor = HandlerUtil.getActiveEditor(event);

		if (null != editor) {
			final ISelection selection = HandlerUtil.getCurrentSelection(event);
			final List<FBNetworkElement> fbelements = getSelectedFBNElements(selection);

			if (!fbelements.isEmpty()) {
				final Rectangle bounds = getParentSubappBounds(editor, fbelements);
				final int destX = bounds.x;
				final int destY = bounds.y + bounds.height + 20;

				final MoveElementsFromSubAppCommand cmd = new MoveElementsFromSubAppCommand(fbelements,
						new Point(destX, destY));
				getCommandStack(editor).execute(cmd);

				// select moved elements in editor
				selectElements(editor, fbelements);
			}
		}
		return Status.OK_STATUS;
	}

	private static void selectElements(final IEditorPart editor, final List<FBNetworkElement> fbelements) {
		final GraphicalViewer viewer = getViewer(fbelements.get(0).getFbNetwork(), editor);
		final List<EditPart> eps = fbelements.stream().map(el -> (EditPart) viewer.getEditPartRegistry().get(el))
				.collect(Collectors.toList());
		viewer.setSelection(new StructuredSelection(eps));
	}

	private static Rectangle getParentSubappBounds(final IEditorPart editor,
			final List<FBNetworkElement> fbelements) {
		final FBNetwork subappNetwork = getParentOfParent(fbelements.get(0));
		final GraphicalViewer viewer = getViewer(subappNetwork, editor);
		viewer.flush();
		final GraphicalEditPart ep = (GraphicalEditPart) viewer.getEditPartRegistry()
				.get(fbelements.get(0).getFbNetwork().eContainer());
		return ep.getFigure().getBounds();
	}

	private static GraphicalViewer getViewer(final FBNetwork subappNetwork,
			final IEditorPart parent) {
		if (!getFBNetwork(parent).equals(subappNetwork)) {
			// source subapp editor, subapp content is not open
			return HandlerHelper.getViewer(openEditor(subappNetwork.eContainer()));
		}
		return HandlerHelper.getViewer(parent);
	}

	@Override
	public void setEnabled(final Object evaluationContext) {
		final ISelection selection = (ISelection) HandlerUtil.getVariable(evaluationContext,
				ISources.ACTIVE_CURRENT_SELECTION_NAME);

		final List<FBNetworkElement> fbelements = getSelectedFBNElements(selection);
		if ((!fbelements.isEmpty()) && (fbelements.get(0).getFbNetwork().eContainer() instanceof SubApp)) {
			// we are inside of a subapp
			final FBNetwork parent = fbelements.get(0).getFbNetwork();
			final boolean sameParentNetwork = fbelements.stream()
					.allMatch(el -> parent.equals(el.getFbNetwork()));
			setBaseEnabled(sameParentNetwork);
			return;
		}

		setBaseEnabled(false);
	}

	private static List<FBNetworkElement> getSelectedFBNElements(final ISelection selection) {
		if ((selection instanceof IStructuredSelection) && !selection.isEmpty()) {
			final IStructuredSelection sel = (IStructuredSelection) selection;
			return ((List<?>) sel.toList()).stream()
					.filter(AbstractFBNElementEditPart.class::isInstance)
					.map(ep -> ((AbstractFBNElementEditPart) ep).getModel())
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	private static FBNetwork getParentOfParent(final FBNetworkElement fbNetworkElement) {
		return (FBNetwork) fbNetworkElement.getFbNetwork().eContainer().eContainer();
	}
}
