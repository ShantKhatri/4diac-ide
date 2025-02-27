/*******************************************************************************
 * Copyright (c) 2008, 2020, 2021 Profactor GmbH, TU Wien ACIN, fortiss GmbH, Johannes
 *                          Kepler University Linz, Primetals Technologies Austria GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Gerhard Ebenhofer, Alois Zoitl, Monika Wenger
 *   - initial API and implementation and/or initial documentation
 *   Daniel Lindhuber
 *   - added possibility to free interface elements for layouting
 *   Alois Zoitl - Integrated code from child classes to reduce code duplication
 *               - Reworked figure so that interface bars are always at the most
 *                 left or right position
 *               - forwarding the getDragDracker request to the parent edit parts
 *                 as with the new interface bar this didn't happen automatically
 *   Daniel Lindhuber - instance comment
 *   Alois Zoitl - fixed layout of interface bars, cleaned cration code for these
 *******************************************************************************/
package org.eclipse.fordiac.ide.application.editparts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.fordiac.ide.gef.draw2d.SingleLineBorder;
import org.eclipse.fordiac.ide.gef.editparts.AbstractFBNetworkEditPart;
import org.eclipse.fordiac.ide.gef.editparts.InterfaceEditPart;
import org.eclipse.fordiac.ide.model.FordiacKeywords;
import org.eclipse.fordiac.ide.model.libraryElement.Attribute;
import org.eclipse.fordiac.ide.model.libraryElement.CompositeFBType;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.IInterfaceElement;
import org.eclipse.fordiac.ide.model.libraryElement.InterfaceList;
import org.eclipse.fordiac.ide.model.libraryElement.SubAppType;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.swt.events.ControlListener;

public abstract class EditorWithInterfaceEditPart extends AbstractFBNetworkEditPart {
	private static final int MIN_INTERFACE_BAR_WIDTH = 200;
	private static final int TOP_BOTTOM_MARGIN = 1;
	private static final int LEFT_RIGHT_MARGIN = 10;
	private static final Insets RIGHT_LIST_BORDER_INSET = new Insets(TOP_BOTTOM_MARGIN, 0, TOP_BOTTOM_MARGIN,
			LEFT_RIGHT_MARGIN);  // no left margin to have interface directly at inner border
	private static final Insets LEFT_LIST_BORDER_INSET = new Insets(TOP_BOTTOM_MARGIN, LEFT_RIGHT_MARGIN,
			TOP_BOTTOM_MARGIN, 0);  // no right margin to have interface directly at inner border

	private static final int BASE_WIDTH = 400;
	private static final int BASE_HEIGHT = 200;

	private class InterfaceBarLayout extends BorderLayout {
		@Override
		protected Dimension calculatePreferredSize(final IFigure figure, final int wHint, final int hHint) {
			final Rectangle newBounds = new Rectangle();
			newBounds.setSize(super.calculatePreferredSize(figure, wHint, hHint));
			newBounds.setLocation(getContentPane().getFreeformExtent().getLocation());
			newBounds.x -= leftInterfaceContainer.getPreferredSize().width;
			newBounds.y -= commentContainer.getPreferredSize().height;
			if (newBounds.x > 0) {
				newBounds.x = 0;
			}
			if (newBounds.y > 0) {
				newBounds.y = 0;
			}
			// get the size of the feedback/handle layer and use it to calculate our size, this is needed when stuff is
			// moved around or
			FreeformLayer layer = (FreeformLayer) getLayer(LayerConstants.FEEDBACK_LAYER);
			layer.validate();
			newBounds.union(layer.getFreeformExtent());
			layer = (FreeformLayer) getLayer(LayerConstants.HANDLE_LAYER);
			layer.validate();
			newBounds.union(layer.getFreeformExtent());

			// take the border into our calculation
			newBounds.shrink(leftInterfaceContainer.getInsets());

			final Rectangle resultingBounds = calculateModuloExtent(newBounds);
			// it is important to keep the width and height in the constraints to -1 otherwise it will never be
			// recalculated
			figure.getParent().setConstraint(figure, new Rectangle(resultingBounds.x, resultingBounds.y, -1, -1));
			return resultingBounds.getSize();
		}

		private Rectangle calculateModuloExtent(final Rectangle newBounds) {
			// adjust size to be a multiple of the base width/height
			final int x = calcAxisOrigin(newBounds.x, BASE_WIDTH);
			final int y = calcAxisOrigin(newBounds.y, BASE_HEIGHT);
			final int width = calcAxisExtent(newBounds.x, x, newBounds.width, BASE_WIDTH);
			final int height = calcAxisExtent(newBounds.y, y, newBounds.height, BASE_HEIGHT);
			return new Rectangle(x, y, width, height);
		}

