package it.polito.dp2.NFV.sol3.client1;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import it.polito.dp2.NFV.LinkReader;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.VNFTypeReader;
import it.polito.dp2.NFV.lab3.AllocationException;
import it.polito.dp2.NFV.lab3.DeployedNffg;
import it.polito.dp2.NFV.lab3.LinkAlreadyPresentException;
import it.polito.dp2.NFV.lab3.NoNodeException;
import it.polito.dp2.NFV.lab3.ServiceException;
import it.polito.dp2.NFV.sol3.jaxb.LinkType;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;
import it.polito.dp2.NFV.sol3.jaxb.NodeType;
import it.polito.dp2.NFV.sol3.jaxb.ObjectFactory;

public class MyDeployedNffg implements DeployedNffg
{
	private WebTarget target;
	private ObjectFactory objFactory;
	private NffgType nffg;
	private String nffgName;
	
	// Class constructor
	public MyDeployedNffg(WebTarget target, NffgType nffg)
	{
		this.target = target;
		this.nffg = nffg;
		this.nffgName = nffg.getName();
		
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
		
		try {
			responseNode = target.path("nffgs/" + nffgName + "/nodes")
			                     .request(MediaType.APPLICATION_XML)
			                     .post(Entity.entity(newNode, MediaType.APPLICATION_XML), NodeType.class);
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
		
		return null;
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
		LinkType responseLink;
		
		try {
			responseLink = target.path("nffgs/" + nffgName + "/nodes/" + srcNodeName)
			                     .request(MediaType.APPLICATION_XML)
			                     .post(Entity.entity(newLink, MediaType.APPLICATION_XML), LinkType.class);
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
		
		return null;
	}

	@Override
	public NffgReader getReader() throws ServiceException
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
		
		return null;
	}

}
