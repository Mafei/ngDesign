/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.synflow.models.dpn.impl;

import java.util.Iterator;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import com.synflow.models.dpn.Action;
import com.synflow.models.dpn.DpnPackage;
import com.synflow.models.dpn.Pattern;
import com.synflow.models.dpn.Port;
import com.synflow.models.ir.Procedure;

/**
 * <!-- begin-user-doc --> An implementation of the model object ' <em><b>Action</b></em>'. <!--
 * end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link com.synflow.models.dpn.impl.ActionImpl#getBody <em>Body</em>}</li>
 * <li>{@link com.synflow.models.dpn.impl.ActionImpl#getInputPattern <em>Input Pattern</em>}</li>
 * <li>{@link com.synflow.models.dpn.impl.ActionImpl#getOutputPattern <em>Output Pattern</em>}</li>
 * <li>{@link com.synflow.models.dpn.impl.ActionImpl#getPeekPattern <em>Peek Pattern</em>}</li>
 * <li>{@link com.synflow.models.dpn.impl.ActionImpl#getScheduler <em>Scheduler</em>}</li>
 * <li>{@link com.synflow.models.dpn.impl.ActionImpl#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ActionImpl extends EObjectImpl implements Action {

	/**
	 * The cached value of the '{@link #getBody() <em>Body</em>}' containment reference. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getBody()
	 * @generated
	 * @ordered
	 */
	protected Procedure body;

	/**
	 * The cached value of the '{@link #getInputPattern() <em>Input Pattern</em>}' containment
	 * reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getInputPattern()
	 * @generated
	 * @ordered
	 */
	protected Pattern inputPattern;

	/**
	 * The cached value of the '{@link #getOutputPattern() <em>Output Pattern</em>}' containment
	 * reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getOutputPattern()
	 * @generated
	 * @ordered
	 */
	protected Pattern outputPattern;

	/**
	 * The cached value of the '{@link #getPeekPattern() <em>Peek Pattern</em>}' containment
	 * reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getPeekPattern()
	 * @generated
	 * @ordered
	 */
	protected Pattern peekPattern;

	/**
	 * The cached value of the '{@link #getScheduler() <em>Scheduler</em>}' containment reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getScheduler()
	 * @generated
	 * @ordered
	 */
	protected Procedure scheduler;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ActionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public NotificationChain basicSetBody(Procedure newBody, NotificationChain msgs) {
		Procedure oldBody = body;
		body = newBody;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					DpnPackage.ACTION__BODY, oldBody, newBody);
			if (msgs == null)
				msgs = notification;
			else
				msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public NotificationChain basicSetInputPattern(Pattern newInputPattern, NotificationChain msgs) {
		Pattern oldInputPattern = inputPattern;
		inputPattern = newInputPattern;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					DpnPackage.ACTION__INPUT_PATTERN, oldInputPattern, newInputPattern);
			if (msgs == null)
				msgs = notification;
			else
				msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public NotificationChain basicSetOutputPattern(Pattern newOutputPattern, NotificationChain msgs) {
		Pattern oldOutputPattern = outputPattern;
		outputPattern = newOutputPattern;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					DpnPackage.ACTION__OUTPUT_PATTERN, oldOutputPattern, newOutputPattern);
			if (msgs == null)
				msgs = notification;
			else
				msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public NotificationChain basicSetPeekPattern(Pattern newPeekPattern, NotificationChain msgs) {
		Pattern oldPeekPattern = peekPattern;
		peekPattern = newPeekPattern;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					DpnPackage.ACTION__PEEK_PATTERN, oldPeekPattern, newPeekPattern);
			if (msgs == null)
				msgs = notification;
			else
				msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public NotificationChain basicSetScheduler(Procedure newScheduler, NotificationChain msgs) {
		Procedure oldScheduler = scheduler;
		scheduler = newScheduler;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					DpnPackage.ACTION__SCHEDULER, oldScheduler, newScheduler);
			if (msgs == null)
				msgs = notification;
			else
				msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case DpnPackage.ACTION__BODY:
			return getBody();
		case DpnPackage.ACTION__INPUT_PATTERN:
			return getInputPattern();
		case DpnPackage.ACTION__OUTPUT_PATTERN:
			return getOutputPattern();
		case DpnPackage.ACTION__PEEK_PATTERN:
			return getPeekPattern();
		case DpnPackage.ACTION__SCHEDULER:
			return getScheduler();
		case DpnPackage.ACTION__NAME:
			return getName();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID,
			NotificationChain msgs) {
		switch (featureID) {
		case DpnPackage.ACTION__BODY:
			return basicSetBody(null, msgs);
		case DpnPackage.ACTION__INPUT_PATTERN:
			return basicSetInputPattern(null, msgs);
		case DpnPackage.ACTION__OUTPUT_PATTERN:
			return basicSetOutputPattern(null, msgs);
		case DpnPackage.ACTION__PEEK_PATTERN:
			return basicSetPeekPattern(null, msgs);
		case DpnPackage.ACTION__SCHEDULER:
			return basicSetScheduler(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case DpnPackage.ACTION__BODY:
			return body != null;
		case DpnPackage.ACTION__INPUT_PATTERN:
			return inputPattern != null;
		case DpnPackage.ACTION__OUTPUT_PATTERN:
			return outputPattern != null;
		case DpnPackage.ACTION__PEEK_PATTERN:
			return peekPattern != null;
		case DpnPackage.ACTION__SCHEDULER:
			return scheduler != null;
		case DpnPackage.ACTION__NAME:
			return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
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
		case DpnPackage.ACTION__BODY:
			setBody((Procedure) newValue);
			return;
		case DpnPackage.ACTION__INPUT_PATTERN:
			setInputPattern((Pattern) newValue);
			return;
		case DpnPackage.ACTION__OUTPUT_PATTERN:
			setOutputPattern((Pattern) newValue);
			return;
		case DpnPackage.ACTION__PEEK_PATTERN:
			setPeekPattern((Pattern) newValue);
			return;
		case DpnPackage.ACTION__SCHEDULER:
			setScheduler((Procedure) newValue);
			return;
		case DpnPackage.ACTION__NAME:
			setName((String) newValue);
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
		return DpnPackage.Literals.ACTION;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case DpnPackage.ACTION__BODY:
			setBody((Procedure) null);
			return;
		case DpnPackage.ACTION__INPUT_PATTERN:
			setInputPattern((Pattern) null);
			return;
		case DpnPackage.ACTION__OUTPUT_PATTERN:
			setOutputPattern((Pattern) null);
			return;
		case DpnPackage.ACTION__PEEK_PATTERN:
			setPeekPattern((Pattern) null);
			return;
		case DpnPackage.ACTION__SCHEDULER:
			setScheduler((Procedure) null);
			return;
		case DpnPackage.ACTION__NAME:
			setName(NAME_EDEFAULT);
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Procedure getBody() {
		return body;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Pattern getInputPattern() {
		return inputPattern;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Pattern getOutputPattern() {
		return outputPattern;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Pattern getPeekPattern() {
		return peekPattern;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Procedure getScheduler() {
		return scheduler;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setBody(Procedure newBody) {
		if (newBody != body) {
			NotificationChain msgs = null;
			if (body != null)
				msgs = ((InternalEObject) body).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
						- DpnPackage.ACTION__BODY, null, msgs);
			if (newBody != null)
				msgs = ((InternalEObject) newBody).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
						- DpnPackage.ACTION__BODY, null, msgs);
			msgs = basicSetBody(newBody, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DpnPackage.ACTION__BODY, newBody,
					newBody));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setInputPattern(Pattern newInputPattern) {
		if (newInputPattern != inputPattern) {
			NotificationChain msgs = null;
			if (inputPattern != null)
				msgs = ((InternalEObject) inputPattern).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
						- DpnPackage.ACTION__INPUT_PATTERN, null, msgs);
			if (newInputPattern != null)
				msgs = ((InternalEObject) newInputPattern).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
						- DpnPackage.ACTION__INPUT_PATTERN, null, msgs);
			msgs = basicSetInputPattern(newInputPattern, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DpnPackage.ACTION__INPUT_PATTERN,
					newInputPattern, newInputPattern));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DpnPackage.ACTION__NAME, oldName,
					name));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setOutputPattern(Pattern newOutputPattern) {
		if (newOutputPattern != outputPattern) {
			NotificationChain msgs = null;
			if (outputPattern != null)
				msgs = ((InternalEObject) outputPattern).eInverseRemove(this,
						EOPPOSITE_FEATURE_BASE - DpnPackage.ACTION__OUTPUT_PATTERN, null, msgs);
			if (newOutputPattern != null)
				msgs = ((InternalEObject) newOutputPattern).eInverseAdd(this,
						EOPPOSITE_FEATURE_BASE - DpnPackage.ACTION__OUTPUT_PATTERN, null, msgs);
			msgs = basicSetOutputPattern(newOutputPattern, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					DpnPackage.ACTION__OUTPUT_PATTERN, newOutputPattern, newOutputPattern));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setPeekPattern(Pattern newPeekPattern) {
		if (newPeekPattern != peekPattern) {
			NotificationChain msgs = null;
			if (peekPattern != null)
				msgs = ((InternalEObject) peekPattern).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
						- DpnPackage.ACTION__PEEK_PATTERN, null, msgs);
			if (newPeekPattern != null)
				msgs = ((InternalEObject) newPeekPattern).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
						- DpnPackage.ACTION__PEEK_PATTERN, null, msgs);
			msgs = basicSetPeekPattern(newPeekPattern, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DpnPackage.ACTION__PEEK_PATTERN,
					newPeekPattern, newPeekPattern));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setScheduler(Procedure newScheduler) {
		if (newScheduler != scheduler) {
			NotificationChain msgs = null;
			if (scheduler != null)
				msgs = ((InternalEObject) scheduler).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
						- DpnPackage.ACTION__SCHEDULER, null, msgs);
			if (newScheduler != null)
				msgs = ((InternalEObject) newScheduler).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
						- DpnPackage.ACTION__SCHEDULER, null, msgs);
			msgs = basicSetScheduler(newScheduler, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DpnPackage.ACTION__SCHEDULER,
					newScheduler, newScheduler));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (name != null) {
			builder.append("'");
			builder.append(name);
			builder.append("': ");
		}

		builder.append("peeks: [");
		toString(builder, getPeekPattern());
		builder.append("], reads: [");
		toString(builder, getInputPattern());
		builder.append("], writes: [");
		toString(builder, getOutputPattern());
		return builder.append(']').toString();
	}

	private void toString(StringBuilder builder, Pattern pattern) {
		Iterator<Port> it = pattern.getPorts().iterator();
		if (it.hasNext()) {
			builder.append(it.next().getName());
			while (it.hasNext()) {
				builder.append(", ");
				builder.append(it.next().getName());
			}
		}
	}

}
