/*
 * generated by Xtext 2.25.0
 */
package org.eclipse.fordiac.ide.structuredtextfunctioneditor.validation;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fordiac.ide.structuredtextcore.validation.STCoreValidator;

public abstract class AbstractSTFunctionValidator extends STCoreValidator {
	
	@Override
	protected List<EPackage> getEPackages() {
		List<EPackage> result = new ArrayList<EPackage>(super.getEPackages());
		result.add(org.eclipse.fordiac.ide.structuredtextfunctioneditor.sTFunction.STFunctionPackage.eINSTANCE);
		result.add(EPackage.Registry.INSTANCE.getEPackage("org.eclipse.fordiac.ide.model.libraryElement"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.eclipse.org/fordiac/ide/structuredtextcore/STCore"));
		return result;
	}
}
