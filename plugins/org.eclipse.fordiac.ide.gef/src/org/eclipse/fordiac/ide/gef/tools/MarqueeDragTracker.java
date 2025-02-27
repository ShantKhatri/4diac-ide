/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Johannes Kepler University - merged with MarqueeDragTracker and updated figure
 *                                  to not use XOR mode.
 *******************************************************************************/
package org.eclipse.fordiac.ide.gef.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.fordiac.ide.gef.policies.ModifiedMoveHandle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.tools.AbstractTool;
import org.eclipse.gef.tools.MarqueeSelectionTool;
import org.eclipse.gef.util.EditPartUtilities;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

/** A Tool which selects multiple {@link EditPart}s inside a rectangular area of a Graphical Viewer. Selection behavior
 * can be configured by selecting (via {@link #setMarqueeBehavior(int)}) one of six supported marquee behaviors, which
 * are:
 *
 * <ul>
 * <li>{@link #BEHAVIOR_NODES_CONTAINED}</li>
 * <li>{@link #BEHAVIOR_NODES_TOUCHED}</li>
 * <li>{@link #BEHAVIOR_CONNECTIONS_CONTAINED}</li>
 * <li>{@link #BEHAVIOR_CONNECTIONS_TOUCHED}</li>
 * <li>{@link #BEHAVIOR_NODES_CONTAINED_AND_RELATED_CONNECTIONS}</li>
 * <li>{@link #BEHAVIOR_NODES_TOUCHED_AND_RELATED_CONNECTIONS}</li>
 * </ul>
 *
 * By default, only edit parts whose figure's are on the primary layer that are fully enclosed in the marquee selection
 * rectangle will be considered ( {@link #BEHAVIOR_NODES_CONTAINED}).
 *
 * Besides different marquee behaviors, the tool supports different modes, which the user can influence by pressing
 * modifier keys at the beginning of the drag operation. I.e. if the SHIFT key is pressed at the beginning of the drag,
 * the enclosed items will be appended to the current viewer selection. If the MOD1 key is pressed at the beginning of
 * the drag, the enclosed items will have their selection state inverted.
 *
 * @author ebordeau
 * @author dlee
 * @author rhudson
 * @author delee
 * @author msorens
 * @author pshah
 * @author anyssen
 *
 *
 *         Alois Zoitl April 28, 2021. This class is a copy of MarqueeSelectionTool combined with MarqueeDragTracker to
 *         allow replacing the MarqueeRectangleFigure */

public class MarqueeDragTracker extends AbstractTool implements DragTracker {

	private static class MarqueeRectangleFigure extends Figure {

		@Override
		protected void paintFigure(final Graphics graphics) {
			final Rectangle bounds = getBounds().getCopy();
			graphics.translate(getLocation());

			graphics.setForegroundColor(ModifiedMoveHandle.getSelectionColor());
			graphics.setLineStyle(Graphics.LINE_DASH);
			graphics.setLineWidth(ModifiedMoveHandle.SELECTION_BORDER_WIDTH + 1);

			graphics.drawRectangle(0, 0, bounds.width - 1, bounds.height - 1);
		}
	}

	static final int DEFAULT_MODE = 0;
	static final int TOGGLE_MODE = 1;
	static final int APPEND_MODE = 2;


	private static final Request MARQUEE_REQUEST = new Request(RequestConstants.REQ_SELECTION);

	private int marqueeBehavior = MarqueeSelectionTool.DEFAULT_MARQUEE_BEHAVIOR;
	private Figure marqueeRectangleFigure;
	private int mode;

	private Collection<EditPart> selectedEditParts;

	private Request targetRequest;

	/**
	 * Creates a new MarqueeSelectionTool of default type
	 * {@link #BEHAVIOR_NODES_CONTAINED}.
	 */
	public MarqueeDragTracker() {
		setDefaultCursor(Cursors.CROSS);
		setUnloadWhenFinished(false);
	}

