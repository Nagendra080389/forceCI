//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.10.20 at 01:14:19 PM IST 
//


package com.model.configurationJenkins;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for hudson.plugins.git.UserRemoteConfigType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="hudson.plugins.git.UserRemoteConfigType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="refspec" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="credentialsId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hudson.plugins.git.UserRemoteConfigType", propOrder = {
    "refspec",
    "url",
    "credentialsId"
})
public class HudsonPluginsGitUserRemoteConfigType {

    @XmlElement(required = true)
    protected String refspec;
    @XmlElement(required = true)
    protected String url;
    @XmlElement(required = true)
    protected String credentialsId;

    /**
     * Gets the value of the refspec property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefspec() {
        return refspec;
    }

    /**
     * Sets the value of the refspec property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefspec(String value) {
        this.refspec = value;
    }

    /**
     * Gets the value of the url property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrl(String value) {
        this.url = value;
    }

    /**
     * Gets the value of the credentialsId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCredentialsId() {
        return credentialsId;
    }

    /**
     * Sets the value of the credentialsId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCredentialsId(String value) {
        this.credentialsId = value;
    }

}
