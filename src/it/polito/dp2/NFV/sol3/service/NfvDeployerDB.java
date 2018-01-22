package it.polito.dp2.NFV.sol3.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.polito.dp2.NFV.sol3.jaxb.ConnectionType;
import it.polito.dp2.NFV.sol3.jaxb.HostType;
import it.polito.dp2.NFV.sol3.jaxb.LinkType;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;
import it.polito.dp2.NFV.sol3.jaxb.NodeRefType;
import it.polito.dp2.NFV.sol3.jaxb.NodeType;
import it.polito.dp2.NFV.sol3.jaxb.VnfType;

public class NfvDeployerDB
{
	private static long nextNffg = 0;
	private static List<String> hostNameList = new ArrayList<String>();
	private static Map<String, HostsStatus> hostsStatusMap = new ConcurrentHashMap<String, HostsStatus>();
	
	private static Map<String, VnfType> vnfMap = new ConcurrentHashMap<String, VnfType>();
	private static Map<String, NffgType> nffgMap = new ConcurrentHashMap<String, NffgType>();
	private static Map<String, List<NodeType>> nodeListMap = new ConcurrentHashMap<String, List<NodeType>>();
	private static Map<String, NodeType> nodeMap = new ConcurrentHashMap<String, NodeType>();
	private static Map<String, List<LinkType>> linkListMap = new ConcurrentHashMap<String, List<LinkType>>(); // Maps a link list to a node
	private static Map<String, List<LinkType>> nffgLinkListMap = new ConcurrentHashMap<String, List<LinkType>>(); // Maps a link list to a nffg
	private static Map<String, HostType> hostMap = new ConcurrentHashMap<String, HostType>();
	private static Map<String, List<NodeRefType>> nodeRefListMap = new ConcurrentHashMap<String, List<NodeRefType>>();
	private static Map<String, ConnectionType> connectionMap = new ConcurrentHashMap<String, ConnectionType>();
	
	private static Map<String, String> nodeIdMap = new ConcurrentHashMap<String, String>();
	private static Map<String, String> hostIdMap = new ConcurrentHashMap<String, String>();
	
	public static synchronized long getNextNffg()
	{
		return nextNffg++;
	}
	
	public static Map<String, VnfType> getVnfMap()
	{
		return vnfMap;
	}
	
	public static Map<String, NffgType> getNffgMap()
	{
		return nffgMap;
	}
	
	public static Map<String, List<NodeType>> getNodeListMap()
	{
		return nodeListMap;
	}
	
	public static Map<String, NodeType> getNodeMap()
	{
		return nodeMap;
	}
	
	public static Map<String, List<LinkType>> getLinkListMap()
	{
		return linkListMap;
	}
	
	public static Map<String, List<LinkType>> getNffgLinkListMap()
	{
		return nffgLinkListMap;
	}
	
	public static Map<String, HostType> getHostMap()
	{
		return hostMap;
	}
	
	public static Map<String, List<NodeRefType>> getNodeRefListMap()
	{
		return nodeRefListMap;
	}
	
	public static Map<String, ConnectionType> getConnectionMap()
	{
		return connectionMap;
	}
	
	public static Map<String, String> getNodeIdMap()
	{
		return nodeIdMap;
	}
	
	public static Map<String, String> getHostIdMap()
	{
		return hostIdMap;
	}
	
	public static void setHostNameList(List<String> newHostNameList)
	{
		hostNameList = newHostNameList;
	}
	
	public static synchronized List<String> shuffleHostNameList()
	{
		Collections.shuffle(hostNameList);
		return hostNameList;
	}
	
	public static Map<String, HostsStatus> copyHostsStatusMap()
	{
		if (hostsStatusMap == null)
			return null;
		
		Map<String, HostsStatus> copiedMap = new ConcurrentHashMap<String, HostsStatus>();
		
		for (HostsStatus hs: hostsStatusMap.values())
		{
			HostsStatus hs_copy = hs.clone();
			copiedMap.put(hs_copy.getName(), hs_copy);
		}
		
		return copiedMap;
	}
	
	public static void setHostsStatusMap(Map<String, HostsStatus> newHostsStatusMap)
	{
		hostsStatusMap = newHostsStatusMap;
	}
}