	/** @see org.eclipse.gef.tools.AbstractTool#applyProperty(java.lang.Object, java.lang.Object) */
	@Override
	protected void applyProperty(final Object key, final Object value) {
		if (MarqueeSelectionTool.PROPERTY_MARQUEE_BEHAVIOR.equals(key)) {
			if (value instanceof Integer) {
				setMarqueeBehavior(((Integer) value).intValue());
			}
			return;
		}
		super.applyProperty(key, value);
	}

	/** Called from {@link #performMarqueeSelect()} to determine those {@link EditPart}s that are affected by the
	 * current marquee selection. In default and append mode, the edit parts returned here will become selected in the
	 * current viewer's new selection (which is calculated and set in {@link #performMarqueeSelect()}), while in toggle
	 * mode their selection state will be inverted.
	 *
	 * Calculation is delegated to {@link #calculatePrimaryMarqueeSelectedEditParts()} and
	 * {@link #calculateSecondaryMarqueeSelectedEditParts(Collection)} to compute the set of marquee selected edit parts
	 * in a two step-process, where all directly affected edit parts are determined first, and those indirectly affected
	 * (related connections in case of {@link #BEHAVIOR_NODES_TOUCHED_AND_RELATED_CONNECTIONS}, or
	 * {@link #BEHAVIOR_NODES_CONTAINED_AND_RELATED_CONNECTIONS}) afterwards.
	 *
	 * Clients may overwrite to customize the calculation of marquee selected edit parts.
	 *
	 * @return A collection containing all edit parts that should be regarded as being included in the current marquee
	 *         selection, i.e. which should get selected in default or append mode, and whose selection state should get
	 *         inverted in toggle mode.
	 * @since 3.7 */
	protected Collection<EditPart> calculateMarqueeSelectedEditParts() {
		final Collection<EditPart> marqueeSelectedEditParts = new HashSet<>();
		marqueeSelectedEditParts.addAll(calculatePrimaryMarqueeSelectedEditParts());
		marqueeSelectedEditParts.addAll(calculateSecondaryMarqueeSelectedEditParts(marqueeSelectedEditParts));
		return marqueeSelectedEditParts;
	}

	/** Responsible of calculating those edit parts that should be regarded as directly affected by the current marquee
	 * selection. By default, the method calculates which edit parts are potential candidates based on the current
	 * marquee behavior and delegates to {@link #isMarqueeSelectable(GraphicalEditPart)} and
	 * {@link #isPrimaryMarqueeSelectedEditPart(GraphicalEditPart)} to decide whether the candidate is to be included in
	 * the marquee selection.
	 *
	 * @return A {@link Collection} containing all {@link EditPart}s that should be regarded as being directly affected
	 *         by the current marquee selection.
	 * @since 3.7 */
	private Collection<EditPart> calculatePrimaryMarqueeSelectedEditParts() {
		final Collection<EditPart> editPartsToProcess = new HashSet<>();
		if (marqueeBehavior != MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_CONTAINED
				&& marqueeBehavior != MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED) {
			// process nodes
			editPartsToProcess
			.addAll(EditPartUtilities.getAllChildren((GraphicalEditPart) getCurrentViewer().getRootEditPart()));
		}

		if (marqueeBehavior != MarqueeSelectionTool.BEHAVIOR_NODES_CONTAINED
				&& marqueeBehavior != MarqueeSelectionTool.BEHAVIOR_NODES_TOUCHED) {
			// process connections
			editPartsToProcess.addAll(EditPartUtilities
					.getAllNestedConnectionEditParts((GraphicalEditPart) getCurrentViewer().getRootEditPart()));
		}

		// process all edit parts and determine which are affected by the
		// current marquee selection
		final Collection<EditPart> marqueeSelectedEditParts = new ArrayList<>();
		for (final EditPart element : editPartsToProcess) {
			final GraphicalEditPart editPart = (GraphicalEditPart) element;
			if (isMarqueeSelectable(editPart) && isPrimaryMarqueeSelectedEditPart(editPart)) {
				marqueeSelectedEditParts.add(editPart);
			}
		}
		return marqueeSelectedEditParts;
	}

