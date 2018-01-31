package it.polito.dp2.NFV.sol3.client1.NfvReader;

import it.polito.dp2.NFV.ConnectionPerformanceReader;

public class MyConnectionPerformanceReader implements ConnectionPerformanceReader
{
	private int latency;
	private float throughput;
	
	// Class constructor
	public MyConnectionPerformanceReader(int latency, float throughput)
	{
		this.latency = latency;
		this.throughput = throughput;
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
