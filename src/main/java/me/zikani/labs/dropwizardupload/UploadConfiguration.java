package me.zikani.labs.dropwizardupload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

public class UploadConfiguration extends Configuration {

    @NotNull
    @JsonProperty
    private String uploadsDir;

    public String getUploadsDir() {
        return uploadsDir;
    }
}