	/** Responsible of calculating those edit parts that should be regarded as being indirectly affected by the marquee
	 * selection. By default, the method calculates which edit parts are potential candidates based on the current
	 * marquee behavior and delegates to {@link #isMarqueeSelectable(GraphicalEditPart)} and
	 * {@link #isSecondaryMarqueeSelectedEditPart(Collection, EditPart)} to decide whether the candidate is to be
	 * included in the marquee selection.
	 *
	 * @param directlyMarqueeSelectedEditParts A collection containing those {@link EditPart}s that were already
	 *                                         identified as being directly affected by the marquee selection
	 * @return A {@link Collection} containing all {@link EditPart}s that are indirectly affected by the current marquee
	 *         selection
	 * @since 3.7 */
	private Collection<EditPart> calculateSecondaryMarqueeSelectedEditParts(
			final Collection<EditPart> directlyMarqueeSelectedEditParts) {

		final Collection<EditPart> editPartsToProcess = new HashSet<>();
		for (final EditPart directlyMarqueeSelectedEditPart : directlyMarqueeSelectedEditParts) {
			final GraphicalEditPart marqueeSelectedEditPart = (GraphicalEditPart) directlyMarqueeSelectedEditPart;
			editPartsToProcess.addAll(marqueeSelectedEditPart.getSourceConnections());
			editPartsToProcess.addAll(marqueeSelectedEditPart.getTargetConnections());
		}

		// process all edit parts and decide, whether they are indirectly
		// affected by marquee selection
		final Collection<EditPart> secondaryMarqueeSelectedEditParts = new HashSet<>();
		for (final EditPart element : editPartsToProcess) {
			final GraphicalEditPart editPart = (GraphicalEditPart) element;
			if (isSecondaryMarqueeSelectedEditPart(directlyMarqueeSelectedEditParts, editPart)) {
				secondaryMarqueeSelectedEditParts.add(editPart);
			}
		}
		return secondaryMarqueeSelectedEditParts;
	}

	private static Request createTargetRequest() {
		return MARQUEE_REQUEST;
	}

	/** Erases feedback if necessary and puts the tool into the terminal state. */
	@Override
	public void deactivate() {
		if (isInState(STATE_DRAG_IN_PROGRESS)) {
			eraseMarqueeFeedback();
			eraseTargetFeedback();
		}
		super.deactivate();
		setState(STATE_TERMINAL);
	}

	private void eraseMarqueeFeedback() {
		if (marqueeRectangleFigure != null) {
			removeFeedback(marqueeRectangleFigure);
			marqueeRectangleFigure = null;
		}
	}

	private void eraseTargetFeedback() {
		if (selectedEditParts == null) {
			return;
		}
		for (final EditPart editPart : selectedEditParts) {
			editPart.eraseTargetFeedback(getTargetRequest());
		}
	}

	/** @see org.eclipse.gef.tools.AbstractTool#getCommandName() */
	@Override
	protected String getCommandName() {
		return REQ_SELECTION;
	}

	/** Returns the current marquee selection rectangle.
	 *
	 * @return A {@link Rectangle} representing the current marquee selection.
	 * @since 3.7 */
	protected Rectangle getCurrentMarqueeSelectionRectangle() {
		return new Rectangle(getStartLocation(), getLocation());
	}

	/** Returns the current selection mode, i.e. default, append, or toggle
	 *
	 * @return on of {@link #DEFAULT_MODE}, {@link #APPEND_MODE}, or {@link #TOGGLE_MODE}
	 * @since 3.7 */
	protected int getCurrentSelectionMode() {
		return mode;
	}

