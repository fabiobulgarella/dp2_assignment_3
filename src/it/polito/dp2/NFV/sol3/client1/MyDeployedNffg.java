package it.polito.dp2.NFV.sol3.client1;

import javax.ws.rs.client.WebTarget;

import it.polito.dp2.NFV.LinkReader;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.VNFTypeReader;
import it.polito.dp2.NFV.lab3.AllocationException;
import it.polito.dp2.NFV.lab3.DeployedNffg;
import it.polito.dp2.NFV.lab3.LinkAlreadyPresentException;
import it.polito.dp2.NFV.lab3.NoNodeException;
import it.polito.dp2.NFV.lab3.ServiceException;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;

public class MyDeployedNffg implements DeployedNffg
{
	private WebTarget target;
	private NffgType nffg;
	private String nffgName;
	
	// Class constructor
	public MyDeployedNffg(WebTarget target, NffgType nffg)
	{
		this.target = target;
		this.nffg = nffg;
		this.nffgName = nffg.getName();
	}

	@Override
	public NodeReader addNode(VNFTypeReader type, String hostName) throws AllocationException, ServiceException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkReader addLink(NodeReader source, NodeReader dest, boolean overwrite) throws NoNodeException, LinkAlreadyPresentException, ServiceException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NffgReader getReader() throws ServiceException
	{
		// TODO Auto-generated method stub
		return null;
	}

}
