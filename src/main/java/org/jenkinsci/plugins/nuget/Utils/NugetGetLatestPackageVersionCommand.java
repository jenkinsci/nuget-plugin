package org.jenkinsci.plugins.nuget.Utils;

import hudson.FilePath;
import hudson.Launcher;
import hudson.util.ArgumentListBuilder;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Arnaud TAMAILLON
 */
class NugetGetLatestPackageVersionCommand extends NugetCommandBase {

    private ForkingOutputStream fork;
    private final String packageName;
    private final boolean checkPrerelease;
    private final XTriggerLog log;
    private String version;

    NugetGetLatestPackageVersionCommand(XTriggerLog log, NugetGlobalConfiguration configuration, FilePath workDir, String packageName, boolean checkPrerelease) {
        super(log.getListener(), configuration, workDir);
        this.log = log;
        this.packageName = packageName;
        this.checkPrerelease = checkPrerelease;
        this.retryCount = 3;
    }

    @Override
    protected void enrichArguments(ArgumentListBuilder builder) {
        builder.add("list");
        builder.add(packageName);
        if (checkPrerelease) {
            builder.add(PRE_RELEASE);
        }
        builder.add(NON_INTERACTIVE);
    }

    @Override
    protected Launcher.ProcStarter customize(Launcher.ProcStarter starter) {
        OutputStream stream = listener.getLogger();
        fork = new ForkingOutputStream(stream);
        return starter.stdout(fork).stderr(stream);
    }

    @Override
    protected void HandleResult(int result) throws IOException {
        super.HandleResult(result);
        List<String> out = fork.getLines();
        for (String line : out) {
            String[] parts = line.split(" ", 2);
            if (parts.length == 2 && parts[0].equalsIgnoreCase(packageName)) {
                version = parts[1];
            }
        }
        fork.close();
    }

    public String getVersion() {
        return version;
    }

    @Override
    protected void logInfo(String message) {
        log.info(message);
    }

    @Override
    protected void logError(String message) {
        log.error(message);
    }
}
