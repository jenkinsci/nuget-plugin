package org.jenkinsci.plugins.nuget.builders;

import java.io.File;
import java.io.IOException;
import java.util.List;
import com.google.common.collect.Lists;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;
import org.jenkinsci.plugins.nuget.NugetRepository;
import org.jenkinsci.plugins.nuget.utils.NugetRestoreCommand;

/**
 * This is a Callable that is able to run commands on files on an agent. This
 * class runs the <code>NugetRestoreCommand</code>.
 *
 * @author Dennis Lundberg
 * @since 1.1
 */
class NugetRestoreCallable extends MasterToSlaveFileCallable<List<NugetRestoreResult>> {
    private final NugetGlobalConfiguration configuration;
    private final TaskListener listener;
    private final String nugetVerbosity;
    private final FilePath projectPath;
    private final NugetRepository nugetRepository;
    private final FilePath workspace;


    NugetRestoreCallable(NugetGlobalConfiguration configuration, TaskListener listener, String nugetVerbosity, NugetRepository nugetRepository, FilePath projectPath, FilePath workspace) {
        this.configuration = configuration;
        this.listener = listener;
        this.nugetRepository = nugetRepository;
        this.nugetVerbosity = nugetVerbosity;
        this.projectPath = projectPath;
        this.workspace = workspace;
    }

    @Override
    public List<NugetRestoreResult> invoke(File file, VirtualChannel virtualChannel) throws IOException, InterruptedException {
        List<NugetRestoreResult> results = Lists.newArrayList();
        NugetRestoreCommand restoreCommand = new NugetRestoreCommand(
            listener,
            configuration,
            workspace,
            nugetVerbosity,
            nugetRepository,
            projectPath);
        boolean success = restoreCommand.execute();
        results.add(new NugetRestoreResult(projectPath.toString(), success));
        listener.getLogger().println("Finished restoring Nuget packages for the project path '" + projectPath + "'. Success=" + success + ".");
        return results;
    }
}
