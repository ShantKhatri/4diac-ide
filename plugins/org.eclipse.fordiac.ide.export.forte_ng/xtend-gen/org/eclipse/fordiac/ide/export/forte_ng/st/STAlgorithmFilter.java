/**
 * Copyright (c) 2019 fortiss GmbH
 *               2020 Johannes Kepler University Linz
 * 				 2021 Primetals Technologies Austria GmbH
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Martin Jobst - initial API and implementation and/or initial documentation
 *   Ernst Blecha - add multibit partial access
 *   Martin Melik Merkumians - fixes partial access and changes type resolution from
 * 		String information to object comparison
 */
package org.eclipse.fordiac.ide.export.forte_ng.st;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fordiac.ide.export.forte_ng.ForteLibraryElementTemplate;
import org.eclipse.fordiac.ide.model.FordiacKeywords;
import org.eclipse.fordiac.ide.model.data.DataType;
import org.eclipse.fordiac.ide.model.data.StructuredType;
import org.eclipse.fordiac.ide.model.datatype.helper.IecTypes;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterDeclaration;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterFBType;
import org.eclipse.fordiac.ide.model.libraryElement.BaseFBType;
import org.eclipse.fordiac.ide.model.libraryElement.BasicFBType;
import org.eclipse.fordiac.ide.model.libraryElement.FB;
import org.eclipse.fordiac.ide.model.libraryElement.FBType;
import org.eclipse.fordiac.ide.model.libraryElement.IInterfaceElement;
import org.eclipse.fordiac.ide.model.libraryElement.InterfaceList;
import org.eclipse.fordiac.ide.model.libraryElement.STAlgorithm;
import org.eclipse.fordiac.ide.model.libraryElement.SimpleFBType;
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration;
import org.eclipse.fordiac.ide.model.structuredtext.parser.antlr.StructuredTextParser;
import org.eclipse.fordiac.ide.model.structuredtext.resource.StructuredTextResource;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.AdapterRoot;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.AdapterVariable;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.Argument;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.ArrayVariable;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.AssignmentStatement;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.BinaryExpression;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.BinaryOperator;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.BoolLiteral;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.Call;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.CaseClause;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.CaseStatement;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.Constant;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.ContinueStatement;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.ElseClause;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.ElseIfClause;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.ExitStatement;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.Expression;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.FBCall;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.ForStatement;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.IfStatement;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.InArgument;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.IntLiteral;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.LocalVariable;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.OutArgument;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.PartialAccess;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.PrimaryVariable;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.RealLiteral;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.RepeatStatement;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.ReturnStatement;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.Statement;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.StatementList;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.StringLiteral;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.StructuredTextAlgorithm;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.TimeLiteral;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.UnaryExpression;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.UnaryOperator;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.Variable;
import org.eclipse.fordiac.ide.model.structuredtext.structuredText.WhileStatement;
import org.eclipse.fordiac.ide.model.structuredtext.validation.DatetimeLiteral;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.LazyStringInputStream;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.ExclusiveRange;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;

@SuppressWarnings("all")
public class STAlgorithmFilter {
  private static final String SYNTHETIC_URI_NAME = "__synthetic";
  
  private static final String URI_SEPERATOR = ".";
  
  private static final String FB_URI_EXTENSION = "xtextfbt";
  
  private static final String ST_URI_EXTENSION = "st";
  
  private static final CharSequence EXPORT_PREFIX = ForteLibraryElementTemplate.EXPORT_PREFIX;
  
  private static final IResourceServiceProvider SERVICE_PROVIDER = IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(URI.createURI(((STAlgorithmFilter.SYNTHETIC_URI_NAME + STAlgorithmFilter.URI_SEPERATOR) + STAlgorithmFilter.ST_URI_EXTENSION)));
  
  public void createFBResource(final XtextResourceSet resourceSet, final BaseFBType fbType) {
    final Resource fbResource = resourceSet.createResource(this.computeUnusedUri(resourceSet, STAlgorithmFilter.FB_URI_EXTENSION));
    fbResource.getContents().add(fbType);
    final Consumer<AdapterDeclaration> _function = (AdapterDeclaration adp) -> {
      this.createAdapterResource(resourceSet, adp);
    };
    fbType.getInterfaceList().getSockets().forEach(_function);
    final Consumer<AdapterDeclaration> _function_1 = (AdapterDeclaration adp) -> {
      this.createAdapterResource(resourceSet, adp);
    };
    fbType.getInterfaceList().getPlugs().forEach(_function_1);
    final Consumer<VarDeclaration> _function_2 = (VarDeclaration v) -> {
      this.createStructResource(resourceSet, v);
    };
    fbType.getInterfaceList().getInputVars().forEach(_function_2);
    final Consumer<VarDeclaration> _function_3 = (VarDeclaration v) -> {
      this.createStructResource(resourceSet, v);
    };
    fbType.getInterfaceList().getOutputVars().forEach(_function_3);
    final Consumer<VarDeclaration> _function_4 = (VarDeclaration v) -> {
      this.createStructResource(resourceSet, v);
    };
    fbType.getInternalVars().forEach(_function_4);
  }
  
  public void createAdapterResource(final XtextResourceSet resourceSet, final AdapterDeclaration adapter) {
    final Resource adapterResource = resourceSet.createResource(this.computeUnusedUri(resourceSet, STAlgorithmFilter.FB_URI_EXTENSION));
    adapterResource.getContents().add(adapter.getType().getAdapterFBType());
  }
  
  public void createStructResource(final XtextResourceSet resourceSet, final VarDeclaration variable) {
    DataType _type = variable.getType();
    if ((_type instanceof StructuredType)) {
      final Resource structResource = resourceSet.createResource(this.computeUnusedUri(resourceSet, STAlgorithmFilter.FB_URI_EXTENSION));
      DataType _type_1 = variable.getType();
      final StructuredType type = ((StructuredType) _type_1);
      structResource.getContents().add(type);
      final Consumer<VarDeclaration> _function = (VarDeclaration v) -> {
        this.createStructResource(resourceSet, v);
      };
      type.getMemberVariables().forEach(_function);
    }
  }
  
  protected URI computeUnusedUri(final ResourceSet resourceSet, final String fileExtension) {
    ExclusiveRange _doubleDotLessThan = new ExclusiveRange(0, Integer.MAX_VALUE, true);
    for (final Integer i : _doubleDotLessThan) {
      {
        final URI syntheticUri = URI.createURI((((STAlgorithmFilter.SYNTHETIC_URI_NAME + i) + STAlgorithmFilter.URI_SEPERATOR) + fileExtension));
        Resource _resource = resourceSet.getResource(syntheticUri, false);
        boolean _tripleEquals = (_resource == null);
        if (_tripleEquals) {
          return syntheticUri;
        }
      }
    }
    throw new IllegalStateException();
  }
  
