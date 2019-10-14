package edu.uci.ics.matthes3.service.idm.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = { "valid" })
public interface Validate {
    boolean isValid();
}
