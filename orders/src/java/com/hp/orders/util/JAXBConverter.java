package com.hp.orders.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

public class JAXBConverter {

	@SuppressWarnings("unchecked")
	public static <T> T convert(Class<T> cls, String xml) throws JAXBException {
		T res = null;
		if (cls == String.class) {
			res = (T) xml;
			return res;
		}
		JAXBContext ctx = JAXBContext.newInstance(cls);
		Unmarshaller marshaller = ctx.createUnmarshaller();
		res = (T) marshaller.unmarshal(new StringReader(xml));
		return res;
	}

	@SuppressWarnings("unchecked")
	public static <T> T convert(String xml, Class<? extends T>... cls) throws JAXBException {
		T res = null;
		try {
			JAXBContext ctx = JAXBContext.newInstance(cls);
			Unmarshaller marshaller = ctx.createUnmarshaller();
			res = (T) marshaller.unmarshal(new StringReader(xml));
		}
		catch (JAXBException e) {
			//throw new RestClientException("Failed to convert xml to JAXB " + cls.toString() + " class: " + xml + ".", e);

		}
		return res;
	}

	public static <T> String toXml(Class<T> cls, T resource) {
		try {
			JAXBContext ctx = JAXBContext.newInstance(cls);
			Marshaller marshaller = ctx.createMarshaller();
			StringWriter xml = new StringWriter();
			marshaller.marshal(resource, xml);
			return xml.toString();
		}
		catch (JAXBException e) {
			//throw new RestClientRuntimeException("Failed to convert JAXB class to XML string.", e);
		}
        return null;
	}
}
