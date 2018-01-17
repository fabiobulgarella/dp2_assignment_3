package it.polito.dp2.NFV.sol3.service;

import java.util.GregorianCalendar;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;
import it.polito.dp2.NFV.sol3.jaxb.NffgsType;
import it.polito.dp2.NFV.sol3.jaxb.ObjectFactory;

@Path("/nffgs")
public class NffgsResource
{
	// Instantiate NffgsService in charge of execute all needed operations
	private NfvDeployerService nfvService = NfvDeployerService.getInstance();
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public JAXBElement<NffgsType> getNffgs()
	{
		NffgsType nffgs = new NffgsType();
		NffgType nffg = new NffgType();
		nffg.setName("Test1");
		try {
			nffg.setDeployTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
		}
		catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		nffgs.getNffg().add(nffg);
		
		return new ObjectFactory().createNffgs(nffgs);
    }
	
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public JAXBElement<NffgType> getNffg(@PathParam("id") String id)
	{
		NffgType nffg = new NffgType();
		nffg.setName(id);
		try {
			nffg.setDeployTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
		}
		catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		
		return new ObjectFactory().createNffg(nffg);
    }
}
