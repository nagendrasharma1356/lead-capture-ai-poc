package com.hexawarre.leadpoc.dto;

import jakarta.validation.constraints.*;

public class LeadRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "Phone looks invalid")
    private String phone;

    @Email(message = "Email looks invalid")
    private String email;

    @NotBlank(message = "Zip is required")
    @Size(max = 12)
    private String zip;

    @NotBlank(message = "Property address is required")
    @Size(max = 200)
    private String propertyAddress;

    private String propertyType;
    private String propertyCondition;

    @AssertTrue(message = "Consent is required to submit the form")
    private boolean consent;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }
    public String getPropertyAddress() { return propertyAddress; }
    public void setPropertyAddress(String propertyAddress) { this.propertyAddress = propertyAddress; }
    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }
    public String getPropertyCondition() { return propertyCondition; }
    public void setPropertyCondition(String propertyCondition) { this.propertyCondition = propertyCondition; }
    public boolean isConsent() { return consent; }
    public void setConsent(boolean consent) { this.consent = consent; }
}
