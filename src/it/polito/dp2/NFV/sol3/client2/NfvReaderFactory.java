package it.polito.dp2.NFV.sol3.client2;

import java.io.File;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import it.polito.dp2.NFV.NfvReader;
import it.polito.dp2.NFV.NfvReaderException;
import it.polito.dp2.NFV.sol3.jaxb.NfvType;

public class NfvReaderFactory extends it.polito.dp2.NFV.NfvReaderFactory {

	@SuppressWarnings("unchecked")
	@Override
	public NfvReader newNfvReader() throws NfvReaderException
	{
		// Create NfvType object that will contain unmarshalled data
		NfvType nfv;
		
		// Read system property containing the name of xml file
		String fileName = System.getProperty("it.polito.dp2.NFV.sol1.NfvInfo.file");
		
		// Check if System Property has been read correctly
		if (fileName == null)
		{
			throw new NfvReaderException("System property \"it.polito.dp2.NFV.sol1.NfvInfo.file\" not found");
		}
		
		try {
			// Initialize JAXBContext and create unmarshaller
			JAXBContext jc = JAXBContext.newInstance("it.polito.dp2.NFV.sol1.jaxb");
			Unmarshaller u = jc.createUnmarshaller();
			
			// Set validation wrt schema using default validation handler (rises exception with non-valid files)
			String xsdPath = "xsd" + File.separator + "nfvInfo.xsd";
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(new File(xsdPath));
			u.setSchema(schema);
			
			// Unmarshal xml file named fileName
			JAXBElement<NfvType> jaxbNfv = (JAXBElement<NfvType>) u.unmarshal( new File(fileName) );
			nfv = jaxbNfv.getValue();
		}
		catch (UnmarshalException ue) {
			throw new NfvReaderException(ue, "Caught UnmarshalException");
		}
		catch (JAXBException je) {
			throw new NfvReaderException(je, "Error while unmarshalling or marshalling");
		}
		catch (SAXException se) {
			throw new NfvReaderException(se, "Unable to validate file or schema");
		}
		catch (Exception e) {
			throw new NfvReaderException(e, "Unexpected exception");
		}
		
		return new MyNfvReader(nfv);
	}
}
