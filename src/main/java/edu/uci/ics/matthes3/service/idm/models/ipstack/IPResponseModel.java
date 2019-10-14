package edu.uci.ics.matthes3.service.idm.models.ipstack;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IPResponseModel {

    private String ip;
    private String hostname;
    private String type;
    private String continent_code;
    private String continent_name;
    private String country_code;
    private String country_name;
    private String region_code;
    private String region_name;
    private String city;
    private String zip;
    private Float latitude;
    private Float longitude;
    private LocationModel location;

    public IPResponseModel() {
    }

    public IPResponseModel(String ip, String hostname, String type, String continent_code,
                           String continent_name, String country_code,
                           String country_name, String region_code, String region_name,
                           String city, String zip, Float latitude, Float longitude,
                           LocationModel location) {
        this.ip = ip;
        this.hostname = hostname;
        this.type = type;
        this.continent_code = continent_code;
        this.continent_name = continent_name;
        this.country_code = country_code;
        this.country_name = country_name;
        this.region_code = region_code;
        this.region_name = region_name;
        this.city = city;
        this.zip = zip;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
    }

    @JsonProperty(value = "ip")
    public String getIp() {
        return ip;
    }

    @JsonProperty(value = "hostname")
    public String getHostname() {
        return hostname;
    }

    @JsonProperty(value = "type")
    public String getType() {
        return type;
    }

    @JsonProperty(value = "continent_code")
    public String getContinent_code() {
        return continent_code;
    }

    @JsonProperty(value = "continent_name")
    public String getContinent_name() {
        return continent_name;
    }

    @JsonProperty(value = "country_code")
    public String getCountry_code() {
        return country_code;
    }

    @JsonProperty(value = "country_name")
    public String getCountry_name() {
        return country_name;
    }

    @JsonProperty(value = "region_code")
    public String getRegion_code() {
        return region_code;
    }

    @JsonProperty(value = "region_name")
    public String getRegion_name() {
        return region_name;
    }

    @JsonProperty(value = "city")
    public String getCity() {
        return city;
    }

    @JsonProperty(value = "zip")
    public String getZip() {
        return zip;
    }

    @JsonProperty(value = "latitude")
    public Float getLatitude() {
        return latitude;
    }

    @JsonProperty(value = "longitude")
    public Float getLongitude() {
        return longitude;
    }

    @JsonProperty(value = "location")
    public LocationModel getLocation() {
        return location;
    }
}
