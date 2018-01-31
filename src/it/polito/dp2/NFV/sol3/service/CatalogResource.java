package it.polito.dp2.NFV.sol3.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import it.polito.dp2.NFV.sol3.jaxb.CatalogType;

@Path("/catalog")
public class CatalogResource
{
	// Instantiate NffgsService in charge of execute all needed operations
	private NfvDeployerService nfvService = NfvDeployerService.getInstance();
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public JAXBElement<CatalogType> getCatalog()
	{
		return nfvService.getCatalog();
	}

}
