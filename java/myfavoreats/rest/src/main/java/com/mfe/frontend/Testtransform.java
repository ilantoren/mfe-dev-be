package com.mfe.frontend;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;



public class Testtransform {
	
	public static final String LINE = "line.json";
	
	public void run() throws FileNotFoundException, IOException, XMLStreamException, FactoryConfigurationError, TransformerException {
		System.out.println( "Start");
		String xsl = "display-recipe-pair.xsl";
//		ObjectMapper mapper = new ObjectMapper();
//		XmlMapper xmap = new XmlMapper();
//		Line line = mapper.readValue( new FileReader( LINE ), Line.class);
//		String xml = xmap.writeValueAsString(line );
//		System.out.println(  xml );
		
		TransformerFactory factory = SAXTransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new FileInputStream(xsl));
        FileInputStream reader2 = new FileInputStream( "pojo.xml" );
        StreamResult result = new StreamResult(new FileWriter( "result.xml"));
        Transformer transformer;
        transformer = factory.newTransformer(new StreamSource( new FileInputStream(xsl)));
        transformer.transform(new StreamSource(reader2), result);
        
	}
	

}
