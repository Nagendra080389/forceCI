
package com.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.model package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Package_QNAME = new QName("http://soap.sforce.com/2006/04/metadata", "Package");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PackageType }
     * 
     */
    public PackageType createPackageType() {
        return new PackageType();
    }

    /**
     * Create an instance of {@link TypesType }
     * 
     */
    public TypesType createTypesType() {
        return new TypesType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PackageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.sforce.com/2006/04/metadata", name = "Package")
    public JAXBElement<PackageType> createPackage(PackageType value) {
        return new JAXBElement<PackageType>(_Package_QNAME, PackageType.class, null, value);
    }

}
