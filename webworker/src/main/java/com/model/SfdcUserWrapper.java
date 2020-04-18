package com.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document(collection = "SfdcUserWrapper")
public class SfdcUserWrapper implements Serializable {
}
