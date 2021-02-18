package org.jenkinsci.plugins.nuget.utils;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;

/**
 * Run the Nuget command line application with the
 * <a href="https://docs.microsoft.com/en-us/nuget/reference/cli-reference/cli-ref-pack">pack</a>
 * command.
 *
 * @author Dennis Lundberg
 * @since 1.1
 */
public class NugetPackCommand extends NugetCommandBase {
    private FilePath nuspecPath;
    private FilePath outputDirectory;

    public NugetPackCommand(TaskListener listener, NugetGlobalConfiguration configuration, FilePath workDir, FilePath nuspecPath, FilePath outputDirectory, String nugetVerbosity) {
        super(listener, configuration, workDir, nugetVerbosity);
        this.nuspecPath = nuspecPath;
        this.outputDirectory = outputDirectory;
    }

    @Override
    protected void enrichArguments(ArgumentListBuilder builder) {
        builder.add("pack");
        builder.add(nuspecPath);
        builder.add("-OutputDirectory");
        builder.add(outputDirectory);
        builder.add(NON_INTERACTIVE);
    }
}
