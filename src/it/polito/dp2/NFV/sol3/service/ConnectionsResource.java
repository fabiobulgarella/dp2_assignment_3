package it.polito.dp2.NFV.sol3.service;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import it.polito.dp2.NFV.sol3.jaxb.ConnectionType;

@Path("/connections")
public class ConnectionsResource
{
	// Instantiate NffgsService in charge of execute all needed operations
	private NfvDeployerService nfvService = NfvDeployerService.getInstance();
	
	@GET
	@Path("{host1}/{host2}")
	@Produces(MediaType.APPLICATION_XML)
	public JAXBElement<ConnectionType> getConnection(@PathParam("host1") String host1, @PathParam("host2") String host2)
	{
		JAXBElement<ConnectionType> connection = nfvService.getConnection(host1, host2);
		
		if (connection == null)
			throw new NotFoundException();
		
		return connection;
    }
}
