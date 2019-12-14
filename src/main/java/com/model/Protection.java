package com.model;

import java.io.Serializable;

public class Protection  implements Serializable {
    private Required_status_checks required_status_checks;

    private String enabled;

    public Required_status_checks getRequired_status_checks() {
        return required_status_checks;
    }

    public void setRequired_status_checks(Required_status_checks required_status_checks) {
        this.required_status_checks = required_status_checks;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "ClassPojo [required_status_checks = " + required_status_checks + ", enabled = " + enabled + "]";
    }
}