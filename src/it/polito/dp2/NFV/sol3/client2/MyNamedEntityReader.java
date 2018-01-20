package it.polito.dp2.NFV.sol3.client2;

import it.polito.dp2.NFV.NamedEntityReader;

public class MyNamedEntityReader implements NamedEntityReader
{
	private String name;
	
	// Class constructor
	public MyNamedEntityReader(String name)
	{
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

}
