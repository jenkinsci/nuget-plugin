package org.jenkinsci.plugins.nuget.publishers;

import com.google.common.collect.Lists;
import hudson.FilePath;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;
import org.jenkinsci.plugins.nuget.NugetPublication;
import org.jenkinsci.plugins.nuget.Utils.NugetPublishCommand;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Arnaud TAMAILLON
 */
class NugetPublisherCallable extends MasterToSlaveFileCallable<List<PublicationResult>> {
    private final String packagesPattern;
    private final String publishPath;
    private final String packagesExclusionPattern;
    private final BuildListener listener;
    private final NugetGlobalConfiguration configuration;
    private final NugetPublication publication;

    NugetPublisherCallable(String packagesPattern, String packagesExclusionPattern, BuildListener listener, NugetGlobalConfiguration configuration, String publishPath, NugetPublication publication) {
        this.packagesPattern = packagesPattern;
        this.publishPath = publishPath;
        this.packagesExclusionPattern = packagesExclusionPattern;
        this.listener = listener;
        this.configuration = configuration;
        this.publication = publication;
    }

    @Override
    public List<PublicationResult> invoke(File file, VirtualChannel virtualChannel) throws IOException, InterruptedException {
        List<String> packages = getFiles(file, packagesPattern, packagesExclusionPattern);
        List<PublicationResult> results = Lists.newArrayList();
        for(String pack : packages) {
            File packageFile = new File(pack);
            NugetPublishCommand publishCommand = new NugetPublishCommand(
                    listener,
                    configuration,
                    new FilePath(file),
                    new FilePath(packageFile),
                    publishPath,
                    publication);
            boolean success = publishCommand.execute();
            results.add(new PublicationResult(packageFile.getName(), success));
        }
        return results;
    }

    private static List<String> getFiles(File parentPath, String pattern, String exclusionPattern) {
        FileSet fs = Util.createFileSet(parentPath, pattern, exclusionPattern);
        DirectoryScanner ds = fs.getDirectoryScanner();
        String[] files = ds.getIncludedFiles();
        return Arrays.asList(files);
    }
}
