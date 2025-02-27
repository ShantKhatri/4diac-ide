/*******************************************************************************
 * Copyright (c) 2008 - 2016 Profactor GbmH, TU Wien ACIN, fortiss GmbH
 * 				 2018 - 2019 Johannes Kepler University
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Gerhard Ebenhofer, Alois Zoitl, Monika Wenger
 *     - initial API and implementation and/or initial documentation
 *   Alois Zoitl - fixed copy/paste handling
 *   Alois Zoitl - added diagram font preference
 *******************************************************************************/
package org.eclipse.fordiac.ide.gef;

import java.util.EventObject;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.fordiac.ide.gef.dnd.ParameterDropTargetListener;
import org.eclipse.fordiac.ide.gef.editparts.ZoomScalableFreeformRootEditPart;
import org.eclipse.fordiac.ide.gef.handlers.AdvancedGraphicalViewerKeyHandler;
import org.eclipse.fordiac.ide.gef.listeners.DiagramFontChangeListener;
import org.eclipse.fordiac.ide.gef.listeners.FigureFontUpdateListener;
import org.eclipse.fordiac.ide.gef.listeners.IFontUpdateListener;
import org.eclipse.fordiac.ide.gef.print.PrintPreviewAction;
import org.eclipse.fordiac.ide.gef.ruler.FordiacRulerComposite;
import org.eclipse.fordiac.ide.gef.tools.AdvancedPanningSelectionTool;
import org.eclipse.fordiac.ide.model.libraryElement.AutomationSystem;
import org.eclipse.fordiac.ide.model.ui.editors.AdvancedScrollingGraphicalViewer;
import org.eclipse.fordiac.ide.ui.editors.I4diacModelEditor;
import org.eclipse.fordiac.ide.ui.preferences.PreferenceConstants;
import org.eclipse.fordiac.ide.util.UntypedEditorInput;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.FreeformGraphicalRootEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.AlignmentAction;
import org.eclipse.gef.ui.actions.DirectEditAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.rulers.RulerComposite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * A base editor for various graphical editors.
 *
 * @author Gerhard Ebenhofer (gerhard.ebenhofer@profactor.at)
 */
