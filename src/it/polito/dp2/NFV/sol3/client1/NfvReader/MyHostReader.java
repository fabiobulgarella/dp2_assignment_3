package it.polito.dp2.NFV.sol3.client1.NfvReader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.sol3.jaxb.HostType;
import it.polito.dp2.NFV.sol3.jaxb.NodeRefType;

public class MyHostReader extends MyNamedEntityReader implements HostReader
{
	private int availableMemory;
	private int availableStorage;
	private int maxVNFs;
	private List<NodeRefType> nodeRefList;
	private HashMap<String, NodeReader> nodeMap;
	
	// Class constructor
	public MyHostReader(HostType host, HashMap<String, NodeReader> nodeMap)
	{
		super(host.getName());
		this.availableMemory = host.getMemory();
		this.availableStorage = host.getStorage();
		this.maxVNFs = host.getMaxVnfs();
		this.nodeRefList = host.getNodeRef();
		this.nodeMap = nodeMap;
	}
	
	@Override
	public int getAvailableMemory()
	{
		return availableMemory;
	}
	
	@Override
	public int getAvailableStorage()
	{
		return availableStorage;
	}
	
	@Override
	public int getMaxVNFs()
	{
		return maxVNFs;
	}
	
	@Override
	public Set<NodeReader> getNodes()
	{
		Set<NodeReader> set = new HashSet<NodeReader>();
		
		for (NodeRefType nodeRef: nodeRefList)
		{
			NodeReader newNode_r = nodeMap.get( nodeRef.getName() );
			set.add(newNode_r);
		}
		
		return set;
	}

}