	/** @see org.eclipse.gef.tools.AbstractTool#getDebugName() */
	@Override
	protected String getDebugName() {
		return "Marquee Tool: " + marqueeBehavior;//$NON-NLS-1$
	}

	private IFigure getMarqueeFeedbackFigure() {
		if (marqueeRectangleFigure == null) {
			marqueeRectangleFigure = createMarqueeFeedbackFigure();
			addFeedback(marqueeRectangleFigure);
		}
		return marqueeRectangleFigure;
	}

	@SuppressWarnings("static-method")
	protected MarqueeRectangleFigure createMarqueeFeedbackFigure() {
		return new MarqueeRectangleFigure();
	}

	private Request getTargetRequest() {
		if (targetRequest == null) {
			targetRequest = createTargetRequest();
		}
		return targetRequest;
	}

	/** @see org.eclipse.gef.tools.AbstractTool#handleButtonDown(int) */
	@Override
	protected boolean handleButtonDown(final int button) {
		if (!isCurrentViewerGraphical()) {
			return true;
		}
		if (button != 1) {
			setState(STATE_INVALID);
			handleInvalidInput();
		}
		if (stateTransition(STATE_INITIAL, STATE_DRAG_IN_PROGRESS)) {
			if (getCurrentInput().isModKeyDown(SWT.MOD1)) {
				setSelectionMode(TOGGLE_MODE);
			} else if (getCurrentInput().isShiftKeyDown()) {
				setSelectionMode(APPEND_MODE);
			} else {
				setSelectionMode(DEFAULT_MODE);
			}
		}
		return true;
	}

	/** @see org.eclipse.gef.tools.AbstractTool#handleButtonUp(int) */
	@Override
	protected boolean handleButtonUp(final int button) {
		if (stateTransition(STATE_DRAG_IN_PROGRESS, STATE_TERMINAL)) {
			eraseTargetFeedback();
			eraseMarqueeFeedback();
			performMarqueeSelect();
		}
		handleFinished();
		return true;
	}

	/** @see org.eclipse.gef.tools.AbstractTool#handleDragInProgress() */
	@Override
	protected boolean handleDragInProgress() {
		if (isInState(STATE_DRAG | STATE_DRAG_IN_PROGRESS)) {
			showMarqueeFeedback();
			eraseTargetFeedback();
			selectedEditParts = calculateMarqueeSelectedEditParts();
			showTargetFeedback();
		}
		return true;
	}

	/** @see org.eclipse.gef.tools.AbstractTool#handleFocusLost() */
	@Override
	protected boolean handleFocusLost() {
		if (isInState(STATE_DRAG | STATE_DRAG_IN_PROGRESS)) {
			handleFinished();
			return true;
		}
		return false;
	}

	/** This method is called when mouse or keyboard input is invalid and erases the feedback.
	 *
	 * @return <code>true</code> */
	@Override
	protected boolean handleInvalidInput() {
		eraseTargetFeedback();
		eraseMarqueeFeedback();
		return true;
	}

	/** Handles high-level processing of a key down event. KeyEvents are forwarded to the current viewer's
	 * {@link KeyHandler}, via {@link KeyHandler#keyPressed(KeyEvent)}.
	 *
	 * @see AbstractTool#handleKeyDown(KeyEvent) */
	@Override
	protected boolean handleKeyDown(final KeyEvent e) {
		if (super.handleKeyDown(e)) {
			return true;
		}
		if (getCurrentViewer().getKeyHandler() != null) {
			return getCurrentViewer().getKeyHandler().keyPressed(e);
		}
		return false;
	}

