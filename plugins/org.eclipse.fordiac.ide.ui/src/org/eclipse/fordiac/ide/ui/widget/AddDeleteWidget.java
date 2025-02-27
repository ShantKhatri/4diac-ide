/*******************************************************************************
 * Copyright (c) 2019, 2020 Johannes Kepler University Linz
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Alois Zoitl - initial implementation
 * Bianca Wiesmayr - enhanced add functionality
 * Daniel Lindhuber - added separate delete listener
 *******************************************************************************/
package org.eclipse.fordiac.ide.ui.widget;

import java.util.List;

import org.eclipse.fordiac.ide.ui.providers.CommandProvider;
import org.eclipse.fordiac.ide.ui.providers.CreationCommand;
import org.eclipse.fordiac.ide.ui.providers.CreationCommandProvider;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class AddDeleteWidget {
	private Button createButton;
	private Button deleteButton;
	private Composite container;

	public void createControls(final Composite parent, final FormToolkit widgetFactory) {
		container = createContainer(widgetFactory, parent);

		createAddButton(widgetFactory, container);

		createDeleteButton(widgetFactory, container);

		// initially nothing should be selected therefore deactivate the buttons
		setButtonEnablement(false);
	}

	public void setVisible(final boolean visible) {
		setVisible(visible, container);
	}

	private static void setVisible(final boolean visible, final Control widget) {
		widget.setVisible(visible);
		if (null != widget.getLayoutData()) {
			((GridData) widget.getLayoutData()).exclude = !visible;
		} else {
			widget.setLayoutData(GridDataFactory.fillDefaults().exclude(!visible).create());
		}
		widget.getParent().pack();
	}

	public void setVisibleCreateButton(final boolean visible) {
		setVisible(visible, createButton);
	}

	public void setVisibleDeleteButton(final boolean visible) {
		setVisible(visible, deleteButton);
	}

	protected void createDeleteButton(final FormToolkit widgetFactory, final Composite container) {
		deleteButton = widgetFactory.createButton(container, "", SWT.PUSH); //$NON-NLS-1$
		deleteButton.setToolTipText("Delete selected interface element"); //$NON-NLS-1$
		deleteButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
		deleteButton.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
	}

	protected void createAddButton(final FormToolkit widgetFactory, final Composite container) {
		createButton = widgetFactory.createButton(container, "", SWT.PUSH); //$NON-NLS-1$
		createButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD));
		createButton.setToolTipText("Create element");
		createButton.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
	}

	protected static Composite createContainer(final FormToolkit widgetFactory, final Composite parent) {
		final Composite container = widgetFactory.createComposite(parent, SWT.NONE);
		final GridData buttonCompLayoutData = new GridData(SWT.CENTER, SWT.TOP, false, false);

		container.setLayoutData(buttonCompLayoutData);
		container.setLayout(new GridLayout(1, true));
		return container;
	}

	public void setButtonEnablement(final boolean enable) {
		deleteButton.setEnabled(enable);
		deleteButton
		.setImage((enable) ? PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE)
				: PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE_DISABLED));
	}

	public void setCreateButtonEnablement(final boolean enable) {
		createButton.setEnabled(enable);
	}

	public void addCreateListener(final Listener createListener) {
		createButton.addListener(SWT.Selection, createListener);
	}

	public void addDeleteListener(final Listener deleteListener) {
		deleteButton.addListener(SWT.Selection, deleteListener);
	}

	public void bindToTableViewer(final TableViewer viewer, final CommandExecutor executor,
			final CreationCommandProvider addCommand, final CommandProvider deleteCommand) {

		final Listener createListener = getAddListener(viewer, executor, addCommand);

		final Listener deleteListener = getDeleteListener(viewer, executor, deleteCommand);

		bindToTableViewer(viewer, createListener, deleteListener);
	}

	public void bindToTableViewer(final TableViewer viewer, final Listener createListener,
			final Listener deleteListener) {

		addCreateListener(createListener);
		addDeleteListener(deleteListener);

		viewer.addSelectionChangedListener(ev -> setButtonEnablement(!viewer.getSelection().isEmpty()));

		viewer.getTable().addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(final KeyEvent e) {
				// Nothing to do here
			}

			@Override
			public void keyPressed(final KeyEvent e) {
				if ((e.keyCode == SWT.INSERT) && (e.stateMask == 0)) {
					createListener.handleEvent(null);
				} else if ((e.character == SWT.DEL) && (e.stateMask == 0)) {
					deleteListener.handleEvent(null);
				}
			}
		});
	}

	public static Listener getSelectionListener(final TableViewer viewer, final CommandExecutor executor,
			final CommandProvider commandProvider) {
		return ev -> {
			if (!viewer.getStructuredSelection().isEmpty()) {
				executeCompoundCommandForList(viewer, viewer.getStructuredSelection().toList(), executor,
						commandProvider);
			}
		};
	}

	protected static void executeCompoundCommandForList(final TableViewer viewer, final List<Object> selection,
			final CommandExecutor executor, final CommandProvider commandProvider) {
		final CompoundCommand cmd = new CompoundCommand();
		selection.stream().forEach(elem -> cmd.add(commandProvider.getCommand(elem)));
		executor.executeCommand(cmd);
		viewer.refresh();
	}

	public static Listener getDeleteListener(final TableViewer viewer, final CommandExecutor executor,
			final CommandProvider commandProvider) {
		return ev -> {
			if (!viewer.getStructuredSelection().isEmpty()) {
				int pos = viewer.getTable().getSelectionIndices()[0];
				executeCompoundCommandForList(viewer, viewer.getStructuredSelection().toList(), executor,
						commandProvider);
				final int itemCnt = viewer.getTable().getItemCount();
				if (pos <= itemCnt) {
					if (pos == itemCnt) {
						pos--;
					}
					viewer.getTable().forceFocus();
					// the selection has to be set again via the table viewer for the widgets to recognize it
					viewer.getTable().setSelection(pos);
					viewer.setSelection(viewer.getSelection());
				}
			}
		};
	}

	private static Listener getAddListener(final TableViewer viewer, final CommandExecutor executor,
			final CreationCommandProvider commandProvider) {
		return ev -> {
			final CreationCommand cmd = commandProvider.getCommand(getReferencedElement(viewer));
			executor.executeCommand((Command) cmd);
			viewer.refresh();
			final StructuredSelection selection = new StructuredSelection(cmd.getCreatedElement());
			viewer.setSelection(selection);
		};
	}

	private static Object getReferencedElement(final TableViewer viewer) {
		if (!viewer.getStructuredSelection().isEmpty()) {
			return viewer.getStructuredSelection().toList().get(viewer.getStructuredSelection().size() - 1);
		}
		return null;
	}
}
