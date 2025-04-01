package com.cos.webscraper.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Regions {

    @JsonProperty("geoType")
    private int geoType;

    @JsonProperty("regionId")
    private String regionId;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("countryId")
    private String countryId;

    @JsonProperty("stateCode")
    private String stateCode;

    @JsonProperty("legacyRegionId")
    private int legacyRegionId;

    @JsonProperty("legacyRegionCode")
    private String legacyRegionCode;

    @JsonProperty("metroAreaId")
    private int metroAreaId;

    @JsonProperty("regionName")
    private String regionName;

    @JsonProperty("regionNameSeo")
    private String regionNameSeo;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("locationDetected")
    private boolean locationDetected;
}
