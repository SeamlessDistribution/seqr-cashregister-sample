package com.seqr.cashregister.sample;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.seamless.ers.interfaces.external.AcknowledgmentMode;
import com.seamless.ers.interfaces.external.Amount;
import com.seamless.ers.interfaces.external.ErswsPaymentStatusResponse;
import com.seamless.ers.interfaces.external.ErswsRegisterTerminalResponse;
import com.seamless.ers.interfaces.external.ErswsSendInvoiceResponse;
import com.seamless.ers.interfaces.external.Invoice;
import com.seamless.ers.interfaces.external.Invoice.InvoiceRows;
import com.seamless.ers.interfaces.external.InvoiceStatus;
import com.seamless.ers.interfaces.external.PaymentInvoiceRow;

public class Main
{

	// Polling timeout, in ms
	private static final int POLLING_TIMEOUT = 1000;

	private static SecureRandom random = new SecureRandom();

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		// setup communication channel with administrative rights level
		String endpointAddress = "http://mer1/extclientproxy/service/v2";
		SEQRCommunication commShop = new SEQRCommunication(endpointAddress, "test_shop/9900", "RESELLERUSER", "testpassword");

		// this will generate a long enough password
		String password = new BigInteger(150, random).toString(32);
		ErswsRegisterTerminalResponse registerResponse = commShop.registerTerminal("POS_1265", password, "Cashregister 3");
		String terminalId = registerResponse.getTerminalId();

		// now we switch to the terminal level rights
		SEQRCommunication commTerminal = new SEQRCommunication(endpointAddress, terminalId, "TERMINALID", password);

		Invoice invoice = createSampleInvoice();

		ErswsSendInvoiceResponse sendInvoice = commTerminal.sendInvoice(invoice);

		File file = generateQRCodeImage(sendInvoice.getInvoiceQRCode());
		System.out.println(format("Please open %s and scan the QR code inside.", file));

		ErswsPaymentStatusResponse paymentStatus;
		do
		{
			Thread.sleep(POLLING_TIMEOUT);
			System.out.print(".");
			paymentStatus = commTerminal.getPaymentStatus(sendInvoice.getInvoiceReference());
		}
		while (paymentStatus.getStatus() == InvoiceStatus.ISSUED);

		System.out.println();
		if(paymentStatus.getStatus() == InvoiceStatus.PAID)
		{
			System.out.println("Payment completed successfully.");
		}else
		{
			System.out.println("Payment failed, payment status: " + paymentStatus.getStatus().name());
		}
		

	}

	private static Invoice createSampleInvoice()
	{
		Amount totalAmount = new Amount();
		totalAmount.setCurrency("SEK");
		totalAmount.setValue(new BigDecimal(10));
		Invoice invoice = new Invoice();
		invoice.setCashierId("Bob");
		invoice.setTotalAmount(totalAmount);
		invoice.setTitle("sample title");
		
		InvoiceRows rows = new InvoiceRows();
		PaymentInvoiceRow row = new PaymentInvoiceRow();
		row.setItemDescription("Ice cream");
		row.setItemSKU("1233");
		row.setItemEAN("59012341234567");
		Amount amount = new Amount();
		amount.setCurrency("SEK");
		amount.setValue(new BigDecimal(10));
		row.setItemTotalAmount(amount);
		row.setItemUnitPrice(amount);
		row.setItemQuantity(new BigDecimal(1));
		row.setItemUnit("EA");
		rows.getInvoiceRow().add(row);
		invoice.setInvoiceRows(rows);
		
		// we don't want any acknowledgment when the user has scanned the invoice
		// acknowledgment is only needed for loyalty/coupons scenarios
		invoice.setAcknowledgmentMode(AcknowledgmentMode.NO_ACKNOWLEDGMENT);
		return invoice;
	}

	private static File generateQRCodeImage(String qrCodeContents) throws WriterException, IOException
	{
		MultiFormatWriter barcodeWriter = new MultiFormatWriter();
		BitMatrix matrix = barcodeWriter.encode(qrCodeContents, BarcodeFormat.QR_CODE, 200, 200);
		File file = File.createTempFile("QR_CODE", ".png");
		MatrixToImageWriter.writeToFile(matrix, "PNG", file);
		return file;
	}

}
