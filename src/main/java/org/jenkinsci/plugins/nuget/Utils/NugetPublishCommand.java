package org.jenkinsci.plugins.nuget.Utils;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;
import org.jenkinsci.plugins.nuget.NugetPublication;

/**
 * @author Arnaud TAMAILLON
 */
public class NugetPublishCommand extends NugetCommandBase {
    private FilePath packageFile;
    private NugetPublication publication;

    public NugetPublishCommand(TaskListener listener, NugetGlobalConfiguration configuration, FilePath workDir, FilePath packageFile, NugetPublication publication) {
        super(listener, configuration, workDir);
        this.packageFile = packageFile;
        this.publication = publication;
    }

    @Override
    protected void enrichArguments(ArgumentListBuilder builder) {
        builder.add("push");
        builder.add(packageFile);
        builder.addMasked(publication.getApiKey());
        builder.add("-Source");
        builder.add(publication.getUrl());
        builder.add(NON_INTERACTIVE);
    }
}
