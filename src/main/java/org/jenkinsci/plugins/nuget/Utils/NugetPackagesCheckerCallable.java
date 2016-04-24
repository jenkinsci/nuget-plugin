package org.jenkinsci.plugins.nuget.Utils;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;
import org.jenkinsci.plugins.nuget.triggers.NugetTrigger;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Arnaud TAMAILLON
 */
class NugetPackagesCheckerCallable extends MasterToSlaveFileCallable<Boolean> {
    private final boolean preReleaseChecked;
    private final XTriggerLog log;
    private final NugetGlobalConfiguration configuration;

    NugetPackagesCheckerCallable(NugetGlobalConfiguration configuration, boolean preReleaseChecked, XTriggerLog log) {
        this.configuration = configuration;
        this.preReleaseChecked = preReleaseChecked;
        this.log = log;
    }

    public Boolean invoke(File file, VirtualChannel vc) throws IOException, InterruptedException {
        try {
            FilePath filePath = new FilePath(file);
            NugetPackageCheckerVisitor visitor = new NugetPackageCheckerVisitor(log, configuration, preReleaseChecked, filePath);
            Files.walkFileTree(Paths.get(file.getPath()), visitor);
            return visitor.isUpdated();
        } catch (ParserConfigurationException ex) {
            log.error(ex.toString());
            return false;
        }
    }
}
