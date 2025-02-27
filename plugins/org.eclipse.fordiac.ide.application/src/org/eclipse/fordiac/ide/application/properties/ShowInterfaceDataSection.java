/*******************************************************************************
 * Copyright (c) 2020 Primetals Technologies Germany GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bianca Wiesmayr
 *     - initial API and implementation and/or initial documentation
 *   Daniel Lindhuber
 *     - updated label provider
 *******************************************************************************/
package org.eclipse.fordiac.ide.application.properties;

import org.eclipse.fordiac.ide.application.editparts.AbstractFBNElementEditPart;
import org.eclipse.fordiac.ide.application.editparts.SubAppForFBNetworkEditPart;
import org.eclipse.fordiac.ide.application.properties.ShowInterfaceEventSection.CellImmutableModifier;
import org.eclipse.fordiac.ide.gef.properties.AbstractEditInterfaceDataSection;
import org.eclipse.fordiac.ide.model.commands.change.ChangeDataTypeCommand;
import org.eclipse.fordiac.ide.model.commands.change.ChangeInterfaceOrderCommand;
import org.eclipse.fordiac.ide.model.commands.create.CreateInterfaceElementCommand;
import org.eclipse.fordiac.ide.model.commands.delete.DeleteInterfaceCommand;
import org.eclipse.fordiac.ide.model.data.DataType;
import org.eclipse.fordiac.ide.model.edit.providers.DataLabelProvider;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.FBType;
import org.eclipse.fordiac.ide.model.libraryElement.IInterfaceElement;
import org.eclipse.fordiac.ide.model.libraryElement.StructManipulator;
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class ShowInterfaceDataSection extends AbstractEditInterfaceDataSection {
	@Override
	protected FBNetworkElement getInputType(final Object input) {
		if ((input instanceof SubAppForFBNetworkEditPart) || (input instanceof AbstractFBNElementEditPart)) {
			return (FBNetworkElement) ((EditPart) input).getModel();
		}
		if (input instanceof FBNetworkElement) {
			return (FBNetworkElement) input;
		}
		return null;
	}

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage tabbedPropertySheetPage) {
		createButtons = false;
		super.createControls(parent, tabbedPropertySheetPage);
		final Table inputTable = (Table) getInputsViewer().getControl();
		inputTable.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		inputTable.setHeaderBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		getInputsViewer().setCellModifier(new CellImmutableModifier());

		final Table outputTable = (Table) getOutputsViewer().getControl();
		outputTable.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		outputTable.setHeaderBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		getOutputsViewer().setCellModifier(new CellImmutableModifier());
	}

	@Override
	protected CreateInterfaceElementCommand newCreateCommand(final IInterfaceElement interfaceElement, final boolean isInput) {
		return null;
	}

	@Override
	protected CreateInterfaceElementCommand newInsertCommand(final IInterfaceElement interfaceElement,
			final boolean isInput,
			final int index) {
		return null;
	}

	@Override
	protected DeleteInterfaceCommand newDeleteCommand(final IInterfaceElement selection) {
		return null;
	}

	@Override
	protected ChangeInterfaceOrderCommand newOrderCommand(final IInterfaceElement selection, final boolean moveUp) {
		return null;
	}

	@Override
	protected ChangeDataTypeCommand newChangeTypeCommand(final VarDeclaration data, final DataType newType) {
		return null;
	}

	@Override
	protected FBNetworkElement getType() {
		return (FBNetworkElement) type;
	}

	@Override
	protected InterfaceCellModifier getCellModifier(final TableViewer viewer) {
		return new DataInterfaceCellModifier(viewer) {
			@Override
			public boolean canModify(final Object element, final String property) {
				return false;
			}
		};
	}

	@Override
	protected LabelProvider getLabelProvider() {
		return new DataLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				if ((element instanceof VarDeclaration) && (columnIndex == DataLabelProvider.INITIALVALUE_COL_INDEX)) {
					final VarDeclaration varDecl = (VarDeclaration) element;
					final FBType fbType = varDecl.getFBNetworkElement().getType();
					VarDeclaration varDeclType;

					if (fbType != null) {
						varDeclType = fbType.getInterfaceList().getVariable(varDecl.getName());
						if ((varDeclType == null) && (varDecl.getFBNetworkElement() instanceof StructManipulator)) {
							// struct var of a struct manipulator inside a typed subapp/composite
							varDeclType = ((StructManipulator) varDecl.getFBNetworkElement()).getStructType().getMemberVariables()
									.stream()
									.filter(memberVar -> varDecl.getName().equals(memberVar.getName()))
									.findFirst()
									.orElse(null);
						}
					} else {
						// var declaration of untyped subapp inside typed subapp/composite
						varDeclType = varDecl;
					}

					if ((varDeclType == null) || (varDeclType.getValue() == null)) {
						return ""; //$NON-NLS-1$
					}

					return varDeclType.getValue().getValue();
				}
				return super.getColumnText(element, columnIndex);
			}
		};
	}
}
