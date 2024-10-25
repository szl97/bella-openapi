package com.ke.bella.openapi.login.cas;

import com.ke.bella.openapi.Operator;
import com.ke.bella.openapi.login.session.SessionManager;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ProxyReceivingTicketValidationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.ke.bella.openapi.login.cas.BellaCasClient.redirectParameter;

public class BellaValidatorFilter extends Cas30ProxyReceivingTicketValidationFilter {
    private final String idAttribute;
    private final String nameAttribute;
    private final SessionManager sessionManager;

    public BellaValidatorFilter(String idAttribute, String nameAttribute, SessionManager sessionManager) {
        this.idAttribute = idAttribute;
        this.nameAttribute = nameAttribute;
        this.sessionManager = sessionManager;
    }

    @Override
    protected void onSuccessfulValidation(final HttpServletRequest request, final HttpServletResponse response,
            final Assertion assertion) {
        Operator operator = new Operator();
        operator.setUserId(Long.parseLong(assertion.getPrincipal().getAttributes().get(idAttribute).toString()));
        operator.setUserName(assertion.getPrincipal().getAttributes().get(nameAttribute).toString());
        sessionManager.create(operator, request, response);
        if(StringUtils.isNotBlank(request.getParameter(redirectParameter))) {
            request.setAttribute(redirectParameter, request.getParameter(redirectParameter));
        }
    }
}
