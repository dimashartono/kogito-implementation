package com.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @NotBlank(message = "Street address is required")
    @JsonProperty("street")
    private String street;

    @NotBlank(message = "City is required")
    @JsonProperty("city")
    private String city;

    @NotBlank(message = "Province is required")
    @JsonProperty("province")
    private String province;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "\\d{5}", message = "Postal code must be 5 digits")
    @JsonProperty("postal_code")
    private String postalCode;

    @NotBlank(message = "Country is required")
    @JsonProperty("country")
    private String country;

    @JsonProperty("additional_info")
    private String additionalInfo;
}
