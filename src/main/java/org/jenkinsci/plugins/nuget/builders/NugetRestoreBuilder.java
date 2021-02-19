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
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;
import org.jenkinsci.plugins.nuget.NugetRepository;
import org.jenkinsci.plugins.nuget.utils.NugetUtils;
import org.jenkinsci.plugins.nuget.utils.Validations;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * This is a build step that performs 'nuget restore' using a configured project
 * path.
 * <p>
 * For private Nuget repositories, you need to configure the credentials needed
 * to access the Nuget repository in one your Nuget congfiguration files.
 *
 * @author Dennis Lundberg
 * @since 1.1
 */
public class NugetRestoreBuilder extends Builder implements SimpleBuildStep {
    private final String nugetRepositoryName;
    private final String nugetVerbosity;
    private final String projectPath;

    @DataBoundConstructor
    public NugetRestoreBuilder(String nugetRepositoryName, String nugetVerbosity, String projectPath) {
        this.nugetVerbosity = nugetVerbosity;
        this.nugetRepositoryName = nugetRepositoryName;
        this.projectPath = projectPath;
    }

    public String getNugetVerbosity() {
        return nugetVerbosity;
    }

    public String getNugetRepositoryName() {
        return nugetRepositoryName;
    }

    public String getProjectPath() {
        return projectPath;
    }

    @Override
    public void perform(Run build,
                        FilePath workspace,
                        Launcher launcher,
                        TaskListener listener) throws InterruptedException, IOException {
        // Expand all parameters that are supposed to be able to take environment variables as part of the input
        String expandedProjectPath = Util.replaceMacro(projectPath, build.getEnvironment(listener));
        // Note that if expandedProjectPath is absolute, then expandedProjectPath is used as is and workspace is ignored
        FilePath projectFilePath = new FilePath(workspace, expandedProjectPath);
        NugetRepository nugetRepository = NugetRepository.get(nugetRepositoryName);
        listener.getLogger().println("Preparing to restore Nuget packages from the Nuget repository named '"
                                         + nugetRepositoryName + "' at '"
                                         + nugetRepository.getUrl()
                                         + "' for the project path '"
                                         + projectFilePath + "'.");
        NugetGlobalConfiguration configuration = GlobalConfiguration.all().get(NugetGlobalConfiguration.class);

        NugetRestoreCallable callable = new NugetRestoreCallable(configuration, listener, nugetVerbosity, nugetRepository, projectFilePath, workspace);
        List<NugetRestoreResult> results = workspace.act(callable);
        listener.getLogger().println("Finished restoring Nuget packages.");
        checkErrors(results);
    }

    private void checkErrors(List<NugetRestoreResult> results) throws AbortException {
        if (results.isEmpty()) {
            throw new AbortException("No Nuget packages were restored.");
        }
        for(NugetRestoreResult result : results) {
            if (!result.isSuccess()) {
                throw new AbortException("There were errors while restoring Nuget packages.");
            }
        }
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public ListBoxModel doFillNugetRepositoryNameItems() {
            ListBoxModel items = new ListBoxModel();

            for (NugetRepository choice : getNugetRepositories()) {
                items.add(choice.getName());
            }

            return items;
        }

        public ListBoxModel doFillNugetVerbosityItems() {
            return NugetUtils.createVerbositiesListBoxModel();
        }

        @Override
        public String getDisplayName() {
            return "Restore NuGet packages";
        }

        public List<NugetRepository> getNugetRepositories() {
            return NugetRepository.all();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> t) {
            return true;
        }
    }
}
