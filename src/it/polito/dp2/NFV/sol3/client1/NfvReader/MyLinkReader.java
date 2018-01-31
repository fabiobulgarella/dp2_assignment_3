package it.polito.dp2.NFV.sol3.client1.NfvReader;

import java.util.HashMap;

import it.polito.dp2.NFV.LinkReader;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.sol3.jaxb.LinkType;

public class MyLinkReader extends MyNamedEntityReader implements LinkReader
{
	private String srcNodeName;
	private String dstNodeName;
	private int latency;
	private float throughput;
	private HashMap<String, NodeReader> nodeMap;
	
	// Class constructor
	public MyLinkReader(LinkType link, String srcNodeName, HashMap<String, NodeReader> nodeMap)
	{
		super(link.getName());
		this.srcNodeName = srcNodeName;
		this.dstNodeName = link.getDstNode();
		this.latency = link.getMaxLatency();
		this.throughput = link.getMinThroughput();
		this.nodeMap = nodeMap;
	}
	
	@Override
	public NodeReader getSourceNode()
	{
		return nodeMap.get(srcNodeName);
	}
	
	@Override
	public NodeReader getDestinationNode()
	{
		return nodeMap.get(dstNodeName);
	}
	
	@Override
	public int getLatency()
	{
		return latency;
	}
	
	@Override
	public float getThroughput()
	{
		return throughput;
	}

}
