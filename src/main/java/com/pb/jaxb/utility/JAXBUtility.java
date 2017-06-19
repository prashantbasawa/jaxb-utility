/**
 * 
 */
package com.pb.jaxb.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * @author Prashant Basawa
 *
 */
public class JAXBUtility {
	//
	private static final String JAXB_NAMESPACE_PREFIX_MAPPER = "com.sun.xml.bind.namespacePrefixMapper";
	private static final String JAXB_CHARACTER_ESCAPE_HANDLER = "com.sun.xml.bind.characterEscapeHandler";

	public static String marshalObject(Object jaxbObject, URL schemaFileUrl, ValidationEventHandler errorCollector, NamespacePrefixMapper namespacePrefixMapper) {
		String payloadXml = null;
		
		try {
			Marshaller marshaller = JAXBContext.newInstance(getPayloadClass(jaxbObject)).createMarshaller();
			
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			
			if(namespacePrefixMapper != null) {
				marshaller.setProperty(JAXB_NAMESPACE_PREFIX_MAPPER, namespacePrefixMapper);
			}
			
			marshaller.setProperty(JAXB_CHARACTER_ESCAPE_HANDLER, characterEscapeHandler);
			
			if(schemaFileUrl != null) {
				marshaller.setSchema(newSchema(schemaFileUrl));
				
				if(errorCollector != null) {
					marshaller.setEventHandler(errorCollector);
				}
			}
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			
			marshaller.marshal(jaxbObject, stream);
			
			payloadXml = stream.toString("UTF-8");
			
		} catch (Throwable t) {
			throw new RuntimeException("Couldn't marshal the object", t);
		}
		
		return payloadXml;
	}
	
	public static String marshalObject(Object jaxbObject, URL schemaFileUrl) {
		return marshalObject(jaxbObject, schemaFileUrl, null, null);
	}
	
	public static String marshalObject(Object jaxbObject, URL schemaFileUrl, ValidationEventHandler errorCollector) {
		return marshalObject(jaxbObject, schemaFileUrl, errorCollector, null);
	}
	
	public static String marshalObjectWithoutValidation(Object jaxbObject, NamespacePrefixMapper namespacePrefixMapper) {
		return marshalObject(jaxbObject, null, null, namespacePrefixMapper);
	}
	
	public static String marshalObjectWithoutValidation(Object jaxbObject) {
		return marshalObject(jaxbObject, null, null, null);
	}

	@SuppressWarnings("rawtypes")
	private static Class<?> getPayloadClass(Object payload) {
		if(payload instanceof JAXBElement) {
			return ((JAXBElement) payload).getDeclaredType();
		}
		
		return payload.getClass();
	}
	
	private static Schema newSchema(URL schemaFileUrl) throws SAXException {
		return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaFileUrl);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T unmarshal(String xmlContent, Class<T> classObj, URL schemaFileUrl, ValidationEventHandler errorCollector) {
		T targetObj = null;
		
		try {
			Unmarshaller unmarshaller = JAXBContext.newInstance(classObj.getPackage().getName()).createUnmarshaller();
			
			if(schemaFileUrl != null) {
				unmarshaller.setSchema(newSchema(schemaFileUrl));
				
				if(errorCollector != null) {
					unmarshaller.setEventHandler(errorCollector);
				}
			}
			
			Object object = unmarshaller.unmarshal(new ByteArrayInputStream(xmlContent.getBytes()));
			
			if(object instanceof JAXBElement) {
				targetObj = ((JAXBElement<T>) object).getValue();
			} else {
				targetObj = classObj.cast(object);
			}
		} catch (Throwable t) {
			throw new RuntimeException("Couldn't unmarshal the XML", t);
		}
		
		return targetObj;
	}
	
	public static <T> T unmarshal(String xmlContent, Class<T> classObj, URL schemaFileUrl) {
		return unmarshal(xmlContent, classObj, schemaFileUrl, null);
	}
	
	public static <T> T unmarshalWithoutValidation(String xmlContent, Class<T> classObj) {
		return unmarshal(xmlContent, classObj, null, null);
	}
	
	public static Collection<String> validatePayloadAgainstSchemaWithoutMarshalling(final Object jaxbObject, final URL schemaFileUrl) {
		final Collection<String> errors = new ArrayList<>();
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(getPayloadClass(jaxbObject));
			JAXBSource source = new JAXBSource(jaxbContext, jaxbObject);
			
			Validator validator = newSchema(schemaFileUrl).newValidator();
			validator.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException exception) throws SAXException {
					//Not interested in warnings
				}
				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
					errors.add(exception.getMessage());
				}
				@Override
				public void error(SAXParseException exception) throws SAXException {
					errors.add(exception.getMessage());
				}
			});			
			validator.validate(source);
			
		} catch(Throwable t) {
			throw new RuntimeException("Couldn't validate the payload", t);
		}
		
		return errors;
	}
	
	private static CharacterEscapeHandler characterEscapeHandler = new CharacterEscapeHandler() {		
		@Override
		public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
			int limit = start + length;
	        
			for (int i = start; i < limit; i++) {
	            switch (ch[i]) {
	            case '&':
	                out.write("&amp;");
	                break;
	            case '<':
	                out.write("&lt;");
	                break;
	            case '>':
	                out.write("&gt;");
	                break;
	            case '\'':
	                out.write("&apos;");
	                break;
	            case '\"':
	                if (isAttVal) {
	                    out.write("&quot;");
	                } else {
	                    out.write('\"');
	                }
	                break;
	            default:
	            	if(ch[i] > 127) {
	            		out.write("&#");
	                    out.write(Integer.toString(ch[i]));
	                    out.write(';');
	            	} else {
	            		out.write(ch[i]);	            		
	            	}	                
	            }
	        }
		}
	};
}
