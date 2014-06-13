/**
 */
package com.synflow.models.dpn.impl;

import static com.synflow.models.ir.util.IrUtil.getDirection;

import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.synflow.models.dpn.DpnPackage;
import com.synflow.models.dpn.InterfaceType;
import com.synflow.models.dpn.Port;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.impl.VarImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Port</b></em>'. <!--
 * end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link com.synflow.models.dpn.impl.PortImpl#getInterface <em>Interface</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class PortImpl extends VarImpl implements Port {

	/**
	 * The default value of the '{@link #getInterface() <em>Interface</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getInterface()
	 * @generated
	 * @ordered
	 */
	protected static final InterfaceType INTERFACE_EDEFAULT = InterfaceType.BARE;

	/**
	 * The cached value of the '{@link #getInterface() <em>Interface</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getInterface()
	 * @generated
	 * @ordered
	 */
	protected InterfaceType interface_ = INTERFACE_EDEFAULT;

	private Function<String, String> prependName = new Function<String, String>() {
		@Override
		public String apply(String signal) {
			return name + "_" + signal;
		}
	};

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected PortImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case DpnPackage.PORT__INTERFACE:
			return getInterface();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case DpnPackage.PORT__INTERFACE:
			return interface_ != INTERFACE_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case DpnPackage.PORT__INTERFACE:
			setInterface((InterfaceType) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DpnPackage.Literals.PORT;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case DpnPackage.PORT__INTERFACE:
			setInterface(INTERFACE_EDEFAULT);
			return;
		}
		super.eUnset(featureID);
	}

	public Iterable<String> getAdditionalInputs() {
		InterfaceType iface = getInterface();
		if (iface == InterfaceType.BARE) {
			return ImmutableSet.of();
		}

		Map<String, Expression> signals = iface.getInputs(getDirection(this));
		return Iterables.transform(signals.keySet(), prependName);
	}

	public Iterable<String> getAdditionalOutputs() {
		InterfaceType iface = getInterface();
		if (iface == InterfaceType.BARE) {
			return ImmutableSet.of();
		}

		Map<String, Expression> signals = iface.getOutputs(getDirection(this));
		return Iterables.transform(signals.keySet(), prependName);
	}

	public Iterable<String> getAdditionalSignals() {
		InterfaceType iface = getInterface();
		if (iface == InterfaceType.BARE) {
			return ImmutableSet.of();
		}

		return Iterables.transform(iface.getSignals().keySet(), prependName);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public InterfaceType getInterface() {
		return interface_;
	}

	@Override
	public boolean isSync() {
		return interface_ == InterfaceType.SYNC;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setInterface(InterfaceType newInterface) {
		InterfaceType oldInterface = interface_;
		interface_ = newInterface == null ? INTERFACE_EDEFAULT : newInterface;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DpnPackage.PORT__INTERFACE,
					oldInterface, interface_));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy())
			return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (interface: ");
		result.append(interface_);
		result.append(')');
		return result.toString();
	}

} // PortImpl
