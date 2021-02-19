package org.jenkinsci.plugins.nuget.utils;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;
import org.jenkinsci.plugins.nuget.NugetRepository;

/**
 * Run the Nuget command line application with the
 * <a href="https://docs.microsoft.com/en-us/nuget/reference/cli-reference/cli-ref-restore">restore</a>
 * command.
 *
 * @author Dennis Lundberg
 * @since 1.1
 */
public class NugetRestoreCommand extends NugetCommandBase {
    private NugetRepository nugetRepository;
    private FilePath projectPath;

    public NugetRestoreCommand(TaskListener listener, NugetGlobalConfiguration configuration, FilePath workDir, String nugetVerbosity, NugetRepository nugetRepository, FilePath projectPath) {
        super(listener, configuration, workDir, nugetVerbosity);
        this.projectPath = projectPath;
        this.nugetRepository = nugetRepository;
    }

    @Override
    protected void enrichArguments(ArgumentListBuilder builder) {
        builder.add("restore");
        builder.add(projectPath);
        builder.add("-Source");
        builder.add(nugetRepository.getUrl());
        builder.add(NON_INTERACTIVE);
    }
}
