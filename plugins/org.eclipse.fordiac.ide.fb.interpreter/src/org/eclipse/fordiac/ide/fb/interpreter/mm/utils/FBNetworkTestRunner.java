/*******************************************************************************
 * Copyright (c) 2022 Paul Pavlicek and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Paul Pavlicek, Bianca Wiesmayr
 *       - initial implementation and/or documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.fb.interpreter.mm.utils;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fordiac.ide.fb.interpreter.OpSem.EventManager;
import org.eclipse.fordiac.ide.fb.interpreter.OpSem.EventOccurrence;
import org.eclipse.fordiac.ide.fb.interpreter.OpSem.FBTransaction;
import org.eclipse.fordiac.ide.fb.interpreter.OpSem.Transaction;
import org.eclipse.fordiac.ide.fb.interpreter.api.EventManagerFactory;
import org.eclipse.fordiac.ide.fb.interpreter.api.FBTransactionBuilder;
import org.eclipse.fordiac.ide.model.libraryElement.Event;
import org.eclipse.fordiac.ide.model.libraryElement.FB;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetwork;

public final class FBNetworkTestRunner {
	public static EList<Transaction> runFBNetworkTest(final FBNetwork network, final String fbInstanceName,
			final String pinName) {
		final FB initialFb = network.getFBNamed(fbInstanceName);
		if (initialFb == null) {
			throw new IllegalArgumentException("FB to trigger does not exist"); //$NON-NLS-1$
		}

		final Event event = (Event) initialFb.getInterfaceElement(pinName);
		if (event == null) {
			throw new IllegalArgumentException("Event pin does not exist"); //$NON-NLS-1$
		}

		final EventManager eventManager = EventManagerFactory.createFrom(event, EcoreUtil.copy(network));
		EventManagerUtils.processNetwork(eventManager);

		checkInitialTransaction(eventManager.getTransactions().get(0), fbInstanceName, pinName);

		return eventManager.getTransactions();
	}

	private static void checkInitialTransaction(final Transaction t0, final String fbInstanceName,
			final String pinName) {
		assert (pinName.equals(t0.getInputEventOccurrence().getEvent().getName())); // $NON-NLS-1$
		assert (fbInstanceName.equals(t0.getInputEventOccurrence().getParentFB().getName())); // $NON-NLS-1$
	}

	public static void checkTransaction(final Transaction res, final FBTransactionBuilder expectedResult)
			throws IllegalTraceException {
		final FBTransaction result = (FBTransaction) res;
		checkEvent(result.getInputEventOccurrence(), expectedResult.getInputEventName());

		if (result.getOutputEventOccurrences().size() != expectedResult.getOutputEventNames().size()) {
			throw new IllegalTraceException("Expected " + expectedResult.getOutputEventNames().size()
					+ " transactions, received " + result.getOutputEventOccurrences().size());
		}
		for (int i = 0; i < result.getOutputEventOccurrences().size(); i++) {
			checkEvent(result.getOutputEventOccurrences().get(i), expectedResult.getOutputEventName(i));
		}
	}

	private static void checkEvent(final EventOccurrence result, final String expectedEventName)
			throws IllegalTraceException {
		final String[] expectedEventNames = expectedEventName.split("\\."); //$NON-NLS-1$
		if (result == null) {
			throw new IllegalTraceException("Incomplete Transaction: input event occurrence was null");
		}
		if (expectedEventNames.length == 2) {
			if (!matchFbInstanceName(result, expectedEventNames)) {
				throw new IllegalTraceException(
						"Unexpected input event occurrence at FB " + result.getParentFB().getName() //$NON-NLS-1$
								+ ", expected: " + expectedEventName);
			}
			if (!matchEventName(result, expectedEventNames[1])) {
				throw new IllegalTraceException("Unexpected input event occurrence, expected: " + expectedEventName); //$NON-NLS-1$
			}
		} else if (!matchEventName(result, expectedEventNames[0])) {
			throw new IllegalTraceException("Unexpected input event occurrence, expected: " + expectedEventName); //$NON-NLS-1$
		}
	}

	private static boolean matchEventName(final EventOccurrence result, final String expectedEventNames) {
		return result.getEvent().getName().equals(expectedEventNames);
	}

	private static boolean matchFbInstanceName(final EventOccurrence result, final String[] expectedEventNames) {
		return result.getParentFB().getName().equals(expectedEventNames[0]);
	}

	public static class IllegalTraceException extends Exception {
		private static final long serialVersionUID = -8594643083723450800L;

		public IllegalTraceException(final String message) {
			super(message);
		}
	}

	private FBNetworkTestRunner() {
		throw new UnsupportedOperationException();
	}
}
