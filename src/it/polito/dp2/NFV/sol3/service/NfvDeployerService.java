package it.polito.dp2.NFV.sol3.service;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import it.polito.dp2.NFV.lab3.ServiceException;
import it.polito.dp2.NFV.sol3.jaxb.CatalogType;
import it.polito.dp2.NFV.sol3.jaxb.ConnectionType;
import it.polito.dp2.NFV.sol3.jaxb.HostType;
import it.polito.dp2.NFV.sol3.jaxb.HostsType;
import it.polito.dp2.NFV.sol3.jaxb.LinkType;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;
import it.polito.dp2.NFV.sol3.jaxb.NffgsType;
import it.polito.dp2.NFV.sol3.jaxb.NodeRefType;
import it.polito.dp2.NFV.sol3.jaxb.NodeType;
import it.polito.dp2.NFV.sol3.jaxb.ObjectFactory;
import it.polito.dp2.NFV.sol3.jaxb.VnfType;
import it.polito.dp2.NFV.sol3.neo4j.Labels;
import it.polito.dp2.NFV.sol3.neo4j.Node;
import it.polito.dp2.NFV.sol3.neo4j.Nodes;
import it.polito.dp2.NFV.sol3.neo4j.Properties;
import it.polito.dp2.NFV.sol3.neo4j.Property;
import it.polito.dp2.NFV.sol3.neo4j.Relationship;

public class NfvDeployerService
{
	private static NfvDeployerService instance = null;
	private ObjectFactory objFactory;
	private it.polito.dp2.NFV.sol3.neo4j.ObjectFactory neo4jFactory;
	private WebTarget target;
	private String neo4jURL;
	
	// DB -> concurrent maps
	private Map<String, VnfType> vnfMap = NfvDeployerDB.getVnfMap();
	private Map<String, NffgType> nffgMap = NfvDeployerDB.getNffgMap();
	private Map<String, List<NodeType>> nodeListMap = NfvDeployerDB.getNodeListMap();
	private Map<String, NodeType> nodeMap = NfvDeployerDB.getNodeMap();
	private Map<String, List<LinkType>> linkListMap = NfvDeployerDB.getLinkListMap();
	private Map<String, LinkType> linkNameMap = NfvDeployerDB.getLinkNameMap();
	private Map<String, HostType> hostMap = NfvDeployerDB.getHostMap();
	private Map<String, List<NodeRefType>> nodeRefListMap = NfvDeployerDB.getNodeRefListMap();
	private Map<String, ConnectionType> connectionMap = NfvDeployerDB.getConnectionMap();
	
	private Map<String, String> nodeIdMap = NfvDeployerDB.getNodeIdMap();
	private Map<String, String> hostIdMap = NfvDeployerDB.getHostIdMap();
	
	// Class constructor
	private NfvDeployerService()
	{
		// Instantiate ObjectFactory
		objFactory = new ObjectFactory();
		neo4jFactory = new it.polito.dp2.NFV.sol3.neo4j.ObjectFactory();
		
		// Initialize NfvDeployer
		NfvDeployerInit nfvInit = new NfvDeployerInit();
		NffgType nffg0 = nfvInit.bootstrap();
		
		// Read Neo4JSimpleXML url
		neo4jURL = System.getProperty("it.polito.dp2.NFV.lab3.Neo4JSimpleXMLURL");
		if (neo4jURL == null)
			neo4jURL = "http://localhost:8080/Neo4JSimpleXML/rest";
		
		// Create JAX-RS Client and WebTarget
		Client client = ClientBuilder.newClient();
		try {
			target = client.target(neo4jURL);
		}
		catch (IllegalArgumentException iae) {
			throw new InternalServerErrorException();
		}
		
		// Deploy Nffg0
		if ( postNffg(nffg0) == null )
			throw new InternalServerErrorException();
	}
	
	// Singleton instance method
	public static synchronized NfvDeployerService getInstance()
	{
		if (instance == null)
		{
			instance = new NfvDeployerService();
		}
		
		return instance;
	}
	
	/*
	 * CATALOG METHODS
	 */
	public JAXBElement<CatalogType> getCatalog()
	{
		CatalogType catalog = objFactory.createCatalogType();
		
		for (VnfType vnf: vnfMap.values())
		{
			catalog.getVnf().add(vnf);
		}
		
		return objFactory.createCatalog(catalog);
	}
	
