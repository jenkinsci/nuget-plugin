package org.jenkinsci.plugins.nuget;

import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.XmlFile;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.nuget.utils.Validations;
import org.jenkinsci.plugins.nuget.triggers.NugetTrigger;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Arnaud TAMAILLON
 * @author Dennis Lundberg
 */
@Extension
public class NugetGlobalConfiguration extends GlobalConfiguration implements Serializable {

    private String nugetExe;
    @CopyOnWrite
    private volatile List<NugetPublication> publications = Collections.EMPTY_LIST;
    @CopyOnWrite
    private volatile List<NugetRepository> nugetRepositories = new ArrayList<>();

    public NugetGlobalConfiguration() {
        super();
        load();
    }

    public String getNugetExe() {
        migrate();
        return nugetExe;
    }

    public List<NugetPublication> getPublications() {
        return publications;
    }

    public List<NugetRepository> getNugetRepositories() {
        return nugetRepositories;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) {
        nugetExe = json.getString("nugetExe");
        publications = req.bindJSONToList(NugetPublication.class, json.get("publication"));
        nugetRepositories = req.bindJSONToList(NugetRepository.class, json.get("nugetRepository"));
        save();
        return true;
    }

    public FormValidation doCheckMandatory(@QueryParameter String value) {
        return Validations.mandatory(value);
    }

    private void migrate() {
        synchronized (this) {
            Jenkins jenkins = Jenkins.getInstance();
            if (jenkins != null) {
                NugetTrigger.NugetTriggerDescriptor description = jenkins.getDescriptorByType(NugetTrigger.NugetTriggerDescriptor.class);
                if (description != null) {
                    XmlFile oldFile = description.getConfigFile();
                    if (oldFile.exists()) {
                        nugetExe = description.getNugetExe();
                        save();
                        oldFile.delete();
                    }
                }
            }
        }
    }
}