		private int calcAxisExtent(final int baseOrigin, final int newOrigin, final int sourceExtent,
				final int baseUnit) {
			final int startExtent = (sourceExtent + baseOrigin) - newOrigin;

			int newExtend = ((startExtent / baseUnit) + 1) * baseUnit;
			if (newExtend < (3 * baseUnit)) {
				newExtend = 3 * baseUnit;
			}
			return newExtend;
		}

		private int calcAxisOrigin(final int axisPos, final int baseUnit) {
			if (axisPos < 0) {
				// when negative we need to go one beyond to have the correct origin
				return ((axisPos / baseUnit) - 1) * baseUnit;
			}
			return (axisPos / baseUnit) * baseUnit;
		}

	}

	private static class MinSizeFigure extends Figure {
		@Override
		public Dimension getPreferredSize(final int wHint, final int hHint) {
			final Dimension prefSize = super.getPreferredSize(wHint, hHint);
			// we want to have this container a minimum width
			prefSize.union(getMinimumSize());
			return prefSize;
		}
	}

	private Figure leftInterfaceContainer;
	private Figure leftEventContainer;
	private Figure leftVarContainer;
	private Figure leftAdapterContainer;
	private Figure rightInterfaceContainer;
	private Figure rightEventContainer;
	private Figure rightVarContainer;
	private Figure rightAdapterContainer;
	private FreeformLayer contentContainer;
	private ControlListener controlListener;

	private InstanceComment instanceComment;
	private Figure commentContainer;

	@Override
	protected IFigure createFigure() {
		final IFigure mainFigure = new Figure();
		final BorderLayout mainLayout = new InterfaceBarLayout();
		mainLayout.setVerticalSpacing(-1); // remove spacing between comment and interface container

		mainFigure.setLayoutManager(mainLayout);
		mainFigure.setOpaque(false);

		createLeftInterface(mainFigure);

		contentContainer = new FreeformLayer();
		contentContainer.setLayoutManager(new FreeformLayout());
		mainFigure.add(contentContainer, BorderLayout.CENTER);

		createRightInterface(mainFigure);

		createCommentContainer(mainFigure);

		final IFigure root = super.createFigure();
		root.setBorder(null);  // we don't want a border here
		root.add(mainFigure);
		root.setConstraint(mainFigure, new Rectangle(0, 0, -1, -1));

		return root;
	}

	private void createLeftInterface(final IFigure mainFigure) {
		leftInterfaceContainer = createRootContainer(mainFigure, BorderLayout.LEFT);
		configureLeftContainer(leftInterfaceContainer);

		final Figure leftInnerContainer = createInnerContainer(leftInterfaceContainer, LEFT_LIST_BORDER_INSET);
		configureLeftContainer(leftInnerContainer);

		leftEventContainer = createInterfaceElementsContainer(leftInnerContainer);
		configureLeftContainer(leftEventContainer);
		leftVarContainer = createInterfaceElementsContainer(leftInnerContainer);
		configureLeftContainer(leftVarContainer);
		leftAdapterContainer = createInterfaceElementsContainer(leftInnerContainer);
		configureLeftContainer(leftAdapterContainer);
	}

	private void createRightInterface(final IFigure mainFigure) {
		rightInterfaceContainer = createRootContainer(mainFigure, BorderLayout.RIGHT);

		final Figure rightInnerContainer = createInnerContainer(rightInterfaceContainer, RIGHT_LIST_BORDER_INSET);

		rightEventContainer = createInterfaceElementsContainer(rightInnerContainer);
		rightVarContainer = createInterfaceElementsContainer(rightInnerContainer);
		rightAdapterContainer = createInterfaceElementsContainer(rightInnerContainer);
	}

	private static Figure createRootContainer(final IFigure parent, final Integer layoutConstraint) {
		final Figure rootContainer = new Figure();
		rootContainer.setLayoutManager(new ToolbarLayout(false));
		rootContainer.setOpaque(true);
		rootContainer.setBorder(new SingleLineBorder());
		parent.add(rootContainer, layoutConstraint);
		return rootContainer;
	}

	private static void configureLeftContainer(final Figure container) {
		((ToolbarLayout) container.getLayoutManager()).setMinorAlignment(OrderedLayout.ALIGN_BOTTOMRIGHT);
	}

