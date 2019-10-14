package edu.uci.ics.matthes3.service.idm.models.ipstack;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LanguagesModel {
    private String code;
    private String name;
    private String _native;

    public LanguagesModel() {
    }

    public LanguagesModel(String code, String name, String _native) {
        this.code = code;
        this.name = name;
        this._native = _native;
    }

    @JsonProperty(value = "code")
    public String getCode() {
        return code;
    }

    @JsonProperty(value = "name")
    public String getName() {
        return name;
    }

    @JsonProperty(value = "native")
    public String get_native() {
        return _native;
    }
}
