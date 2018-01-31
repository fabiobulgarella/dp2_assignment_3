package it.polito.dp2.NFV.sol3.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import it.polito.dp2.NFV.sol3.jaxb.HostType;
import it.polito.dp2.NFV.sol3.jaxb.HostsType;

@Path("/hosts")
public class HostsResource
{
	// Instantiate NffgsService in charge of execute all needed operations
	private NfvDeployerService nfvService = NfvDeployerService.getInstance();
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public JAXBElement<HostsType> getHosts()
	{
		return nfvService.getHosts();
	}
	
	@GET
	@Path("{hostName}")
	@Produces(MediaType.APPLICATION_XML)
	public JAXBElement<HostType> getHost(@PathParam("hostName") String hostName)
	{
		return nfvService.getHost(hostName);
	}

}
