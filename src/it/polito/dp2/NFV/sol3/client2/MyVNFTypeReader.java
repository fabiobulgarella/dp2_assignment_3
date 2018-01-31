package it.polito.dp2.NFV.sol3.client2;

import it.polito.dp2.NFV.FunctionalType;
import it.polito.dp2.NFV.VNFTypeReader;
import it.polito.dp2.NFV.sol3.jaxb.VnfType;

public class MyVNFTypeReader extends MyNamedEntityReader implements VNFTypeReader
{
	private FunctionalType functionalType;
	private int requiredMemory;
	private int requiredStorage;
	
	// Class constructor
	public MyVNFTypeReader(VnfType vnf)
	{
		super(vnf.getName());
		this.functionalType = FunctionalType.fromValue( vnf.getFunctionalType() );
		this.requiredMemory = vnf.getReqMemory();
		this.requiredStorage = vnf.getReqStorage();
	}
	
	@Override
	public FunctionalType getFunctionalType()
	{
		return functionalType;
	}
	
	@Override
	public int getRequiredMemory()
	{
		return requiredMemory;
	}
	
	@Override
	public int getRequiredStorage()
	{
		return requiredStorage;
	}

}
