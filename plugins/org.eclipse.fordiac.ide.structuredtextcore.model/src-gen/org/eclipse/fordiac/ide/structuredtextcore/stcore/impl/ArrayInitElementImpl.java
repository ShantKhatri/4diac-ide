/**
 * *******************************************************************************
 * Copyright (c) 2022 Primetals Technologies GmbH,
 *               2022 Martin Erich Jobst
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *    Martin Jobst, Martin Melik Merkumians
 *      - initial API and implementation and/or initial documentation
 * *******************************************************************************
 */
package org.eclipse.fordiac.ide.structuredtextcore.stcore.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fordiac.ide.structuredtextcore.stcore.ArrayInitElement;
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STCorePackage;
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STExpression;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Array Init Element</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fordiac.ide.structuredtextcore.stcore.impl.ArrayInitElementImpl#getIndexOrInitExpression <em>Index Or Init Expression</em>}</li>
 *   <li>{@link org.eclipse.fordiac.ide.structuredtextcore.stcore.impl.ArrayInitElementImpl#getInitExpression <em>Init Expression</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ArrayInitElementImpl extends MinimalEObjectImpl.Container implements ArrayInitElement {
	/**
	 * The cached value of the '{@link #getIndexOrInitExpression() <em>Index Or Init Expression</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIndexOrInitExpression()
	 * @generated
	 * @ordered
	 */
	protected STExpression indexOrInitExpression;

	/**
	 * The cached value of the '{@link #getInitExpression() <em>Init Expression</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInitExpression()
	 * @generated
	 * @ordered
	 */
	protected STExpression initExpression;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ArrayInitElementImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return STCorePackage.Literals.ARRAY_INIT_ELEMENT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public STExpression getIndexOrInitExpression() {
		return indexOrInitExpression;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetIndexOrInitExpression(STExpression newIndexOrInitExpression, NotificationChain msgs) {
		STExpression oldIndexOrInitExpression = indexOrInitExpression;
		indexOrInitExpression = newIndexOrInitExpression;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, STCorePackage.ARRAY_INIT_ELEMENT__INDEX_OR_INIT_EXPRESSION, oldIndexOrInitExpression, newIndexOrInitExpression);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIndexOrInitExpression(STExpression newIndexOrInitExpression) {
		if (newIndexOrInitExpression != indexOrInitExpression) {
			NotificationChain msgs = null;
			if (indexOrInitExpression != null)
				msgs = ((InternalEObject)indexOrInitExpression).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - STCorePackage.ARRAY_INIT_ELEMENT__INDEX_OR_INIT_EXPRESSION, null, msgs);
			if (newIndexOrInitExpression != null)
				msgs = ((InternalEObject)newIndexOrInitExpression).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - STCorePackage.ARRAY_INIT_ELEMENT__INDEX_OR_INIT_EXPRESSION, null, msgs);
			msgs = basicSetIndexOrInitExpression(newIndexOrInitExpression, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, STCorePackage.ARRAY_INIT_ELEMENT__INDEX_OR_INIT_EXPRESSION, newIndexOrInitExpression, newIndexOrInitExpression));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public STExpression getInitExpression() {
		return initExpression;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetInitExpression(STExpression newInitExpression, NotificationChain msgs) {
		STExpression oldInitExpression = initExpression;
		initExpression = newInitExpression;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, STCorePackage.ARRAY_INIT_ELEMENT__INIT_EXPRESSION, oldInitExpression, newInitExpression);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setInitExpression(STExpression newInitExpression) {
		if (newInitExpression != initExpression) {
			NotificationChain msgs = null;
			if (initExpression != null)
				msgs = ((InternalEObject)initExpression).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - STCorePackage.ARRAY_INIT_ELEMENT__INIT_EXPRESSION, null, msgs);
			if (newInitExpression != null)
				msgs = ((InternalEObject)newInitExpression).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - STCorePackage.ARRAY_INIT_ELEMENT__INIT_EXPRESSION, null, msgs);
			msgs = basicSetInitExpression(newInitExpression, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, STCorePackage.ARRAY_INIT_ELEMENT__INIT_EXPRESSION, newInitExpression, newInitExpression));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case STCorePackage.ARRAY_INIT_ELEMENT__INDEX_OR_INIT_EXPRESSION:
				return basicSetIndexOrInitExpression(null, msgs);
			case STCorePackage.ARRAY_INIT_ELEMENT__INIT_EXPRESSION:
				return basicSetInitExpression(null, msgs);
			default:
				return super.eInverseRemove(otherEnd, featureID, msgs);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case STCorePackage.ARRAY_INIT_ELEMENT__INDEX_OR_INIT_EXPRESSION:
				return getIndexOrInitExpression();
			case STCorePackage.ARRAY_INIT_ELEMENT__INIT_EXPRESSION:
				return getInitExpression();
			default:
				return super.eGet(featureID, resolve, coreType);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case STCorePackage.ARRAY_INIT_ELEMENT__INDEX_OR_INIT_EXPRESSION:
				setIndexOrInitExpression((STExpression)newValue);
				return;
			case STCorePackage.ARRAY_INIT_ELEMENT__INIT_EXPRESSION:
				setInitExpression((STExpression)newValue);
				return;
			default:
				super.eSet(featureID, newValue);
				return;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case STCorePackage.ARRAY_INIT_ELEMENT__INDEX_OR_INIT_EXPRESSION:
				setIndexOrInitExpression((STExpression)null);
				return;
			case STCorePackage.ARRAY_INIT_ELEMENT__INIT_EXPRESSION:
				setInitExpression((STExpression)null);
				return;
			default:
				super.eUnset(featureID);
				return;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case STCorePackage.ARRAY_INIT_ELEMENT__INDEX_OR_INIT_EXPRESSION:
				return indexOrInitExpression != null;
			case STCorePackage.ARRAY_INIT_ELEMENT__INIT_EXPRESSION:
				return initExpression != null;
			default:
				return super.eIsSet(featureID);
		}
	}

} //ArrayInitElementImpl
