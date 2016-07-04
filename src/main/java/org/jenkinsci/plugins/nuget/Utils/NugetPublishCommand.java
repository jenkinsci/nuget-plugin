package org.jenkinsci.plugins.nuget.Utils;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;
import org.jenkinsci.plugins.nuget.NugetPublication;

/**
 * @author Arnaud TAMAILLON
 */
public class NugetPublishCommand extends NugetCommandBase {
    private FilePath packageFile;
    private NugetPublication publication;
    private String publishPath;

    public NugetPublishCommand(TaskListener listener, NugetGlobalConfiguration configuration, FilePath workDir, FilePath packageFile, String publishPath, NugetPublication publication) {
        super(listener, configuration, workDir);
        this.packageFile = packageFile;
        this.publication = publication;
        this.publishPath = publishPath;
    }

    @Override
    protected void enrichArguments(ArgumentListBuilder builder) {
  	String fullUrl = publication.getUrl();

        //only append the path if it exists        
        if ( publishPath != null && !publishPath.trim().isEmpty() ) {
            //ensure we have a separator
            if (!StringUtils.endsWith(fullUrl, "/")) {
                fullUrl += "/";
	    }
            fullUrl += publishPath;
        }
	
        builder.add("push");
        builder.add(packageFile);
        builder.addMasked(publication.getApiKey());
        builder.add("-Source");
        builder.add(fullUrl);
        builder.add(NON_INTERACTIVE);
    }
}
