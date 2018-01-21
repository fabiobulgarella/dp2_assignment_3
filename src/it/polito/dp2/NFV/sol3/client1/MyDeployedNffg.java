package it.polito.dp2.NFV.sol3.client1;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.polito.dp2.NFV.LinkReader;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.NfvReader;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.VNFTypeReader;
import it.polito.dp2.NFV.lab3.AllocationException;
import it.polito.dp2.NFV.lab3.DeployedNffg;
import it.polito.dp2.NFV.lab3.LinkAlreadyPresentException;
import it.polito.dp2.NFV.lab3.NoNodeException;
import it.polito.dp2.NFV.lab3.ServiceException;
import it.polito.dp2.NFV.sol3.client1.NfvReader.MyNfvReader;
import it.polito.dp2.NFV.sol3.jaxb.LinkType;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;
import it.polito.dp2.NFV.sol3.jaxb.NffgsType;
import it.polito.dp2.NFV.sol3.jaxb.NfvType;
import it.polito.dp2.NFV.sol3.jaxb.NodeType;
import it.polito.dp2.NFV.sol3.jaxb.ObjectFactory;

public class MyDeployedNffg implements DeployedNffg
{
	private WebTarget target;
	private ObjectFactory objFactory;
	private String nffgName;
	
	private NfvType nfv;
	
	// Class constructor
	public MyDeployedNffg(WebTarget target, NfvType nfv, String nffgName)
	{
		this.target = target;
		this.nfv = nfv;
		this.nffgName = nffgName;
		
		// Instantiate ObjectFactory
		objFactory = new ObjectFactory();
	}

	@Override
	public NodeReader addNode(VNFTypeReader type, String hostName) throws AllocationException, ServiceException
	{
		NodeType newNode = objFactory.createNodeType();
		newNode.setName("dummy");
		newNode.setVnfRef( type.getName() );
		newNode.setHostRef(hostName);
		
		// Call NfvDeployer REST Web Service
		NodeType responseNode;
		
		Response response = target.path("nffgs/" + nffgName + "/nodes")
				                  .request(MediaType.APPLICATION_XML)
				                  .post(Entity.entity(objFactory.createNode(newNode), MediaType.APPLICATION_XML));
		
		try {
			if (response.getStatus() == 200)
				responseNode = response.readEntity(NodeType.class);
			else if (response.getStatus() == 409)
				throw new AllocationException();
			else
				throw new ServiceException();
		} 
		finally {
			response.close();
		}
		
		// Build and return NodeReader response
		return getReader().getNode( responseNode.getName() );
	}

	@Override
	public LinkReader addLink(NodeReader source, NodeReader dest, boolean overwrite) throws NoNodeException, LinkAlreadyPresentException, ServiceException
	{
		LinkType newLink = objFactory.createLinkType();
		newLink.setDstNode( dest.getName() );
		newLink.setOverwrite(overwrite);
		
		// Get srcNodeName
		String srcNodeName = source.getName();
		
		// Call NfvDeployer REST Web Service
		LinkType responseLink = null;
		
		Response response = target.path("nffgs/" + nffgName + "/nodes/" + srcNodeName + "/links")
                                  .request(MediaType.APPLICATION_XML)
                                  .post(Entity.entity(objFactory.createLink(newLink), MediaType.APPLICATION_XML));
		
		try {
			if (response.getStatus() == 200)
				responseLink = response.readEntity(LinkType.class);
			else if (response.getStatus() == 403)
				throw new NoNodeException();
			else if (response.getStatus() == 409)
				throw new LinkAlreadyPresentException();
			else
				throw new ServiceException();
		} 
		finally {
			response.close();
		}
		
		// Build and return LinkReader response
		LinkReader resLink_r = null;
		
		for ( LinkReader link_r: getReader().getNode(srcNodeName).getLinks() )
		{
			if ( link_r.getName().equals( responseLink.getName() ) )
				resLink_r = link_r;
		}
		
		return resLink_r; 
	}

	@Override
	public NffgReader getReader() throws ServiceException
	{
		// Get Nffgs
		nfv.setNffgs( getNffgs() );
		
		// Create NfvReader
		NfvReader nfv_r = new MyNfvReader(nfv);
		
		return nfv_r.getNffg(nffgName);
	}
	
	private NffgsType getNffgs() throws ServiceException
	{
		// Call NfvDeployer REST Web Service
		NffgsType nffgs;
		
		try {
			nffgs = target.path("nffgs")
					      .request()
					      .accept(MediaType.APPLICATION_XML)
					      .get(NffgsType.class);
		}
		catch (ProcessingException pe) {
			throw new ServiceException("Error during JAX-RS request processing", pe);
		}
		catch (WebApplicationException wae) {
			throw new ServiceException("Server returned error", wae);
		}
		catch (Exception e) {
			throw new ServiceException("Unexpected exception", e);
		}
		
		// Create a complete host set (including nodeRefs)
		NffgsType newNffgs = objFactory.createNffgsType();
		
		for (NffgType nffg: nffgs.getNffg())
		{
			NffgType newNffg = getNffg( nffg.getName() );
			newNffgs.getNffg().add(newNffg);
		}
		
		return newNffgs;
	}
	
	private NffgType getNffg(String nffgName) throws ServiceException
	{
		// Call NfvDeployer REST Web Service
		NffgType nffg;
		
		try {
			nffg = target.path("nffgs/" + nffgName)
					     .request()
					     .accept(MediaType.APPLICATION_XML)
					     .get(NffgType.class);
		}
		catch (ProcessingException pe) {
			throw new ServiceException("Error during JAX-RS request processing", pe);
		}
		catch (WebApplicationException wae) {
			throw new ServiceException("Server returned error", wae);
		}
		catch (Exception e) {
			throw new ServiceException("Unexpected exception", e);
		}
		
		return nffg;
	}

}
