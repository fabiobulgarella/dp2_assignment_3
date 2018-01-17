package it.polito.dp2.NFV.sol3.service;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;
import it.polito.dp2.NFV.sol3.jaxb.NffgsType;

@Path("/nffgs")
public class NffgsResource
{
	// Instantiate NffgsService in charge of execute all needed operations
	private NfvDeployerService nfvService = NfvDeployerService.getInstance();
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public JAXBElement<NffgsType> getNffgs()
	{
		return nfvService.getNffgs();
    }
	
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public JAXBElement<NffgType> getNffg(@PathParam("id") String id)
	{
		JAXBElement<NffgType> nffg = nfvService.getNffg(id);
		
		if (nffg == null)
			throw new NotFoundException();
		
		return nffg;
    }
	
	@DELETE
	@Path("{id}")
	public void deleteNffg(@PathParam("id") String id)
	{
		throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
    }
	
	@DELETE
	@Path("{id}/nodes/{id2}")
	public void deleteNode(@PathParam("id") String id, @PathParam("id2") String id2)
	{
		throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
    }
	
	@DELETE
	@Path("{id}/nodes/{id2}/links/{id3}")
	public void deleteLink(@PathParam("id") String id, @PathParam("id2") String id2, @PathParam("id3") String id3)
	{
		throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
    }
	
}
