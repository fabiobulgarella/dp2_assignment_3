package it.polito.dp2.NFV.sol3.client1;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import it.polito.dp2.NFV.lab3.AllocationException;
import it.polito.dp2.NFV.lab3.DeployedNffg;
import it.polito.dp2.NFV.lab3.LinkDescriptor;
import it.polito.dp2.NFV.lab3.NffgDescriptor;
import it.polito.dp2.NFV.lab3.NfvClient;
import it.polito.dp2.NFV.lab3.NfvClientException;
import it.polito.dp2.NFV.lab3.NodeDescriptor;
import it.polito.dp2.NFV.lab3.ServiceException;
import it.polito.dp2.NFV.lab3.UnknownEntityException;
import it.polito.dp2.NFV.sol3.jaxb.LinkType;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;
import it.polito.dp2.NFV.sol3.jaxb.NodeType;
import it.polito.dp2.NFV.sol3.jaxb.ObjectFactory;

public class MyNfvClient implements NfvClient
{
	private WebTarget target;
	private ObjectFactory objFactory;

	// Class constructor
	public MyNfvClient(String serviceURL) throws NfvClientException
	{
		// Create JAX-RS Client and WebTarget
		Client client = ClientBuilder.newClient();
		try {
			target = client.target(serviceURL);
		}
		catch (IllegalArgumentException iae) {
			throw new NfvClientException(iae, "Url is not a valid URI");
		}
		
		// Instantiate ObjectFactory
		objFactory = new ObjectFactory();
	}

	@Override
	public DeployedNffg deployNffg(NffgDescriptor nffg) throws AllocationException, ServiceException
	{
		long nextNodeName = 0;
		
		// Create a map to associate NodeDescriptor with fake names
		Map<NodeDescriptor, String> fakeNameMap = new HashMap<>();
		
		// Get a set of NodeDescriptor to be deployed
		Set<NodeDescriptor> nodeSet = nffg.getNodes();
		
		// Build fakeNameMap
		for (NodeDescriptor node_d: nodeSet)
		{
			fakeNameMap.put(node_d, "node" + nextNodeName);
			nextNodeName++;
		}
		
		// Create a new NffgType
		NffgType nffgType = objFactory.createNffgType();
		
		// Add Node to nffgType
		for (NodeDescriptor node_d: nodeSet)
		{
			NodeType nodeType = objFactory.createNodeType();
			nodeType.setName( fakeNameMap.get(node_d) );
			nodeType.setVnfRef( node_d.getFuncType().getName() );
			nodeType.setHostRef( node_d.getHostName() );
			
			// Get links relative to this NodeDescriptor
			Set<LinkDescriptor> linkSet = node_d.getLinks();
			
			for (LinkDescriptor link_d: linkSet)
			{
				LinkType linkType = objFactory.createLinkType();
				linkType.setDstNode( fakeNameMap.get( link_d.getDestinationNode() ) );
				linkType.setMinThroughput( link_d.getThroughput() );
				linkType.setMaxLatency( link_d.getLatency() );
				
				nodeType.getLink().add(linkType);
			}
			
			// Add generated nodeType to nffgType
			nffgType.getNode().add(nodeType);
		}
		
		// Call Neo4JSimpleXML API
		NffgType deployedNffg;
		
		try {
			deployedNffg = target.path("nffgs")
			                     .request(MediaType.APPLICATION_XML)
			                     .post(Entity.entity(nffgType, MediaType.APPLICATION_XML), NffgType.class);
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
		
		return new MyDeployedNffg(target, deployedNffg);
	}

	@Override
	public DeployedNffg getDeployedNffg(String name) throws UnknownEntityException, ServiceException
	{
		
		return null;
	}

}
