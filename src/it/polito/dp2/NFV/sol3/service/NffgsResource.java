package it.polito.dp2.NFV.sol3.service;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;

import it.polito.dp2.NFV.sol3.jaxb.LinkType;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;
import it.polito.dp2.NFV.sol3.jaxb.NffgsType;
import it.polito.dp2.NFV.sol3.jaxb.NodeType;

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
	
	@GET
	@Path("{id}/nodes/{id2}")
	@Produces(MediaType.APPLICATION_XML)
	public JAXBElement<NodeType> getNode(@PathParam("id") String id, @PathParam("id2") String id2)
	{
		JAXBElement<NodeType> node = nfvService.getNode(id, id2);
		
		if (node == null)
			throw new NotFoundException();
		
		return node;
    }
	
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	public JAXBElement<NffgType> postNffg(JAXBElement<NffgType> nffgElement)
	{
		NffgType nffg;
		
		if (nffgElement == null)
			throw new BadRequestException();
		
		if ( !(nffgElement.getValue() instanceof NffgType) )
			throw new BadRequestException();
		
		nffg = nffgElement.getValue();
		
		JAXBElement<NffgType> nffgRes = nfvService.postNffg(nffg);
		
		if (nffgRes == null)
			throw new ForbiddenException();
		
		return nffgRes;
    }
	
	@POST
	@Path("{nffgName}/nodes")
	@Consumes(MediaType.APPLICATION_XML)
	public Response postNode(@PathParam("nffgName") String nffgName, JAXBElement<NodeType> nodeElement)
	{
		NodeType node;
		
		if (nodeElement == null)
			throw new BadRequestException();
		
		if ( !(nodeElement.getValue() instanceof NodeType) )
			throw new BadRequestException("Inviare un nodo e non un Nffg, grazie.");
		
		node = nodeElement.getValue();
		
		boolean result = nfvService.postNode(nffgName, node);
		
		if (!result)
			throw new ForbiddenException();
		
		return Response.ok().build();
    }
	
	@POST
	@Path("{nffgName}/nodes/{nodeName}/links")
	@Consumes(MediaType.APPLICATION_XML)
	public Response postLink(@PathParam("nffgName") String nffgName, @PathParam("nodeName") String nodeName, JAXBElement<LinkType> linkElement)
	{
		LinkType link;
		
		if (linkElement == null)
			throw new BadRequestException();
		
		if ( !(linkElement.getValue() instanceof LinkType) )
			throw new BadRequestException("Inviare un nodo e non un Nffg, grazie.");
		
		link = linkElement.getValue();
		
		boolean result = nfvService.postLink(nffgName, nodeName, link);
		
		if (!result)
			throw new ForbiddenException();
		
		return Response.ok().build();
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
