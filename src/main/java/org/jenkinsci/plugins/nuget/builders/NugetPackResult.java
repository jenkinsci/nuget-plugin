package org.jenkinsci.plugins.nuget.builders;

import java.io.Serializable;

/**
 * The result of running a <code>NugetPackCommand</code>.
 *
 * @author Dennis Lundberg
 * @since 1.1
 */
public class NugetPackResult implements Serializable {
    private final String nuspecFile;
    private boolean success;

    public NugetPackResult(String nuspecFile, boolean success) {
        this.nuspecFile = nuspecFile;
        this.success = success;
    }

    public String getNuspecFile() {
        return nuspecFile;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
