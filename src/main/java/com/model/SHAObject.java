package com.model;

public class SHAObject {
    private String ref;

    private String url;

    private String node_id;

    private Object object;

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNode_id() {
        return node_id;
    }

    public void setNode_id(String node_id) {
        this.node_id = node_id;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return "ClassPojo [ref = " + ref + ", url = " + url + ", node_id = " + node_id + ", object = " + object + "]";
    }
}
