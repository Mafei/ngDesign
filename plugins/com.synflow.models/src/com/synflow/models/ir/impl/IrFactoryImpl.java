/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.synflow.models.ir.impl;

import static com.synflow.models.ir.ExprTypeConv.SIGNED;
import static com.synflow.models.ir.ExprTypeConv.UNSIGNED;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

import com.synflow.models.ir.BlockBasic;
import com.synflow.models.ir.BlockIf;
import com.synflow.models.ir.BlockWhile;
import com.synflow.models.ir.Def;
import com.synflow.models.ir.ExprBinary;
import com.synflow.models.ir.ExprBool;
import com.synflow.models.ir.ExprFloat;
import com.synflow.models.ir.ExprInt;
import com.synflow.models.ir.ExprList;
import com.synflow.models.ir.ExprResize;
import com.synflow.models.ir.ExprString;
import com.synflow.models.ir.ExprTypeConv;
import com.synflow.models.ir.ExprUnary;
import com.synflow.models.ir.ExprVar;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.InstAssign;
import com.synflow.models.ir.InstCall;
import com.synflow.models.ir.InstLoad;
import com.synflow.models.ir.InstPhi;
import com.synflow.models.ir.InstReturn;
import com.synflow.models.ir.InstStore;
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.IrPackage;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.OpUnary;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.TypeArray;
import com.synflow.models.ir.TypeBool;
import com.synflow.models.ir.TypeFloat;
import com.synflow.models.ir.TypeInt;
import com.synflow.models.ir.TypeString;
import com.synflow.models.ir.TypeVoid;
import com.synflow.models.ir.Use;
import com.synflow.models.ir.Var;
import com.synflow.models.ir.util.IrUtil;
import com.synflow.models.ir.util.TypeUtil;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!-- end-user-doc -->
 * 
 * @generated
 */
public class IrFactoryImpl extends EFactoryImpl implements IrFactory {

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static IrPackage getPackage() {
		return IrPackage.eINSTANCE;
	}