	/** Decides whether the given edit part may potentially be included in the current marquee selection.
	 *
	 * @param editPart the {@link EditPart} of interest
	 * @return <code>true</code> if the given edit part may be included into the marquee selection, <code>false</code>
	 *         otherwise
	 * @since 3.7 */
	@SuppressWarnings("static-method")
	protected boolean isMarqueeSelectable(final GraphicalEditPart editPart) {
		// IMPORTANT: MarqueeSelectionTool is not a TargetingTool, thus the
		// pre-selection does not depend on hit-testing. Therefore, the visible
		// state of the edit part's figure has to be taken into consideration as
		// well.
		return editPart.getTargetEditPart(MARQUEE_REQUEST) == editPart && editPart.isSelectable()
				&& FigureUtilities.isNotFullyClipped(editPart.getFigure());
	}

	/** Determines which edit parts are directly affected by the current marquee selection. Calculation is performed by
	 * regarding the current marquee selection rectangle ( {@link #getCurrentMarqueeSelectionRectangle()}), taking into
	 * consideration the current marquee behavior (contained vs. touched) that was provided (
	 * {@link #setMarqueeBehavior(int)} ).
	 *
	 * @param editPart the {@link EditPart} whose state is to be determined
	 * @return <code>true</code> if the {@link EditPart} should be regarded as being included in the current marquee
	 *         selection, <code>false</code> otherwise.
	 * @since 3.7 */
	private boolean isPrimaryMarqueeSelectedEditPart(final GraphicalEditPart editPart) {
		// figure bounds are used to determine if edit part is included in
		// selection
		final IFigure figure = editPart.getFigure();
		final Rectangle r = figure.getBounds().getCopy();
		figure.translateToAbsolute(r);

		boolean included = false;
		final Rectangle marqueeSelectionRectangle = getCurrentMarqueeSelectionRectangle();
		if (editPart instanceof ConnectionEditPart) {
			if (marqueeBehavior == MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED
					|| marqueeBehavior == MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_CONTAINED) {
				if (marqueeSelectionRectangle.intersects(r)) {
					// children will contain ConnectionEditParts only in case
					// behavior is BEHAVIOR_CONNECTIONS_TOUCHED or
					// BEHAVIOR_CONNECTIONS_CONTAINED
					final Rectangle relMarqueeRect = Rectangle.SINGLETON;
					figure.translateToRelative(relMarqueeRect.setBounds(marqueeSelectionRectangle));
					if (marqueeBehavior == MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED) {
						included = ((Connection) figure).getPoints().intersects(relMarqueeRect);
					} else {
						included = relMarqueeRect.contains(((Connection) figure).getPoints().getBounds());
					}
				}
			}
		} else {
			// otherwise children will only be 'node' edit parts
			if (marqueeBehavior == MarqueeSelectionTool.BEHAVIOR_NODES_TOUCHED
					|| marqueeBehavior == MarqueeSelectionTool.BEHAVIOR_NODES_TOUCHED_AND_RELATED_CONNECTIONS) {
				included = marqueeSelectionRectangle.intersects(r);
			} else if (marqueeBehavior == MarqueeSelectionTool.BEHAVIOR_NODES_CONTAINED
					|| marqueeBehavior == MarqueeSelectionTool.BEHAVIOR_NODES_CONTAINED_AND_RELATED_CONNECTIONS) {
				included = marqueeSelectionRectangle.contains(r);
			}
		}
		return included;
	}