  public XtextResource parseAlgorithm(final STAlgorithm alg) {
    try {
      ResourceSet _get = STAlgorithmFilter.SERVICE_PROVIDER.<ResourceSet>get(ResourceSet.class);
      final XtextResourceSet resourceSet = ((XtextResourceSet) _get);
      EObject _rootContainer = EcoreUtil.getRootContainer(alg);
      this.createFBResource(resourceSet, ((BaseFBType) _rootContainer));
      Resource _createResource = resourceSet.createResource(this.computeUnusedUri(resourceSet, STAlgorithmFilter.ST_URI_EXTENSION));
      final XtextResource resource = ((XtextResource) _createResource);
      String _text = alg.getText();
      LazyStringInputStream _lazyStringInputStream = new LazyStringInputStream(_text);
      Pair<String, Boolean> _mappedTo = Pair.<String, Boolean>of(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
      resource.load(_lazyStringInputStream, Collections.<String, Boolean>unmodifiableMap(CollectionLiterals.<String, Boolean>newHashMap(_mappedTo)));
      EObject _rootASTElement = resource.getParseResult().getRootASTElement();
      final StructuredTextAlgorithm stalg = ((StructuredTextAlgorithm) _rootASTElement);
      final Consumer<VarDeclaration> _function = (VarDeclaration v) -> {
        this.createStructResource(resourceSet, v);
      };
      stalg.getLocalVariables().forEach(_function);
      return resource;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public EList<VarDeclaration> generateLocalVariables(final STAlgorithm alg) {
    final IParseResult parseResult = this.parseAlgorithm(alg).getParseResult();
    EObject _rootASTElement = parseResult.getRootASTElement();
    final StructuredTextAlgorithm stalg = ((StructuredTextAlgorithm) _rootASTElement);
    final Consumer<VarDeclaration> _function = (VarDeclaration v) -> {
      v.setTypeName(v.getType().getName());
    };
    stalg.getLocalVariables().forEach(_function);
    return stalg.getLocalVariables();
  }
  
  public CharSequence generate(final STAlgorithm alg, final List<String> errors) {
    CharSequence _xblockexpression = null;
    {
      final XtextResource resource = this.parseAlgorithm(alg);
      final IParseResult parseResult = resource.getParseResult();
      final IResourceValidator validator = resource.getResourceServiceProvider().getResourceValidator();
      final List<Issue> issues = validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
      boolean _isEmpty = issues.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final Function1<Issue, String> _function = (Issue it) -> {
          return MessageFormat.format("{0}, Line {1}: {2}", alg.getName(), Long.toString((it.getLineNumber()).intValue()), it.getMessage());
        };
        errors.addAll(ListExtensions.<Issue, String>map(issues, _function));
        return null;
      }
      EObject _rootASTElement = parseResult.getRootASTElement();
      final StructuredTextAlgorithm stalg = ((StructuredTextAlgorithm) _rootASTElement);
      _xblockexpression = this.generateStructuredTextAlgorithm(stalg);
    }
    return _xblockexpression;
  }
  
  public CharSequence generate(final String expression, final BasicFBType fb, final List<String> errors) {
    try {
      CharSequence _xblockexpression = null;
      {
        ResourceSet _get = STAlgorithmFilter.SERVICE_PROVIDER.<ResourceSet>get(ResourceSet.class);
        final XtextResourceSet resourceSet = ((XtextResourceSet) _get);
        BasicFBType _copy = EcoreUtil.<BasicFBType>copy(fb);
        this.createFBResource(resourceSet, ((BaseFBType) _copy));
        Resource _createResource = resourceSet.createResource(this.computeUnusedUri(resourceSet, STAlgorithmFilter.ST_URI_EXTENSION));
        final XtextResource resource = ((XtextResource) _createResource);
        IParser _parser = resource.getParser();
        final StructuredTextParser parser = ((StructuredTextParser) _parser);
        LazyStringInputStream _lazyStringInputStream = new LazyStringInputStream(expression);
        ParserRule _expressionRule = parser.getGrammarAccess().getExpressionRule();
        Pair<String, ParserRule> _mappedTo = Pair.<String, ParserRule>of(StructuredTextResource.OPTION_PARSER_RULE, _expressionRule);
        resource.load(_lazyStringInputStream, 
          Collections.<String, ParserRule>unmodifiableMap(CollectionLiterals.<String, ParserRule>newHashMap(_mappedTo)));
        final IParseResult parseResult = resource.getParseResult();
        final IResourceValidator validator = resource.getResourceServiceProvider().getResourceValidator();
        final List<Issue> issues = validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
        boolean _isEmpty = issues.isEmpty();
        boolean _not = (!_isEmpty);
        if (_not) {
          final Function1<Issue, String> _function = (Issue it) -> {
            return it.getMessage();
          };
          errors.addAll(ListExtensions.<Issue, String>map(issues, _function));
          return null;
        }
        EObject _rootASTElement = parseResult.getRootASTElement();
        final Expression expr = ((Expression) _rootASTElement);
        _xblockexpression = this.generateExpression(expr);
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  protected CharSequence generateStructuredTextAlgorithm(final StructuredTextAlgorithm alg) {
    StringConcatenation _builder = new StringConcatenation();
    CharSequence _generateLocalVariables = this.generateLocalVariables(alg.getLocalVariables());
    _builder.append(_generateLocalVariables);
    _builder.newLineIfNotEmpty();
    CharSequence _generateStatementList = this.generateStatementList(alg.getStatements());
    _builder.append(_generateStatementList);
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  private int _BitSize(final PartialAccess part) {
    int _xifexpression = (int) 0;
    if ((part != null)) {
      int _xifexpression_1 = (int) 0;
      boolean _isBitaccess = part.isBitaccess();
      if (_isBitaccess) {
        _xifexpression_1 = this.BitSize(IecTypes.ElementaryTypes.BOOL);
      } else {
        int _xifexpression_2 = (int) 0;
        boolean _isByteaccess = part.isByteaccess();
        if (_isByteaccess) {
          _xifexpression_2 = this.BitSize(IecTypes.ElementaryTypes.BYTE);
        } else {
          int _xifexpression_3 = (int) 0;
          boolean _isWordaccess = part.isWordaccess();
          if (_isWordaccess) {
            _xifexpression_3 = this.BitSize(IecTypes.ElementaryTypes.WORD);
          } else {
            int _xifexpression_4 = (int) 0;
            boolean _isDwordaccess = part.isDwordaccess();
            if (_isDwordaccess) {
              _xifexpression_4 = this.BitSize(IecTypes.ElementaryTypes.DWORD);
            } else {
              _xifexpression_4 = 0;
            }
            _xifexpression_3 = _xifexpression_4;
          }
          _xifexpression_2 = _xifexpression_3;
        }
        _xifexpression_1 = _xifexpression_2;
      }
      _xifexpression = _xifexpression_1;
    } else {
      _xifexpression = 0;
    }
    return _xifexpression;
  }
  
  private int _BitSize(final PrimaryVariable variable) {
    return this.BitSize(variable.getVar().getType());
  }
  
  private int _BitSize(final AdapterVariable variable) {
    return this.BitSize(variable.getVar().getType());
  }
  
  private int _BitSize(final VarDeclaration declaration) {
    return this.BitSize(declaration.getType());
  }
  
  private int _BitSize(final DataType type) {
    int _switchResult = (int) 0;
    boolean _matched = false;
    if (Objects.equal(type, IecTypes.ElementaryTypes.LWORD)) {
      _matched=true;
      _switchResult = 64;
    }
    if (!_matched) {
      if (Objects.equal(type, IecTypes.ElementaryTypes.DWORD)) {
        _matched=true;
        _switchResult = 32;
      }
    }
    if (!_matched) {
      if (Objects.equal(type, IecTypes.ElementaryTypes.WORD)) {
        _matched=true;
        _switchResult = 16;
      }
    }
    if (!_matched) {
      if (Objects.equal(type, IecTypes.ElementaryTypes.BYTE)) {
        _matched=true;
        _switchResult = 8;
      }
    }
    if (!_matched) {
      if (Objects.equal(type, IecTypes.ElementaryTypes.BOOL)) {
        _matched=true;
        _switchResult = 1;
      }
    }
    if (!_matched) {
      _switchResult = 0;
    }
    return _switchResult;
  }
  
  private int _BitSize(final CharSequence str) {
    int _switchResult = (int) 0;
    boolean _matched = false;
    if (Objects.equal(str, FordiacKeywords.LWORD)) {
      _matched=true;
      _switchResult = 64;
    }
    if (!_matched) {
      if (Objects.equal(str, FordiacKeywords.DWORD)) {
        _matched=true;
        _switchResult = 32;
      }
    }
    if (!_matched) {
      if (Objects.equal(str, FordiacKeywords.WORD)) {
        _matched=true;
        _switchResult = 16;
      }
    }
    if (!_matched) {
      if (Objects.equal(str, FordiacKeywords.BYTE)) {
        _matched=true;
        _switchResult = 8;
      }
    }
    if (!_matched) {
      if (Objects.equal(str, FordiacKeywords.BOOL)) {
        _matched=true;
        _switchResult = 1;
      }
    }
    if (!_matched) {
      _switchResult = 0;
    }
    return _switchResult;
  }
  
  protected CharSequence generateArrayDecl(final LocalVariable variable) {
    StringConcatenation _builder = new StringConcatenation();
    {
      boolean _isLocated = variable.isLocated();
      if (_isLocated) {
        _builder.newLineIfNotEmpty();
        CharSequence _generateArrayDeclLocated = this.generateArrayDeclLocated(variable);
        _builder.append(_generateArrayDeclLocated);
      } else {
        CharSequence _generateArrayDeclLocal = this.generateArrayDeclLocal(variable);
        _builder.append(_generateArrayDeclLocal);
      }
    }
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  protected CharSequence generateArrayDeclLocated(final LocalVariable variable) {
    CharSequence _xblockexpression = null;
    {
      final Variable l = variable.getLocation();
      CharSequence _switchResult = null;
      boolean _matched = false;
      if (l instanceof PrimaryVariable) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        {
          if (((this.BitSize(variable.getType().getName()) > 0) && (this.BitSize(((PrimaryVariable)l).getVar().getType().getName()) > 0))) {
            {
              int _BitSize = this.BitSize(((PrimaryVariable)l).getVar().getType().getName());
              int _BitSize_1 = this.BitSize(variable.getType().getName());
              boolean _greaterThan = (_BitSize > _BitSize_1);
              if (_greaterThan) {
                _builder.append("ARRAY_AT<CIEC_");
                String _name = variable.getType().getName();
                _builder.append(_name);
                _builder.append(", CIEC_");
                String _name_1 = ((PrimaryVariable)l).getVar().getType().getName();
                _builder.append(_name_1);
                _builder.append(", 0, ");
                int _arraySize = variable.getArraySize();
                int _minus = (_arraySize - 1);
                _builder.append(_minus);
                _builder.append("> ");
                CharSequence _generateVarAccessLocal = this.generateVarAccessLocal(variable);
                _builder.append(_generateVarAccessLocal);
                _builder.append("(");
                CharSequence _generateVarAccess = this.generateVarAccess(((PrimaryVariable)l).getVar());
                _builder.append(_generateVarAccess);
                _builder.append(");");
                _builder.newLineIfNotEmpty();
              } else {
                _builder.append("#error Accessing CIEC_");
                String _name_2 = ((PrimaryVariable)l).getVar().getType().getName();
                _builder.append(_name_2);
                _builder.append(" via CIEC_");
                String _name_3 = variable.getType().getName();
                _builder.append(_name_3);
                _builder.append(" would result in undefined behaviour");
                _builder.newLineIfNotEmpty();
              }
            }
          } else {
            _builder.append("#error Piecewise access is supported only for types with defined bit-representation (e.g. not CIEC_");
            String _name_4 = ((PrimaryVariable)l).getVar().getType().getName();
            _builder.append(_name_4);
            _builder.append(" via CIEC_");
            String _name_5 = variable.getType().getName();
            _builder.append(_name_5);
            _builder.append(")");
            _builder.newLineIfNotEmpty();
          }
        }
        _switchResult = _builder;
      }
      if (!_matched) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("#error unhandled located array");
        _switchResult = _builder;
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }
  
  protected CharSequence generateArrayDeclLocal(final LocalVariable variable) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CIEC_");
    String _name = variable.getType().getName();
    _builder.append(_name);
    _builder.append(" ");
    _builder.append(STAlgorithmFilter.EXPORT_PREFIX);
    String _name_1 = variable.getName();
    _builder.append(_name_1);
    _builder.append("[");
    int _arraySize = variable.getArraySize();
    _builder.append(_arraySize);
    _builder.append("]");
    CharSequence _generateLocalVariableInitializer = this.generateLocalVariableInitializer(variable);
    _builder.append(_generateLocalVariableInitializer);
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  protected CharSequence generateVariableDeclLocated(final LocalVariable variable) {
    CharSequence _xblockexpression = null;
    {
      final Variable l = variable.getLocation();
      CharSequence _switchResult = null;
      boolean _matched = false;
      if (l instanceof PrimaryVariable) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("// replacing all instances of ");
        CharSequence _extractTypeInformation = this.extractTypeInformation(variable);
        _builder.append(_extractTypeInformation);
        _builder.append(":");
        CharSequence _generateVarAccessLocal = this.generateVarAccessLocal(variable);
        _builder.append(_generateVarAccessLocal);
        _builder.append(" with ");
        CharSequence _generateVarAccess = this.generateVarAccess(variable);
        _builder.append(_generateVarAccess);
        _switchResult = _builder;
      }
      if (!_matched) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("#error located variable of unhandled type");
        _switchResult = _builder;
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }
  
  protected CharSequence generateVariableDeclLocal(final LocalVariable variable) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CIEC_");
    String _name = variable.getType().getName();
    _builder.append(_name);
    _builder.append(" ");
    CharSequence _generateVarAccessLocal = this.generateVarAccessLocal(variable);
    _builder.append(_generateVarAccessLocal);
    CharSequence _generateLocalVariableInitializer = this.generateLocalVariableInitializer(variable);
    _builder.append(_generateLocalVariableInitializer);
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  protected CharSequence generateLocalVariables(final List<VarDeclaration> variables) {
    StringConcatenation _builder = new StringConcatenation();
    {
      for(final VarDeclaration variable : variables) {
        CharSequence _switchResult = null;
        boolean _matched = false;
        if (variable instanceof LocalVariable) {
          if (((!((LocalVariable)variable).isLocated()) && (!((LocalVariable)variable).isArray()))) {
            _matched=true;
            _switchResult = this.generateVariableDeclLocal(((LocalVariable)variable));
          }
        }
        if (!_matched) {
          if (variable instanceof LocalVariable) {
            if (((!((LocalVariable)variable).isLocated()) && ((LocalVariable)variable).isArray())) {
              _matched=true;
              _switchResult = this.generateArrayDeclLocal(((LocalVariable)variable));
            }
          }
        }
        if (!_matched) {
          if (variable instanceof LocalVariable) {
            if (((((LocalVariable)variable).isLocated() && (null != ((LocalVariable)variable).getLocation())) && (!((LocalVariable)variable).isArray()))) {
              _matched=true;
              _switchResult = this.generateVariableDeclLocated(((LocalVariable)variable));
            }
          }
        }
        if (!_matched) {
          if (variable instanceof LocalVariable) {
            if (((((LocalVariable)variable).isLocated() && (null != ((LocalVariable)variable).getLocation())) && ((LocalVariable)variable).isArray())) {
              _matched=true;
              _switchResult = this.generateArrayDeclLocated(((LocalVariable)variable));
            }
          }
        }
        _builder.append(_switchResult);
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder;
  }
  
  protected CharSequence generateLocalVariableInitializer(final VarDeclaration variable) {
    CharSequence _switchResult = null;
    boolean _matched = false;
    if (variable instanceof LocalVariable) {
      Constant _initialValue = ((LocalVariable)variable).getInitialValue();
      boolean _tripleNotEquals = (_initialValue != null);
      if (_tripleNotEquals) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append(" ");
        _builder.append("= ");
        CharSequence _generateExpression = this.generateExpression(((LocalVariable)variable).getInitialValue());
        _builder.append(_generateExpression, " ");
        _switchResult = _builder;
      }
    }
    if (!_matched) {
      _switchResult = "";
    }
    return _switchResult;
  }
  
  protected CharSequence generateStatementList(final StatementList list) {
    StringConcatenation _builder = new StringConcatenation();
    {
      EList<Statement> _statements = list.getStatements();
      for(final Statement stmt : _statements) {
        CharSequence _generateStatement = this.generateStatement(stmt);
        _builder.append(_generateStatement);
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder;
  }
  
  protected CharSequence _generateStatement(final Statement stmt) {
    EClass _eClass = stmt.eClass();
    String _plus = (_eClass + " not supported");
    throw new UnsupportedOperationException(_plus);
  }
  
  protected CharSequence _generateStatement(final AssignmentStatement stmt) {
    StringConcatenation _builder = new StringConcatenation();
    CharSequence _generateExpression = this.generateExpression(stmt.getVariable());
    _builder.append(_generateExpression);
    _builder.append(" = ");
    CharSequence _generateExpression_1 = this.generateExpression(stmt.getExpression());
    _builder.append(_generateExpression_1);
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  protected CharSequence _generateStatement(final Call stmt) {
    return this.generateExpression(stmt);
  }
  
  protected CharSequence _generateStatement(final FBCall fbCall) {
    StringConcatenation _builder = new StringConcatenation();
    CharSequence _generateInAssignments = this.generateInAssignments(fbCall);
    _builder.append(_generateInAssignments);
    _builder.newLineIfNotEmpty();
    _builder.append("mInternalFBs[");
    Integer _internalFbIndexFromName = this.internalFbIndexFromName(fbCall.getFb());
    _builder.append(_internalFbIndexFromName);
    _builder.append("]->receiveInputEvent(");
    Integer _eventIndexFromName = this.eventIndexFromName(fbCall);
    _builder.append(_eventIndexFromName);
    _builder.append(", nullptr);");
    _builder.newLineIfNotEmpty();
    CharSequence _generateOutAssignments = this.generateOutAssignments(fbCall);
    _builder.append(_generateOutAssignments);
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  public CharSequence generateInAssignments(final FBCall call) {
    CharSequence _xblockexpression = null;
    {
      final Iterable<InArgument> inArgs = Iterables.<InArgument>filter(call.getArgs(), InArgument.class);
      StringConcatenation _builder = new StringConcatenation();
      {
        for(final InArgument inArg : inArgs) {
          String _generateInAssigmentExpression = this.generateInAssigmentExpression(call.getFb(), inArg);
          _builder.append(_generateInAssigmentExpression);
          _builder.append(" = ");
          CharSequence _generateInAssigmentRHS = this.generateInAssigmentRHS(inArg);
          _builder.append(_generateInAssigmentRHS);
          _builder.append(";");
          _builder.newLineIfNotEmpty();
        }
      }
      _xblockexpression = _builder;
    }
    return _xblockexpression;
  }
  
  public CharSequence generateInAssigmentRHS(final InArgument inArg) {
    CharSequence _xblockexpression = null;
    {
      final Expression inArgRHS = inArg.getExpr();
      CharSequence _xifexpression = null;
      if (((inArgRHS instanceof PrimaryVariable) && ((PrimaryVariable) inArgRHS).getVar().isArray())) {
        CharSequence _xblockexpression_1 = null;
        {
          final VarDeclaration inArgVar = ((PrimaryVariable) inArgRHS).getVar();
          CharSequence _xifexpression_1 = null;
          EObject _eContainer = ((PrimaryVariable) inArgRHS).getVar().eContainer();
          if ((_eContainer instanceof InterfaceList)) {
            CharSequence _xblockexpression_2 = null;
            {
              EObject _eContainer_1 = ((PrimaryVariable) inArgRHS).getVar().eContainer();
              final InterfaceList argFBInterfaceList = ((InterfaceList) _eContainer_1);
              StringConcatenation _builder = new StringConcatenation();
              _builder.append("*static_cast<CIEC_ARRAY*>(getDI(");
              Integer _inputIndex = this.getInputIndex(argFBInterfaceList, inArgVar.getName());
              _builder.append(_inputIndex);
              _builder.append("))");
              _xblockexpression_2 = _builder;
            }
            _xifexpression_1 = _xblockexpression_2;
          } else {
            CharSequence _xifexpression_2 = null;
            EObject _eContainer_1 = ((PrimaryVariable) inArgRHS).getVar().eContainer();
            if ((_eContainer_1 instanceof SimpleFBType)) {
              CharSequence _xblockexpression_3 = null;
              {
                EObject _eContainer_2 = ((PrimaryVariable) inArgRHS).getVar().eContainer();
                final SimpleFBType fbType = ((SimpleFBType) _eContainer_2);
                StringConcatenation _builder = new StringConcatenation();
                _builder.append("*static_cast<CIEC_ARRAY*>(");
                String _generateGetVariable = this.generateGetVariable(fbType, inArgVar.getName());
                _builder.append(_generateGetVariable);
                _builder.append(")");
                _xblockexpression_3 = _builder;
              }
              _xifexpression_2 = _xblockexpression_3;
            } else {
              CharSequence _xifexpression_3 = null;
              EObject _eContainer_2 = ((PrimaryVariable) inArgRHS).getVar().eContainer();
              if ((_eContainer_2 instanceof StructuredTextAlgorithm)) {
                _xifexpression_3 = this.generateExpression(inArg.getExpr());
              }
              _xifexpression_2 = _xifexpression_3;
            }
            _xifexpression_1 = _xifexpression_2;
          }
          _xblockexpression_1 = _xifexpression_1;
        }
        _xifexpression = _xblockexpression_1;
      } else {
        return this.generateExpression(inArg.getExpr());
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
  
  public String generateInAssigmentExpression(final FB calledFb, final InArgument argument) {
    final VarDeclaration varDec = this.getInputVarDeclaration(calledFb, argument);
    boolean _isArray = varDec.isArray();
    if (_isArray) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("*static_cast<CIEC_ARRAY*>(mInternalFBs[");
      Integer _internalFbIndexFromName = this.internalFbIndexFromName(calledFb);
      _builder.append(_internalFbIndexFromName);
      _builder.append("]->getDI(");
      Integer _inputIndex = this.getInputIndex(calledFb, argument.getVar());
      _builder.append(_inputIndex);
      _builder.append("))");
      return _builder.toString();
    } else {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("*static_cast<CIEC_");
      String _name = varDec.getType().getName();
      _builder_1.append(_name);
      _builder_1.append("*>(mInternalFBs[");
      Integer _internalFbIndexFromName_1 = this.internalFbIndexFromName(calledFb);
      _builder_1.append(_internalFbIndexFromName_1);
      _builder_1.append("]->getDI(");
      Integer _inputIndex_1 = this.getInputIndex(calledFb, argument.getVar());
      _builder_1.append(_inputIndex_1);
      _builder_1.append("))");
      return _builder_1.toString();
    }
  }
  
  public VarDeclaration getInputVarDeclaration(final FB fb, final InArgument argument) {
    final Function1<VarDeclaration, Boolean> _function = (VarDeclaration it) -> {
      String _name = it.getName();
      String _var = argument.getVar();
      return Boolean.valueOf(Objects.equal(_name, _var));
    };
    return IterableExtensions.<VarDeclaration>findFirst(fb.getInterface().getInputVars(), _function);
  }
  
  public Integer getInterfaceElementIndex(final EList<? extends IInterfaceElement> interfaceList, final String elementName) {
    int index = 0;
    for (final IInterfaceElement interfaceElement : interfaceList) {
      {
        String _name = interfaceElement.getName();
        boolean _equals = Objects.equal(_name, elementName);
        if (_equals) {
          return Integer.valueOf(index);
        }
        index++;
      }
    }
    return null;
  }
  
  public Integer getInputIndex(final InterfaceList list, final String varName) {
    return this.getInterfaceElementIndex(list.getInputVars(), varName);
  }
  
  public Integer getInputIndex(final FBType fbType, final String varName) {
    return this.getInputIndex(fbType.getInterfaceList(), varName);
  }
  
  public Integer getInputIndex(final FB fb, final String varName) {
    return this.getInputIndex(fb.getInterface(), varName);
  }
  
  public CharSequence generateOutAssignments(final FBCall call) {
    CharSequence _xblockexpression = null;
    {
      final Iterable<OutArgument> outArgs = Iterables.<OutArgument>filter(call.getArgs(), OutArgument.class);
      StringConcatenation _builder = new StringConcatenation();
      {
        for(final OutArgument outArg : outArgs) {
          CharSequence _generateOutAssignmentLHS = this.generateOutAssignmentLHS(outArg);
          _builder.append(_generateOutAssignmentLHS);
          _builder.append(" = ");
          CharSequence _generateOutAssignmentRHS = this.generateOutAssignmentRHS(call, outArg);
          _builder.append(_generateOutAssignmentRHS);
          _builder.append(";");
          _builder.newLineIfNotEmpty();
        }
      }
      _xblockexpression = _builder;
    }
    return _xblockexpression;
  }
  
  public CharSequence generateOutAssignmentRHS(final FBCall fbCall, final OutArgument argument) {
    CharSequence _xblockexpression = null;
    {
      final FBType fbType = fbCall.getFb().getType();
      final VarDeclaration varDec = this.getTargetVarDeclaration(fbType, argument);
      CharSequence _xifexpression = null;
      if ((varDec != null)) {
        CharSequence _xifexpression_1 = null;
        boolean _isArray = varDec.isArray();
        if (_isArray) {
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("*static_cast<CIEC_ARRAY*>(mInternalFBs[");
          Integer _internalFbIndexFromName = this.internalFbIndexFromName(fbCall.getFb());
          _builder.append(_internalFbIndexFromName);
          _builder.append("]->");
          String _generateGetVariable = this.generateGetVariable(fbType, varDec.getName());
          _builder.append(_generateGetVariable);
          _builder.append(")");
          _xifexpression_1 = _builder;
        } else {
          StringConcatenation _builder_1 = new StringConcatenation();
          _builder_1.append("*static_cast<CIEC_");
          String _name = varDec.getType().getName();
          _builder_1.append(_name);
          _builder_1.append("*>(mInternalFBs[");
          Integer _internalFbIndexFromName_1 = this.internalFbIndexFromName(fbCall.getFb());
          _builder_1.append(_internalFbIndexFromName_1);
          _builder_1.append("]->");
          String _generateGetVariable_1 = this.generateGetVariable(fbType, varDec.getName());
          _builder_1.append(_generateGetVariable_1);
          _builder_1.append(")");
          _xifexpression_1 = _builder_1;
        }
        _xifexpression = _xifexpression_1;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
  
  public String generateGetVariable(final FBType type, final String name) {
    final Integer inputIndex = this.getInputIndex(type, name);
    if ((inputIndex != null)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("getDI(");
      _builder.append(inputIndex);
      _builder.append(")");
      return _builder.toString();
    }
    final Integer outputIndex = this.getOutputIndex(type, name);
    if ((outputIndex != null)) {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("getDO(");
      _builder_1.append(outputIndex);
      _builder_1.append(")");
      return _builder_1.toString();
    }
    Integer _xifexpression = null;
    if ((type instanceof BaseFBType)) {
      _xifexpression = this.getInternalVarIndex(((BaseFBType)type), name);
    } else {
      _xifexpression = null;
    }
    final Integer internalVariableIndex = _xifexpression;
    if ((internalVariableIndex != null)) {
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("getVarInternal(");
      Integer _internalVarIndex = this.getInternalVarIndex(((BaseFBType) type), name);
      _builder_2.append(_internalVarIndex);
      _builder_2.append(")");
      return _builder_2.toString();
    }
    String _name = type.getName();
    String _plus = ((("Name " + name) + " not a variable on FB type ") + _name);
    throw new IllegalArgumentException(_plus);
  }
  
  public CharSequence generateOutAssignmentLHS(final OutArgument argument) {
    CharSequence _xblockexpression = null;
    {
      final Variable goalExpression = argument.getExpr();
      CharSequence _xifexpression = null;
      if ((goalExpression instanceof PrimaryVariable)) {
        CharSequence _xblockexpression_1 = null;
        {
          final VarDeclaration outVariable = ((PrimaryVariable)goalExpression).getVar();
          EObject _eContainer = outVariable.eContainer().eContainer();
          final FBType callingFBType = ((FBType) _eContainer);
          CharSequence _xifexpression_1 = null;
          boolean _isArray = outVariable.isArray();
          if (_isArray) {
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("*static_cast<CIEC_ARRAY*>(");
            String _generateGetVariable = this.generateGetVariable(callingFBType, outVariable.getName());
            _builder.append(_generateGetVariable);
            _builder.append(")");
            return _builder.toString();
          } else {
            StringConcatenation _builder_1 = new StringConcatenation();
            CharSequence _generateExpression = this.generateExpression(argument.getExpr());
            _builder_1.append(_generateExpression);
            _xifexpression_1 = _builder_1;
          }
          _xblockexpression_1 = _xifexpression_1;
        }
        _xifexpression = _xblockexpression_1;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
  
  public VarDeclaration getVarDeclaration(final EList<? extends IInterfaceElement> interfaceList, final String varName) {
    final Function1<IInterfaceElement, Boolean> _function = (IInterfaceElement it) -> {
      String _name = it.getName();
      return Boolean.valueOf(Objects.equal(_name, varName));
    };
    IInterfaceElement _findFirst = IterableExtensions.findFirst(interfaceList, _function);
    return ((VarDeclaration) _findFirst);
  }
  
  public VarDeclaration getTargetVarDeclaration(final FB fb, final OutArgument argument) {
    return this.getTargetVarDeclaration(fb.getType(), argument);
  }
  
  public VarDeclaration getTargetVarDeclaration(final FBType fbType, final OutArgument argument) {
    VarDeclaration _xblockexpression = null;
    {
      final String varName = argument.getVar();
      VarDeclaration _varDeclaration = this.getVarDeclaration(fbType.getInterfaceList().getOutputVars(), varName);
      VarDeclaration _varDeclaration_1 = this.getVarDeclaration(fbType.getInterfaceList().getInputVars(), varName);
      VarDeclaration _or = this.operator_or(_varDeclaration, _varDeclaration_1);
      VarDeclaration _xifexpression = null;
      if ((fbType instanceof BaseFBType)) {
        _xifexpression = this.getVarDeclaration(((BaseFBType)fbType).getInternalVars(), varName);
      } else {
        _xifexpression = null;
      }
      _xblockexpression = this.operator_or(_or, _xifexpression);
    }
    return _xblockexpression;
  }
  
  public VarDeclaration operator_or(final VarDeclaration element, final VarDeclaration element2) {
    VarDeclaration _xifexpression = null;
    if ((element != null)) {
      _xifexpression = element;
    } else {
      _xifexpression = element2;
    }
    return _xifexpression;
  }
  
  public Integer getOutputIndex(final FB fb, final String varName) {
    return this.getInterfaceElementIndex(fb.getInterface().getOutputVars(), varName);
  }
  
  public Integer getOutputIndex(final FBType fbType, final String varName) {
    return this.getInterfaceElementIndex(fbType.getInterfaceList().getOutputVars(), varName);
  }
  
  public Object getInternalVarIndex(final FB fb, final String varName) {
    FBType _type = fb.getType();
    if ((_type instanceof BaseFBType)) {
      FBType _type_1 = fb.getType();
      this.getInternalVarIndex(((BaseFBType) _type_1), varName);
    }
    return null;
  }
  
  public Integer getInternalVarIndex(final BaseFBType fbType, final String varName) {
    return this.getInterfaceElementIndex(fbType.getInternalVars(), varName);
  }
  
  public Integer eventIndexFromName(final FBCall fbCall) {
    return this.getInterfaceElementIndex(fbCall.getFb().getInterface().getEventInputs(), fbCall.getEvent());
  }
  
  public Integer internalFbIndexFromName(final FB fb) {
    EObject _eContainer = fb.eContainer();
    final BaseFBType fbType = ((BaseFBType) _eContainer);
    int index = 0;
    EList<FB> _internalFbs = fbType.getInternalFbs();
    for (final FB internalFb : _internalFbs) {
      {
        if ((fb == internalFb)) {
          return Integer.valueOf(index);
        }
        index++;
      }
    }
    return null;
  }
  
  protected CharSequence _generateStatement(final ReturnStatement stmt) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("return;");
    return _builder;
  }
  
  protected CharSequence _generateStatement(final IfStatement stmt) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("if(");
    CharSequence _generateExpression = this.generateExpression(stmt.getExpression());
    _builder.append(_generateExpression);
    _builder.append(") {");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    CharSequence _generateStatementList = this.generateStatementList(stmt.getStatments());
    _builder.append(_generateStatementList, "\t");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    _builder.newLine();
    {
      EList<ElseIfClause> _elseif = stmt.getElseif();
      for(final ElseIfClause elseif : _elseif) {
        _builder.append("else if(");
        CharSequence _generateExpression_1 = this.generateExpression(elseif.getExpression());
        _builder.append(_generateExpression_1);
        _builder.append(") {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        CharSequence _generateStatementList_1 = this.generateStatementList(elseif.getStatements());
        _builder.append(_generateStatementList_1, "\t");
        _builder.newLineIfNotEmpty();
        _builder.append("}");
        _builder.newLine();
      }
    }
    {
      ElseClause _else = stmt.getElse();
      boolean _tripleNotEquals = (_else != null);
      if (_tripleNotEquals) {
        _builder.append("else {");
        _builder.newLine();
        _builder.append("\t");
        CharSequence _generateStatementList_2 = this.generateStatementList(stmt.getElse().getStatements());
        _builder.append(_generateStatementList_2, "\t");
        _builder.newLineIfNotEmpty();
        _builder.append("}");
        _builder.newLine();
      }
    }
    return _builder;
  }
  
  protected CharSequence _generateStatement(final CaseStatement stmt) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("switch (");
    CharSequence _generateExpression = this.generateExpression(stmt.getExpression());
    _builder.append(_generateExpression);
    _builder.append(") {");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    {
      EList<CaseClause> _case = stmt.getCase();
      for(final CaseClause clause : _case) {
        CharSequence _generateCaseClause = this.generateCaseClause(clause);
        _builder.append(_generateCaseClause, "\t");
      }
    }
    _builder.newLineIfNotEmpty();
    {
      ElseClause _else = stmt.getElse();
      boolean _tripleNotEquals = (_else != null);
      if (_tripleNotEquals) {
        _builder.append("\t");
        _builder.append("default:");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("\t");
        CharSequence _generateStatementList = this.generateStatementList(stmt.getElse().getStatements());
        _builder.append(_generateStatementList, "\t\t");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("\t");
        _builder.append("break;");
        _builder.newLine();
      }
    }
    _builder.append("}");
    _builder.newLine();
    return _builder;
  }
  
  protected CharSequence generateCaseClause(final CaseClause clause) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("case ");
    {
      EList<Constant> _case = clause.getCase();
      boolean _hasElements = false;
      for(final Constant value : _case) {
        if (!_hasElements) {
          _hasElements = true;
        } else {
          _builder.appendImmediate(" case ", "");
        }
        CharSequence _generateExpression = this.generateExpression(value);
        _builder.append(_generateExpression);
        _builder.append(":");
      }
    }
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    CharSequence _generateStatementList = this.generateStatementList(clause.getStatements());
    _builder.append(_generateStatementList, "\t");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("break;");
    _builder.newLine();
    return _builder;
  }
  
  protected CharSequence _generateStatement(final ExitStatement stmt) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("break;");
    return _builder;
  }
  
  protected CharSequence _generateStatement(final ContinueStatement stmt) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("continue;");
    return _builder;
  }
  
  private EList<Statement> _containedStatements(final IfStatement statement) {
    final EList<Statement> list = new BasicEList<Statement>();
    list.addAll(statement.getStatments().getStatements());
    EList<ElseIfClause> _elseif = statement.getElseif();
    for (final ElseIfClause elseif : _elseif) {
      StatementList _statements = null;
      if (elseif!=null) {
        _statements=elseif.getStatements();
      }
      EList<Statement> _statements_1 = null;
      if (_statements!=null) {
        _statements_1=_statements.getStatements();
      }
      boolean _tripleNotEquals = (_statements_1 != null);
      if (_tripleNotEquals) {
        list.addAll(elseif.getStatements().getStatements());
      }
    }
    ElseClause _else = null;
    if (statement!=null) {
      _else=statement.getElse();
    }
    StatementList _statements_2 = null;
    if (_else!=null) {
      _statements_2=_else.getStatements();
    }
    EList<Statement> _statements_3 = null;
    if (_statements_2!=null) {
      _statements_3=_statements_2.getStatements();
    }
    boolean _tripleNotEquals_1 = (_statements_3 != null);
    if (_tripleNotEquals_1) {
      list.addAll(statement.getElse().getStatements().getStatements());
    }
    return list;
  }
  
  private EList<Statement> _containedStatements(final CaseStatement statement) {
    final EList<Statement> list = new BasicEList<Statement>();
    EList<CaseClause> _case = statement.getCase();
    for (final CaseClause casz : _case) {
      StatementList _statements = null;
      if (casz!=null) {
        _statements=casz.getStatements();
      }
      EList<Statement> _statements_1 = null;
      if (_statements!=null) {
        _statements_1=_statements.getStatements();
      }
      boolean _tripleNotEquals = (_statements_1 != null);
      if (_tripleNotEquals) {
        list.addAll(casz.getStatements().getStatements());
      }
    }
    EList<Statement> _statements_2 = statement.getElse().getStatements().getStatements();
    boolean _tripleNotEquals_1 = (_statements_2 != null);
    if (_tripleNotEquals_1) {
      list.addAll(statement.getElse().getStatements().getStatements());
    }
    return list;
  }
  
  private EList<Statement> _containedStatements(final ForStatement statement) {
    return statement.getStatements().getStatements();
  }
  
  private EList<Statement> _containedStatements(final WhileStatement statement) {
    return statement.getStatements().getStatements();
  }
  
  private EList<Statement> _containedStatements(final RepeatStatement statement) {
    return statement.getStatements().getStatements();
  }
  
  private EList<Statement> _containedStatements(final Statement statement) {
    final EList<Statement> list = new BasicEList<Statement>();
    return list;
  }
  
  private String indexForLoopVariables(final ForStatement forStatement) {
    EObject _rootContainer = EcoreUtil.getRootContainer(forStatement);
    final StructuredTextAlgorithm forRootAlgorithm = ((StructuredTextAlgorithm) _rootContainer);
    Statement searchItem = forStatement;
    EObject container = forStatement.eContainer().eContainer();
    int containmentLevel = 0;
    final ArrayList<Integer> indexList = new ArrayList<Integer>();
    while ((container != forRootAlgorithm)) {
      {
        if ((container instanceof Statement)) {
          final Function1<Statement, Boolean> _function = (Statement it) -> {
            return Boolean.valueOf((it instanceof ForStatement));
          };
          final Iterable<Statement> forStatementsInContainer = IterableExtensions.<Statement>filter(this.containedStatements(((Statement)container)), _function);
          for (int i = 0; (i < IterableExtensions.size(forStatementsInContainer)); i++) {
            Object _get = ((Object[])Conversions.unwrapArray(forStatementsInContainer, Object.class))[i];
            boolean _tripleEquals = (searchItem == _get);
            if (_tripleEquals) {
              indexList.add(Integer.valueOf(i));
            }
          }
          containmentLevel++;
          searchItem = ((Statement)container);
        }
        container = container.eContainer().eContainer();
      }
    }
    final Function1<Statement, Boolean> _function = (Statement it) -> {
      return Boolean.valueOf((it instanceof ForStatement));
    };
    final Iterable<Statement> forStatementsInAlgorithm = IterableExtensions.<Statement>filter(forRootAlgorithm.getStatements().getStatements(), _function);
    for (int i = 0; (i < IterableExtensions.size(forStatementsInAlgorithm)); i++) {
      Object _get = ((Object[])Conversions.unwrapArray(forStatementsInAlgorithm, Object.class))[i];
      boolean _tripleEquals = (searchItem == _get);
      if (_tripleEquals) {
        indexList.add(Integer.valueOf(i));
      }
    }
    String indexString = "";
    List<Integer> _reverse = ListExtensions.<Integer>reverse(indexList);
    for (final Integer i : _reverse) {
      String _indexString = indexString;
      indexString = (_indexString + ("_" + i));
    }
    return indexString;
  }
  
  protected CharSequence _generateStatement(final ForStatement stmt) {
    CharSequence _xblockexpression = null;
    {
      final String loopVarIndex = this.indexForLoopVariables(stmt);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("const auto by");
      _builder.append(loopVarIndex);
      _builder.append(" = ");
      {
        Expression _by = stmt.getBy();
        boolean _tripleNotEquals = (_by != null);
        if (_tripleNotEquals) {
          CharSequence _generateExpression = this.generateExpression(stmt.getBy());
          _builder.append(_generateExpression);
        } else {
          _builder.append("1");
        }
      }
      _builder.append(";");
      _builder.newLineIfNotEmpty();
      _builder.append("const auto to");
      _builder.append(loopVarIndex);
      _builder.append(" = ");
      CharSequence _generateExpression_1 = this.generateExpression(stmt.getTo());
      _builder.append(_generateExpression_1);
      _builder.append(";");
      _builder.newLineIfNotEmpty();
      _builder.append("for(");
      CharSequence _generateExpression_2 = this.generateExpression(stmt.getVariable());
      _builder.append(_generateExpression_2);
      _builder.append(" = ");
      CharSequence _generateExpression_3 = this.generateExpression(stmt.getFrom());
      _builder.append(_generateExpression_3);
      _builder.append(";");
      _builder.newLineIfNotEmpty();
      _builder.append("    ");
      _builder.append("(by");
      _builder.append(loopVarIndex, "    ");
      _builder.append(" >  0 && ");
      CharSequence _generateExpression_4 = this.generateExpression(stmt.getVariable());
      _builder.append(_generateExpression_4, "    ");
      _builder.append(" <= to");
      _builder.append(loopVarIndex, "    ");
      _builder.append(") ||");
      _builder.newLineIfNotEmpty();
      _builder.append("    ");
      _builder.append("(by");
      _builder.append(loopVarIndex, "    ");
      _builder.append(" <= 0 && ");
      CharSequence _generateExpression_5 = this.generateExpression(stmt.getVariable());
      _builder.append(_generateExpression_5, "    ");
      _builder.append(" >= to");
      _builder.append(loopVarIndex, "    ");
      _builder.append(");");
      _builder.newLineIfNotEmpty();
      _builder.append("    ");
      CharSequence _generateExpression_6 = this.generateExpression(stmt.getVariable());
      _builder.append(_generateExpression_6, "    ");
      _builder.append(" = ");
      CharSequence _generateExpression_7 = this.generateExpression(stmt.getVariable());
      _builder.append(_generateExpression_7, "    ");
      _builder.append(" + by");
      _builder.append(loopVarIndex, "    ");
      _builder.append("){");
      _builder.newLineIfNotEmpty();
      _builder.append("\t");
      CharSequence _generateStatementList = this.generateStatementList(stmt.getStatements());
      _builder.append(_generateStatementList, "\t");
      _builder.newLineIfNotEmpty();
      _builder.append("}");
      _builder.newLine();
      _xblockexpression = _builder;
    }
    return _xblockexpression;
  }
  
  protected CharSequence _generateStatement(final WhileStatement stmt) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("while(");
    CharSequence _generateExpression = this.generateExpression(stmt.getExpression());
    _builder.append(_generateExpression);
    _builder.append(") {");
    _builder.newLineIfNotEmpty();
    _builder.append("  ");
    CharSequence _generateStatementList = this.generateStatementList(stmt.getStatements());
    _builder.append(_generateStatementList, "  ");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    _builder.newLine();
    return _builder;
  }
  
  protected CharSequence _generateStatement(final RepeatStatement stmt) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("do {");
    _builder.newLine();
    _builder.append("  ");
    CharSequence _generateStatementList = this.generateStatementList(stmt.getStatements());
    _builder.append(_generateStatementList, "  ");
    _builder.newLineIfNotEmpty();
    _builder.append("} while(!((");
    CharSequence _generateExpression = this.generateExpression(stmt.getExpression());
    _builder.append(_generateExpression);
    _builder.append(")));");
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  protected CharSequence generateBinaryOperator(final BinaryOperator op) {
    CharSequence _switchResult = null;
    if (op != null) {
      switch (op) {
        case OR:
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("||");
          _switchResult = _builder;
          break;
        case XOR:
          StringConcatenation _builder_1 = new StringConcatenation();
          _builder_1.append("^");
          _switchResult = _builder_1;
          break;
        case AND:
          StringConcatenation _builder_2 = new StringConcatenation();
          _builder_2.append("&&");
          _switchResult = _builder_2;
          break;
        case AMPERSAND:
          StringConcatenation _builder_3 = new StringConcatenation();
          _builder_3.append("&&");
          _switchResult = _builder_3;
          break;
        case EQ:
          StringConcatenation _builder_4 = new StringConcatenation();
          _builder_4.append("==");
          _switchResult = _builder_4;
          break;
        case NE:
          StringConcatenation _builder_5 = new StringConcatenation();
          _builder_5.append("!=");
          _switchResult = _builder_5;
          break;
        case LT:
          StringConcatenation _builder_6 = new StringConcatenation();
          _builder_6.append("<");
          _switchResult = _builder_6;
          break;
        case LE:
          StringConcatenation _builder_7 = new StringConcatenation();
          _builder_7.append("<=");
          _switchResult = _builder_7;
          break;
        case GT:
          StringConcatenation _builder_8 = new StringConcatenation();
          _builder_8.append(">");
          _switchResult = _builder_8;
          break;
        case GE:
          StringConcatenation _builder_9 = new StringConcatenation();
          _builder_9.append(">=");
          _switchResult = _builder_9;
          break;
        case MOD:
          StringConcatenation _builder_10 = new StringConcatenation();
          _builder_10.append("%");
          _switchResult = _builder_10;
          break;
        default:
          StringConcatenation _builder_11 = new StringConcatenation();
          _builder_11.append("The operator ");
          _builder_11.append(op);
          _builder_11.append(" is not supported");
          throw new UnsupportedOperationException(_builder_11.toString());
      }
    } else {
      StringConcatenation _builder_11 = new StringConcatenation();
      _builder_11.append("The operator ");
      _builder_11.append(op);
      _builder_11.append(" is not supported");
      throw new UnsupportedOperationException(_builder_11.toString());
    }
    return _switchResult;
  }
  
  protected CharSequence generateUnaryOperator(final UnaryOperator op) {
    CharSequence _switchResult = null;
    if (op != null) {
      switch (op) {
        case MINUS:
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("-");
          _switchResult = _builder;
          break;
        case PLUS:
          StringConcatenation _builder_1 = new StringConcatenation();
          _builder_1.append("+");
          _switchResult = _builder_1;
          break;
        case NOT:
          StringConcatenation _builder_2 = new StringConcatenation();
          _builder_2.append("!");
          _switchResult = _builder_2;
          break;
        default:
          break;
      }
    }
    return _switchResult;
  }
  
  protected CharSequence _generateExpression(final Call expr) {
    StringConcatenation _builder = new StringConcatenation();
    String _func = expr.getFunc();
    _builder.append(_func);
    _builder.append("(");
    {
      EList<Argument> _args = expr.getArgs();
      boolean _hasElements = false;
      for(final Argument arg : _args) {
        if (!_hasElements) {
          _hasElements = true;
        } else {
          _builder.appendImmediate(", ", "");
        }
        CharSequence _generateExpression = this.generateExpression(arg);
        _builder.append(_generateExpression);
      }
    }
    _builder.append(")");
    return _builder;
  }
  
  protected CharSequence _generateExpression(final InArgument arg) {
    return this.generateExpression(arg.getExpr());
  }
  
  protected CharSequence _generateExpression(final BinaryExpression expr) {
    CharSequence _switchResult = null;
    BinaryOperator _operator = expr.getOperator();
    if (_operator != null) {
      switch (_operator) {
        case POWER:
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("EXPT(");
          CharSequence _generateExpression = this.generateExpression(expr.getLeft());
          _builder.append(_generateExpression);
          _builder.append(", ");
          CharSequence _generateExpression_1 = this.generateExpression(expr.getRight());
          _builder.append(_generateExpression_1);
          _builder.append(")");
          _switchResult = _builder;
          break;
        case ADD:
          StringConcatenation _builder_1 = new StringConcatenation();
          _builder_1.append("ADD(");
          CharSequence _generateExpression_2 = this.generateExpression(expr.getLeft());
          _builder_1.append(_generateExpression_2);
          _builder_1.append(", ");
          CharSequence _generateExpression_3 = this.generateExpression(expr.getRight());
          _builder_1.append(_generateExpression_3);
          _builder_1.append(")");
          _switchResult = _builder_1;
          break;
        case SUB:
          StringConcatenation _builder_2 = new StringConcatenation();
          _builder_2.append("SUB(");
          CharSequence _generateExpression_4 = this.generateExpression(expr.getLeft());
          _builder_2.append(_generateExpression_4);
          _builder_2.append(", ");
          CharSequence _generateExpression_5 = this.generateExpression(expr.getRight());
          _builder_2.append(_generateExpression_5);
          _builder_2.append(")");
          _switchResult = _builder_2;
          break;
        case DIV:
          StringConcatenation _builder_3 = new StringConcatenation();
          _builder_3.append("DIV(");
          CharSequence _generateExpression_6 = this.generateExpression(expr.getLeft());
          _builder_3.append(_generateExpression_6);
          _builder_3.append(", ");
          CharSequence _generateExpression_7 = this.generateExpression(expr.getRight());
          _builder_3.append(_generateExpression_7);
          _builder_3.append(")");
          _switchResult = _builder_3;
          break;
        case MUL:
          StringConcatenation _builder_4 = new StringConcatenation();
          _builder_4.append("MUL(");
          CharSequence _generateExpression_8 = this.generateExpression(expr.getLeft());
          _builder_4.append(_generateExpression_8);
          _builder_4.append(", ");
          CharSequence _generateExpression_9 = this.generateExpression(expr.getRight());
          _builder_4.append(_generateExpression_9);
          _builder_4.append(")");
          _switchResult = _builder_4;
          break;
        default:
          StringConcatenation _builder_5 = new StringConcatenation();
          _builder_5.append("(");
          CharSequence _generateExpression_10 = this.generateExpression(expr.getLeft());
          _builder_5.append(_generateExpression_10);
          _builder_5.append(" ");
          CharSequence _generateBinaryOperator = this.generateBinaryOperator(expr.getOperator());
          _builder_5.append(_generateBinaryOperator);
          _builder_5.append(" ");
          CharSequence _generateExpression_11 = this.generateExpression(expr.getRight());
          _builder_5.append(_generateExpression_11);
          _builder_5.append(")");
          _switchResult = _builder_5;
          break;
      }
    } else {
      StringConcatenation _builder_5 = new StringConcatenation();
      _builder_5.append("(");
      CharSequence _generateExpression_10 = this.generateExpression(expr.getLeft());
      _builder_5.append(_generateExpression_10);
      _builder_5.append(" ");
      CharSequence _generateBinaryOperator = this.generateBinaryOperator(expr.getOperator());
      _builder_5.append(_generateBinaryOperator);
      _builder_5.append(" ");
      CharSequence _generateExpression_11 = this.generateExpression(expr.getRight());
      _builder_5.append(_generateExpression_11);
      _builder_5.append(")");
      _switchResult = _builder_5;
    }
    return _switchResult;
  }
  
  protected CharSequence _generateExpression(final TimeLiteral expr) {
    StringConcatenation _builder = new StringConcatenation();
    String _literal = expr.getLiteral();
    DatetimeLiteral _datetimeLiteral = new DatetimeLiteral(_literal);
    _builder.append(_datetimeLiteral);
    return _builder;
  }
  
  protected CharSequence _generateExpression(final UnaryExpression expr) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("(");
    CharSequence _generateUnaryOperator = this.generateUnaryOperator(expr.getOperator());
    _builder.append(_generateUnaryOperator);
    _builder.append(" ");
    CharSequence _generateExpression = this.generateExpression(expr.getExpression());
    _builder.append(_generateExpression);
    _builder.append(")");
    return _builder;
  }
  
  protected CharSequence _generateExpression(final BoolLiteral expr) {
    StringConcatenation _builder = new StringConcatenation();
    String _string = Boolean.valueOf(expr.isValue()).toString();
    _builder.append(_string);
    return _builder;
  }
  
  protected CharSequence _generateExpression(final IntLiteral expr) {
    StringConcatenation _builder = new StringConcatenation();
    String _string = Long.valueOf(expr.getValue()).toString();
    _builder.append(_string);
    return _builder;
  }
  
  protected CharSequence _generateExpression(final RealLiteral expr) {
    StringConcatenation _builder = new StringConcatenation();
    String _string = Double.valueOf(expr.getValue()).toString();
    _builder.append(_string);
    return _builder;
  }
  
  protected CharSequence _generateExpression(final StringLiteral expr) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("\"");
    String _convertToJavaString = Strings.convertToJavaString(expr.getValue());
    _builder.append(_convertToJavaString);
    _builder.append("\"");
    return _builder;
  }
  
  protected CharSequence _generateExpression(final ArrayVariable expr) {
    StringConcatenation _builder = new StringConcatenation();
    CharSequence _generateExpression = this.generateExpression(expr.getArray());
    _builder.append(_generateExpression);
    {
      EList<Expression> _index = expr.getIndex();
      boolean _hasElements = false;
      for(final Expression index : _index) {
        if (!_hasElements) {
          _hasElements = true;
          _builder.append("[");
        } else {
          _builder.appendImmediate("][", "");
        }
        CharSequence _generateExpression_1 = this.generateExpression(index);
        _builder.append(_generateExpression_1);
      }
      if (_hasElements) {
        _builder.append("]");
      }
    }
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  protected CharSequence _generateExpression(final AdapterVariable expr) {
    StringConcatenation _builder = new StringConcatenation();
    CharSequence _generateExpression = this.generateExpression(expr.getCurr());
    _builder.append(_generateExpression);
    _builder.append(".");
    String _name = expr.getVar().getName();
    _builder.append(_name);
    _builder.append("()");
    CharSequence _xifexpression = null;
    EObject _eContainer = expr.eContainer();
    boolean _not = (!(_eContainer instanceof AdapterVariable));
    if (_not) {
      _xifexpression = this.generateBitaccess(expr);
    }
    _builder.append(_xifexpression);
    return _builder;
  }
  
  protected CharSequence _generateExpression(final AdapterRoot expr) {
    StringConcatenation _builder = new StringConcatenation();
    CharSequence _generateVarAccess = this.generateVarAccess(expr.getAdapter());
    _builder.append(_generateVarAccess);
    return _builder;
  }
  
  public CharSequence generateStructAdapterVarAccess(final EList<VarDeclaration> list) {
    StringConcatenation _builder = new StringConcatenation();
    {
      boolean _hasElements = false;
      for(final VarDeclaration variable : list) {
        if (!_hasElements) {
          _hasElements = true;
          _builder.append(".");
        } else {
          _builder.appendImmediate(".", "");
        }
        String _name = variable.getName();
        _builder.append(_name);
        _builder.append("()");
      }
    }
    return _builder;
  }
  
  protected CharSequence _generateExpression(final PrimaryVariable expr) {
    StringConcatenation _builder = new StringConcatenation();
    CharSequence _generateVarAccess = this.generateVarAccess(expr.getVar());
    _builder.append(_generateVarAccess);
    CharSequence _generateBitaccess = this.generateBitaccess(expr);
    _builder.append(_generateBitaccess);
    return _builder;
  }
  
  protected CharSequence generateVarAccessLocal(final LocalVariable variable) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append(STAlgorithmFilter.EXPORT_PREFIX);
    String _name = variable.getName();
    _builder.append(_name);
    return _builder;
  }
  
  protected CharSequence _generateVarAccess(final VarDeclaration variable) {
    CharSequence _xifexpression = null;
    EObject _eContainer = variable.eContainer().eContainer();
    if ((_eContainer instanceof AdapterFBType)) {
      StringConcatenation _builder = new StringConcatenation();
      String _name = variable.getName();
      _builder.append(_name);
      _builder.append("()");
      _xifexpression = _builder;
    } else {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append(STAlgorithmFilter.EXPORT_PREFIX);
      String _name_1 = variable.getName();
      _builder_1.append(_name_1);
      _builder_1.append("()");
      _xifexpression = _builder_1;
    }
    return _xifexpression;
  }
  
  protected CharSequence _generateVarAccess(final LocalVariable variable) {
    StringConcatenation _builder = new StringConcatenation();
    {
      boolean _isLocated = variable.isLocated();
      if (_isLocated) {
        CharSequence _generateVarAccessLocated = this.generateVarAccessLocated(variable);
        _builder.append(_generateVarAccessLocated);
      } else {
        CharSequence _generateVarAccessLocal = this.generateVarAccessLocal(variable);
        _builder.append(_generateVarAccessLocal);
      }
    }
    return _builder;
  }
  
  protected CharSequence generateVarAccessLocated(final LocalVariable variable) {
    StringConcatenation _builder = new StringConcatenation();
    {
      boolean _isArray = variable.isArray();
      if (_isArray) {
        CharSequence _generateVarAccessLocal = this.generateVarAccessLocal(variable);
        _builder.append(_generateVarAccessLocal);
      } else {
        CharSequence _generateExpression = this.generateExpression(variable.getLocation());
        _builder.append(_generateExpression);
        CharSequence _generateBitaccess = this.generateBitaccess(variable, this.extractTypeInformation(variable.getLocation()), this.extractTypeInformation(variable), 0);
        _builder.append(_generateBitaccess);
      }
    }
    return _builder;
  }
  
  protected CharSequence generateBitaccess(final AdapterVariable variable) {
    CharSequence _xifexpression = null;
    PartialAccess _part = variable.getPart();
    boolean _tripleNotEquals = (null != _part);
    if (_tripleNotEquals) {
      CharSequence _xblockexpression = null;
      {
        final VarDeclaration lastvar = variable.getVar();
        _xblockexpression = this.generateBitaccess(lastvar, variable.getPart());
      }
      _xifexpression = _xblockexpression;
    }
    return _xifexpression;
  }
  
  protected CharSequence generateBitaccess(final PrimaryVariable variable) {
    CharSequence _xifexpression = null;
    PartialAccess _part = variable.getPart();
    boolean _tripleNotEquals = (null != _part);
    if (_tripleNotEquals) {
      _xifexpression = this.generateBitaccess(variable.getVar(), variable.getPart());
    }
    return _xifexpression;
  }
  
  protected String partialAccessTypeName(final PartialAccess part) {
    String _xifexpression = null;
    boolean _isBitaccess = part.isBitaccess();
    if (_isBitaccess) {
      _xifexpression = FordiacKeywords.BOOL;
    } else {
      String _xifexpression_1 = null;
      boolean _isByteaccess = part.isByteaccess();
      if (_isByteaccess) {
        _xifexpression_1 = FordiacKeywords.BYTE;
      } else {
        String _xifexpression_2 = null;
        boolean _isWordaccess = part.isWordaccess();
        if (_isWordaccess) {
          _xifexpression_2 = FordiacKeywords.WORD;
        } else {
          String _xifexpression_3 = null;
          boolean _isDwordaccess = part.isDwordaccess();
          if (_isDwordaccess) {
            _xifexpression_3 = FordiacKeywords.DWORD;
          } else {
            _xifexpression_3 = "";
          }
          _xifexpression_2 = _xifexpression_3;
        }
        _xifexpression_1 = _xifexpression_2;
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }
  
  protected CharSequence generateBitaccess(final VarDeclaration variable, final PartialAccess part) {
    CharSequence _xblockexpression = null;
    {
      final int maxVarBitIndex = this.BitSize(variable);
      int _BitSize = this.BitSize(part);
      int _index = part.getIndex();
      int _plus = (_index + 1);
      final int endBitIndexAccessor = (_BitSize * _plus);
      CharSequence _xifexpression = null;
      if ((maxVarBitIndex > endBitIndexAccessor)) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append(".partial<CIEC_");
        String _partialAccessTypeName = this.partialAccessTypeName(part);
        _builder.append(_partialAccessTypeName);
        _builder.append(",");
        String _string = Long.toString(part.getIndex());
        _builder.append(_string);
        _builder.append(">()");
        _xifexpression = _builder;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
  
  protected CharSequence generateBitaccess(final VarDeclaration variable, final CharSequence dataType, final CharSequence accessorType, final int index) {
    CharSequence _xifexpression = null;
    if ((((this.BitSize(accessorType) > 0) && variable.isArray()) && 
      ((variable.getArraySize() * this.BitSize(dataType)) > this.BitSize(accessorType)))) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append(".partial<CIEC_");
      _builder.append(accessorType);
      _builder.append(",");
      String _string = Long.toString(index);
      _builder.append(_string);
      _builder.append(">()");
      _xifexpression = _builder;
    } else {
      CharSequence _xifexpression_1 = null;
      int _BitSize = this.BitSize(dataType);
      int _BitSize_1 = this.BitSize(accessorType);
      boolean _equals = (_BitSize == _BitSize_1);
      if (_equals) {
        StringConcatenation _builder_1 = new StringConcatenation();
        _xifexpression_1 = _builder_1;
      } else {
        StringConcatenation _builder_2 = new StringConcatenation();
        _xifexpression_1 = _builder_2;
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }
  
  private CharSequence extractTypeInformationWithPartialAccess(final PartialAccess part, final CharSequence DataType) {
    CharSequence _xifexpression = null;
    if ((null != part)) {
      String _xifexpression_1 = null;
      boolean _isBitaccess = part.isBitaccess();
      if (_isBitaccess) {
        _xifexpression_1 = FordiacKeywords.BOOL;
      } else {
        String _xifexpression_2 = null;
        boolean _isByteaccess = part.isByteaccess();
        if (_isByteaccess) {
          _xifexpression_2 = FordiacKeywords.BYTE;
        } else {
          String _xifexpression_3 = null;
          boolean _isWordaccess = part.isWordaccess();
          if (_isWordaccess) {
            _xifexpression_3 = FordiacKeywords.WORD;
          } else {
            String _xifexpression_4 = null;
            boolean _isDwordaccess = part.isDwordaccess();
            if (_isDwordaccess) {
              _xifexpression_4 = FordiacKeywords.DWORD;
            } else {
              _xifexpression_4 = "";
            }
            _xifexpression_3 = _xifexpression_4;
          }
          _xifexpression_2 = _xifexpression_3;
        }
        _xifexpression_1 = _xifexpression_2;
      }
      _xifexpression = _xifexpression_1;
    } else {
      _xifexpression = DataType;
    }
    return _xifexpression;
  }
  
  private CharSequence extractTypeInformation(final PrimaryVariable variable, final CharSequence DataType) {
    CharSequence _xifexpression = null;
    PartialAccess _part = variable.getPart();
    boolean _tripleNotEquals = (null != _part);
    if (_tripleNotEquals) {
      _xifexpression = this.extractTypeInformationWithPartialAccess(variable.getPart(), DataType);
    } else {
      _xifexpression = DataType;
    }
    return _xifexpression;
  }
  
  protected CharSequence _extractTypeInformation(final PrimaryVariable variable) {
    return this.extractTypeInformation(variable, this.extractTypeInformation(variable.getVar()));
  }
  
  protected CharSequence _extractTypeInformation(final VarDeclaration variable) {
    return variable.getType().getName();
  }
  
  protected CharSequence _extractTypeInformation(final AdapterVariable variable) {
    return variable.getVar().getType().getName();
  }
  
  private int BitSize(final Object declaration) {
    if (declaration instanceof VarDeclaration) {
      return _BitSize((VarDeclaration)declaration);
    } else if (declaration instanceof DataType) {
      return _BitSize((DataType)declaration);
    } else if (declaration instanceof AdapterVariable) {
      return _BitSize((AdapterVariable)declaration);
    } else if (declaration instanceof PrimaryVariable) {
      return _BitSize((PrimaryVariable)declaration);
    } else if (declaration instanceof PartialAccess) {
      return _BitSize((PartialAccess)declaration);
    } else if (declaration instanceof CharSequence) {
      return _BitSize((CharSequence)declaration);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(declaration).toString());
    }
  }
  
  protected CharSequence generateStatement(final Statement stmt) {
    if (stmt instanceof AssignmentStatement) {
      return _generateStatement((AssignmentStatement)stmt);
    } else if (stmt instanceof Call) {
      return _generateStatement((Call)stmt);
    } else if (stmt instanceof CaseStatement) {
      return _generateStatement((CaseStatement)stmt);
    } else if (stmt instanceof ContinueStatement) {
      return _generateStatement((ContinueStatement)stmt);
    } else if (stmt instanceof ExitStatement) {
      return _generateStatement((ExitStatement)stmt);
    } else if (stmt instanceof FBCall) {
      return _generateStatement((FBCall)stmt);
    } else if (stmt instanceof ForStatement) {
      return _generateStatement((ForStatement)stmt);
    } else if (stmt instanceof IfStatement) {
      return _generateStatement((IfStatement)stmt);
    } else if (stmt instanceof RepeatStatement) {
      return _generateStatement((RepeatStatement)stmt);
    } else if (stmt instanceof ReturnStatement) {
      return _generateStatement((ReturnStatement)stmt);
    } else if (stmt instanceof WhileStatement) {
      return _generateStatement((WhileStatement)stmt);
    } else if (stmt != null) {
      return _generateStatement(stmt);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(stmt).toString());
    }
  }
  
  private EList<Statement> containedStatements(final Statement statement) {
    if (statement instanceof CaseStatement) {
      return _containedStatements((CaseStatement)statement);
    } else if (statement instanceof ForStatement) {
      return _containedStatements((ForStatement)statement);
    } else if (statement instanceof IfStatement) {
      return _containedStatements((IfStatement)statement);
    } else if (statement instanceof RepeatStatement) {
      return _containedStatements((RepeatStatement)statement);
    } else if (statement instanceof WhileStatement) {
      return _containedStatements((WhileStatement)statement);
    } else if (statement != null) {
      return _containedStatements(statement);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(statement).toString());
    }
  }
  
  protected CharSequence generateExpression(final EObject expr) {
    if (expr instanceof AdapterRoot) {
      return _generateExpression((AdapterRoot)expr);
    } else if (expr instanceof IntLiteral) {
      return _generateExpression((IntLiteral)expr);
    } else if (expr instanceof RealLiteral) {
      return _generateExpression((RealLiteral)expr);
    } else if (expr instanceof AdapterVariable) {
      return _generateExpression((AdapterVariable)expr);
    } else if (expr instanceof ArrayVariable) {
      return _generateExpression((ArrayVariable)expr);
    } else if (expr instanceof BoolLiteral) {
      return _generateExpression((BoolLiteral)expr);
    } else if (expr instanceof PrimaryVariable) {
      return _generateExpression((PrimaryVariable)expr);
    } else if (expr instanceof StringLiteral) {
      return _generateExpression((StringLiteral)expr);
    } else if (expr instanceof TimeLiteral) {
      return _generateExpression((TimeLiteral)expr);
    } else if (expr instanceof BinaryExpression) {
      return _generateExpression((BinaryExpression)expr);
    } else if (expr instanceof Call) {
      return _generateExpression((Call)expr);
    } else if (expr instanceof InArgument) {
      return _generateExpression((InArgument)expr);
    } else if (expr instanceof UnaryExpression) {
      return _generateExpression((UnaryExpression)expr);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(expr).toString());
    }
  }
  
  protected CharSequence generateVarAccess(final VarDeclaration variable) {
    if (variable instanceof LocalVariable) {
      return _generateVarAccess((LocalVariable)variable);
    } else if (variable != null) {
      return _generateVarAccess(variable);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(variable).toString());
    }
  }
  
  protected CharSequence extractTypeInformation(final EObject variable) {
    if (variable instanceof VarDeclaration) {
      return _extractTypeInformation((VarDeclaration)variable);
    } else if (variable instanceof AdapterVariable) {
      return _extractTypeInformation((AdapterVariable)variable);
    } else if (variable instanceof PrimaryVariable) {
      return _extractTypeInformation((PrimaryVariable)variable);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(variable).toString());
    }
  }
}
