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
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import org.jenkinsci.plugins.nuget.Messages;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;
import org.jenkinsci.plugins.nuget.NugetPublication;
import org.jenkinsci.plugins.nuget.utils.Validations;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Arnaud TAMAILLON
 */
public class NugetPublisher extends Recorder {

    protected static final Logger logger = Logger.getLogger(NugetPublisher.class.getName());

    protected String name;
    protected String packagesPattern;
    protected String publishPath;
    protected String nugetPublicationName;
    protected String packagesExclusionPattern;
    protected boolean doNotFailIfNoPackagesArePublished;
    protected String nugetVerbosity;

    @DataBoundConstructor
    public NugetPublisher(String name, String packagesPattern, String publishPath, String nugetPublicationName, String packagesExclusionPattern, boolean doNotFailIfNoPackagesArePublished, String nugetVerbosity) {
        this.name = name;
        this.packagesPattern = packagesPattern;
        this.publishPath = StringUtils.trim(publishPath);
        this.nugetPublicationName = nugetPublicationName;
        this.packagesExclusionPattern = packagesExclusionPattern;
        this.doNotFailIfNoPackagesArePublished = doNotFailIfNoPackagesArePublished;
        this.nugetVerbosity = nugetVerbosity;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
       
        //expand parameters 
        String expandedName = Util.replaceMacro(name, build.getEnvironment(listener));
        String pattern = Util.replaceMacro(packagesPattern, build.getEnvironment(listener));
        String exclusionPattern = Util.replaceMacro(packagesExclusionPattern, build.getEnvironment(listener));
        String expandedPublishPath = Util.replaceMacro(publishPath, build.getEnvironment(listener));
        
        listener.getLogger().format("Starting %s publication%n", expandedName);
        NugetGlobalConfiguration configuration = GlobalConfiguration.all().get(NugetGlobalConfiguration.class);
        NugetPublication publication = NugetPublication.get(nugetPublicationName);
        NugetPublisherCallable callable = new NugetPublisherCallable(pattern, exclusionPattern, listener, configuration, expandedPublishPath, publication, nugetVerbosity);

        FilePath filesRoot = this.getFilesRoot(build);

        List<PublicationResult> results = filesRoot.act(callable);
        if (results.size() > 0) {
            build.addAction(new NugetPublisherRunAction(expandedName, results));
        }
        else {
            listener.getLogger().println("No packages were found to publish.");
        }
        listener.getLogger().format("Ended %s publication%n", expandedName);
        checkErrors(results);
        return true;
    }

    protected FilePath getFilesRoot(AbstractBuild<?, ?> build) {
        return getWorkspace(build);
    }

    private void checkErrors(List<PublicationResult> results) throws AbortException {
        if (results.isEmpty() && !doNotFailIfNoPackagesArePublished) {
            throw new AbortException("No packages were published.");
        }
        for(PublicationResult result : results) {
            if (!result.isSuccess()) {
                throw new AbortException("There were errors while publishing packages to NuGet.");
            }
        }
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "We shoud have.")
    protected FilePath getWorkspace(AbstractBuild<?, ?> build) {
        return build.getWorkspace();
    }

    public String getName() {
        return name;
    }

    public String getPackagesPattern() {
        return packagesPattern;
    }

    public String getPublishPath() {
        return publishPath;
    }

    public String getPackagesExclusionPattern() {
        return packagesExclusionPattern;
    }

    public String getNugetPublicationName() {
        return nugetPublicationName;
    }

    public String getNugetVerbosity() {
        return nugetVerbosity;
    }

    public boolean isDoNotFailIfNoPackagesArePublished() {
        return doNotFailIfNoPackagesArePublished;
    }

    @Extension
    public static final class NugetPublisherDescriptor extends BuildStepDescriptor<Publisher> {

        private static final String[] NUGET_VERBOSITIES = {
            "Normal",
            "Quiet",
            "Detailed"
        };
        private static final String PROMOTION_JOB_TYPE = "hudson.plugins.promoted_builds.PromotionProcess";

        public NugetPublisherDescriptor() {
            super(NugetPublisher.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
            return !PROMOTION_JOB_TYPE.equals(type.getCanonicalName());
        }

        public ListBoxModel doFillNugetVerbosityItems() {
            ListBoxModel items = new ListBoxModel();

            for (String choice : NUGET_VERBOSITIES) {
                items.add(choice);
            }

            return items;
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
        
        public FormValidation doCheckPublishPath(@QueryParameter String value) {
            return Validations.urlPath(value);
        }
    }
}