	private static Figure createInnerContainer(final IFigure parent, final Insets borderInset) {
		final Figure innerContainer = new MinSizeFigure();
		innerContainer.setMinimumSize(new Dimension(MIN_INTERFACE_BAR_WIDTH, -1));
		final ToolbarLayout innerLayout = new ToolbarLayout(false);
		innerContainer.setLayoutManager(innerLayout);
		innerContainer.setBorder(new MarginBorder(borderInset));
		parent.add(innerContainer);
		return innerContainer;
	}

	private static Figure createInterfaceElementsContainer(final IFigure parent) {
		final Figure container = new Figure();
		container.setLayoutManager(new ToolbarLayout(false));
		parent.add(container);
		return container;
	}


	private void createCommentContainer(final IFigure mainFigure) {
		commentContainer = new Figure();
		final Border border = new SingleLineBorder() {

			private final Insets insets = new Insets(5); // spacing

			@Override
			public Insets getInsets(final IFigure figure) {
				return insets;
			}

		};
		commentContainer.setBorder(border);
		final ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
		layout.setStretchMinorAxis(false);
		commentContainer.setOpaque(true);

		commentContainer.setLayoutManager(layout);
		mainFigure.add(commentContainer, BorderLayout.TOP);
	}

	public Figure getLeftInterfaceContainer() {
		return leftInterfaceContainer;
	}

	public Figure getLeftEventInterfaceContainer() {
		return leftEventContainer;
	}

	public Figure getLeftVarInterfaceContainer() {
		return leftVarContainer;
	}

	public Figure getLeftAdapterInterfaceContainer() {
		return leftAdapterContainer;
	}

	public Figure getRightInterfaceContainer() {
		return rightInterfaceContainer;
	}

	public Figure getRightEventInterfaceContainer() {
		return rightEventContainer;
	}

	public Figure getRightVarInterfaceContainer() {
		return rightVarContainer;
	}

	public Figure getRightAdapterInterfaceContainer() {
		return rightAdapterContainer;
	}

	@Override
	public FreeformLayer getContentPane() {
		return contentContainer;
	}

	protected abstract InterfaceList getInterfaceList();

	@Override
	protected List<?> getModelChildren() {
		if (getModel() != null) {
			final ArrayList<Object> children = new ArrayList<>(super.getModelChildren());
			final InterfaceList ifList = getInterfaceList();
			children.addAll(ifList.getEventInputs());
			children.addAll(ifList.getEventOutputs());
			children.addAll(ifList.getInputVars());
			children.addAll(ifList.getOutputVars());
			if (showAdapterPorts()) {
				children.addAll(ifList.getPlugs());
				children.addAll(ifList.getSockets());
			}
			if (isInstance()) {
				children.add(getInstanceComment());
			}
			return children;
		}
		return Collections.emptyList();
	}

	private boolean isInstance() {
		return getModel().eContainer() instanceof FBNetworkElement;
	}

	private InstanceComment getInstanceComment() {
		if (null == instanceComment) {
			instanceComment = new InstanceComment((FBNetworkElement) getModel().eContainer());
		}
		return instanceComment;
	}

	private boolean showAdapterPorts() {
		// show adapters if it is not a cfb type
		return !((getModel().eContainer() instanceof CompositeFBType)
				&& (!(getModel().eContainer() instanceof SubAppType)));
	}

	@Override
	protected void addChildVisual(final EditPart childEditPart, final int index) {
		if (childEditPart instanceof InterfaceEditPart) {
			addChildVisualInterfaceElement((InterfaceEditPart) childEditPart);
		} else if (childEditPart instanceof InstanceCommentEditPart) {
			final Figure commentFigure = ((InstanceCommentEditPart) childEditPart).getFigure();
			commentFigure.setBorder(null);
			commentContainer.add(commentFigure);
		} else {
			super.addChildVisual(childEditPart, index);
		}
	}

	@Override
	public void deactivate() {
		super.deactivate();
		if ((controlListener != null) && (getParent() != null) && (getParent().getViewer() != null)
				&& (getParent().getViewer().getControl() != null)) {
			getParent().getViewer().getControl().removeControlListener(controlListener);
		}
	}

	/**
	 * Removes the childEditParts figures from the correct container.
	 *
	 * @param childEditPart the child edit part
	 */
	@Override
	protected void removeChildVisual(final EditPart childEditPart) {
		if (childEditPart instanceof InterfaceEditPart) {
			removeChildVisualInterfaceElement((InterfaceEditPart) childEditPart);
		} else if (childEditPart instanceof InstanceCommentEditPart) {
			commentContainer.remove(((InstanceCommentEditPart) childEditPart).getFigure());
		} else {
			super.removeChildVisual(childEditPart);
		}
	}

