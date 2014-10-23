/*******************************************************************************
 * Copyright (c) 2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * Copyright (c) 2011, IETR/INSA of Rennes
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.synflow.models.ir.util;

import static com.synflow.models.ir.IrFactory.eINSTANCE;

import java.math.BigInteger;
import java.util.Iterator;

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
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.OpUnary;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.TypeArray;
import com.synflow.models.ir.TypeInt;

/**
 * This class defines static utility methods to deal with types.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class TypeUtil {

	private static class IrTyper extends IrSwitch<Type> {

		@Override
		public Type caseExprBinary(ExprBinary expr) {
			Type t1 = doSwitch(expr.getE1());
			Type t2 = doSwitch(expr.getE2());
			Object amount = ValueUtil.getValue(expr.getE2());
			return getTypeBinary(expr.getOp(), t1, t2, amount);
		}

		@Override
		public Type caseExprBool(ExprBool expr) {
			return IrFactory.eINSTANCE.createTypeBool();
		}

		@Override
		public Type caseExprFloat(ExprFloat expr) {
			int precision = expr.getValue().precision();
			if (precision <= 11) {
				return IrFactory.eINSTANCE.createTypeFloat(16);
			} else if (precision <= 24) {
				return IrFactory.eINSTANCE.createTypeFloat(32);
			} else {
				return IrFactory.eINSTANCE.createTypeFloat(64);
			}
		}

		@Override
		public Type caseExprInt(ExprInt expr) {
			TypeInt type = IrFactory.eINSTANCE.createTypeIntOrUint(expr.getValue());
			if (expr.getSize() != 0) {
				type.setSize(expr.getSize());
			}
			return type;
		}

		@Override
		public Type caseExprList(ExprList expr) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Type caseExprResize(ExprResize cast) {
			Type type = getType(cast.getExpr());
			boolean signed = false;
			if (type != null && type.isInt()) {
				signed = ((TypeInt) type).isSigned();
				return IrFactory.eINSTANCE.createTypeInt(cast.getTargetSize(), signed);
			}
			return type;
		}

		@Override
		public Type caseExprString(ExprString expr) {
			return IrFactory.eINSTANCE.createTypeString();
		}

		@Override
		public Type caseExprTypeConv(ExprTypeConv cast) {
			Type typeExpr = getType(cast.getExpr());
			boolean signed = false;
			if (typeExpr != null && typeExpr.isInt()) {
				signed = ((TypeInt) typeExpr).isSigned();
			}

			TypeInt type = IrFactory.eINSTANCE.createTypeInt();
			if (ExprTypeConv.SIGNED.equals(cast.getTypeName())) {
				signed = true;
			} else if (ExprTypeConv.UNSIGNED.equals(cast.getTypeName())) {
				signed = false;
			}
			type.setSigned(signed);
			type.setSize(TypeUtil.getSize(typeExpr));
			return type;
		}

		@Override
		public Type caseExprUnary(ExprUnary expr) {
			return getTypeUnary(expr.getOp(), doSwitch(expr.getExpr()));
		}

		@Override
		public Type caseExprVar(ExprVar expr) {
			return expr.getUse().getVariable().getType();
		}

	}

	/**
	 * Returns <code>true</code> if an expression of type src can be assigned to a target of type
	 * dst.
	 * 
	 * @param src
	 *            a type
	 * @param dst
	 *            the type src should be converted to
	 * @return <code>true</code> if type src can be converted to type dst
	 */
	public static boolean canAssign(Type src, Type dst) {
		if (src == null || dst == null) {
			return false;
		}

		if (src.isBool() && dst.isBool() || src.isString() && dst.isString()) {
			return true;
		}

		int srcSize = 0;
		if (src.isInt()) {
			TypeInt srcInt = (TypeInt) src;
			srcSize = srcInt.getSize();
		}

		int dstSize = 0;
		if (dst.isInt()) {
			TypeInt dstInt = (TypeInt) dst;
			dstSize = dstInt.getSize();
		}

		if (src.isInt()) {
			if (dst.isBool()) {
				return srcSize == 1;
			} else if (dst.isInt()) {
				return srcSize <= dstSize;
			}
		}

		if (src.isInt() && dst.isFloat()) {
			return true;
		}

		if (src.isArray() && dst.isArray()) {
			TypeArray typeSrc = (TypeArray) src;
			TypeArray typeDst = (TypeArray) dst;
			// Recursively check type convertibility
			if (canAssign(typeSrc.getElementType(), typeDst.getElementType())) {
				Iterator<Integer> itD = typeDst.getDimensions().iterator();
				Iterator<Integer> itS = typeSrc.getDimensions().iterator();
				while (itD.hasNext()) {
					if (!itS.hasNext()) {
						// not the same dimensions
						return false;
					}
				}

				if (itS.hasNext()) {
					return false;
				}
			}
		}

		return false;
	}

	/**
	 * Returns a new type that is as large as the biggest type and compatible with both. If no such
	 * type exists (e.g. when types are not compatible with each other), <code>null</code> is
	 * returned.
	 * 
	 * @param t1
	 *            a type
	 * @param t2
	 *            another type
	 * @return a type compatible with the given types and big enough to contain both
	 */
	public static Type getLargest(Type t1, Type t2) {
		if (t1 == null || t2 == null) {
			return null;
		}

		if (t1.isBool() && t2.isBool()) {
			return eINSTANCE.createTypeBool();
		} else if (t1.isString() && t2.isString()) {
			return eINSTANCE.createTypeString();
		} else if (t1.isInt() && t2.isInt()) {
			TypeInt ti1 = (TypeInt) t1;
			TypeInt ti2 = (TypeInt) t2;
			boolean signed = ti1.isSigned() || ti2.isSigned();
			int size = Math.max(ti1.getSize(), ti2.getSize());
			return eINSTANCE.createTypeInt(size, signed);
		} else if (t1.isArray() && t2.isArray()) {
			TypeArray lt1 = (TypeArray) t1;
			TypeArray lt2 = (TypeArray) t2;
			Type type = getLargest(lt1.getElementType(), lt2.getElementType());
			if (type != null) {
				// only return a list when the underlying type is valid
				TypeArray array = eINSTANCE.createTypeArray();
				array.setElementType(type);
				Iterator<Integer> itD = lt1.getDimensions().iterator();
				Iterator<Integer> itS = lt2.getDimensions().iterator();
				boolean ok = true;
				while (itD.hasNext()) {
					if (!itS.hasNext()) {
						// not the same dimensions
						ok = false;
					} else {
						int sizeD = itD.next();
						int sizeS = itS.next();
						int dim = Math.min(sizeD, sizeS);
						array.getDimensions().add(dim);
					}
				}

				// not ok if itD finished but not itS
				ok &= !itS.hasNext();

				if (ok) {
					return array;
				}

				// not ok: delete array
				IrUtil.delete(array);
			}
		}

		return null;
	}

	public static Type getLargestPlus1(Type t1, Type t2) {
		Type type = TypeUtil.getLargest(t1, t2);
		if (type instanceof TypeInt) {
			TypeInt typeInt = (TypeInt) type;
			typeInt.setSize(typeInt.getSize() + 1);
		}
		return type;
	}

	/**
	 * Returns the number of bits in the two's-complement representation of the given number,
	 * including a sign bit <i>only</i> if <code>number</code> is less than zero.
	 * 
	 * @param number
	 *            a number
	 * @return the number of bits in the two's-complement representation of the given number,
	 *         <i>including</i> a sign bit
	 */
	public static int getSize(BigInteger number) {
		int cmp = number.compareTo(BigInteger.ZERO);
		if (cmp == 0) {
			// 0 is represented as a u1
			return 1;
		} else {
			int bitLength = number.bitLength();
			return (cmp > 0) ? bitLength : bitLength + 1;
		}
	}

	/**
	 * Returns the number of bits in the two's-complement representation of the given number,
	 * including a sign bit <i>only</i> if <code>number</code> is less than zero.
	 * 
	 * @param number
	 *            a number
	 * @return the number of bits in the two's-complement representation of the given number,
	 *         <i>including</i> a sign bit
	 */
	public static int getSize(long number) {
		return getSize(BigInteger.valueOf(number));
	}

	/**
	 * Returns the size in bits of the given type. Returns <code>null</code> if the size cannot be
	 * computed.
	 * 
	 * @param type
	 *            a type
	 * @return a size (1 for bool)
	 */
	public static int getSize(Type type) {
		if (type.isBool()) {
			return 1;
		} else if (type.isInt()) {
			TypeInt typeInt = (TypeInt) type;
			return typeInt.getSize();
		}
		return 0;
	}

	/**
	 * Returns the smallest type of (t1, t2). When t1 and t2 have different signedness (e.g. one is
	 * a int and the other is a uint), an int is returned.
	 * 
	 * @param t1
	 *            a type
	 * @param t2
	 *            another type
	 * @return the smallest of the given types
	 */
	public static Type getSmallest(Type t1, Type t2) {
		if (t1.isBool() && t2.isBool()) {
			return eINSTANCE.createTypeBool();
		} else if (t1.isInt() && t2.isInt()) {
			TypeInt ti1 = (TypeInt) t1;
			TypeInt ti2 = (TypeInt) t2;
			boolean signed = ti1.isSigned() || ti2.isSigned();
			int size = Math.min(ti1.getSize(), ti2.getSize());
			return eINSTANCE.createTypeInt(size, signed);
		}

		return null;
	}

	/**
	 * Returns a new type whose size is the sum of the two given types' sizes. If types are not
	 * compatible, returns <code>null</code>.
	 * 
	 * @param t1
	 *            type 1
	 * @param t2
	 *            type 2
	 * @return a type
	 */
	public static Type getSum(Type t1, Type t2) {
		Type type = TypeUtil.getLargest(t1, t2);
		if (type instanceof TypeInt) {
			TypeInt ti1 = (TypeInt) t1;
			TypeInt ti2 = (TypeInt) t2;
			TypeInt typeInt = (TypeInt) type;
			int size1 = ti1.getSize();
			int size2 = ti2.getSize();
			typeInt.setSize(size1 + size2);
		}
		return type;
	}

	/**
	 * Returns the unresolved type of the given expression.
	 * 
	 * @param expr
	 *            an expression
	 * @return a type
	 */
	public static Type getType(Expression expr) {
		return new IrTyper().doSwitch(expr);
	}

	/**
	 * Returns the type of a binary expression whose left operand has type t1 and right operand has
	 * type t2, and whose operator is given.
	 * 
	 * @param op
	 *            operator
	 * @param t1
	 *            type of the first operand
	 * @param t2
	 *            type of the second operand
	 * @return the type of the binary expression, or <code>null</code>
	 */
	public static Type getTypeBinary(OpBinary op, Type t1, Type t2, Object amount) {
		if (t1 == null || t2 == null) {
			return null;
		}

		Type type = tryGetPreciseType(t1, op, amount);
		if (type != null) {
			return type;
		}

		switch (op) {
		case BITAND:
			return TypeUtil.getSmallest(t1, t2);

		case BITOR:
		case BITXOR:
			return TypeUtil.getLargest(t1, t2);

		case TIMES:
			return TypeUtil.getSum(t1, t2);

		case MINUS:
		case PLUS:
			return TypeUtil.getLargestPlus1(t1, t2);

		case EQ:
		case NE:
			// can compare (in)equality of any two compatible types
			if (getLargest(t1, t2) != null) {
				return IrFactory.eINSTANCE.createTypeBool();
			}
			return null;

		case GE:
		case GT:
		case LE:
		case LT: {
			// can compare any two compatible types (except bool)
			Type largest = getLargest(t1, t2);
			if (largest != null && !largest.isBool()) {
				return IrFactory.eINSTANCE.createTypeBool();
			}
			return null;
		}

		case LOGIC_AND:
		case LOGIC_OR:
			// operands must be booleans
			if (t1.isBool() && t2.isBool()) {
				return IrFactory.eINSTANCE.createTypeBool();
			}
			return null;

		default:
			// shifts, div, mod are typed by tryGetPreciseType
			// exp is not typed
			return null;
		}
	}

	/**
	 * Returns the type of a unary expression whose operand has type type and whose operator is
	 * given.
	 * 
	 * @param op
	 *            operator
	 * @param type
	 *            type of the first operand
	 * @return the type of the unary expression, or <code>null</code>
	 */
	public static Type getTypeUnary(OpUnary op, Type type) {
		if (type == null) {
			return null;
		}

		switch (op) {
		case BITNOT:
		case LOGIC_NOT:
			return IrUtil.copy(type);
		case MINUS:
			// -x <=> 0 - x <=> adds one bit to the type of x
			if (type.isInt()) {
				int size = ((TypeInt) type).getSize();
				return IrFactory.eINSTANCE.createTypeInt(size + 1, true);
			}
		default:
			return null;
		}
	}

	/**
	 * Try and get a precise type if the given expression evaluates to a compile-time integer.
	 * 
	 * @param t1
	 * @param op
	 * @param right
	 * @return
	 */
	private static Type tryGetPreciseType(Type t1, OpBinary op, Object val2) {
		if (!ValueUtil.isInt(val2)) {
			return null;
		}

		int n = ((BigInteger) val2).intValue();
		if (n <= 0) {
			return null;
		}

		if (!(t1 instanceof TypeInt)) {
			return null;
		}

		TypeInt typeInt = (TypeInt) IrUtil.copy(t1);
		if (op == OpBinary.DIV) {
			// valid because we know "n" is a multiple of two (see validator)
			int size = BigInteger.valueOf(n - 1).bitLength();
			typeInt.setSize(typeInt.getSize() - size);
		} else if (op == OpBinary.MOD) {
			// valid because we know "n" is a multiple of two (see validator)
			int size = BigInteger.valueOf(n - 1).bitLength();
			typeInt.setSize(size);
		} else if (op == OpBinary.SHIFT_LEFT) {
			typeInt.setSize(typeInt.getSize() + n);
		} else if (op == OpBinary.SHIFT_RIGHT) {
			typeInt.setSize(typeInt.getSize() - n);
		} else {
			return null;
		}

		return typeInt;
	}

}
