package com.seqr.cashregister.sample;

/**
 * Exception in case of SEQR system returning an error 
 *
 */
@SuppressWarnings("serial")
public class SEQRException extends Exception
{

	public SEQRException(
			int resultCode,
			String resultDescription)
	{
		super(String.format("%s(%d)", resultDescription, resultCode));
	}

}
