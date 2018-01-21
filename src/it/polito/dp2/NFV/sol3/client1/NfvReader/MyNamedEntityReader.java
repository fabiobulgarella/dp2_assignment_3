package it.polito.dp2.NFV.sol3.client1.NfvReader;

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
