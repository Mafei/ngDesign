package com.synflow.cflow.internal.instantiation.v2;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IGlobalScopeProvider;
import org.eclipse.xtext.scoping.IScope;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.synflow.cflow.cflow.CflowPackage.Literals;
import com.synflow.cflow.cflow.NamedEntity;

public class InstModelBuilder {

	@Inject
	private IGlobalScopeProvider globalScopeProvider;

	@Inject
	private Provider<InstModel> provider;

	public InstModel buildModel(NamedEntity entity) {
		InstModel model = provider.get();

		Resource resource = entity.eResource();
		IScope scope = globalScopeProvider.getScope(resource, Literals.INST__ENTITY, null);
		for (IEObjectDescription objDesc : scope.getAllElements()) {
			EObject proxy = objDesc.getEObjectOrProxy();
			EObject eObject = EcoreUtil.resolve(proxy, resource);
			EcoreUtil.resolveAll(eObject);

			// if (objDesc.getEClass() == Literals.NETWORK) {
			// URI uri = objDesc.getEObjectURI().trimFragment();
			// IResourceDescription resDesc = resourceDescriptions.getResourceDescription(uri);
			// for (IReferenceDescription refDesc : resDesc.getReferenceDescriptions()) {
			// URI target = refDesc.getTargetEObjectUri();
			// }
			//
			// EObject proxy = objDesc.getEObjectOrProxy();
			// EObject eObject = EcoreUtil.resolve(proxy, resource);
			// EcoreUtil.resolveAll(eObject);
			//
			// System.out.println(eObject);
			// }
		}

		return model;
	}

}
