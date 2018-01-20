package it.polito.dp2.NFV.sol3.client1;

import it.polito.dp2.NFV.lab3.NfvClient;
import it.polito.dp2.NFV.lab3.NfvClientException;

public class NfvClientFactory extends it.polito.dp2.NFV.lab3.NfvClientFactory
{

	@Override
	public NfvClient newNfvClient() throws NfvClientException
	{
		// Read Web Service URL
		String serviceURL = System.getProperty("it.polito.dp2.NFV.lab3.URL");
		
		if (serviceURL == null)
			serviceURL = "http://localhost:8080/NfvDeployer/rest/";
		
		return new MyNfvClient(serviceURL);
	}
	
}
