package org.jenkinsci.plugins.nuget.builders;

import java.io.IOException;
import java.util.List;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;
import org.jenkinsci.plugins.nuget.utils.NugetUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * This is a build step that performs 'nuget pack' using one or more configured
 * nuspec-files.
 *
 * @author Dennis Lundberg
 * @since 1.1
 */
public class NugetPackBuilder extends Builder implements SimpleBuildStep {
    protected boolean failIfNoPackagesAreCreated;
    private final String nugetVerbosity;
    private final String nuspecPattern;
    private final String outputDirectory;

    @DataBoundConstructor
    public NugetPackBuilder(boolean failIfNoPackagesAreCreated, String nugetVerbosity, String nuspecPattern, String outputDirectory) {
        this.failIfNoPackagesAreCreated = failIfNoPackagesAreCreated;
        this.nugetVerbosity = nugetVerbosity;
        this.nuspecPattern = nuspecPattern;
        this.outputDirectory = outputDirectory;
    }

    public String getNugetVerbosity() {
        return nugetVerbosity;
    }

    public String getNuspecPattern() {
        return nuspecPattern;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public boolean isFailIfNoPackagesAreCreated() {
        return failIfNoPackagesAreCreated;
    }

    @Override
    public void perform(Run build,
                        FilePath workspace,
                        Launcher launcher,
                        TaskListener listener) throws InterruptedException, IOException {
        // Expand all parameters that are supposed to be able to take environment variables as part of the input
        String expandedNuspecPattern = Util.replaceMacro(nuspecPattern, build.getEnvironment(listener));
        String expandedOutputDirectory = Util.replaceMacro(outputDirectory, build.getEnvironment(listener));
        listener.getLogger().println("Preparing to create Nuget packages in the folder '"
                                         + expandedOutputDirectory
                                         + "' for all nuspec files matching the pattern '"
                                         + expandedNuspecPattern + "'.");
        NugetGlobalConfiguration configuration = GlobalConfiguration.all().get(NugetGlobalConfiguration.class);

        // Get all nuspec files that match the supplied pattern
        NugetPackCallable callable = new NugetPackCallable(configuration, listener, nugetVerbosity, expandedNuspecPattern, expandedOutputDirectory, workspace);
        List<NugetPackResult> results = workspace.act(callable);
        // Count failures from the return values of the Callable
        long failCount = 0;
        for(NugetPackResult result : results) {
            if(!result.isSuccess()) {
                failCount++;
            }
        }
        listener.getLogger().println("Finished creating " + (results.size() - failCount) + "/" + results.size() + " Nuget packages.");

        checkErrors(results, failCount);
    }

    private void checkErrors(List<NugetPackResult> results, long failCount) throws AbortException {
        if (results.isEmpty() && failIfNoPackagesAreCreated) {
            throw new AbortException("No Nuget packages were created.");
        }
        if(failCount > 0) {
            throw new AbortException(failCount + " error" + (failCount == 1 ? "" : "s") + " occured while creating Nuget packages.");
        }
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public ListBoxModel doFillNugetVerbosityItems() {
            return NugetUtils.createVerbositiesListBoxModel();
        }

        @Override
        public String getDisplayName() {
            return "Create NuGet packages";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> t) {
            return true;
        }
    }
}
