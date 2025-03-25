package com.zzeng.dnscache.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class DnsBatchRequest {

    @NotEmpty(message = "Domain list cannot be empty")
    private List<@NotBlank(message = "Each domain must not be blank") String> domains;

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }
}
