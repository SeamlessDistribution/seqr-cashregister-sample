package com.seqr.cashregister.sample;

import static java.lang.String.format;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import com.seamless.ers.interfaces.external.ClientContext;
import com.seamless.ers.interfaces.external.ERSWSExternalClientService;
import com.seamless.ers.interfaces.external.ErswsPaymentStatusResponse;
import com.seamless.ers.interfaces.external.ErswsRegisterTerminalResponse;
import com.seamless.ers.interfaces.external.ErswsSendInvoiceResponse;
import com.seamless.ers.interfaces.external.Invoice;
import com.seamless.ers.interfaces.external.PrincipalId;
import com.seqr.cashregister.constants.ResultCodes;

/**
 * Communication wrapper for SEQR auth provisioning interface
 * 
 */
public class SEQRCommunication
{
	private ClientContext context;
	private ERSWSExternalClientService seqrService;

	public SEQRCommunication(
			String endpointAddress,
			String principalId,
			String principalType,
			String password)
	{
		seqrService = connect(ERSWSExternalClientService.class, endpointAddress);
		context = createClientContext(principalId, principalType, password);
	}

	private static ClientContext createClientContext(String principalId, String principalType, String password)
	{
		ClientContext context = new ClientContext();
		PrincipalId p = new PrincipalId();
		p.setId(principalId);
		p.setType(principalType);
		context.setPassword(password);
		context.setInitiatorPrincipalId(p);
		return context;
	}

	public ErswsRegisterTerminalResponse registerTerminal(String externalTerminalId, String password, String name) throws SEQRException
	{
		ErswsRegisterTerminalResponse response = seqrService.registerTerminal(context, externalTerminalId, password, name);
		if (response.getResultCode() != ResultCodes.SUCCESS)
		{
			System.out.println(format("registerTerminal returned error: %s(%d)", response.getResultDescription(), response.getResultCode()));
			throw new SEQRException(response.getResultCode(), response.getResultDescription());
		}
		return response;
	}

	public ErswsSendInvoiceResponse sendInvoice(Invoice invoice) throws SEQRException
	{
		ErswsSendInvoiceResponse response = seqrService.sendInvoice(context, invoice, null);

		if (response.getResultCode() != ResultCodes.SUCCESS)
		{
			System.out.println(format("sendInvoice returned error: %s(%d)", response.getResultDescription(), response.getResultCode()));
			throw new SEQRException(response.getResultCode(), response.getResultDescription());
		}

		return response;
	}

	public ErswsPaymentStatusResponse getPaymentStatus(String invoiceReference) throws SEQRException
	{
		ErswsPaymentStatusResponse response = seqrService.getPaymentStatus(context, invoiceReference, 0);

		if (response.getResultCode() != ResultCodes.SUCCESS)
		{
			System.out.println(format("getPaymentStatus returned error: %s(%d)", response.getResultDescription(), response.getResultCode()));
			throw new SEQRException(response.getResultCode(), response.getResultDescription());
		}

		return response;
	}

	/**
	 * Create a proxy class for connecting to the SOAP endpoint
	 * 
	 * @param serviceClass
	 * @param address
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T connect(Class<T> serviceClass, String address)
	{
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(serviceClass);
		factory.setAddress(address);

		return (T) factory.create();
	}
}
