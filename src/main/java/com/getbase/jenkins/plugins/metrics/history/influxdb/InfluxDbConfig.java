package com.getbase.jenkins.plugins.metrics.history.influxdb;

import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class InfluxDbConfig extends GlobalConfiguration {
    public static final String PROTOCOL_ERROR_MESSAGE = "Only http and https protocols are supported";

    private String description;
    private String url;
    private String username;
    private String password;
    private String database;
    private String retentionPolicy;

    public InfluxDbConfig(){
        load();
    }

    public static InfluxDbConfig get() {
        return GlobalConfiguration.all().get(InfluxDbConfig.class);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getRetentionPolicy() {
        return retentionPolicy;
    }

    public void setRetentionPolicy(String retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
    }

    @Override
    public boolean configure(StaplerRequest request, JSONObject json) throws FormException {
        request.bindJSON(this, json);
        save();
        return true;
    }
    @Override
    public String toString() {
        return "[url=" + this.url + ", description=" + this.description + ", username=" + this.username
                + ", password=*****, database=" + this.database + "]";
    }

    public FormValidation doCheckUrl(@QueryParameter("url") final String url) {
        if (StringUtils.isBlank(url)) {
            return FormValidation.error("Provide valid InfluxDB URL. " + "For ex: \"http://localhost:8086\"");
        }
        if (validateProtocolUsed(url))
            return FormValidation.error(PROTOCOL_ERROR_MESSAGE);
        return FormValidation.ok();
    }

    private boolean validateProtocolUsed(String url) {
        return !(url.startsWith("http://") || url.startsWith("https://"));
    }
}
