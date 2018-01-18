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
import it.polito.dp2.NFV.sol3.jaxb.NodeRefType;
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
	private static Map<String, List<LinkType>> linkListMap = NfvDeployerDB.getLinkListMap();
	private static Map<String, HostType> hostMap = NfvDeployerDB.getHostMap();
	private static Map<String, List<NodeRefType>> nodeRefListMap = NfvDeployerDB.getNodeRefListMap();
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
		
		// Prepare hostsStatusMap and hostNameList structures
		Map<String, HostsStatus> hostsStatusMap = new ConcurrentHashMap<String, HostsStatus>();
		List<String> hostNameList = new ArrayList<String>();
		
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
			
			// Update hostsStatusMap
			HostsStatus hs = new HostsStatus(host_r.getName(), host_r.getMaxVNFs(), host_r.getAvailableMemory(), host_r.getAvailableStorage());
			hostsStatusMap.put(host_r.getName(), hs);
			
			// Update hostNameList
			hostNameList.add(host_r.getName());
			
			// Create an empty nodeRefList
			List<NodeRefType> nodeRefList = new ArrayList<NodeRefType>();
			nodeRefListMap.put(host_r.getName(), nodeRefList);
		}
		
		// Save hostResources and hostNameList structures
		NfvDeployerDB.setHostsStatusMap(hostsStatusMap);
		NfvDeployerDB.setHostNameList(hostNameList);
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

		// Get nodes
		for (NodeReader nr: nffg_r.getNodes())
		{
			// Create a new node object
			NodeType node = objFactory.createNodeType();
			node.setName( nr.getName() );
			node.setVnfRef( nr.getFuncType().getName() );
			node.setHostRef( nr.getHost().getName() );
			
			// Get related links
			for (LinkReader lr: nr.getLinks())
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
			
			// Add generated node to nodes list
			nffg.getNode().add(node);
		}
		
		// Save NffgType Object of Nffg0 for consequent deploy
		NfvDeployerDB.nffg0 = nffg;
	}
	
}
