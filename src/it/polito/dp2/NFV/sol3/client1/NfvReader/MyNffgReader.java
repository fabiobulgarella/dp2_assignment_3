package it.polito.dp2.NFV.sol3.client1.NfvReader;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.VNFTypeReader;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;
import it.polito.dp2.NFV.sol3.jaxb.NodeType;

public class MyNffgReader extends MyNamedEntityReader implements NffgReader
{
	private Calendar deployTime;
	private Set<NodeReader> nodes;
	private HashMap<String, NodeReader> nodeMap;
	
	// Class constructor
	public MyNffgReader(NffgType nffg, HashMap<String, VNFTypeReader> vnfMap, HashMap<String, NffgReader> nffgMap, HashMap<String, NodeReader> nodeMap, HashMap<String, HostReader> hostMap)
	{
		super(nffg.getName());
		this.deployTime = nffg.getDeployTime().toGregorianCalendar();
		this.nodeMap = nodeMap;
		
		// Load nodes
		nodes = new HashSet<NodeReader>();
		
		for (NodeType node: nffg.getNode())
		{
			NodeReader newNode_r = new MyNodeReader(node, nffg.getName(), vnfMap, nffgMap, nodeMap, hostMap);
			this.nodeMap.put(node.getName(), newNode_r);
			nodes.add(newNode_r);
		}
	}
	
	@Override
	public Calendar getDeployTime()
	{
		return deployTime;
	}
	
	@Override
	public NodeReader getNode(String arg0)
	{
		if(arg0 == null)
		{
			// Argument is null, don't even scan nodeMap
			return null;
		}
		
		return nodeMap.get(arg0);
	}
	
	@Override
	public Set<NodeReader> getNodes()
	{
		return nodes;
	}

}
