package it.polito.dp2.NFV.sol3.service;

public class HostsStatus
{
	private String name;
    public int maxVnfs;
    public int vnfs;
    public int memory;
    public int usedMemory;
    public int storage;
    public int usedStorage;
    
    // Class constructor
    public HostsStatus(String name, int maxVnfs, int memory, int storage)
    {
    	this.name = name;
    	this.maxVnfs = maxVnfs;
    	this.vnfs = 0;
    	this.memory = memory;
    	this.usedMemory = 0;
    	this.storage = storage;
    	this.usedStorage = 0;
    }
    
    // Empty constructor for clone function
    public HostsStatus() {}
    
	public String getName()
	{
	    return name;
	}
	
	public HostsStatus clone()
	{
		HostsStatus copy = new HostsStatus();
		copy.name = this.name;
		copy.maxVnfs = this.maxVnfs;
		copy.vnfs = this.vnfs;
		copy.memory = this.memory;
		copy.usedMemory = this.usedMemory;
		copy.storage = this.storage;
		copy.usedStorage = this.usedStorage;
		
		return copy;
	}
}