	/** Determines which edit parts are indirectly affected by the current marquee selection through those edit parts
	 * being directly affected. In case of {@link #BEHAVIOR_NODES_CONTAINED_AND_RELATED_CONNECTIONS} or
	 * {@link #BEHAVIOR_NODES_TOUCHED_AND_RELATED_CONNECTIONS} marquee behavior, this method will be used to calculate
	 * the related connections after all respective nodes have been identified as primary selected edit parts.
	 *
	 * @param directlyMarqueeSelectedEditParts A collection of {@link EditPart}s which are regarded to be directly
	 *                                         included in the current marquee selection.
	 * @param editPart                         the {@link EditPart} of concern
	 * @return <code>true</code> if the {@link EditPart} should be regarded as being included in the current marquee
	 *         selection, <code>false</code> otherwise.
	 * @since 3.7 */
	private boolean isSecondaryMarqueeSelectedEditPart(final Collection<EditPart> directlyMarqueeSelectedEditParts,
			final EditPart editPart) {
		boolean included = false;
		if (editPart instanceof ConnectionEditPart
				&& (marqueeBehavior == MarqueeSelectionTool.BEHAVIOR_NODES_CONTAINED_AND_RELATED_CONNECTIONS
				|| marqueeBehavior == MarqueeSelectionTool.BEHAVIOR_NODES_TOUCHED_AND_RELATED_CONNECTIONS)) {
			// connections are included, if related nodes are included
			final ConnectionEditPart connection = (ConnectionEditPart) editPart;
			final GraphicalEditPart source = (GraphicalEditPart) connection.getSource();
			final GraphicalEditPart target = (GraphicalEditPart) connection.getTarget();
			final boolean sourceIncludedInMarqueeSelection = directlyMarqueeSelectedEditParts.contains(source);
			final boolean targetIncludedInMarqueeSelection = directlyMarqueeSelectedEditParts.contains(target);

			if (mode == DEFAULT_MODE) {
				// in default mode, select connection if source and
				// target are included in marqee selection
				included = sourceIncludedInMarqueeSelection && targetIncludedInMarqueeSelection;
			} else if (mode == APPEND_MODE) {
				// in append mode, the current viewer selection is of interest
				// as well, so select connection if not already selected and
				// source and target are already selected or will get selected
				included = connection.getSelected() == EditPart.SELECTED_NONE
						&& (getCurrentViewer().getSelectedEditParts().contains(source)
								|| sourceIncludedInMarqueeSelection)
						&& (getCurrentViewer().getSelectedEditParts().contains(target)
								|| targetIncludedInMarqueeSelection);
			} else if (mode == TOGGLE_MODE) {
				if (connection.getSelected() == EditPart.SELECTED_NONE) {
					// connection is currently deselected, include it in the
					// marquee selection, i.e. select it, if one of
					// source or target will become selected in the new viewer
					// selection
					included = ((source.getSelected() == EditPart.SELECTED_NONE && sourceIncludedInMarqueeSelection)
							|| (source.getSelected() != EditPart.SELECTED_NONE && !sourceIncludedInMarqueeSelection))
							&& ((target.getSelected() == EditPart.SELECTED_NONE && targetIncludedInMarqueeSelection)
									|| (target.getSelected() != EditPart.SELECTED_NONE
									&& !targetIncludedInMarqueeSelection));
				} else {
					// connection is currently selected, include it in marquee
					// selection, i.e. deselect it, if one of source or target
					// will become deselected in the new viewer selection
					included = (source.getSelected() != EditPart.SELECTED_NONE && sourceIncludedInMarqueeSelection)
							|| (target.getSelected() != EditPart.SELECTED_NONE && targetIncludedInMarqueeSelection);
				}
			}
		}
		return included;
	}

	/** MarqueeSelectionTool is only interested in GraphicalViewers, not TreeViewers.
	 *
	 * @see org.eclipse.gef.tools.AbstractTool#isViewerImportant(org.eclipse.gef.EditPartViewer) */
	@Override
	protected boolean isViewerImportant(final EditPartViewer viewer) {
		return isCurrentViewerGraphical();
	}

	private boolean isCurrentViewerGraphical() {
		return getCurrentViewer() instanceof GraphicalViewer;
	}