public abstract class DiagramEditorWithFlyoutPalette extends GraphicalEditorWithFlyoutPalette
implements ITabbedPropertySheetPageContributor, I4diacModelEditor {

	/** The PROPERTY_CONTRIBUTOR_ID. */
	public static final String PROPERTY_CONTRIBUTOR_ID = "org.eclipse.fordiac.ide.application.editors.DiagramEditor"; //$NON-NLS-1$

	/** The shared key handler. */
	private KeyHandler sharedKeyHandler;

	/** The outline page. */
	private DiagramOutlinePage outlinePage;

	// needed for tabbed property sheets
	@Override
	public CommandStack getCommandStack() {
		return super.getCommandStack();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#commandStackChanged(java.util
	 * .EventObject)
	 */
	@Override
	public void commandStackChanged(final EventObject event) {
		firePropertyChange(IEditorPart.PROP_DIRTY);
		super.commandStackChanged(event);
	}

	/**
	 * refresh all child editparts when editor gets focus.
	 */
	@Override
	public void setFocus() {
		super.setFocus();
		for (final Object element : getGraphicalViewer().getRootEditPart().getChildren()) {
			final EditPart ep = (EditPart) element;
			ep.refresh();
		}
	}

	private RulerComposite rulerComp;

	@Override
	public void createPartControl(final Composite parent) {
		super.createPartControl(parent);

		final AdvancedScrollingGraphicalViewer viewer = getGraphicalViewer();
		if (viewer.getControl() instanceof FigureCanvas) {
			final FigureCanvas canvas = (FigureCanvas) viewer.getControl();

			Display.getDefault().asyncExec(() -> {
				viewer.flush();
				// if an editpart is selected then the viewer has bee created with something to be shown centered
				// therefore we will not show the initial position
				// do not use getSelection() here because it will return always at least one element
				if (viewer.getSelectedEditParts().isEmpty()) {
					final Point scrollPos = getInitialScrollPos();
					canvas.scrollTo(scrollPos.x, scrollPos.y);
				} else {
					// if we have a selected edit part we want to show it in the middle
					viewer.revealEditPart((EditPart) viewer.getSelectedEditParts().get(0));
				}
			});
		}
	}

	protected Point getInitialScrollPos() {
		final FreeformGraphicalRootEditPart rootEditPart = (FreeformGraphicalRootEditPart) getGraphicalViewer()
				.getRootEditPart();
		final Rectangle drawingAreaBounds = rootEditPart.getContentPane().getBounds();
		return new Point(drawingAreaBounds.x - DiagramEditor.INITIAL_SCROLL_OFFSET,
				drawingAreaBounds.y - DiagramEditor.INITIAL_SCROLL_OFFSET);
	}

	@Override
	protected void createGraphicalViewer(final Composite parent) {
		rulerComp = new FordiacRulerComposite(parent, SWT.NONE);

		final AdvancedScrollingGraphicalViewer viewer = new AdvancedScrollingGraphicalViewer();
		viewer.createControl(rulerComp);

		setGraphicalViewer(viewer);
		configureGraphicalViewer();
		hookGraphicalViewer();
		initializeGraphicalViewer();

		final IFontUpdateListener rootFigureListener = new FigureFontUpdateListener(
				((ScalableFreeformRootEditPart) viewer.getRootEditPart()).getFigure(),
				PreferenceConstants.DIAGRAM_FONT);
		final IPropertyChangeListener fontChangeListener = new DiagramFontChangeListener(rootFigureListener);

		rootFigureListener.updateFonts(); // ensure that root figure has the right font set

		JFaceResources.getFontRegistry().addListener(fontChangeListener);
		viewer.getControl()
		.addDisposeListener(e -> JFaceResources.getFontRegistry().removeListener(fontChangeListener));

		rulerComp.setGraphicalViewer(getGraphicalViewer());
	}

	@Override
	protected Control getGraphicalControl() {
		return rulerComp;
	}

	protected ScalableFreeformRootEditPart createRootEditPart() {
		return new ZoomScalableFreeformRootEditPart(getSite(), getActionRegistry());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
	 */
	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		final AdvancedScrollingGraphicalViewer viewer = getGraphicalViewer();

		final ScalableFreeformRootEditPart root = createRootEditPart();

		final ContextMenuProvider cmp = getContextMenuProvider(viewer, root.getZoomManager());
		if (cmp != null) {
			viewer.setContextMenu(cmp);
			getSite().registerContextMenu("org.eclipse.fordiac.ide.gef.contextmenu", //$NON-NLS-1$
					cmp, viewer);
		}

		viewer.setRootEditPart(root);
		viewer.setEditPartFactory(getEditPartFactory());

		final AdvancedGraphicalViewerKeyHandler keyHandler = new AdvancedGraphicalViewerKeyHandler(viewer);
		keyHandler.setParent(getCommonKeyHandler());
		viewer.setKeyHandler(keyHandler);

		viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.MOD1), MouseWheelZoomHandler.SINGLETON);

	}

	public ZoomManager getZoomManger() {
		return ((ScalableFreeformRootEditPart) (getGraphicalViewer().getRootEditPart())).getZoomManager();
	}

	/**
	 * Gets the edits the part factory.
	 *
	 * @return the edits the part factory
	 */
	protected abstract EditPartFactory getEditPartFactory();

	/**
	 * Gets the context menu provider.
	 *
	 * @param viewer the viewer
	 * @param zoom   the zoom manager of the root edit part
	 *
	 * @return the context menu provider
	 */
	protected abstract ContextMenuProvider getContextMenuProvider(ScrollingGraphicalViewer viewer,
			ZoomManager zoomManager);

	/**
	 * Creates the transfer drop target listener.
	 *
	 * @return the transfer drop target listener
	 */
	protected abstract TransferDropTargetListener createTransferDropTargetListener();

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#initializeGraphicalViewer()
	 */
	@Override
	protected void initializeGraphicalViewer() {
		final GraphicalViewer viewer = getGraphicalViewer();
		viewer.setContents(getModel());
		// listen for dropped parts
		final TransferDropTargetListener listener = createTransferDropTargetListener();
		if (listener != null) {
			viewer.addDropTargetListener(listener);
		}
		// enable drag from palette
		getGraphicalViewer().addDropTargetListener(new TemplateTransferDropTargetListener(getGraphicalViewer()));
		viewer.addDropTargetListener(new ParameterDropTargetListener(getGraphicalViewer()));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite,
	 * org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
		setModel(input);
		super.init(site, input);
		setEditorPartName(input);
		final ActionRegistry registry = getActionRegistry();
		final IActionBars bars = site.getActionBars();
		String id = ActionFactory.UNDO.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.REDO.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.DELETE.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		bars.updateActionBars();
	}

	protected void setEditorPartName(final IEditorInput input) {
		if (input.getName() != null) {
			setPartName(input.getName());
		}
	}

	protected void updateEditorTitle(final String newTitle) {
		((UntypedEditorInput) getEditorInput()).setName(newTitle); // update the editor input so that the tooltip and
		// header bars are correct as well
		setPartName(newTitle);
	}

	/**
	 * Sets the model.
	 *
	 * @param input the new model
	 */
	protected void setModel(final IEditorInput input) {

		setEditDomain(new DefaultEditDomain(this));
		getEditDomain().setDefaultTool(createDefaultTool());
		getEditDomain().setActiveTool(getEditDomain().getDefaultTool());
		// use one "System - Wide" command stack to avoid inconsistencies due to
		// undo redo
		if (null != getSystem()) {
			getEditDomain().setCommandStack(getSystem().getCommandStack());
		}
	}

	@SuppressWarnings("static-method")
	protected AdvancedPanningSelectionTool createDefaultTool() {
		return new AdvancedPanningSelectionTool();
	}

	/**
	 * Gets the system.
	 *
	 * @return the system
	 */
	public abstract AutomationSystem getSystem();

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	@Override
	public abstract void doSave(final IProgressMonitor monitor);

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#doSaveAs()
	 */
	@Override
	public abstract void doSaveAs();

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Gets the common key handler.
	 *
	 * @return the common key handler
	 */
	protected KeyHandler getCommonKeyHandler() {
		if (sharedKeyHandler == null) {
			sharedKeyHandler = new KeyHandler();
			sharedKeyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
					getActionRegistry().getAction(ActionFactory.DELETE.getId()));
			sharedKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0),
					getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT));
		}
		return sharedKeyHandler;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getAdapter(
	 * java.lang.Class)
	 */
	@Override
	public Object getAdapter(final Class type) {
		if (type == ZoomManager.class) {
			return getGraphicalViewer().getProperty(ZoomManager.class.toString());
		}
		if (type == IContentOutlinePage.class) {
			outlinePage = new DiagramOutlinePage(getGraphicalViewer());
			return outlinePage;
		}
		if (type == IPropertySheetPage.class) {
			return new TabbedPropertySheetPage(this);
		}

		return super.getAdapter(type);
	}

	/**
	 * Gets the editor.
	 *
	 * @return the editor
	 */
	protected FigureCanvas getEditor() {
		return (FigureCanvas) getGraphicalViewer().getControl();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#createActions()
	 */
	@Override
	protected void createActions() {
		final ActionRegistry registry = getActionRegistry();
		IAction action;

		action = new DirectEditAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.LEFT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.RIGHT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.TOP);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.BOTTOM);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.CENTER);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.MIDDLE);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		super.createActions();

		// remove the default print action and register our own one
		registry.removeAction(registry.getAction(ActionFactory.PRINT.getId()));
		action = new PrintPreviewAction(getGraphicalViewer());
		registry.registerAction(action);
		getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.PRINT.getId(), action);
	}

	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
		super.selectionChanged(part, selection);
		updateActions(getSelectionActions());
	}

	/** Gets the selection actions list
	 *
	 * @return the sel actions */
	@SuppressWarnings("rawtypes")
	public List getSelActions() {
		return getSelectionActions();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		outlinePage = null;
		super.dispose();
	}

	/**
	 * Returns the GraphicalViewer of this Editor.
	 *
	 * @return the GraphicalViewer
	 */
	public AdvancedScrollingGraphicalViewer getViewer() {
		return getGraphicalViewer();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#
	 * createPaletteViewerProvider()
	 */
	@Override
	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(getEditDomain()) {
			@Override
			protected void configurePaletteViewer(final PaletteViewer viewer) {
				super.configurePaletteViewer(viewer);
				viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
			}
		};
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor
	 * #getContributorId()
	 */
	@Override
	public String getContributorId() {
		return PROPERTY_CONTRIBUTOR_ID;
	}

	@Override
	protected AdvancedScrollingGraphicalViewer getGraphicalViewer() {
		return (AdvancedScrollingGraphicalViewer) super.getGraphicalViewer();
	}

}
