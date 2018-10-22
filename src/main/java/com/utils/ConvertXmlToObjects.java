package com.utils;

import com.model.configurationJenkins.ObjectFactory;
import com.model.configurationJenkins.ProjectType;
import org.springframework.stereotype.Component;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class ConvertXmlToObjects {
    public ProjectType convertToObjects(String xmlFile) throws JAXBException, IOException {
        //1. We need to create JAXContext instance
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);

        //2. Use JAXBContext instance to create the Unmarshaller.
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        if (xmlFile != null) {
            InputStream stream = new ByteArrayInputStream(xmlFile.getBytes(StandardCharsets.UTF_8));
            JAXBElement<ProjectType> unmarshalledObject =
                    (JAXBElement<ProjectType>) unmarshaller.unmarshal(stream);
            return unmarshalledObject.getValue();
        }


        //3. Use the Unmarshaller to unmarshal the XML document to get an instance of JAXBElement.

        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("config.xml");
        String ruleSetFilePath = "";

        if (resourceAsStream != null) {
            File file = BuildUtils.stream2file(resourceAsStream);
            ruleSetFilePath = file.getPath();
        }

        InputStream stream = new FileInputStream(ruleSetFilePath);

        JAXBElement<ProjectType> unmarshalledObject =
                (JAXBElement<ProjectType>) unmarshaller.unmarshal(stream);
        return unmarshalledObject.getValue();
    }

    public String convertFromObjects(ProjectType ProjectType) throws JAXBException, IOException {

        String xmlString = "";
        try {
            JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            Marshaller m = context.createMarshaller();

            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // To format XML

            StringWriter sw = new StringWriter();
            JAXBElement<ProjectType> rootElement = new JAXBElement<ProjectType>(new QName("project"), ProjectType.class,
                    null, ProjectType);
            m.marshal(rootElement, sw);
            xmlString = sw.toString();

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return xmlString;
    }
}
