package org.jenkinsci.plugins.nuget.builders;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import com.google.common.collect.Lists;
import hudson.FilePath;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;
import org.jenkinsci.plugins.nuget.utils.NugetPackCommand;

/**
 * This is a Callable that is able to run commands on files on an agent. This
 * class runs the <code>NugetPackCommand</code>.
 *
 * @author Dennis Lundberg
 * @since 1.1
 */
class NugetPackCallable extends MasterToSlaveFileCallable<List<NugetPackResult>> {
    private final NugetGlobalConfiguration configuration;
    private final TaskListener listener;
    private final String nugetVerbosity;
    private final String nuspecPattern;
    private final String outputDirectory;
    private final FilePath workspace;


    NugetPackCallable(NugetGlobalConfiguration configuration, TaskListener listener, String nugetVerbosity, String nuspecPattern, String outputDirectory, FilePath workspace) {
        this.nuspecPattern = nuspecPattern;
        this.outputDirectory = outputDirectory;
        this.listener = listener;
        this.configuration = configuration;
        this.nugetVerbosity = nugetVerbosity;
        this.workspace = workspace;
    }

    @Override
    public List<NugetPackResult> invoke(File file, VirtualChannel virtualChannel) throws IOException, InterruptedException {
        List<String> nuspecPaths = getFiles(file, nuspecPattern);
        // Note that if outputDirectory is absolute, then outputDirectory is used as is and workspace is ignored
        FilePath outputDirectoryFilePath = new FilePath(workspace, outputDirectory);
        List<NugetPackResult> results = Lists.newArrayList();
        for(String nuspecPath : nuspecPaths) {
            NugetPackCommand packCommand = new NugetPackCommand(
                listener,
                configuration,
                workspace,
                new FilePath(new File(nuspecPath)),
                outputDirectoryFilePath,
                nugetVerbosity);
            boolean success = packCommand.execute();
            results.add(new NugetPackResult(nuspecPath, success));
            listener.getLogger().println("Finished creating a Nuget package based on the nuspec file '" + nuspecPath + "'. Success=" + success + ".");
        }
        return results;
    }

    private static List<String> getFiles(File directory, String pattern) {
        FileSet fs = Util.createFileSet(directory, pattern);
        DirectoryScanner ds = fs.getDirectoryScanner();
        String[] files = ds.getIncludedFiles();
        return Arrays.asList(files);
    }
}
