/**
 * generated by Xtext 2.25.0
 */
package org.eclipse.fordiac.ide.structuredtextalgorithm.sTAlgorithm.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fordiac.ide.model.Palette.PalettePackage;

import org.eclipse.fordiac.ide.model.data.DataPackage;

import org.eclipse.fordiac.ide.model.libraryElement.LibraryElementPackage;

import org.eclipse.fordiac.ide.structuredtextalgorithm.sTAlgorithm.STAlgorithm;
import org.eclipse.fordiac.ide.structuredtextalgorithm.sTAlgorithm.STAlgorithmBody;
import org.eclipse.fordiac.ide.structuredtextalgorithm.sTAlgorithm.STAlgorithmFactory;
import org.eclipse.fordiac.ide.structuredtextalgorithm.sTAlgorithm.STAlgorithmPackage;
import org.eclipse.fordiac.ide.structuredtextalgorithm.sTAlgorithm.STAlgorithms;

import org.eclipse.fordiac.ide.structuredtextcore.stcore.STCorePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class STAlgorithmPackageImpl extends EPackageImpl implements STAlgorithmPackage
{
  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass stAlgorithmsEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass stAlgorithmEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass stAlgorithmBodyEClass = null;

  /**
   * Creates an instance of the model <b>Package</b>, registered with
   * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
   * package URI value.
   * <p>Note: the correct way to create the package is via the static
   * factory method {@link #init init()}, which also performs
   * initialization of the package, or returns the registered package,
   * if one already exists.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.eclipse.emf.ecore.EPackage.Registry
   * @see org.eclipse.fordiac.ide.structuredtextalgorithm.sTAlgorithm.STAlgorithmPackage#eNS_URI
   * @see #init()
   * @generated
   */
  private STAlgorithmPackageImpl()
  {
    super(eNS_URI, STAlgorithmFactory.eINSTANCE);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private static boolean isInited = false;

  /**
   * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
   *
   * <p>This method is used to initialize {@link STAlgorithmPackage#eINSTANCE} when that field is accessed.
   * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #eNS_URI
   * @see #createPackageContents()
   * @see #initializePackageContents()
   * @generated
   */
  public static STAlgorithmPackage init()
  {
    if (isInited) return (STAlgorithmPackage)EPackage.Registry.INSTANCE.getEPackage(STAlgorithmPackage.eNS_URI);

    // Obtain or create and register package
    Object registeredSTAlgorithmPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
    STAlgorithmPackageImpl theSTAlgorithmPackage = registeredSTAlgorithmPackage instanceof STAlgorithmPackageImpl ? (STAlgorithmPackageImpl)registeredSTAlgorithmPackage : new STAlgorithmPackageImpl();

    isInited = true;

    // Initialize simple dependencies
    STCorePackage.eINSTANCE.eClass();
    LibraryElementPackage.eINSTANCE.eClass();
    DataPackage.eINSTANCE.eClass();
    PalettePackage.eINSTANCE.eClass();

    // Create package meta-data objects
    theSTAlgorithmPackage.createPackageContents();

    // Initialize created meta-data
    theSTAlgorithmPackage.initializePackageContents();

    // Mark meta-data to indicate it can't be changed
    theSTAlgorithmPackage.freeze();

    // Update the registry and return the package
    EPackage.Registry.INSTANCE.put(STAlgorithmPackage.eNS_URI, theSTAlgorithmPackage);
    return theSTAlgorithmPackage;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EClass getSTAlgorithms()
  {
    return stAlgorithmsEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EReference getSTAlgorithms_Algorithms()
  {
    return (EReference)stAlgorithmsEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EClass getSTAlgorithm()
  {
    return stAlgorithmEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EAttribute getSTAlgorithm_Name()
  {
    return (EAttribute)stAlgorithmEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EReference getSTAlgorithm_Body()
  {
    return (EReference)stAlgorithmEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EClass getSTAlgorithmBody()
  {
    return stAlgorithmBodyEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EReference getSTAlgorithmBody_VarTempDeclarations()
  {
    return (EReference)stAlgorithmBodyEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EReference getSTAlgorithmBody_Statements()
  {
    return (EReference)stAlgorithmBodyEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public STAlgorithmFactory getSTAlgorithmFactory()
  {
    return (STAlgorithmFactory)getEFactoryInstance();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private boolean isCreated = false;

  /**
   * Creates the meta-model objects for the package.  This method is
   * guarded to have no affect on any invocation but its first.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void createPackageContents()
  {
    if (isCreated) return;
    isCreated = true;

    // Create classes and their features
    stAlgorithmsEClass = createEClass(ST_ALGORITHMS);
    createEReference(stAlgorithmsEClass, ST_ALGORITHMS__ALGORITHMS);

    stAlgorithmEClass = createEClass(ST_ALGORITHM);
    createEAttribute(stAlgorithmEClass, ST_ALGORITHM__NAME);
    createEReference(stAlgorithmEClass, ST_ALGORITHM__BODY);

    stAlgorithmBodyEClass = createEClass(ST_ALGORITHM_BODY);
    createEReference(stAlgorithmBodyEClass, ST_ALGORITHM_BODY__VAR_TEMP_DECLARATIONS);
    createEReference(stAlgorithmBodyEClass, ST_ALGORITHM_BODY__STATEMENTS);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private boolean isInitialized = false;

  /**
   * Complete the initialization of the package and its meta-model.  This
   * method is guarded to have no affect on any invocation but its first.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void initializePackageContents()
  {
    if (isInitialized) return;
    isInitialized = true;

    // Initialize package
    setName(eNAME);
    setNsPrefix(eNS_PREFIX);
    setNsURI(eNS_URI);

    // Obtain other dependent packages
    STCorePackage theSTCorePackage = (STCorePackage)EPackage.Registry.INSTANCE.getEPackage(STCorePackage.eNS_URI);

    // Create type parameters

    // Set bounds for type parameters

    // Add supertypes to classes

    // Initialize classes and features; add operations and parameters
    initEClass(stAlgorithmsEClass, STAlgorithms.class, "STAlgorithms", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getSTAlgorithms_Algorithms(), this.getSTAlgorithm(), null, "algorithms", null, 0, -1, STAlgorithms.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(stAlgorithmEClass, STAlgorithm.class, "STAlgorithm", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEAttribute(getSTAlgorithm_Name(), ecorePackage.getEString(), "name", null, 0, 1, STAlgorithm.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getSTAlgorithm_Body(), this.getSTAlgorithmBody(), null, "body", null, 0, 1, STAlgorithm.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(stAlgorithmBodyEClass, STAlgorithmBody.class, "STAlgorithmBody", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getSTAlgorithmBody_VarTempDeclarations(), theSTCorePackage.getVarDeclarationBlock(), null, "varTempDeclarations", null, 0, -1, STAlgorithmBody.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getSTAlgorithmBody_Statements(), theSTCorePackage.getSTStatement(), null, "statements", null, 0, -1, STAlgorithmBody.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    // Create resource
    createResource(eNS_URI);
  }

} //STAlgorithmPackageImpl
