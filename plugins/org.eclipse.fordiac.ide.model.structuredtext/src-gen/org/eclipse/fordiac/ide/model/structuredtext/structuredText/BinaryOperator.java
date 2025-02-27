/**
 * generated by Xtext 2.25.0
 */
package org.eclipse.fordiac.ide.model.structuredtext.structuredText;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Binary Operator</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.eclipse.fordiac.ide.model.structuredtext.structuredText.StructuredTextPackage#getBinaryOperator()
 * @model
 * @generated
 */
public enum BinaryOperator implements Enumerator
{
  /**
   * The '<em><b>OR</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #OR_VALUE
   * @generated
   * @ordered
   */
  OR(0, "OR", "OR"),

  /**
   * The '<em><b>XOR</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #XOR_VALUE
   * @generated
   * @ordered
   */
  XOR(1, "XOR", "XOR"),

  /**
   * The '<em><b>AND</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #AND_VALUE
   * @generated
   * @ordered
   */
  AND(2, "AND", "AND"),

  /**
   * The '<em><b>AMPERSAND</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #AMPERSAND_VALUE
   * @generated
   * @ordered
   */
  AMPERSAND(3, "AMPERSAND", "&"),

  /**
   * The '<em><b>EQ</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #EQ_VALUE
   * @generated
   * @ordered
   */
  EQ(4, "EQ", "="),

  /**
   * The '<em><b>NE</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #NE_VALUE
   * @generated
   * @ordered
   */
  NE(5, "NE", "<>"),

  /**
   * The '<em><b>LT</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #LT_VALUE
   * @generated
   * @ordered
   */
  LT(6, "LT", "<"),

  /**
   * The '<em><b>LE</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #LE_VALUE
   * @generated
   * @ordered
   */
  LE(7, "LE", "<="),

  /**
   * The '<em><b>GT</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #GT_VALUE
   * @generated
   * @ordered
   */
  GT(8, "GT", ">"),

  /**
   * The '<em><b>GE</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #GE_VALUE
   * @generated
   * @ordered
   */
  GE(9, "GE", ">="),

  /**
   * The '<em><b>ADD</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #ADD_VALUE
   * @generated
   * @ordered
   */
  ADD(10, "ADD", "+"),

  /**
   * The '<em><b>SUB</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #SUB_VALUE
   * @generated
   * @ordered
   */
  SUB(11, "SUB", "-"),

  /**
   * The '<em><b>MUL</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #MUL_VALUE
   * @generated
   * @ordered
   */
  MUL(12, "MUL", "*"),

  /**
   * The '<em><b>DIV</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #DIV_VALUE
   * @generated
   * @ordered
   */
  DIV(13, "DIV", "/"),

  /**
   * The '<em><b>MOD</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #MOD_VALUE
   * @generated
   * @ordered
   */
  MOD(14, "MOD", "MOD"),

  /**
   * The '<em><b>POWER</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #POWER_VALUE
   * @generated
   * @ordered
   */
  POWER(15, "POWER", "**");

  /**
   * The '<em><b>OR</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #OR
   * @model
   * @generated
   * @ordered
   */
  public static final int OR_VALUE = 0;

  /**
   * The '<em><b>XOR</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #XOR
   * @model
   * @generated
   * @ordered
   */
  public static final int XOR_VALUE = 1;

  /**
   * The '<em><b>AND</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #AND
   * @model
   * @generated
   * @ordered
   */
  public static final int AND_VALUE = 2;

  /**
   * The '<em><b>AMPERSAND</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #AMPERSAND
   * @model literal="&amp;"
   * @generated
   * @ordered
   */
  public static final int AMPERSAND_VALUE = 3;

  /**
   * The '<em><b>EQ</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #EQ
   * @model literal="="
   * @generated
   * @ordered
   */
  public static final int EQ_VALUE = 4;

  /**
   * The '<em><b>NE</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #NE
   * @model literal="&lt;&gt;"
   * @generated
   * @ordered
   */
  public static final int NE_VALUE = 5;

  /**
   * The '<em><b>LT</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #LT
   * @model literal="&lt;"
   * @generated
   * @ordered
   */
  public static final int LT_VALUE = 6;

  /**
   * The '<em><b>LE</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #LE
   * @model literal="&lt;="
   * @generated
   * @ordered
   */
  public static final int LE_VALUE = 7;

