package it.polito.dp2.NFV.sol3.service;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

public class ConflictException extends ClientErrorException
{
	private static final long serialVersionUID = -4515860597298940341L;
	
	// Return a 409 HTTP Status Code
	public ConflictException()
	{
		super(Response.Status.CONFLICT);
	}

}