	/**
	 * Creates the default factory implementation. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public static IrFactory init() {
		try {
			IrFactory theIrFactory = (IrFactory) EPackage.Registry.INSTANCE
					.getEFactory(IrPackage.eNS_URI);
			if (theIrFactory != null) {
				return theIrFactory;
			}
		} catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new IrFactoryImpl();
	}

	/**
	 * Creates an instance of the factory. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public IrFactoryImpl() {
		super();
	}

	@Override
	public void addLocal(Procedure procedure, Var variable) {
		String name = variable.getName();
		Var existing = procedure.getLocal(name);
		int i = 0;
		while (existing != null) {
			name = variable.getName() + i;
			existing = procedure.getLocal(name);
			i++;
		}

		variable.setName(name);
		procedure.getLocals().add(variable);
	}

	@Override
	public Expression cast(Type targetType, Type sourceType, Expression expr) {
		if (sourceType.isInt() && targetType.isInt()) {
			TypeInt srcInt = (TypeInt) sourceType;
			TypeInt tgtInt = (TypeInt) targetType;

			if (srcInt.isSigned() ^ tgtInt.isSigned()) {
				// first convert to the correct type
				String typeName = tgtInt.isSigned() ? SIGNED : UNSIGNED;
				expr = convert(typeName, expr);
			}

			// resize only if necessary
			if (tgtInt.getSize() != srcInt.getSize()) {
				ExprResizeImpl resize = new ExprResizeImpl();
				resize.setTargetSize(tgtInt.getSize());
				resize.setExpr(expr);
				return resize;
			}
		}

		return expr;
	}

	@Override
	public Expression castToUnsigned(int size, Expression expr) {
		if (expr.isExprInt()) {
			((ExprInt) expr).setSize(size);
		}

		Type type = TypeUtil.getType(expr);
		if (type.isInt() && ((TypeInt) type).isSigned()) {
			if (expr.isExprInt()) {
				return convert(UNSIGNED, expr);
			} else {
				ExprResizeImpl exprCast = new ExprResizeImpl();
				exprCast.setTargetSize(size);
				exprCast.setExpr(convert(UNSIGNED, expr));
				return exprCast;
			}
		}
		return expr;
	}

	@Override
	public ExprTypeConv convert(String typeName, Expression expr) {
		ExprTypeConvImpl exprTypeConv = new ExprTypeConvImpl();
		exprTypeConv.setTypeName(typeName);
		exprTypeConv.setExpr(expr);
		return exprTypeConv;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public String convertOpBinaryToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public String convertOpUnaryToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
		case IrPackage.OP_BINARY:
			return convertOpBinaryToString(eDataType, instanceValue);
		case IrPackage.OP_UNARY:
			return convertOpUnaryToString(eDataType, instanceValue);
		default:
			throw new IllegalArgumentException("The datatype '" + eDataType.getName()
					+ "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
		case IrPackage.PROCEDURE:
			return createProcedure();
		case IrPackage.BLOCK_BASIC:
			return createBlockBasic();
		case IrPackage.BLOCK_IF:
			return createBlockIf();
		case IrPackage.BLOCK_WHILE:
			return createBlockWhile();
		case IrPackage.INST_ASSIGN:
			return createInstAssign();
		case IrPackage.INST_CALL:
			return createInstCall();
		case IrPackage.INST_LOAD:
			return createInstLoad();
		case IrPackage.INST_PHI:
			return createInstPhi();
		case IrPackage.INST_RETURN:
			return createInstReturn();
		case IrPackage.INST_STORE:
			return createInstStore();
		case IrPackage.EXPR_BINARY:
			return createExprBinary();
		case IrPackage.EXPR_BOOL:
			return createExprBool();
		case IrPackage.EXPR_FLOAT:
			return createExprFloat();
		case IrPackage.EXPR_INT:
			return createExprInt();
		case IrPackage.EXPR_LIST:
			return createExprList();
		case IrPackage.EXPR_STRING:
			return createExprString();
		case IrPackage.EXPR_RESIZE:
			return createExprResize();
		case IrPackage.EXPR_TYPE_CONV:
			return createExprTypeConv();
		case IrPackage.EXPR_UNARY:
			return createExprUnary();
		case IrPackage.EXPR_VAR:
			return createExprVar();
		case IrPackage.TYPE_ARRAY:
			return createTypeArray();
		case IrPackage.TYPE_BOOL:
			return createTypeBool();
		case IrPackage.TYPE_FLOAT:
			return createTypeFloat();
		case IrPackage.TYPE_INT:
			return createTypeInt();
		case IrPackage.TYPE_STRING:
			return createTypeString();
		case IrPackage.TYPE_VOID:
			return createTypeVoid();
		case IrPackage.DEF:
			return createDef();
		case IrPackage.VAR:
			return createVar();
		case IrPackage.USE:
			return createUse();
		default:
			throw new IllegalArgumentException("The class '" + eClass.getName()
					+ "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public BlockBasic createBlockBasic() {
		BlockBasicImpl blockBasic = new BlockBasicImpl();
		return blockBasic;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public BlockIf createBlockIf() {
		BlockIfImpl blockIf = new BlockIfImpl();
		return blockIf;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public BlockWhile createBlockWhile() {
		BlockWhileImpl blockWhile = new BlockWhileImpl();
		return blockWhile;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Def createDef() {
		DefImpl def = new DefImpl();
		return def;
	}

	@Override
	public Def createDef(Var variable) {
		DefImpl def = new DefImpl();
		def.setVariable(variable);
		return def;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ExprBinary createExprBinary() {
		ExprBinaryImpl exprBinary = new ExprBinaryImpl();
		return exprBinary;
	}

	@Override
	public ExprBinary createExprBinary(Expression e1, OpBinary op, Expression e2) {
		ExprBinaryImpl exprBinary = new ExprBinaryImpl();
		exprBinary.setE1(e1);
		exprBinary.setE2(e2);
		exprBinary.setOp(op);
		return exprBinary;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ExprBool createExprBool() {
		ExprBoolImpl exprBool = new ExprBoolImpl();
		return exprBool;
	}

	@Override
	public ExprBool createExprBool(boolean value) {
		ExprBoolImpl exprBool = new ExprBoolImpl();
		exprBool.setValue(value);
		return exprBool;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ExprFloat createExprFloat() {
		ExprFloatImpl exprFloat = new ExprFloatImpl();
		return exprFloat;
	}

	@Override
	public ExprFloat createExprFloat(BigDecimal value) {
		ExprFloatImpl exprFloat = new ExprFloatImpl();
		exprFloat.setValue(value);
		return exprFloat;
	}

	@Override
	public ExprFloat createExprFloat(float value) {
		ExprFloatImpl exprFloat = new ExprFloatImpl();
		exprFloat.setValue(BigDecimal.valueOf(value));
		return exprFloat;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ExprInt createExprInt() {
		ExprIntImpl exprInt = new ExprIntImpl();
		return exprInt;
	}

	@Override
	public ExprInt createExprInt(BigInteger value) {
		ExprIntImpl exprInt = new ExprIntImpl();
		exprInt.setValue(value);
		return exprInt;
	}

	@Override
	public ExprInt createExprInt(int value) {
		return createExprInt(BigInteger.valueOf(value));
	}

	@Override
	public ExprInt createExprInt(long value) {
		return createExprInt(BigInteger.valueOf(value));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ExprList createExprList() {
		ExprListImpl exprList = new ExprListImpl();
		return exprList;
	}

	@Override
	public ExprList createExprList(ExprList l1, ExprList l2) {
		ExprListImpl exprList = new ExprListImpl();
		exprList.getValue().addAll(l1.getValue());
		exprList.getValue().addAll(l2.getValue());
		return exprList;
	}

	@Override
	public ExprList createExprList(List<Expression> exprs) {
		ExprListImpl exprList = new ExprListImpl();
		exprList.getValue().addAll(exprs);
		return exprList;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ExprResize createExprResize() {
		ExprResizeImpl exprResize = new ExprResizeImpl();
		return exprResize;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ExprString createExprString() {
		ExprStringImpl exprString = new ExprStringImpl();
		return exprString;
	}

	@Override
	public ExprString createExprString(String value) {
		ExprStringImpl exprString = new ExprStringImpl();
		exprString.setValue(value);
		return exprString;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ExprTypeConv createExprTypeConv() {
		ExprTypeConvImpl exprTypeConv = new ExprTypeConvImpl();
		return exprTypeConv;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ExprUnary createExprUnary() {
		ExprUnaryImpl exprUnary = new ExprUnaryImpl();
		return exprUnary;
	}

	@Override
	public ExprUnary createExprUnary(OpUnary op, Expression expression) {
		ExprUnaryImpl exprUnary = new ExprUnaryImpl();
		exprUnary.setExpr(expression);
		exprUnary.setOp(op);
		return exprUnary;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ExprVar createExprVar() {
		ExprVarImpl exprVar = new ExprVarImpl();
		return exprVar;
	}

	@Override
	public ExprVar createExprVar(Use use) {
		ExprVarImpl exprVar = new ExprVarImpl();
		exprVar.setUse(use);
		return exprVar;
	}

	@Override
	public ExprVar createExprVar(Var variable) {
		ExprVarImpl exprVar = new ExprVarImpl();
		exprVar.setUse(IrFactory.eINSTANCE.createUse(variable));
		return exprVar;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
		case IrPackage.OP_BINARY:
			return createOpBinaryFromString(eDataType, initialValue);
		case IrPackage.OP_UNARY:
			return createOpUnaryFromString(eDataType, initialValue);
		default:
			throw new IllegalArgumentException("The datatype '" + eDataType.getName()
					+ "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public InstAssign createInstAssign() {
		InstAssignImpl instAssign = new InstAssignImpl();
		return instAssign;
	}

	@Override
	public InstAssign createInstAssign(int lineNumber, Var target, Expression value) {
		InstAssignImpl instAssign = new InstAssignImpl();
		instAssign.setLineNumber(lineNumber);
		instAssign.setTarget(IrFactory.eINSTANCE.createDef(target));
		instAssign.setValue(value);
		return instAssign;
	}

	@Override
	public InstAssign createInstAssign(Var target, Expression value) {
		return createInstAssign(0, target, value);
	}

	@Override
	public InstAssign createInstAssign(Var target, int value) {
		return createInstAssign(target, createExprInt(value));
	}

	@Override
	public InstAssign createInstAssign(Var target, long value) {
		return createInstAssign(target, createExprInt(value));
	}

	@Override
	public InstAssign createInstAssign(Var target, Var value) {
		return createInstAssign(target, createExprVar(value));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public InstCall createInstCall() {
		InstCallImpl instCall = new InstCallImpl();
		return instCall;
	}

	@Override
	public InstCall createInstCall(int lineNumber, Var target, Procedure procedure,
			List<Expression> parameters) {
		InstCallImpl instCall = new InstCallImpl();
		instCall.setLineNumber(lineNumber);
		if (target != null) {
			instCall.setTarget(IrFactory.eINSTANCE.createDef(target));
		}
		instCall.setProcedure(procedure);
		if (parameters != null) {
			instCall.getArguments().addAll(parameters);
		}

		return instCall;
	}

	@Override
	public InstCall createInstCall(Var target, Procedure procedure, List<Expression> parameters) {
		return createInstCall(0, target, procedure, parameters);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public InstLoad createInstLoad() {
		InstLoadImpl instLoad = new InstLoadImpl();
		return instLoad;
	}

	@Override
	public InstLoad createInstLoad(int lineNumber, Def target, Use source, List<Expression> indexes) {
		InstLoadImpl instLoad = new InstLoadImpl();
		instLoad.setLineNumber(lineNumber);
		instLoad.setTarget(target);
		instLoad.setSource(source);
		instLoad.getIndexes().addAll(indexes);
		return instLoad;
	}

	@Override
	public InstLoad createInstLoad(int lineNumber, Var target, Var source) {
		InstLoadImpl instLoad = new InstLoadImpl();
		instLoad.setLineNumber(lineNumber);
		instLoad.setTarget(IrFactory.eINSTANCE.createDef(target));
		instLoad.setSource(IrFactory.eINSTANCE.createUse(source));
		return instLoad;
	}

	@Override
	public InstLoad createInstLoad(int lineNumber, Var target, Var source, List<Expression> indexes) {
		return createInstLoad(lineNumber, IrFactory.eINSTANCE.createDef(target),
				IrFactory.eINSTANCE.createUse(source), indexes);
	}

	@Override
	public InstLoad createInstLoad(Var target, Var source) {
		return createInstLoad(0, target, source);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public InstPhi createInstPhi() {
		InstPhiImpl instPhi = new InstPhiImpl();
		return instPhi;
	}

	@Override
	public InstPhi createInstPhi(int lineNumber, Def target, List<Expression> values) {
		InstPhiImpl instPhi = new InstPhiImpl();
		instPhi.setLineNumber(lineNumber);
		instPhi.setTarget(target);
		instPhi.getValues().addAll(values);
		return instPhi;
	}

	@Override
	public InstPhi createInstPhi(int lineNumber, Var target, List<Expression> values) {
		return createInstPhi(lineNumber, IrFactory.eINSTANCE.createDef(target), values);
	}

	@Override
	public InstPhi createInstPhi(Var target, List<Expression> values) {
		return createInstPhi(0, IrFactory.eINSTANCE.createDef(target), values);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public InstReturn createInstReturn() {
		InstReturnImpl instReturn = new InstReturnImpl();
		return instReturn;
	}

	@Override
	public InstReturn createInstReturn(Expression value) {
		InstReturnImpl instReturn = new InstReturnImpl();
		instReturn.setValue(value);
		return instReturn;
	}

	@Override
	public InstReturn createInstReturn(int lineNumber, Expression value) {
		InstReturnImpl instReturn = new InstReturnImpl();
		instReturn.setLineNumber(lineNumber);
		instReturn.setValue(value);
		return instReturn;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public InstStore createInstStore() {
		InstStoreImpl instStore = new InstStoreImpl();
		return instStore;
	}

	@Override
	public InstStore createInstStore(int lineNumber, Var target, Collection<Expression> indexes,
			Expression value) {
		InstStoreImpl instStore = new InstStoreImpl();
		instStore.setLineNumber(lineNumber);
		instStore.setTarget(IrFactory.eINSTANCE.createDef(target));
		instStore.setValue(value);
		instStore.getIndexes().addAll(indexes);
		return instStore;
	}

	@Override
	public InstStore createInstStore(int lineNumber, Var target, Expression value) {
		InstStoreImpl instStore = new InstStoreImpl();
		instStore.setLineNumber(lineNumber);
		instStore.setTarget(IrFactory.eINSTANCE.createDef(target));
		instStore.setValue(value);
		return instStore;
	}

	@Override
	public InstStore createInstStore(Var target, Var source) {
		InstStoreImpl instStore = new InstStoreImpl();
		instStore.setTarget(IrFactory.eINSTANCE.createDef(target));
		instStore.setValue(createExprVar(source));
		return instStore;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public OpBinary createOpBinaryFromString(EDataType eDataType, String initialValue) {
		OpBinary result = OpBinary.get(initialValue);
		if (result == null)
			throw new IllegalArgumentException("The value '" + initialValue
					+ "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public OpUnary createOpUnaryFromString(EDataType eDataType, String initialValue) {
		OpUnary result = OpUnary.get(initialValue);
		if (result == null)
			throw new IllegalArgumentException("The value '" + initialValue
					+ "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Procedure createProcedure() {
		ProcedureImpl procedure = new ProcedureImpl();
		return procedure;
	}

	@Override
	public Procedure createProcedure(String name, int lineNumber, Type returnType) {
		ProcedureImpl procedure = new ProcedureImpl();

		procedure.setLineNumber(lineNumber);
		procedure.setName(name);
		procedure.setReturnType(IrUtil.copy(returnType));

		return procedure;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public TypeArray createTypeArray() {
		TypeArrayImpl typeArray = new TypeArrayImpl();
		return typeArray;
	}

	@Override
	public TypeArray createTypeArray(Type type, int... dimensions) {
		TypeArrayImpl typeArray = new TypeArrayImpl();
		typeArray.setElementType(IrUtil.copy(type));
		for (int dim : dimensions) {
			typeArray.getDimensions().add(dim);
		}
		return typeArray;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public TypeBool createTypeBool() {
		TypeBoolImpl typeBool = new TypeBoolImpl();
		return typeBool;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 */
	public TypeFloat createTypeFloat() {
		TypeFloatImpl typeFloat = new TypeFloatImpl();
		return typeFloat;
	}

