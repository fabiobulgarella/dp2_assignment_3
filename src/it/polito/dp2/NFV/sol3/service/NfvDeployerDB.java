package it.polito.dp2.NFV.sol3.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.polito.dp2.NFV.sol3.jaxb.ConnectionType;
import it.polito.dp2.NFV.sol3.jaxb.HostType;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;
import it.polito.dp2.NFV.sol3.jaxb.NodeType;
import it.polito.dp2.NFV.sol3.jaxb.VnfType;

public class NfvDeployerDB
{
	private static Map<String, VnfType> vnfMap = new ConcurrentHashMap<String, VnfType>();
	private static Map<String, NffgType> nffgMap = new ConcurrentHashMap<String, NffgType>();
	private static Map<String, NodeType> nodeMap = new ConcurrentHashMap<String, NodeType>();
	private static Map<String, HostType> hostMap = new ConcurrentHashMap<String, HostType>();
	private static Map<String, ConnectionType> connectionMap = new ConcurrentHashMap<String, ConnectionType>();
	
	public static Map<String, VnfType> getVnfMap()
	{
		return vnfMap;
	}
	
	public static Map<String, NffgType> getNffgMap()
	{
		return nffgMap;
	}
	
	public static Map<String, NodeType> getNodeMap()
	{
		return nodeMap;
	}
	
	public static Map<String, HostType> getHostMap()
	{
		return hostMap;
	}
	
	public static Map<String, ConnectionType> getConnectionMap()
	{
		return connectionMap;
	}
}
