package org.jenkinsci.plugins.nuget.publishers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.*;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.jenkinsci.plugins.nuget.Messages;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;
import org.jenkinsci.plugins.nuget.NugetPublication;
import org.jenkinsci.plugins.nuget.Utils.Validations;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Arnaud TAMAILLON
 */
public class NugetPublisher extends Recorder {

    private static final Logger logger = Logger.getLogger(NugetPublisher.class.getName());

    private String name;
    private String packagesPattern;
    private String nugetPublicationName;
    private String packagesExclusionPattern;

    @DataBoundConstructor
    public NugetPublisher(String name, String packagesPattern, String nugetPublicationName, String packagesExclusionPattern) {
        this.name = name;
        this.packagesPattern = packagesPattern;
        this.nugetPublicationName = nugetPublicationName;
        this.packagesExclusionPattern = packagesExclusionPattern;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        listener.getLogger().format("Starting %s publication%n", name);
        FilePath workspaceRoot = getWorkspace(build);
        NugetGlobalConfiguration configuration = GlobalConfiguration.all().get(NugetGlobalConfiguration.class);
        NugetPublication publication = NugetPublication.get(nugetPublicationName);

        String pattern = Util.replaceMacro(packagesPattern, build.getEnvironment(listener));
        String exclusionPattern = Util.replaceMacro(packagesExclusionPattern, build.getEnvironment(listener));
        NugetPublisherCallable callable = new NugetPublisherCallable(pattern, exclusionPattern, listener, configuration, publication);
        List<PublicationResult> results = workspaceRoot.act(callable);
        if (results.size() > 0) {
            build.addAction(new NugetPublisherRunAction(name, results));
        }
        listener.getLogger().format("Ended %s publication%n", name);
        checkErrors(results);
        return true;
    }

    private void checkErrors(List<PublicationResult> results) throws AbortException {
        for(PublicationResult result : results) {
            if (!result.isSuccess()) {
                throw new AbortException("There were errors while publishing packages to NuGet.");
            }
        }
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "We shoud have.")
    private FilePath getWorkspace(AbstractBuild<?, ?> build) {
        return build.getWorkspace();
    }

    public String getName() {
        return name;
    }

    public String getPackagesPattern() {
        return packagesPattern;
    }

    public String getPackagesExclusionPattern() {
        return packagesExclusionPattern;
    }

    public String getNugetPublicationName() {
        return nugetPublicationName;
    }

    @Extension
    public static final class NugetPublisherDescriptor extends BuildStepDescriptor<Publisher> {

        public NugetPublisherDescriptor() {
            super(NugetPublisher.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.NugetPublisher_DisplayName();
        }

        public List<NugetPublication> getPublications() {
            return NugetPublication.all();
        }

        public FormValidation doCheckName(@QueryParameter String value) {
            return Validations.mandatory(value);
        }

        public FormValidation doCheckPackagesPattern(@QueryParameter String value) {
            return Validations.mandatory(value);
        }

        public FormValidation doCheckNugetPublicationName(@QueryParameter String value) {
            return Validations.mandatory(value);
        }
    }
}
