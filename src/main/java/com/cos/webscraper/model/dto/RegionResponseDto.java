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
public class RegionResponseDto {

    @JsonProperty("regionId")
    private String regionId;

    @JsonProperty("regionName")
    private String regionName;
}