	public void removeChildVisualInterfaceElement(final InterfaceEditPart childEditPart) {
		// we need to get the parent here for handling the padding figure
		final IFigure child = childEditPart.getFigure().getParent();
		final IFigure container = getChildVisualContainer(childEditPart);
		if (child.getParent() == container) {
			container.remove(child);
		} else {
			getContentPane().remove(child);
		}
	}

	protected Figure getChildVisualContainer(final InterfaceEditPart childEditPart) {
		if (childEditPart.getModel().isIsInput()) {
			if (childEditPart.isEvent()) {
				return getLeftEventInterfaceContainer();
			} else if (childEditPart.isAdapter()) {
				return (showAdapterPorts()) ? getLeftAdapterInterfaceContainer() : getLeftInterfaceContainer();
			}
			return getLeftVarInterfaceContainer();
		}
		if (childEditPart.isEvent()) {
			return getRightEventInterfaceContainer();
		} else if (childEditPart.isAdapter()) {
			return (showAdapterPorts()) ? getRightAdapterInterfaceContainer() : getRightInterfaceContainer();
		}
		return getRightVarInterfaceContainer();

	}

	public void addChildVisualInterfaceElement(final InterfaceEditPart childEditPart) {
		final IFigure child = childEditPart.getFigure();
		final InterfaceList ifList = getInterfaceList();
		final Figure targetFigure = getChildVisualContainer(childEditPart);
		int index = 0;
		if (childEditPart.getModel().isIsInput()) { // use model isInput! because EditPart.isInput treats inputs as
			// outputs for visual appearance
			if (childEditPart.isEvent()) {
				index = ifList.getEventInputs().indexOf(childEditPart.getModel());
			} else if (childEditPart.isAdapter()) {
				index = ifList.getSockets().indexOf(childEditPart.getModel());
			} else {
				index = ifList.getInputVars().indexOf(childEditPart.getModel());
			}
		} else {
			if (childEditPart.isEvent()) {
				index = ifList.getEventOutputs().indexOf(childEditPart.getModel());
			} else if (childEditPart.isAdapter()) {
				index = ifList.getPlugs().indexOf(childEditPart.getModel());
			} else {
				index = ifList.getOutputVars().indexOf(childEditPart.getModel());
			}
		}
		final int containerSize = targetFigure.getChildren().size();
		targetFigure.add(createSideBarFigure(childEditPart), (index >= containerSize) ? containerSize : index);
		child.setVisible(isVarVisible(childEditPart));
	}

	private static IFigure createSideBarFigure(final InterfaceEditPart ep) {
		final IFigure container = new Figure();
		container.setLayoutManager(new ToolbarLayout());

		final int yPositionFromAttribute = getYPositionFromAttribute(ep.getModel());

		if (yPositionFromAttribute > 0) {
			final IFigure paddingFigure = new MinSizeFigure();
			paddingFigure.setMinimumSize(new Dimension(-1, yPositionFromAttribute));
			container.add(paddingFigure);
		}

		container.add(ep.getFigure());

		return container;
	}

	private static int getYPositionFromAttribute(final IInterfaceElement ie) {
		final Attribute attribute = ie.getAttribute(FordiacKeywords.INTERFACE_Y_POSITION);

		if (attribute != null) {
			return Integer.parseInt(attribute.getValue());
		}

		return 0;
	}

	@SuppressWarnings("static-method") // this method can be overridden so that editors can hide certain interface
	// elements (e.g., adapters in CFBs)
	protected boolean isVarVisible(final EditPart childEditPart) {
		return true;
	}

	@Override
	public void performRequest(final Request request) {
		// REQ_DIRECT_EDIT -> first select 0.4 sec pause -> click -> edit
		// REQ_OPEN -> doubleclick

		if (((request.getType() == RequestConstants.REQ_DIRECT_EDIT)
				|| (request.getType() == RequestConstants.REQ_OPEN)) && (request instanceof SelectionRequest)) {
			((FBNetworkRootEditPart) getParent()).performDirectEdit((SelectionRequest) request);
		} else {
			super.performRequest(request);
		}
	}

	@Override
	public DragTracker getDragTracker(final Request req) {
		return getParent().getDragTracker(req);
	}

	@Override
	protected void refreshVisuals() {
		final List<EditPart> ies = (List<EditPart>) getChildren().stream().filter(InterfaceEditPart.class::isInstance)
				.collect(Collectors.toList());
		ies.forEach(this::removeChild);
		ies.forEach(ie -> addChild(ie, -1));
	}

}
