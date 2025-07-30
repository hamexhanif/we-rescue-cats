package io.werescuecats.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BreedDto {
    
    private String id;
    private String name;
    private String description;
    private String origin;
    
    @JsonProperty("child_friendly")
    private Integer childFriendly;
    
    @JsonProperty("dog_friendly")
    private Integer dogFriendly;
    
    @JsonProperty("energy_level")
    private Integer energyLevel;
    
    @JsonProperty("grooming")
    private Integer grooming;
    
    @JsonProperty("health_issues")
    private Integer healthIssues;
    
    @JsonProperty("intelligence")
    private Integer intelligence;
    
    @JsonProperty("social_needs")
    private Integer socialNeeds;
    
    @JsonProperty("stranger_friendly")
    private Integer strangerFriendly;
    
    @JsonProperty("adaptability")
    private Integer adaptability;
    
    @JsonProperty("affection_level")
    private Integer affectionLevel;
    
    @JsonProperty("wikipedia_url")
    private String wikipediaUrl;
    
    @JsonProperty("reference_image_id")
    private String referenceImageId;
    
    private BreedImageDto image;
    
    @Data
    public static class BreedImageDto {
        private String id;
        private String url;
        private Integer width;
        private Integer height;
    }
}
