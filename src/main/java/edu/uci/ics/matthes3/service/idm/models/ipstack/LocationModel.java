package edu.uci.ics.matthes3.service.idm.models.ipstack;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class LocationModel {

    private Integer geoname_id;
    private String capital;
    private LanguagesModel languages;
    private URI country_flag;
    private String country_flag_emoji;
    private String country_flag_emoji_unicode;
    private String calling_code;
    private Boolean is_eu;

    public LocationModel() {
    }

    public LocationModel(Integer geoname_id, String capital, LanguagesModel languages,
                         URI country_flag, String country_flag_emoji,
                         String country_flag_emoji_unicode, String calling_code,
                         Boolean is_eu) {
        this.geoname_id = geoname_id;
        this.capital = capital;
        this.languages = languages;
        this.country_flag = country_flag;
        this.country_flag_emoji = country_flag_emoji;
        this.country_flag_emoji_unicode = country_flag_emoji_unicode;
        this.calling_code = calling_code;
        this.is_eu = is_eu;
    }

    @JsonProperty(value = "geoname_id")
    public Integer getGeoname_id() {
        return geoname_id;
    }

    @JsonProperty(value = "capital")
    public String getCapital() {
        return capital;
    }

    @JsonProperty(value = "languages")
    public LanguagesModel getLanguages() {
        return languages;
    }

    @JsonProperty(value = "country_flag")
    public URI getCountry_flag() {
        return country_flag;
    }

    @JsonProperty(value = "country_flag_emoji")
    public String getCountry_flag_emoji() {
        return country_flag_emoji;
    }

    @JsonProperty(value = "country_flag_emoji_unicode")
    public String getCountry_flag_emoji_unicode() {
        return country_flag_emoji_unicode;
    }

    @JsonProperty(value = "calling_code")
    public String getCalling_code() {
        return calling_code;
    }

    @JsonProperty(value = "is_eu")
    public Boolean getIs_eu() {
        return is_eu;
    }
}
