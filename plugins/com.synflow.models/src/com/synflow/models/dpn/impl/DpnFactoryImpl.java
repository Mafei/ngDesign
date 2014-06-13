/**
 */
package com.synflow.models.dpn.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import com.google.common.collect.BiMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.synflow.models.dpn.Action;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.Argument;
import com.synflow.models.dpn.Connection;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.DpnFactory;
import com.synflow.models.dpn.DpnPackage;
import com.synflow.models.dpn.Endpoint;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.FSM;
import com.synflow.models.dpn.Instance;
import com.synflow.models.dpn.InterfaceType;
import com.synflow.models.dpn.Pattern;
import com.synflow.models.dpn.Port;
import com.synflow.models.dpn.State;
import com.synflow.models.dpn.Transition;
import com.synflow.models.dpn.Unit;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.Var;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!-- end-user-doc -->
 * 
 * @generated
 */
public class DpnFactoryImpl extends EFactoryImpl implements DpnFactory {

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static DpnPackage getPackage() {
		return DpnPackage.eINSTANCE;
	}

	/**
	 * Creates the default factory implementation. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public static DpnFactory init() {
		try {
			DpnFactory theDpnFactory = (DpnFactory) EPackage.Registry.INSTANCE
					.getEFactory(DpnPackage.eNS_URI);
			if (theDpnFactory != null) {
				return theDpnFactory;
			}
		} catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new DpnFactoryImpl();
	}

	/**
	 * Creates an instance of the factory. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public DpnFactoryImpl() {
		super();
	}

	@Override
	public void addPatterns(Action target, Action source) {
		target.getInputPattern().add(source.getInputPattern());
		target.getOutputPattern().add(source.getOutputPattern());
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public String convertBiMapToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(instanceValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public String convertInterfaceTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc --><!-- end-user-doc -->
	 */
	public String convertJsonObjectToString(EDataType eDataType, Object instanceValue) {
		return instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
		case DpnPackage.INTERFACE_TYPE:
			return convertInterfaceTypeToString(eDataType, instanceValue);
		case DpnPackage.JSON_OBJECT:
			return convertJsonObjectToString(eDataType, instanceValue);
		case DpnPackage.BI_MAP:
			return convertBiMapToString(eDataType, instanceValue);
		default:
			throw new IllegalArgumentException("The datatype '" + eDataType.getName()
					+ "' is not a valid classifier");
		}
	}

	@Override
	public Action copy(Action action) {
		Action copy = DpnFactory.eINSTANCE.createActionNop();
		DpnFactory.eINSTANCE.addPatterns(copy, action);
		copy.getPeekPattern().add(action.getPeekPattern());
		return copy;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
		case DpnPackage.ACTION:
			return createAction();
		case DpnPackage.ACTOR:
			return createActor();
		case DpnPackage.ARGUMENT:
			return createArgument();
		case DpnPackage.CONNECTION:
			return createConnection();
		case DpnPackage.DPN:
			return createDPN();
		case DpnPackage.ENTITY:
			return createEntity();
		case DpnPackage.FSM:
			return createFSM();
		case DpnPackage.INSTANCE:
			return createInstance();
		case DpnPackage.PATTERN:
			return createPattern();
		case DpnPackage.PORT:
			return createPort();
		case DpnPackage.STATE:
			return createState();
		case DpnPackage.TRANSITION:
			return createTransition();
		case DpnPackage.UNIT:
			return createUnit();
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
	public Action createAction() {
		ActionImpl action = new ActionImpl();
		return action;
	}

	@Override
	public Action createActionEmpty() {
		ActionImpl action = new ActionImpl();

		action.setInputPattern(createPattern());
		action.setOutputPattern(createPattern());
		action.setPeekPattern(createPattern());

		return action;
	}

	@Override
	public Action createActionNop() {
		Action action = createActionEmpty();

		Procedure body = IrFactory.eINSTANCE.createProcedure("", 0,
				IrFactory.eINSTANCE.createTypeVoid());
		action.setBody(body);

		Procedure scheduler = IrFactory.eINSTANCE.createProcedure("", 0,
				IrFactory.eINSTANCE.createTypeBool());
		action.setScheduler(scheduler);

		return action;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Actor createActor() {
		ActorImpl actor = new ActorImpl();
		return actor;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Argument createArgument() {
		ArgumentImpl argument = new ArgumentImpl();
		return argument;
	}

	@Override
	public Argument createArgument(Var variable, Expression value) {
		ArgumentImpl argument = new ArgumentImpl();
		argument.setVariable(variable);
		argument.setValue(value);
		return argument;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public BiMap<?, ?> createBiMapFromString(EDataType eDataType, String initialValue) {
		return (BiMap<?, ?>) super.createFromString(initialValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Connection createConnection() {
		ConnectionImpl connection = new ConnectionImpl();
		return connection;
	}

	@Override
	public Connection createConnection(Endpoint source, Endpoint target) {
		ConnectionImpl connection = new ConnectionImpl();
		connection.setSource(source.getVertex());
		connection.setSourcePort(source.getPort());
		connection.setTarget(target.getVertex());
		connection.setTargetPort(target.getPort());
		return connection;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public DPN createDPN() {
		DPNImpl dpn = new DPNImpl();
		return dpn;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Entity createEntity() {
		EntityImpl entity = new EntityImpl();
		return entity;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
		case DpnPackage.INTERFACE_TYPE:
			return createInterfaceTypeFromString(eDataType, initialValue);
		case DpnPackage.JSON_OBJECT:
			return createJsonObjectFromString(eDataType, initialValue);
		case DpnPackage.BI_MAP:
			return createBiMapFromString(eDataType, initialValue);
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
	public FSM createFSM() {
		FSMImpl fsm = new FSMImpl();
		return fsm;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Instance createInstance() {
		InstanceImpl instance = new InstanceImpl();
		return instance;
	}

	@Override
	public Instance createInstance(String id, Entity entity) {
		InstanceImpl instance = new InstanceImpl();
		instance.setName(id);
		instance.setEntity(entity);
		return instance;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public InterfaceType createInterfaceTypeFromString(EDataType eDataType, String initialValue) {
		InterfaceType result = InterfaceType.get(initialValue);
		if (result == null)
			throw new IllegalArgumentException("The value '" + initialValue
					+ "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc --><!-- end-user-doc -->
	 */
	public JsonObject createJsonObjectFromString(EDataType eDataType, String initialValue) {
		return createJsonObjectFromString(initialValue);
	}

	@Override
	public JsonObject createJsonObjectFromString(String json) {
		return (JsonObject) new JsonParser().parse(json);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Pattern createPattern() {
		PatternImpl pattern = new PatternImpl();
		return pattern;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Port createPort() {
		PortImpl port = new PortImpl();
		return port;
	}

	@Override
	public Port createPort(Type type, String name, InterfaceType iface) {
		PortImpl port = new PortImpl();
		port.setName(name);
		port.setType(type);
		port.setInterface(iface);
		return port;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public State createState() {
		StateImpl state = new StateImpl();
		return state;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Transition createTransition() {
		TransitionImpl transition = new TransitionImpl();
		return transition;
	}

	@Override
	public Transition createTransition(State source, State target) {
		TransitionImpl transition = new TransitionImpl();
		transition.setSource(source);
		transition.setTarget(target);
		return transition;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Unit createUnit() {
		UnitImpl unit = new UnitImpl();
		return unit;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public DpnPackage getDpnPackage() {
		return (DpnPackage) getEPackage();
	}

} // DpnFactoryImpl
