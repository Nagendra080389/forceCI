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
 * <p>Java class for userRemoteConfigsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="userRemoteConfigsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="hudson.plugins.git.UserRemoteConfig" type="{}hudson.plugins.git.UserRemoteConfigType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "userRemoteConfigsType", propOrder = {
    "hudsonPluginsGitUserRemoteConfig"
})
public class UserRemoteConfigsType {

    @XmlElement(name = "hudson.plugins.git.UserRemoteConfig", required = true)
    protected HudsonPluginsGitUserRemoteConfigType hudsonPluginsGitUserRemoteConfig;

    /**
     * Gets the value of the hudsonPluginsGitUserRemoteConfig property.
     * 
     * @return
     *     possible object is
     *     {@link HudsonPluginsGitUserRemoteConfigType }
     *     
     */
    public HudsonPluginsGitUserRemoteConfigType getHudsonPluginsGitUserRemoteConfig() {
        return hudsonPluginsGitUserRemoteConfig;
    }

    /**
     * Sets the value of the hudsonPluginsGitUserRemoteConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link HudsonPluginsGitUserRemoteConfigType }
     *     
     */
    public void setHudsonPluginsGitUserRemoteConfig(HudsonPluginsGitUserRemoteConfigType value) {
        this.hudsonPluginsGitUserRemoteConfig = value;
    }

}
