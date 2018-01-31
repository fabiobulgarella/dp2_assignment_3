package it.polito.dp2.NFV.sol3.client2;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import it.polito.dp2.NFV.ConnectionPerformanceReader;
import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.NfvReader;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.VNFTypeReader;
import it.polito.dp2.NFV.sol3.jaxb.ConnectionType;
import it.polito.dp2.NFV.sol3.jaxb.HostType;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;
import it.polito.dp2.NFV.sol3.jaxb.NfvType;
import it.polito.dp2.NFV.sol3.jaxb.VnfType;

public class MyNfvReader implements NfvReader
{
	private NfvType nfvObject;
	private Set<VNFTypeReader> vnfSet;
	private Set<NffgReader> nffgSet;
	private Set<HostReader> hostSet;
	private HashMap<String, VNFTypeReader> vnfMap;
	private HashMap<String, NffgReader> nffgMap;
	private HashMap<String, NodeReader> nodeMap;
	private HashMap<String, HostReader> hostMap;
	private HashMap<String, ConnectionPerformanceReader> connectionMap;
	
	// Class constructor
	public MyNfvReader(NfvType nfv)
	{
		nfvObject = nfv;
		
		// Create sets classes for storing data
		vnfSet = new HashSet<VNFTypeReader>();
		nffgSet = new HashSet<NffgReader>();
		hostSet = new HashSet<HostReader>();
		
		// Create hash-map for reference entities between them
		vnfMap = new HashMap<String, VNFTypeReader>();
		nffgMap = new HashMap<String, NffgReader>();
		nodeMap = new HashMap<String, NodeReader>();
		hostMap = new HashMap<String, HostReader>();
		connectionMap = new HashMap<String, ConnectionPerformanceReader>();
		
		// Load catalog
		for (VnfType vnf: nfvObject.getCatalog().getVnf())
		{
			VNFTypeReader newVnf_r = new MyVNFTypeReader(vnf);
			vnfMap.put(vnf.getName(), newVnf_r);
			vnfSet.add(newVnf_r);
		}
		
		// Load nffgs
		for (NffgType nffg: nfvObject.getNffgs().getNffg())
		{
			NffgReader newNffg_r = new MyNffgReader(nffg, vnfMap, nffgMap, nodeMap, hostMap);
			nffgMap.put(nffg.getName(), newNffg_r);
			nffgSet.add(newNffg_r);
		}
		
		// Load hosts
		for (HostType host: nfvObject.getHosts().getHost())
		{
			HostReader newHost_r = new MyHostReader(host, nodeMap);
			hostMap.put(host.getName(), newHost_r);
			hostSet.add(newHost_r);
		}
		
		// Load connections
		for (ConnectionType connection: nfvObject.getConnections().getConnection())
		{
			ConnectionPerformanceReader newConnection_r = new MyConnectionPerformanceReader(connection.getLatency(), connection.getThroughput());
			connectionMap.put(connection.getHost1() + connection.getHost2(), newConnection_r);
		}
	}
	
	@Override
	public ConnectionPerformanceReader getConnectionPerformance(HostReader arg0, HostReader arg1)
	{
		if(arg0 == null || arg1 == null)
		{
			// Argument(s) is/are null, don't even scan connectionMap
			return null;
		}
		
		return connectionMap.get(arg0.getName() + arg1.getName());
	}
	
	@Override
	public HostReader getHost(String arg0)
	{
		if(arg0 == null)
		{
			// Argument is null, don't even scan hostMap
			return null;
		}
		
		return hostMap.get(arg0);
	}
	
	@Override
	public Set<HostReader> getHosts()
	{
		return hostSet;
	}
	
	@Override
	public NffgReader getNffg(String arg0)
	{
		if(arg0 == null)
		{
			// Argument is null, don't even scan nffgMap
			return null;
		}
		
		return nffgMap.get(arg0);
	}
	
	@Override
	public Set<NffgReader> getNffgs(Calendar arg0)
	{
		// Return all set 'cause argument is null
		if (arg0 == null)
			return nffgSet;

		Set<NffgReader> set = new HashSet<NffgReader>();
		for (NffgReader nffg: nffgSet)
		{
			if (nffg.getDeployTime().compareTo(arg0) >= 0)
				set.add(nffg);
		}
		
		return set;
	}
	
	@Override
	public Set<VNFTypeReader> getVNFCatalog()
	{
		return vnfSet;
	}

}