	public TypeFloat createTypeFloat(int size) {
		TypeFloatImpl typeFloat = new TypeFloatImpl();
		typeFloat.setSize(size);
		return typeFloat;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public TypeInt createTypeInt() {
		TypeIntImpl typeInt = new TypeIntImpl();
		return typeInt;
	}

	@Override
	public TypeInt createTypeInt(int size, boolean signed) {
		TypeIntImpl typeInt = new TypeIntImpl();
		typeInt.setSize(size);
		typeInt.setSigned(signed);
		return typeInt;
	}

	@Override
	public TypeInt createTypeIntOrUint(BigInteger value) {
		int size = TypeUtil.getSize(value);
		TypeInt typeInt = IrFactory.eINSTANCE.createTypeInt();
		typeInt.setSize(size);
		typeInt.setSigned(value.compareTo(BigInteger.ZERO) < 0);
		return typeInt;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public TypeString createTypeString() {
		TypeStringImpl typeString = new TypeStringImpl();
		return typeString;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public TypeVoid createTypeVoid() {
		TypeVoidImpl typeVoid = new TypeVoidImpl();
		return typeVoid;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Use createUse() {
		UseImpl use = new UseImpl();
		return use;
	}

	@Override
	public Use createUse(Var variable) {
		UseImpl use = new UseImpl();
		use.setVariable(variable);
		return use;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Var createVar() {
		VarImpl var = new VarImpl();
		return var;
	}

	@Override
	public Var createVar(int lineNumber, Type type, String name, boolean assignable) {
		VarImpl var = new VarImpl();
		var.setAssignable(assignable);
		var.setLineNumber(lineNumber);
		var.setName(name);
		var.setType(IrUtil.copy(type));
		return var;
	}

	@Override
	public Var createVar(int lineNumber, Type type, String name, boolean assignable,
			Expression initialValue) {
		VarImpl var = new VarImpl();
		var.setAssignable(assignable);
		var.setInitialValue(initialValue);
		var.setLineNumber(lineNumber);
		var.setName(name);
		var.setType(type);
		return var;
	}

	@Override
	public Var createVar(int lineNumber, Type type, String name, boolean assignable, int index) {
		VarImpl var = new VarImpl();
		var.setAssignable(assignable);
		var.setIndex(index);
		var.setLineNumber(lineNumber);
		var.setName(name);
		var.setType(IrUtil.copy(type));
		return var;
	}

	@Override
	public Var createVar(Type type, String name, boolean assignable) {
		return createVar(0, type, name, assignable);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public IrPackage getIrPackage() {
		return (IrPackage) getEPackage();
	}

	@Override
	public Var newTempLocalVariable(Procedure procedure, Type type, String hint) {
		Var variable = IrFactory.eINSTANCE.createVar(0, type, hint, true, 0);
		addLocal(procedure, variable);
		return variable;
	}

} // IrFactoryImpl
