package it.polito.dp2.NFV.sol3.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

@Provider
@Consumes(MediaType.APPLICATION_XML)
public class NfvDeployerValidationProvider implements MessageBodyReader<JAXBElement<?>>
{
	// JAXB Package, as specified on sol_build.xml
	final String jaxbPackage = "it.polito.dp2.NFV.sol3.jaxb";
	
	Unmarshaller unmarshaller;
	Logger logger;
	
	// Class constructor
	public NfvDeployerValidationProvider()
	{	
		logger = Logger.getLogger(NfvDeployerValidationProvider.class.getName());

		try {			
			// Initialize JAXBContext and create unmarshaller
			JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
			unmarshaller = jc.createUnmarshaller();
			
			// Set validation schema using default validation handler
			String xsdPath = "xsd" + File.separator + "NfvDeployer.xsd";
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(new File(xsdPath));
			unmarshaller.setSchema(schema);
			
			logger.log(Level.INFO, "NfvDeployerValidationProvider initialized successfully");
		}
		catch (SAXException | JAXBException e) {
			logger.log(Level.SEVERE, "Error parsing xml schema file. Service will not work properly.", e);
		}
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
	{
		return JAXBElement.class.equals(type) || jaxbPackage.equals(type.getPackage().getName());
	}
	
	@Override
	public JAXBElement<?> readFrom(Class<JAXBElement<?>> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
	{
		try {
			return (JAXBElement<?>) unmarshaller.unmarshal(entityStream);
		}
		catch (JAXBException je) {
			logger.log(Level.WARNING, "Request body validation error.", je);
			throw new BadRequestException();
		}
	}

}
