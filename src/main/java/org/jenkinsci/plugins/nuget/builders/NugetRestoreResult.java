package org.jenkinsci.plugins.nuget.builders;

import java.io.Serializable;

/**
 * The result of running a <code>NugetRestoreCommand</code>.
 *
 * @author Dennis Lundberg
 * @since 1.1
 */
public class NugetRestoreResult implements Serializable {
    private final String projectPath;
    private boolean success;

    public NugetRestoreResult(String projectPath, boolean success) {
        this.projectPath = projectPath;
        this.success = success;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
