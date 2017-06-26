/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * Sub-resource for patient identifiers
 */
@SubResource(parent = PatientResource1_8.class, path = "identifier", supportedClass = PatientIdentifier.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*" })
public class PatientIdentifierResource1_8 extends DelegatingSubResource<PatientIdentifier, Patient, PatientResource1_8> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("uuid");
			description.addProperty("identifier");
			description.addProperty("identifierType", Representation.REF);
			description.addProperty("location", Representation.REF);
			description.addProperty("preferred");
			description.addProperty("voided");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("uuid");
			description.addProperty("identifier");
			description.addProperty("identifierType", Representation.DEFAULT);
			description.addProperty("location", Representation.DEFAULT);
			description.addProperty("preferred");
			description.addProperty("voided");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("identifier");
		description.addRequiredProperty("identifierType");
		description.addProperty("location");
		description.addProperty("preferred");
		return description;
	}
	
	/**
	 * Sets the identifier type for a patient identifier.
	 * 
	 * @param instance
	 * @param identifierType
	 * @throws org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException
	 */
	@PropertySetter("identifierType")
	public void setIdentifierType(PatientIdentifier instance, PatientIdentifierType identifierType) {
		String uuid = identifierType.getUuid();
		String name = identifierType.getName();
		boolean reset = false;
		if (!reset && !StringUtils.isEmpty(uuid)) {
			PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierTypeByUuid(uuid);
			if (pit != null) {
				instance.setIdentifierType(pit);
				reset = true;
			}
			
		}
		if (!reset && !StringUtils.isEmpty(name)) {
			PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierTypeByName(name);
			if (pit != null) {
				instance.setIdentifierType(pit);
				reset = true;
			}
		}
	}
	
	/**
	 * Sets the location for a patient identifier.
	 * 
	 * @param instance
	 * @param location
	 * @throws org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException
	 */
	@PropertySetter("location")
	public void setLocation(PatientIdentifier instance, Location location) {
		instance.setLocation(Context.getLocationService().getLocationByUuid(location.getUuid()));
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		return getCreatableProperties();
	}
	
	private PatientService service() {
		return Context.getPatientService();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#getParent(java.lang.Object)
	 */
	@Override
	public Patient getParent(PatientIdentifier instance) {
		return instance.getPatient();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#setParent(java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public void setParent(PatientIdentifier instance, Patient patient) {
		instance.setPatient(patient);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public PatientIdentifier getByUniqueId(String uniqueId) {
		return service().getPatientIdentifierByUuid(uniqueId);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public PatientIdentifier save(PatientIdentifier delegate) {
		// make sure it has already been added to the patient
		boolean needToAdd = true;
		for (PatientIdentifier pi : delegate.getPatient().getActiveIdentifiers()) {
			if (pi.equals(delegate)) {
				needToAdd = false;
				break;
			}
		}
		if (needToAdd)
			delegate.getPatient().addIdentifier(delegate);
		return service().savePatientIdentifier(delegate);
	}
	
	@Override
	public PatientIdentifier newDelegate() {
		return new PatientIdentifier();
	}
	
	@Override
	protected void delete(PatientIdentifier delegate, String reason, RequestContext context) throws ResponseException {
		service().voidPatientIdentifier(delegate, reason);
	}
	
	@Override
	public void purge(PatientIdentifier delegate, RequestContext context) throws ResponseException {
		Patient patient = delegate.getPatient();
		patient.removeIdentifier(delegate);
		service().savePatient(patient);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#doGetAll(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public NeedsPaging<PatientIdentifier> doGetAll(Patient parent, RequestContext context) throws ResponseException {
		return new NeedsPaging<PatientIdentifier>(parent.getActiveIdentifiers(), context);
	}
	
	/**
	 * @param id
	 * @return identifier type + identifier (for concise display purposes)
	 */
	@PropertyGetter("display")
	public String getDisplayString(PatientIdentifier id) {
		if (id.getIdentifierType() == null)
			return "";
		
		return id.getIdentifierType().getName() + " = " + id.getIdentifier();
	}
}
