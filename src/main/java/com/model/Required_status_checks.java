package com.model;

import java.io.Serializable;

public class Required_status_checks  implements Serializable {
    private String enforcement_level;

    private String[] contexts;

    public String getEnforcement_level() {
        return enforcement_level;
    }

    public void setEnforcement_level(String enforcement_level) {
        this.enforcement_level = enforcement_level;
    }

    public String[] getContexts() {
        return contexts;
    }

    public void setContexts(String[] contexts) {
        this.contexts = contexts;
    }

}
