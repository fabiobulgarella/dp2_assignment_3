package it.polito.dp2.NFV.sol3.service;

import java.util.Map;
import javax.xml.bind.JAXBElement;

import it.polito.dp2.NFV.sol3.jaxb.CatalogType;
import it.polito.dp2.NFV.sol3.jaxb.ConnectionType;
import it.polito.dp2.NFV.sol3.jaxb.HostType;
import it.polito.dp2.NFV.sol3.jaxb.HostsType;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;
import it.polito.dp2.NFV.sol3.jaxb.NffgsType;
import it.polito.dp2.NFV.sol3.jaxb.ObjectFactory;
import it.polito.dp2.NFV.sol3.jaxb.VnfType;

public class NfvDeployerService
{
	private static NfvDeployerService instance = null;
	private ObjectFactory objFactory;
	
	// DB -> concurrent maps
	private Map<String, VnfType> vnfMap = NfvDeployerDB.getVnfMap();
	private Map<String, NffgType> nffgMap = NfvDeployerDB.getNffgMap();
	private Map<String, HostType> hostMap = NfvDeployerDB.getHostMap();
	private Map<String, ConnectionType> connectionMap = NfvDeployerDB.getConnectionMap();
	
	// Class constructor
	private NfvDeployerService()
	{
		// Instantiate ObjectFactory
		objFactory = new ObjectFactory();
		
		// Initialize NfvDeployer
		NfvDeployerInit.bootstrap();
	}
	
	// Singleton instance method
	public static NfvDeployerService getInstance()
	{
		if (instance == null)
		{
			instance = new NfvDeployerService();
		}
		
		return instance;
	}
	
	/*
	 * CATALOG METHODS
	 */
	public JAXBElement<CatalogType> getCatalog()
	{
		CatalogType catalog = objFactory.createCatalogType();
		
		for (VnfType vnf: vnfMap.values())
		{
			catalog.getVnf().add(vnf);
		}
		
		return objFactory.createCatalog(catalog);
	}
	
	/*
	 * NFFGS METHODS
	 */
	public JAXBElement<NffgsType> getNffgs()
	{
		NffgsType nffgs = objFactory.createNffgsType();
		
		for (NffgType nffg: nffgMap.values())
		{
			nffgs.getNffg().add(nffg);
		}
		
		return objFactory.createNffgs(nffgs);
	}
	
	public JAXBElement<NffgType> getNffg(String id)
	{
		NffgType nffg = nffgMap.get(id);
		
		if (nffg != null)
			return objFactory.createNffg(nffg);
		
		return null;
	}
	
	/*
	 * HOSTS METHODS
	 */
	public JAXBElement<HostsType> getHosts()
	{
		HostsType hosts = objFactory.createHostsType();
		
		for (HostType host: hostMap.values())
		{
			hosts.getHost().add(host);
		}
		
		return objFactory.createHosts(hosts);
	}

	public JAXBElement<HostType> getHost(String id)
	{
		HostType host = hostMap.get(id);
		
		if (host != null)
			return objFactory.createHost(host);
		
		return null;
	}
	
	/*
	 * CONNECTIONS METHODS
	 */
	public JAXBElement<ConnectionType> getConnection(String host1, String host2)
	{
		ConnectionType connection = connectionMap.get(host1 + host2);
		if (connection != null)
			return objFactory.createConnection(connection);
		
		return null;
	}
	
}