	/*
	 * NFFGS METHODS
	 */
	public JAXBElement<NffgsType> getNffgs()
	{
		NffgsType nffgs = objFactory.createNffgsType();
		
		for (NffgType nffg: nffgMap.values())
		{
			nffgs.getNffg().add(nffg);
		}
		
		return objFactory.createNffgs(nffgs);
	}
	
	public JAXBElement<NffgType> getNffg(String id)
	{
		NffgType nffg = nffgMap.get(id);
		List<NodeType> nodeList = nodeListMap.get(id);
		
		if (nffg != null)
		{
			NffgType newNffg = objFactory.createNffgType();
			newNffg.setName( nffg.getName() );
			newNffg.setDeployTime( nffg.getDeployTime() );
			
			if (nodeList != null)
			{
				for (NodeType node: nodeList)
				{
					newNffg.getNode().add(node);
				}
			}
			
			return objFactory.createNffg(newNffg);
		}
		
		return null;
	}
	
	public JAXBElement<NodeType> getNode(String nffgName, String nodeName)
	{
		List<NodeType> nodeList = nodeListMap.get(nffgName);
		NodeType node = nodeMap.get(nodeName);
		List<LinkType> linkList = linkListMap.get(nodeName);
		
		if (nodeList != null && node != null)
		{
			if (nodeList.contains(node))
			{
				NodeType newNode = objFactory.createNodeType();
				newNode.setName( node.getName() );
				newNode.setVnfRef( node.getVnfRef() );
				newNode.setHostRef( node.getHostRef() );
				
				if (linkList != null)
				{
					for (LinkType link: linkList)
					{
						newNode.getLink().add(link);
					}
				}
				
				return objFactory.createNode(newNode);
			}
		}
		
		return null;
	}
	
	public synchronized JAXBElement<NffgType> postNffg(NffgType nffg)
	{
		String nffgName = nffg.getName();
		
		// Check if a new nffgName should be generated
		if ( nffgName == null || isDeployed(nffgName) )
		{
			nffgName = newNffgName();
		}
		
		// Temporary Maps
		Map<String, NodeType> nodeMapTMP = new HashMap<>();
		Map<String, List<LinkType>> linkListMapTMP = new HashMap<>();
		Map<String, LinkType> linkNameMapTMP = new HashMap<>();
		
		// Map used for associate oldNodeName to new generated node
		Map<String, NodeType> oldNameToNewNodeMap = new HashMap<>();
		
		// Get a copy of hostsStatusMap
		Map<String, HostsStatus> hostsStatusMap = NfvDeployerDB.copyHostsStatusMap();
		
		// Create a new NffgType
		NffgType newNffg = objFactory.createNffgType();
		newNffg.setName(nffgName);
		
		List<NodeType> nodeList = nffg.getNode();
		List<NodeType> newNodeList = new ArrayList<NodeType>();
		
		for (NodeType node: nodeList)
		{
			String oldNodeName = node.getName();
			String newNodeName;
			
			// Create a new name for newNode
			if ( nffgName.equals("Nffg0") )
				newNodeName = oldNodeName;
			else
				newNodeName = newNodeName(nodeMapTMP, node.getVnfRef(), nffgName);
			
			// Create a new node object
			NodeType newNode = objFactory.createNodeType();
			newNode.setName(newNodeName);
			newNode.setVnfRef( node.getVnfRef() );
			newNode.setHostRef( node.getHostRef() );
			
			// Add generated node to nodeList and to nodeMap
			newNodeList.add(newNode);
			nodeMapTMP.put(newNodeName, newNode);
			
			// Add generated node to oldNameToNewNodeMap
			oldNameToNewNodeMap.put(oldNodeName, newNode);			
		}
		
		// Load links into the system
		long nextLink = 0;
		for (NodeType node: nodeList)
		{
			// Retrieve newNodeName for related node
			String newNodeName = oldNameToNewNodeMap.get( node.getName() ).getName();
			
			// Get related links
			List<LinkType> linkList = node.getLink();
			List<LinkType> newLinkList = new ArrayList<LinkType>();
			
			for (LinkType link: linkList)
			{
				String newLinkName;
				
				// Create a new name for the link
				if ( nffgName.equals("Nffg0") )
					newLinkName = link.getName();
				else
				{
					newLinkName = "Link" + nextLink;
					nextLink++;
				}
				
				// Get new name of destination node
				String dstNodeName = oldNameToNewNodeMap.get( link.getDstNode() ).getName();
				
				// Create a new link object
				LinkType newLink = objFactory.createLinkType();
				newLink.setName(newLinkName);
				newLink.setDstNode(dstNodeName);
				newLink.setMinThroughput( link.getMinThroughput() );
				newLink.setMaxLatency( link.getMaxLatency() );
				
				// Add generated link to links list
				newLinkList.add(newLink);
				linkNameMapTMP.put(nffgName + newLinkName, newLink);
			}
			
			// Add generated linkList to linkListMap
			linkListMapTMP.put(newNodeName, newLinkList);
		}
		
		// Check if nodes can be allocated into the IN system
		if ( !checkNodesAllocation(nodeMapTMP, hostsStatusMap) )
			return null;
		
		// NEO4J CALLS		
		try {
			// Load nodes into neo4j graph (Type "Node")
			loadNodes("Node", nodeMapTMP);
			
			// Get links and create relationships
			loadRelationships("ForwardsTo", nodeMapTMP, linkListMapTMP);
			
			// Load hosts into neo4j graph (Type "Host")
			loadNodes("Host", nodeMapTMP);
			
			// Create relationship between nodes and hosts
			loadRelationships("AllocatedOn", nodeMapTMP, linkListMapTMP);
		}
		catch (ServiceException se) {
			return null;
		}
		
		// Update nodeRefLists
		for (NodeType node: nodeMapTMP.values())
		{
			NodeRefType nodeRef = objFactory.createNodeRefType();
			nodeRef.setName(node.getName());
			
			nodeRefListMap.get(node.getHostRef()).add(nodeRef);
		}
		
		// Set deployTime variable to actual time
		try {
			newNffg.setDeployTime( DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()) );
		}
		catch (DatatypeConfigurationException e) {
			throw new InternalServerErrorException();
		}
		
