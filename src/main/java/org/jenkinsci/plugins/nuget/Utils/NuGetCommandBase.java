package org.jenkinsci.plugins.nuget.Utils;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;

import java.io.IOException;

/**
 * @author Arnaud TAMAILLON
 */
abstract class NugetCommandBase {

    static final String NON_INTERACTIVE = "-NonInteractive";
    static final String PRE_RELEASE = "-Prerelease";

    int retryCount = 1;
    protected TaskListener listener;
    protected NugetGlobalConfiguration configuration;
    private FilePath workDir;
    protected boolean failed;

    NugetCommandBase(TaskListener listener, NugetGlobalConfiguration configuration, FilePath workDir) {
        this.listener = listener;
        this.configuration = configuration;
        this.workDir = workDir;
    }

    public boolean execute() throws IOException {
        Integer tryNumber = 1;
        Boolean executed = false;

        while (!executed && tryNumber <= retryCount) {
            try {
                singleExecute();
                executed = true;
            } catch(InterruptedException ex) {
                logError(ex.toString());
                logInfo(String.format("Retrying: %d", tryNumber));
                tryNumber++;
            }
        }
        return isSuccess();
    }

    private void singleExecute() throws IOException, InterruptedException {
        failed = false;
        Launcher.LocalLauncher launcher = new Launcher.LocalLauncher(listener);
        ArgumentListBuilder builder = new ArgumentListBuilder(getNugetExe());
        enrichArguments(builder);
        Launcher.ProcStarter starter = launcher
                .launch()
                .pwd(workDir)
                .cmds(builder)
                .stdout(listener);
        starter = customize(starter);
        int result = starter.join();
        HandleResult(result);
    }

    protected void HandleResult(int result) throws IOException {
        failed = result != 0;
    }

    protected abstract void enrichArguments(ArgumentListBuilder builder);

    protected Launcher.ProcStarter customize(Launcher.ProcStarter starter) {
        return starter;
    }

    protected void logInfo(String message) {
        listener.getLogger().println(message);
    }

    protected void logError(String message) {
        listener.error(message);
    }

    private String getNugetExe() {
        String configurationExe = configuration.getNugetExe();
        if (configurationExe != null) {
            return configurationExe;
        }
        return ".nuget\\NuGet.exe";
    }

    public boolean isFailed() {
        return failed;
    }

    public boolean isSuccess() {
        return !isFailed();
    }
}
