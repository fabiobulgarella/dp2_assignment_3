package it.polito.dp2.NFV.sol3.client2;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import it.polito.dp2.NFV.NfvReader;
import it.polito.dp2.NFV.NfvReaderException;
import it.polito.dp2.NFV.sol3.jaxb.CatalogType;
import it.polito.dp2.NFV.sol3.jaxb.ConnectionsType;
import it.polito.dp2.NFV.sol3.jaxb.HostType;
import it.polito.dp2.NFV.sol3.jaxb.HostsType;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;
import it.polito.dp2.NFV.sol3.jaxb.NffgsType;
import it.polito.dp2.NFV.sol3.jaxb.NfvType;
import it.polito.dp2.NFV.sol3.jaxb.ObjectFactory;

public class NfvReaderFactory extends it.polito.dp2.NFV.NfvReaderFactory
{
	private WebTarget target;
	private ObjectFactory objFactory;

	@Override
	public NfvReader newNfvReader() throws NfvReaderException
	{
		// Read Web Service URL
		String serviceURL = System.getProperty("it.polito.dp2.NFV.lab3.URL");
		if (serviceURL == null)
			serviceURL = "http://localhost:8080/NfvDeployer/rest/";
		
		// Create JAX-RS Client and WebTarget
		Client client = ClientBuilder.newClient();
		try {
			target = client.target(serviceURL);
		}
		catch (IllegalArgumentException iae) {
			throw new NfvReaderException(iae, "Url is not a valid URI");
		}
		
		// Instantiate ObjectFactory
		objFactory = new ObjectFactory();
		
		// Create NfvType object that will contain unmarshalled data
		NfvType nfv = objFactory.createNfvType();
		
		// Build NFV methods
		nfv.setCatalog( getCatalog() );
		nfv.setNffgs( getNffgs() );
		nfv.setHosts( getHosts() );
		nfv.setConnections( getConnections() );
		
		return new MyNfvReader(nfv);
	}

	private CatalogType getCatalog() throws NfvReaderException
	{
		// Call NfvDeployer REST Web Service
		CatalogType catalog;
		
		try {
			catalog = target.path("catalog")
					     .request()
					     .accept(MediaType.APPLICATION_XML)
					     .get(CatalogType.class);
		}
		catch (ProcessingException pe) {
			throw new NfvReaderException("Error during JAX-RS request processing");
		}
		catch (WebApplicationException wae) {
			throw new NfvReaderException("Server returned error");
		}
		catch (Exception e) {
			throw new NfvReaderException("Unexpected exception");
		}
		
		return catalog;
	}
	
	private NffgsType getNffgs() throws NfvReaderException
	{
		// Call NfvDeployer REST Web Service
		NffgsType nffgs;
		
		try {
			nffgs = target.path("nffgs")
					      .request()
					      .accept(MediaType.APPLICATION_XML)
					      .get(NffgsType.class);
		}
		catch (ProcessingException pe) {
			throw new NfvReaderException("Error during JAX-RS request processing");
		}
		catch (WebApplicationException wae) {
			throw new NfvReaderException("Server returned error");
		}
		catch (Exception e) {
			throw new NfvReaderException("Unexpected exception");
		}
		
		// Create a complete host set (including nodeRefs)
		NffgsType newNffgs = objFactory.createNffgsType();
		
		for (NffgType nffg: nffgs.getNffg())
		{
			NffgType newNffg = getNffg( nffg.getName() );
			newNffgs.getNffg().add(newNffg);
		}
		
		return newNffgs;
	}
	
	private NffgType getNffg(String nffgName) throws NfvReaderException
	{
		// Call NfvDeployer REST Web Service
		NffgType nffg;
		
		try {
			nffg = target.path("nffgs/" + nffgName)
					     .request()
					     .accept(MediaType.APPLICATION_XML)
					     .get(NffgType.class);
		}
		catch (ProcessingException pe) {
			throw new NfvReaderException("Error during JAX-RS request processing");
		}
		catch (WebApplicationException wae) {
			throw new NfvReaderException("Server returned error");
		}
		catch (Exception e) {
			throw new NfvReaderException("Unexpected exception");
		}
		
		return nffg;
	}
	
	private HostsType getHosts() throws NfvReaderException
	{
		// Call NfvDeployer REST Web Service
		HostsType hosts;
		
		try {
			hosts = target.path("hosts")
					     .request()
					     .accept(MediaType.APPLICATION_XML)
					     .get(HostsType.class);
		}
		catch (ProcessingException pe) {
			throw new NfvReaderException("Error during JAX-RS request processing");
		}
		catch (WebApplicationException wae) {
			throw new NfvReaderException("Server returned error");
		}
		catch (Exception e) {
			throw new NfvReaderException("Unexpected exception");
		}
		
		// Create a complete host set (including nodeRefs)
		HostsType newHosts = objFactory.createHostsType();
		
		for (HostType host: hosts.getHost())
		{
			HostType newHost = getHost( host.getName() );
			newHosts.getHost().add(newHost);
		}
		
		return newHosts;
	}
	
	private HostType getHost(String hostName) throws NfvReaderException
	{
		// Call NfvDeployer REST Web Service
		HostType host;
		
		try {
			host = target.path("hosts/" + hostName)
					     .request()
					     .accept(MediaType.APPLICATION_XML)
					     .get(HostType.class);
		}
		catch (ProcessingException pe) {
			throw new NfvReaderException("Error during JAX-RS request processing");
		}
		catch (WebApplicationException wae) {
			throw new NfvReaderException("Server returned error");
		}
		catch (Exception e) {
			throw new NfvReaderException("Unexpected exception");
		}
		
		return host;
	}
	
	private ConnectionsType getConnections() throws NfvReaderException
	{
		// Call NfvDeployer REST Web Service
		ConnectionsType connections;
		
		try {
			connections = target.path("connections")
					            .request()
					            .accept(MediaType.APPLICATION_XML)
					            .get(ConnectionsType.class);
		}
		catch (ProcessingException pe) {
			throw new NfvReaderException("Error during JAX-RS request processing");
		}
		catch (WebApplicationException wae) {
			throw new NfvReaderException("Server returned error");
		}
		catch (Exception e) {
			throw new NfvReaderException("Unexpected exception");
		}
		
		return connections;
	}
	
}
