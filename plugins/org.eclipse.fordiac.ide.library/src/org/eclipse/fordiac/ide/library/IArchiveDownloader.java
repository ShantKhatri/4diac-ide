/*******************************************************************************
 * Copyright (c) 2024 Primetals Technologies Austria GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Aigner - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.library;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface IArchiveDownloader {
	/**
	 * Lists the available libraries
	 *
	 * @return symbolic names of the available libraries
	 */
	List<String> availableLibraries() throws IOException;

	/**
	 * Lists the available versions of a specific library
	 *
	 * @param symbolicName symbolic name of library
	 * @return the available versions of the specified library, or an empty list if
	 *         library is not available
	 */
	List<String> availableVersions(String symbolicName) throws IOException;

	/**
	 * Download latest available version of the specified library in regard to given
	 * (preferred) version
	 *
	 * @param symbolicName     symbolic name of library
	 * @param version          version (specific or range) of the specified library
	 * @param preferredVersion preferred version to be downloaded, ignored if
	 *                         {@code null} or not contained in version range
	 * @return {@code Path} of the downloaded library archive, or {@code null} if
	 *         archive couldn't be downloaded
	 */
	Path downloadLibrary(String symbolicName, String version, String preferredVersion) throws IOException;
}
