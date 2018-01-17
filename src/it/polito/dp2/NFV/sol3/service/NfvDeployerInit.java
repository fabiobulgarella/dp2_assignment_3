package it.polito.dp2.NFV.sol3.service;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.InternalServerErrorException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import it.polito.dp2.NFV.ConnectionPerformanceReader;
import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.LinkReader;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.NfvReader;
import it.polito.dp2.NFV.NfvReaderException;
import it.polito.dp2.NFV.NfvReaderFactory;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.VNFTypeReader;
import it.polito.dp2.NFV.sol3.jaxb.ConnectionType;
import it.polito.dp2.NFV.sol3.jaxb.HostType;
import it.polito.dp2.NFV.sol3.jaxb.LinkType;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;
import it.polito.dp2.NFV.sol3.jaxb.NodeType;
import it.polito.dp2.NFV.sol3.jaxb.ObjectFactory;
import it.polito.dp2.NFV.sol3.jaxb.VnfType;

public class NfvDeployerInit
{
	private static boolean toBootstrap = true;
	private static NfvReader monitor;
	private static ObjectFactory objFactory;
	
	// DB -> concurrent maps
	private static Map<String, VnfType> vnfMap = NfvDeployerDB.getVnfMap();
	private static Map<String, NffgType> nffgMap = NfvDeployerDB.getNffgMap();
	private static Map<String, List<NodeType>> nodeListMap = NfvDeployerDB.getNodeListMap();
	private static Map<String, NodeType> nodeMap = NfvDeployerDB.getNodeMap();
	private static Map<String, HostType> hostMap = NfvDeployerDB.getHostMap();
	private static Map<String, ConnectionType> connectionMap = NfvDeployerDB.getConnectionMap();
	
	// Make this entire class static
	private NfvDeployerInit() {}
	
	// Execute bootstrap
	public static synchronized void bootstrap()
	{
		// Initialize NfvDeployer if not already done
		if (toBootstrap)
		{
			NfvReaderFactory factory = NfvReaderFactory.newInstance();
			try {
				monitor = factory.newNfvReader();
			}
			catch (NfvReaderException e) {
				System.err.println("Error during initialization of NfvDeployer -> NfvReaderException");
				throw new InternalServerErrorException();
			}
			
			objFactory = new ObjectFactory();
			
			initCatalog();
			initHosts();
			initConnections();
			initNffg0();
			
			toBootstrap = false;
		}
	}
	
	// Initialize catalog map
	private static void initCatalog()
	{
		// Get the list of vnf
		Set<VNFTypeReader> set = monitor.getVNFCatalog();

		// For each VNF type get name and attribute
		for (VNFTypeReader vnfType_r: set)
		{
			// Create a new vnf object
			VnfType vnf = objFactory.createVnfType();
			vnf.setName( vnfType_r.getName() );
			vnf.setFunctionalType( vnfType_r.getFunctionalType().value() );
			vnf.setReqMemory( vnfType_r.getRequiredMemory() );
			vnf.setReqStorage( vnfType_r.getRequiredStorage() );
			
			// Add generated vnf to catalog map
			vnfMap.put(vnfType_r.getName(), vnf);
		}
	}
	
	// Initialize hosts map
	private static void initHosts()
	{
		// Get the list of Hosts
		Set<HostReader> set = monitor.getHosts();
		
		// For each Host get related data
		for (HostReader host_r: set)
		{
			// Create a new host object
			HostType host = objFactory.createHostType();
			host.setName( host_r.getName() );
			host.setMaxVnfs( host_r.getMaxVNFs() );
			host.setMemory( host_r.getAvailableMemory() );
			host.setStorage( host_r.getAvailableStorage() );
			
			// Add generated host to hosts map
			hostMap.put(host_r.getName(), host);
		}
	}
	
	// Initialize connections map
	private static void initConnections()
	{
		// Get the list of Hosts
		Set<HostReader> set = monitor.getHosts();
		
		// For each pair of Hosts get related data
		for (HostReader sri: set) {
			for (HostReader srj: set)
			{
				// Create a new connection object
				ConnectionType connection = objFactory.createConnectionType();
				connection.setHost1( sri.getName() );
				connection.setHost2( srj.getName() );
				
				// Get performance data
				ConnectionPerformanceReader cpr = monitor.getConnectionPerformance(sri, srj);
				connection.setThroughput( cpr.getThroughput() );
				connection.setLatency( cpr.getLatency() );
				
				// Add generated connection to connections map
				connectionMap.put(sri.getName() + srj.getName(), connection);
			}
		}		
	}
	
	private static void initNffg0()
	{
		// Get Nffg0
		NffgReader nffg_r = monitor.getNffg("Nffg0");
	
		// Create a new nffg object
		NffgType nffg = objFactory.createNffgType();
		nffg.setName( nffg_r.getName() );
		
		// Retrieve and convert date
		GregorianCalendar deployTime = (GregorianCalendar) nffg_r.getDeployTime();
		XMLGregorianCalendar convertedTime = null;
		try {
			convertedTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(deployTime);
		}
		catch (DatatypeConfigurationException dce) {
			System.err.println("Error while converting date to XML format");
			throw new InternalServerErrorException();
		}
		nffg.setDeployTime(convertedTime);

		// Get nodes
		Set<NodeReader> nodeSet = nffg_r.getNodes();
		List<NodeType> nodeList = new ArrayList<NodeType>();
		
		for (NodeReader nr: nodeSet)
		{
			// Create a new node object
			NodeType node = objFactory.createNodeType();
			node.setName( nr.getName() );
			node.setVnfRef( nr.getFuncType().getName() );
			node.setHostRef( nr.getHost().getName() );
			
			// Get related links
			Set<LinkReader> linkSet = nr.getLinks();
			
			for (LinkReader lr: linkSet)
			{
				// Create a new link object
				LinkType link = objFactory.createLinkType();
				link.setName( lr.getName() );
				link.setDstNode( lr.getDestinationNode().getName() );
				link.setMinThroughput( lr.getThroughput() );
				link.setMaxLatency( lr.getLatency() );
				
				// Add generated link to links list
				node.getLink().add(link);
			}
			
			// Add generated node to nodes list and to nodeMap
			nodeList.add(node);
			nodeMap.put(nr.getName(), node);
		}
		
		// Add generated nffg to nffgMap
		nodeListMap.put(nffg_r.getName(), nodeList);
		nffgMap.put(nffg_r.getName(), nffg);
	}
	
}
