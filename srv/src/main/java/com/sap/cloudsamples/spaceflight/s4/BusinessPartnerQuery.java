package com.sap.cloudsamples.spaceflight.s4;

import java.util.List;

import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.s4hana.connectivity.ErpCommand;
import com.sap.cloud.sdk.s4hana.connectivity.ErpConfigContext;
import com.sap.cloud.sdk.s4hana.datamodel.odata.helper.Order;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartner;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartnerField;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartnerFluentHelper;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultBusinessPartnerService;
import com.sap.cloud.sdk.service.prov.api.request.OrderByExpression;

public class BusinessPartnerQuery extends ErpCommand<List<BusinessPartner>> {

	private final int top;
	private final int skip;
	private final BusinessPartnerField<String>[] selectedProperties;
	private final List<OrderByExpression> orderByProperties;
	private final ErpConfigContext erpConfigContext;

	@SuppressWarnings("unchecked")
	public BusinessPartnerQuery(ErpConfigContext erpConfigContext, int top, int skip, List<String> properties,
			List<OrderByExpression> orderByProperties) {
		super(BusinessPartnerQuery.class, erpConfigContext);
		this.erpConfigContext = erpConfigContext;
		this.top = top;
		this.skip = skip;
		this.selectedProperties = properties.stream().map(BusinessPartnerField::new)
				.toArray(BusinessPartnerField[]::new);
		this.orderByProperties = orderByProperties;
	}

	@Override
	protected List<BusinessPartner> run() {

		BusinessPartnerFluentHelper service = new DefaultBusinessPartnerService().getAllBusinessPartner();

		orderByProperties.stream()
				.forEach(expression -> service.orderBy(new BusinessPartnerField<>(expression.getOrderByProperty()),
						expression.isDescending() ? Order.DESC : Order.ASC));

		service.select(selectedProperties);

		if (skip > 0)
			service.skip(skip);

		if (top > 0)
			service.top(top);

		try {
			return service.execute(erpConfigContext);

		} catch (final ODataException e) {
			throw new HystrixBadRequestException(e.getMessage(), e);
		}
	}
}