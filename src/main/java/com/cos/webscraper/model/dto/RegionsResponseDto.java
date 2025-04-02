package com.cos.webscraper.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegionsResponseDto {

    private int status;
    private int timeMs;

    @JsonProperty("value")  // Ensure Jackson maps the "value" key to this field
    private List<Regions> regions;
    @JsonProperty("message")
    private String message;
}
