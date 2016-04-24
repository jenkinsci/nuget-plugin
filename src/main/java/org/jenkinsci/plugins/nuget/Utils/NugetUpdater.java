package org.jenkinsci.plugins.nuget.Utils;

import hudson.FilePath;

import java.io.IOException;

import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;

/**
 * @author bgregg
 */
public class NugetUpdater {
    private final FilePath solutionDir;
    private final NugetPackagesCheckerCallable nugetPackagesCheckerCallable;
    private final XTriggerLog log;

    public NugetUpdater(FilePath solutionDir, NugetGlobalConfiguration configuration, boolean checkPrerelease, XTriggerLog log) {
        this.solutionDir = solutionDir;
        this.log = log;
        this.nugetPackagesCheckerCallable = new NugetPackagesCheckerCallable(configuration, checkPrerelease, log);
    }

    public boolean performUpdate() {
        try {
            return checkVersions();
        } catch (Throwable ex) {
            log.error(ex.toString());
            return false;
        }
    }

    private boolean checkVersions() throws InterruptedException, IOException {
        if (solutionDir == null) {
            log.error("No workspace found. Ignoring trigger.");
            return false;
        }
        return solutionDir.act(nugetPackagesCheckerCallable);
    }
}

