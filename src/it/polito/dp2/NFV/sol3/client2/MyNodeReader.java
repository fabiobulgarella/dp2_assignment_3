package it.polito.dp2.NFV.sol3.client2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.LinkReader;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.VNFTypeReader;
import it.polito.dp2.NFV.sol3.jaxb.LinkType;
import it.polito.dp2.NFV.sol3.jaxb.NodeType;

public class MyNodeReader extends MyNamedEntityReader implements NodeReader
{
	private String vnfName;
	private String hostName;
	private Set<LinkReader> links;
	private String nffgName;
	private HashMap<String, VNFTypeReader> vnfMap;
	private HashMap<String, NffgReader> nffgMap;
	private HashMap<String, HostReader> hostMap;
	
	// Class constructor
	public MyNodeReader(NodeType node, String nffgName, HashMap<String, VNFTypeReader> vnfMap, HashMap<String, NffgReader> nffgMap, HashMap<String, NodeReader> nodeMap, HashMap<String, HostReader> hostMap)
	{
		super(node.getName());
		this.vnfName = node.getVnfRef();
		this.hostName = node.getHostRef();
		this.nffgName = nffgName;
		this.vnfMap = vnfMap;
		this.nffgMap = nffgMap;
		this.hostMap = hostMap;
		
		// Load links
		links = new HashSet<LinkReader>();
		
		for (LinkType link: node.getLink())
		{
			LinkReader newLink_r = new MyLinkReader(link, node.getName(), nodeMap);
			links.add(newLink_r);
		}
	}
	
	@Override
	public VNFTypeReader getFuncType()
	{
		return vnfMap.get(vnfName);
	}
	
	@Override
	public HostReader getHost()
	{
		return hostMap.get(hostName);
	}
	
	@Override
	public Set<LinkReader> getLinks()
	{
		return links;
	}
	
	@Override
	public NffgReader getNffg()
	{
		return nffgMap.get(nffgName);
	}

}
