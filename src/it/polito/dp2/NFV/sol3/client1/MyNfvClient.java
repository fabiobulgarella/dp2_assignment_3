package it.polito.dp2.NFV.sol3.client1;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.polito.dp2.NFV.lab3.AllocationException;
import it.polito.dp2.NFV.lab3.DeployedNffg;
import it.polito.dp2.NFV.lab3.LinkDescriptor;
import it.polito.dp2.NFV.lab3.NffgDescriptor;
import it.polito.dp2.NFV.lab3.NfvClient;
import it.polito.dp2.NFV.lab3.NfvClientException;
import it.polito.dp2.NFV.lab3.NodeDescriptor;
import it.polito.dp2.NFV.lab3.ServiceException;
import it.polito.dp2.NFV.lab3.UnknownEntityException;
import it.polito.dp2.NFV.sol3.jaxb.CatalogType;
import it.polito.dp2.NFV.sol3.jaxb.ConnectionsType;
import it.polito.dp2.NFV.sol3.jaxb.HostType;
import it.polito.dp2.NFV.sol3.jaxb.HostsType;
import it.polito.dp2.NFV.sol3.jaxb.LinkType;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;
import it.polito.dp2.NFV.sol3.jaxb.NffgsType;
import it.polito.dp2.NFV.sol3.jaxb.NfvType;
import it.polito.dp2.NFV.sol3.jaxb.NodeType;
import it.polito.dp2.NFV.sol3.jaxb.ObjectFactory;

public class MyNfvClient implements NfvClient
{
	private WebTarget target;
	private ObjectFactory objFactory;
	
	private NfvType nfv;

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
		
		// Instantiate NfvType object
		nfv = objFactory.createNfvType();
		
		// Build NFV methods
		nfv.setCatalog( getCatalog() );
		NffgsType nffgs = objFactory.createNffgsType();
		nfv.setNffgs(nffgs);
		nfv.setHosts( getHosts() );
		nfv.setConnections( getConnections() );
	}
	
	private CatalogType getCatalog() throws NfvClientException
	{
		// Call NfvDeployer REST Web Service
		CatalogType catalog;
		
		try {
			catalog = target.path("catalog")
					     .request()
					     .accept(MediaType.APPLICATION_XML)
					     .get(CatalogType.class);
		}
		catch (ProcessingException pe) {
			throw new NfvClientException("Error during JAX-RS request processing");
		}
		catch (WebApplicationException wae) {
			throw new NfvClientException("Server returned error");
		}
		catch (Exception e) {
			throw new NfvClientException("Unexpected exception");
		}
		
		return catalog;
	}
	
	private HostsType getHosts() throws NfvClientException
	{
		// Call NfvDeployer REST Web Service
		HostsType hosts;
		
		try {
			hosts = target.path("hosts")
					     .request()
					     .accept(MediaType.APPLICATION_XML)
					     .get(HostsType.class);
		}
		catch (ProcessingException pe) {
			throw new NfvClientException("Error during JAX-RS request processing");
		}
		catch (WebApplicationException wae) {
			throw new NfvClientException("Server returned error");
		}
		catch (Exception e) {
			throw new NfvClientException("Unexpected exception");
		}
		
		// Create a complete host set (including nodeRefs)
		HostsType newHosts = objFactory.createHostsType();
		
		for (HostType host: hosts.getHost())
		{
			HostType newHost = getHost( host.getName() );
			newHosts.getHost().add(newHost);
		}
		
		return newHosts;
	}
	
	private HostType getHost(String hostName) throws NfvClientException
	{
		// Call NfvDeployer REST Web Service
		HostType host;
		
		try {
			host = target.path("hosts/" + hostName)
					     .request()
					     .accept(MediaType.APPLICATION_XML)
					     .get(HostType.class);
		}
		catch (ProcessingException pe) {
			throw new NfvClientException("Error during JAX-RS request processing");
		}
		catch (WebApplicationException wae) {
			throw new NfvClientException("Server returned error");
		}
		catch (Exception e) {
			throw new NfvClientException("Unexpected exception");
		}
		
		return host;
	}
	
	private ConnectionsType getConnections() throws NfvClientException
	{
		// Call NfvDeployer REST Web Service
		ConnectionsType connections;
		
		try {
			connections = target.path("connections")
					            .request()
					            .accept(MediaType.APPLICATION_XML)
					            .get(ConnectionsType.class);
		}
		catch (ProcessingException pe) {
			throw new NfvClientException("Error during JAX-RS request processing");
		}
		catch (WebApplicationException wae) {
			throw new NfvClientException("Server returned error");
		}
		catch (Exception e) {
			throw new NfvClientException("Unexpected exception");
		}
		
		return connections;
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
		
		// Call NfvDeployer REST Web Service
		NffgType deployedNffg;
		
		Response response = target.path("nffgs")
				                  .request(MediaType.APPLICATION_XML)
				                  .post(Entity.entity(objFactory.createNffg(nffgType), MediaType.APPLICATION_XML));
		
		try {
			if (response.getStatus() == 200)
				deployedNffg = response.readEntity(NffgType.class);
			else if (response.getStatus() == 409)
				throw new AllocationException();
			else
				throw new ServiceException();
		} 
		finally {
			response.close();
		}
		
		return new MyDeployedNffg(target, nfv, deployedNffg.getName());
	}

	@Override
	public DeployedNffg getDeployedNffg(String name) throws UnknownEntityException, ServiceException
	{
		// Call NfvDeployer REST Web Service
		NffgType deployedNffg;
		
		try {
			deployedNffg = target.path("nffgs/" + name)
					             .request()
					             .accept(MediaType.APPLICATION_XML)
					             .get(NffgType.class);
		}
		catch (ProcessingException pe) {
			throw new ServiceException("Error during JAX-RS request processing", pe);
		}
		catch (NotFoundException nfe) {
			throw new UnknownEntityException();
		}
		catch (WebApplicationException wae) {
			throw new ServiceException("Server returned error", wae);
		}
		catch (Exception e) {
			throw new ServiceException("Unexpected exception", e);
		}
		
		return new MyDeployedNffg(target, nfv, deployedNffg.getName());
	}

}