		// If all it's ok, update data maps
		nffgMap.put(newNffg.getName(), newNffg);
		nodeListMap.put(newNffg.getName(), newNodeList);
		nodeMap.putAll(nodeMapTMP);
		linkListMap.putAll(linkListMapTMP);
		linkNameMap.putAll(linkNameMapTMP);
		NfvDeployerDB.setHostsStatusMap(hostsStatusMap);
		
		return objFactory.createNffg(newNffg);
	}
	
	public synchronized boolean postNode(String nffgName, NodeType node)
	{
		// Check if related nffg is deployed
		if ( !isDeployed(nffgName) )
			return false;
		
		// Temporary Maps
		Map<String, NodeType> nodeMapTMP = new HashMap<>();
		Map<String, List<LinkType>> linkListMapTMP = new HashMap<>();
		
		// Get a copy of hostsStatusMap
		Map<String, HostsStatus> hostsStatusMap = NfvDeployerDB.copyHostsStatusMap();
		
		// Check if exists a node with the same name into the system, if yes abort
		if (nodeMap.get(node.getName()) != null)
			return false;
		
		// Create a new node object
		NodeType newNode = objFactory.createNodeType();
		newNode.setName( node.getName() );
		newNode.setVnfRef( node.getVnfRef() );
		newNode.setHostRef( node.getHostRef() );
		
		// Add generated node to nodeMapTMP
		nodeMapTMP.put(node.getName(), newNode);
		
		// Check if nodes can be allocated into the IN system
		if ( !checkNodesAllocation(nodeMapTMP, hostsStatusMap) )
			return false;
		
		// NEO4J CALLS		
		try {
			// Load nodes into neo4j graph (Type "Node")
			loadNodes("Node", nodeMapTMP);
			
			// Get links and create relationships
			loadRelationships("ForwardsTo", nodeMapTMP, linkListMapTMP);
			
			// Load hosts into neo4j graph (Type "Host")
			loadNodes("Host", nodeMapTMP);
			
			// Create relationship between nodes and hosts
			loadRelationships("AllocatedOn", nodeMapTMP, linkListMapTMP);
		}
		catch (ServiceException se) {
			return false;
		}
		
		// Update nodeRefLists
		NodeRefType nodeRef = objFactory.createNodeRefType();
		nodeRef.setName(newNode.getName());
		nodeRefListMap.get(newNode.getHostRef()).add(nodeRef);
		
		// If all it's ok, update data maps
		nodeListMap.get(nffgName).add(newNode);
		nodeMap.put(newNode.getName(), newNode);
		NfvDeployerDB.setHostsStatusMap(hostsStatusMap);
		
		return true;
	}
	
	public synchronized boolean postLink(String nffgName, String srcNodeName, LinkType link)
	{
		// Check if related nffg is deployed
		if ( !isDeployed(nffgName) )
			return false;
		
		// Check if node exists and belongs to nffg specified
		NodeType srcNode = nodeMap.get(srcNodeName);
		if ( srcNode == null || !nodeListMap.get(nffgName).contains(srcNode) )
			return false;
		
		// Check if destination node exists and belongs to nffg specified
		NodeType dstNode = nodeMap.get( link.getDstNode() );
		if ( dstNode == null || !nodeListMap.get(nffgName).contains(dstNode) )
			return false;
		
		// Check if already exists this link
		List<LinkType> linkList = linkListMap.get(srcNodeName);
		for (LinkType link_t: linkList)
		{
			if ( link_t.getDstNode().equals( link.getDstNode() ) )
			{
				// Check if overwrite attribute is set
				if (link.isOverwrite() != null && link.isOverwrite())
				{
					// Overwrite link information
					link_t.setMinThroughput( link.getMinThroughput() != null ? link.getMinThroughput() : 0 );
					link_t.setMaxLatency( link.getMaxLatency() != null ? link.getMaxLatency() : 0 );
					
					return true;
				}
				
				// If overwrite attribute is not set abort
				return false;
			}
		}
		
		// If already exists a link with the same name into the specified nffg, abort
		if ( linkNameMap.get(nffgName + link.getName()) != null )
			return false;
		
		// If not, generate a new link
		LinkType newLink = objFactory.createLinkType();
		newLink.setName( link.getName() );
		newLink.setDstNode( link.getDstNode() );
		newLink.setMinThroughput( link.getMinThroughput() != null ? link.getMinThroughput() : 0 );
		newLink.setMaxLatency( link.getMaxLatency() != null ? link.getMaxLatency() : 0 );
		
		// Load link into neo4j
		try {
			postRelationships("ForwardsTo", srcNode, newLink);
		}
		catch (ServiceException se) {
			return false;
		}
		
		// Update maps
		linkList.add(newLink);
		linkNameMap.put(nffgName + newLink.getName(), newLink);
		
		return true;
	}

	/*
	 * HOSTS METHODS
	 */
	public JAXBElement<HostsType> getHosts()
	{
		HostsType hosts = objFactory.createHostsType();
		
		for (HostType host: hostMap.values())
		{
			hosts.getHost().add(host);
		}
		
		return objFactory.createHosts(hosts);
	}

	public JAXBElement<HostType> getHost(String id)
	{
		HostType host = hostMap.get(id);
		List<NodeRefType> nodeRefList = nodeRefListMap.get(id);
		
		if (host != null)
		{
			HostType newHost = objFactory.createHostType();
			newHost.setName( host.getName() );
			newHost.setMaxVnfs( host.getMaxVnfs() );
			newHost.setMemory( host.getMemory() );
			newHost.setStorage( host.getStorage() );
			
			if (nodeRefList != null)
			{
				for (NodeRefType nodeRef: nodeRefList)
				{
					newHost.getNodeRef().add(nodeRef);
				}
			}
			
			return objFactory.createHost(newHost);
		}
		
		return null;
	}
	
	public synchronized JAXBElement<HostsType> getReachableHosts(String nffgName, String nodeName)
	{
		// Check if related nffg is deployed
		if ( !isDeployed(nffgName) )
			return null;
		
		// Check if node exists and belongs to nffg specified
		NodeType node = nodeMap.get(nodeName);
		if ( node == null || !nodeListMap.get(nffgName).contains(node) )
			return null;
		
		// Get neo4j node ID
		String nodeID = nodeIdMap.get(nodeName);
		if (nodeID == null)
			return null;
		
		// Call neo4j in order to obtain reachableNodes list
		Nodes reachableNodes;
		try {
			reachableNodes = getReachableNodes(nodeID);
		}
		catch (ServiceException se) {
			return null;
		}
		
		// Create hosts element and hostNameSet (to keep track of host already added in the list)
		HostsType newHosts = objFactory.createHostsType();
		HashSet<String> reachableHostNameSet = new HashSet<String>();
		
		// Add host where node is allocated on into the set
		HostType host = hostMap.get( node.getHostRef() );
		if (host != null)
		{
			newHosts.getHost().add(host);
			reachableHostNameSet.add( host.getName() );
		}
		
		// Search for reachable hosts
		for (Node reachNode: reachableNodes.getNode())
		{
			String reachNodeName = reachNode.getProperties().getProperty().iterator().next().getValue();
			
			// Add reachable host if present AND if not already loaded inside reachableHostSet
			HostType newReachableHost = hostMap.get( nodeMap.get(reachNodeName).getHostRef() );
			
			if (newReachableHost != null)
			{
				String newReachableHostName = newReachableHost.getName();
				
				if ( !reachableHostNameSet.contains(newReachableHostName) )
				{
					newHosts.getHost().add(newReachableHost);
					reachableHostNameSet.add(newReachableHostName);
				}
			}
		}
		
		return objFactory.createHosts(newHosts);
	}
	
	/*
	 * CONNECTIONS METHODS
	 */
	public JAXBElement<ConnectionType> getConnection(String host1, String host2)
	{
		ConnectionType connection = connectionMap.get(host1 + host2);
		if (connection != null)
			return objFactory.createConnection(connection);
		
		return null;
	}
	
	/*
	 * UTILS METHODS (called only by synchronized postNffg method)
	 */
	private boolean isDeployed(String nffgName)
	{
		if (nffgMap.get(nffgName) != null)
			return true;
		
		return false;
	}
	
	private String newNffgName()
	{
		String nffgName;
		
		do {
			nffgName = "Nffg" + NfvDeployerDB.getNextNffg();
		} while(nffgMap.get(nffgName) != null);
		
		return nffgName;
	}
	
	private String newNodeName(Map<String, NodeType> nodeMapTMP, String vnfRef, String nffgName)
	{
		String nodeName;
		long next = 0;
		
		do {
			nodeName = vnfRef + next + nffgName;
			next++;
		} while(nodeMapTMP.get(nodeName) != null);
		
		return nodeName;
	}
	
	private boolean checkNodesAllocation(Map<String, NodeType> nodeMapTMP, Map<String, HostsStatus> hostsStatusMap)
	{	
		for (NodeType node: nodeMapTMP.values())
		{
			String hostRef = node.getHostRef();
			VnfType vnf = vnfMap.get( node.getVnfRef() );
			
			// Check if vnfType exists
			if (vnf == null)
				return false;
			
			// Get resources requirements
			int reqMemory = vnf.getReqMemory();
			int reqStorage = vnf.getReqStorage();
			
			if (hostRef != null && hostMap.get(hostRef) != null)
			{
				HostsStatus hs = hostsStatusMap.get(hostRef);
				
				// Check if user's choice can be satisfy
				if (hs.vnfs < hs.maxVnfs && reqMemory < (hs.memory - hs.usedMemory) && reqStorage < (hs.storage - hs.usedStorage))
				{
					// Update host's resources values
					hs.vnfs++;
					hs.usedMemory += reqMemory;
					hs.usedStorage += reqStorage;
					
					// Continue to next node
					continue;
				}
			}
			
			// Look for an alternative host where allocate the node
			boolean notFound = true;
			
			for (String hostName: NfvDeployerDB.shuffleHostNameList())
			{
				HostsStatus hs = hostsStatusMap.get(hostName);
				
				// Check if host named hostName can host this node
				if (hs.vnfs < hs.maxVnfs && reqMemory < (hs.memory - hs.usedMemory) && reqStorage < (hs.storage - hs.usedStorage))
				{
					// Set new host for target node
					node.setHostRef(hostName);
					
					// Update host's resources values
					hs.vnfs++;
					hs.usedMemory += reqMemory;
					hs.usedStorage += reqStorage;
					
					// Continue to next node
					notFound = false;
					break;
				}
			}
			
			// If host hasn't been found stop deploy
			if (notFound)
				return false;
		}
		
		return true;
	}
	
	/*
	 * NEO4J INTERACTION METHODS
	 */
	private void loadNodes(String type, Map<String, NodeType> nodeMapTMP) throws ServiceException
	{
		for (NodeType node: nodeMapTMP.values())
		{
			String nodeName;
			HostType host;
			
			// Type is Node
			if (type.equals("Node"))
			{
				// Get nodeName (nffg-node)
				nodeName = node.getName();
			}
			
			// Type is Host
			else
			{
				host = hostMap.get( node.getHostRef() );
				
				// Check if node is not allocated on a host
				if (host == null) continue;
				
				// Get hostName
				nodeName = host.getName();
				
				// Check if host has been already uploaded on neo4j
				if (hostIdMap.get(nodeName) != null) continue;
			}
			
			// Create a new node object
			Node newNode = neo4jFactory.createNode();
			Properties newProperties = neo4jFactory.createProperties();
			Property newProperty = neo4jFactory.createProperty();
			newProperty.setName("name");
			newProperty.setValue(nodeName);
			newProperties.getProperty().add(newProperty);
			newNode.setProperties(newProperties);
			
			// Create a new labels object
			Labels newLabels = neo4jFactory.createLabels();
			newLabels.getLabel().add(type);
			
			// Call Neo4JSimpleXML API
			try {
				Node res = target.path("data/node")
						         .request(MediaType.APPLICATION_XML)
						         .post(Entity.entity(newNode, MediaType.APPLICATION_XML), Node.class);
				
				Response res2 = target.path("data/node/" + res.getId() + "/labels")
						              .request(MediaType.APPLICATION_XML)
						              .post(Entity.entity(newLabels, MediaType.APPLICATION_XML));
				
				// Check "res2" response (it doesn't throw exception automatically)
				if (res2.getStatus() != 204)
					throw new WebApplicationException();
				
				if (type.equals("Node"))
					nodeIdMap.put(nodeName, res.getId());
				else
					hostIdMap.put(nodeName, res.getId());
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
		}
	}
	
	private void loadRelationships(String type, Map<String, NodeType> nodeMapTMP, Map<String, List<LinkType>> linkListMapTMP) throws ServiceException
	{
		// Type is ForwardsTo
		if (type.equals("ForwardsTo"))
		{			
			for (NodeType node: nodeMapTMP.values())
			{
				List<LinkType> linkList = linkListMapTMP.get(node.getName());
				
				if (linkList != null)
				{
					for (LinkType link: linkList)
						postRelationships(type, node, link);
				}
			}
		}
		
		// Type is AllocatedOn
		else
		{
			for (NodeType node: nodeMapTMP.values())
				postRelationships(type, node, null);
		}
	}
	
	private void postRelationships(String type, NodeType node, LinkType link) throws ServiceException
	{
		HostType host;
		
		// Retrieve source and destination node id from nodeMap
		String srcNodeID = nodeIdMap.get( node.getName() );
		String dstNodeID;
		
		// Type is ForwardsTo
		if (type.equals("ForwardsTo"))
		{
			// Get dstNodeID relatively to a node
			dstNodeID = nodeIdMap.get( link.getDstNode() );
		}
		
		// Type is AllocatedOn
		else
		{
			host = hostMap.get( node.getHostRef() );
			
			// Check if there's no host that allocates the node
			if (host == null) return;
			
			// Get dstNodeID relatively to an host
			dstNodeID = hostIdMap.get( host.getName() );
		}
		
		// Create a new relationship object
		Relationship newRelationship = neo4jFactory.createRelationship();
		newRelationship.setDstNode(dstNodeID);
		newRelationship.setType(type);
		
		// Call Neo4JSimpleXML API
		try {
			Relationship res = target.path("data/node/" + srcNodeID + "/relationships")
					                 .request(MediaType.APPLICATION_XML)
					                 .post(Entity.entity(newRelationship, MediaType.APPLICATION_XML), Relationship.class);
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
	}
	
	private Nodes getReachableNodes(String nodeID) throws ServiceException
	{
		// Call Neo4JSimpleXML API
		Nodes reachableNodes;
		
		try {
			reachableNodes = target.path("data/node/" + nodeID + "/reachableNodes")
					               .queryParam("relationshipTypes", "ForwardsTo")
					               .queryParam("nodeLabel", "Node")
					               .request()
					               .accept(MediaType.APPLICATION_XML)
					               .get(Nodes.class);
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
		
		return reachableNodes;
	}
	
}