  /**
   * The '<em><b>GT</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #GT
   * @model literal="&gt;"
   * @generated
   * @ordered
   */
  public static final int GT_VALUE = 8;

  /**
   * The '<em><b>GE</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #GE
   * @model literal="&gt;="
   * @generated
   * @ordered
   */
  public static final int GE_VALUE = 9;

  /**
   * The '<em><b>ADD</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #ADD
   * @model literal="+"
   * @generated
   * @ordered
   */
  public static final int ADD_VALUE = 10;

  /**
   * The '<em><b>SUB</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #SUB
   * @model literal="-"
   * @generated
   * @ordered
   */
  public static final int SUB_VALUE = 11;

  /**
   * The '<em><b>MUL</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #MUL
   * @model literal="*"
   * @generated
   * @ordered
   */
  public static final int MUL_VALUE = 12;

  /**
   * The '<em><b>DIV</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #DIV
   * @model literal="/"
   * @generated
   * @ordered
   */
  public static final int DIV_VALUE = 13;

  /**
   * The '<em><b>MOD</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #MOD
   * @model
   * @generated
   * @ordered
   */
  public static final int MOD_VALUE = 14;

  /**
   * The '<em><b>POWER</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #POWER
   * @model literal="**"
   * @generated
   * @ordered
   */
  public static final int POWER_VALUE = 15;

  /**
   * An array of all the '<em><b>Binary Operator</b></em>' enumerators.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private static final BinaryOperator[] VALUES_ARRAY =
    new BinaryOperator[]
    {
      OR,
      XOR,
      AND,
      AMPERSAND,
      EQ,
      NE,
      LT,
      LE,
      GT,
      GE,
      ADD,
      SUB,
      MUL,
      DIV,
      MOD,
      POWER,
    };

  /**
   * A public read-only list of all the '<em><b>Binary Operator</b></em>' enumerators.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public static final List<BinaryOperator> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

  /**
   * Returns the '<em><b>Binary Operator</b></em>' literal with the specified literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param literal the literal.
   * @return the matching enumerator or <code>null</code>.
   * @generated
   */
  public static BinaryOperator get(String literal)
  {
    for (int i = 0; i < VALUES_ARRAY.length; ++i)
    {
      BinaryOperator result = VALUES_ARRAY[i];
      if (result.toString().equals(literal))
      {
        return result;
      }
    }
    return null;
  }

  /**
   * Returns the '<em><b>Binary Operator</b></em>' literal with the specified name.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param name the name.
   * @return the matching enumerator or <code>null</code>.
   * @generated
   */
  public static BinaryOperator getByName(String name)
  {
    for (int i = 0; i < VALUES_ARRAY.length; ++i)
    {
      BinaryOperator result = VALUES_ARRAY[i];
      if (result.getName().equals(name))
      {
        return result;
      }
    }
    return null;
  }

  /**
   * Returns the '<em><b>Binary Operator</b></em>' literal with the specified integer value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the integer value.
   * @return the matching enumerator or <code>null</code>.
   * @generated
   */
  public static BinaryOperator get(int value)
  {
    switch (value)
    {
      case OR_VALUE: return OR;
      case XOR_VALUE: return XOR;
      case AND_VALUE: return AND;
      case AMPERSAND_VALUE: return AMPERSAND;
      case EQ_VALUE: return EQ;
      case NE_VALUE: return NE;
      case LT_VALUE: return LT;
      case LE_VALUE: return LE;
      case GT_VALUE: return GT;
      case GE_VALUE: return GE;
      case ADD_VALUE: return ADD;
      case SUB_VALUE: return SUB;
      case MUL_VALUE: return MUL;
      case DIV_VALUE: return DIV;
      case MOD_VALUE: return MOD;
      case POWER_VALUE: return POWER;
    }
    return null;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private final int value;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private final String name;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private final String literal;

  /**
   * Only this class can construct instances.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private BinaryOperator(int value, String name, String literal)
  {
    this.value = value;
    this.name = name;
    this.literal = literal;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public int getValue()
  {
    return value;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String getName()
  {
    return name;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String getLiteral()
  {
    return literal;
  }

  /**
   * Returns the literal value of the enumerator, which is its string representation.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String toString()
  {
    return literal;
  }
  
} //BinaryOperator