	/** Calculates and sets a new viewer selection based on the current marquee selection.
	 *
	 * By default, this method delegates to {@link #calculateMarqueeSelectedEditParts()} to obtain the set of edit
	 * parts, which should be regarded as being affected by the current marquee selection. It then calculates a new
	 * viewer selection based on the current selection state of all affected edit parts and the current selection mode
	 * of the tool ( {@link #getCurrentSelectionMode()}), as well as the current selection of the viewer (in case of
	 * APPEND mode), which is then passed to the current viewer.
	 *
	 * @since 3.7 */
	protected void performMarqueeSelect() {
		// determine which edit parts are affected by the current marquee
		// selection
		final Collection<EditPart> marqueeSelectedEditParts = calculateMarqueeSelectedEditParts();

		// calculate nodes/connections that are to be selected/deselected,
		// dependent on the current mode of the tool
		final Collection<EditPart> editPartsToSelect = new LinkedHashSet<>();
		final Collection<EditPart> editPartsToDeselect = new HashSet<>();
		for (final EditPart affectedEditPart : marqueeSelectedEditParts) {
			if (affectedEditPart.getSelected() == EditPart.SELECTED_NONE
					|| getCurrentSelectionMode() != TOGGLE_MODE) {
				editPartsToSelect.add(affectedEditPart);
			} else {
				editPartsToDeselect.add(affectedEditPart);
			}
		}

		// include the current viewer selection, if not in DEFAULT mode.
		if (getCurrentSelectionMode() != DEFAULT_MODE) {
			editPartsToSelect.addAll(getCurrentViewer().getSelectedEditParts());
			editPartsToSelect.removeAll(editPartsToDeselect);
		}

		getCurrentViewer().setSelection(new StructuredSelection(editPartsToSelect.toArray()));
	}

	/** Sets the type of parts that this tool will select. This method should only be invoked once: when the tool is
	 * being initialized.
	 *
	 * @param type {@link #BEHAVIOR_CONNECTIONS_TOUCHED} or {@link #BEHAVIOR_CONNECTIONS_CONTAINED}
	 *             {@link #BEHAVIOR_NODES_TOUCHED} or {@link #BEHAVIOR_NODES_CONTAINED} or
	 *             {@link #BEHAVIOR_NODES_TOUCHED_AND_RELATED_CONNECTIONS} or
	 *             {@link #BEHAVIOR_NODES_CONTAINED_AND_RELATED_CONNECTIONS}
	 * @since 3.1 */
	public void setMarqueeBehavior(final int type) {
		if (type != MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED
				&& type != MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_CONTAINED
				&& type != MarqueeSelectionTool.BEHAVIOR_NODES_TOUCHED
				&& type != MarqueeSelectionTool.BEHAVIOR_NODES_TOUCHED_AND_RELATED_CONNECTIONS
				&& type != MarqueeSelectionTool.BEHAVIOR_NODES_CONTAINED
				&& type != MarqueeSelectionTool.BEHAVIOR_NODES_CONTAINED_AND_RELATED_CONNECTIONS) {
			throw new IllegalArgumentException("Invalid marquee behaviour specified."); //$NON-NLS-1$
		}
		marqueeBehavior = type;
	}

	private void setSelectionMode(final int mode) {
		this.mode = mode;
	}

	/** @see org.eclipse.gef.Tool#setViewer(org.eclipse.gef.EditPartViewer) */
	@Override
	public void setViewer(final EditPartViewer viewer) {
		if (viewer == getCurrentViewer()) {
			return;
		}
		super.setViewer(viewer);
		if (viewer instanceof GraphicalViewer) {
			setDefaultCursor(Cursors.CROSS);
		} else {
			setDefaultCursor(Cursors.NO);
		}
	}

	private void showMarqueeFeedback() {
		final Rectangle rect = getCurrentMarqueeSelectionRectangle().getCopy();
		final IFigure marqueeFeedbackFigure = getMarqueeFeedbackFigure();
		marqueeFeedbackFigure.translateToRelative(rect);
		marqueeFeedbackFigure.setBounds(rect);
		marqueeFeedbackFigure.validate();
	}

	private void showTargetFeedback() {
		selectedEditParts.forEach(editPart -> editPart.showTargetFeedback(getTargetRequest()));
	}


	@Override
	protected void handleFinished() {
		/* Called when the mouse button is released. Overridden to do nothing, since a drag tracker does not need to
		 * unload when finished. */
	}
}

