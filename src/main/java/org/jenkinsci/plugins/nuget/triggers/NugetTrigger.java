package org.jenkinsci.plugins.nuget.triggers;
import antlr.ANTLRException;
import hudson.XmlFile;
import hudson.model.Items;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.nuget.Messages;
import org.jenkinsci.plugins.nuget.NugetCause;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;
import org.jenkinsci.plugins.nuget.Utils.NugetUpdater;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Node;
import java.io.File;
import java.util.Collection;
import java.util.Collections;

import org.jenkinsci.lib.xtrigger.AbstractTrigger;
import org.jenkinsci.lib.xtrigger.XTriggerDescriptor;
import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author bgregg
 */
public class NugetTrigger extends AbstractTrigger {

    private boolean checkPrerelease;

    @DataBoundConstructor
    public NugetTrigger(String cronTabSpec, boolean checkPrerelease) throws ANTLRException {
        super(cronTabSpec);
        this.checkPrerelease = checkPrerelease;
    }

    public boolean getCheckPrerelease() {
        return checkPrerelease;
    }

    @Override
    protected File getLogFile() {
        return new File(job.getRootDir(), "nuget-polling.log");
    }

    @Override
    protected boolean requiresWorkspaceForPolling() {
        return true;
    }

    @Override
    protected String getName() {
        return NugetCause.NAME;
    }

    @Override
    protected Action[] getScheduledActions(Node node, XTriggerLog xtl) {
        return new Action[0];
    }

    @Override
    protected boolean checkIfModified(Node node, XTriggerLog xtl) throws XTriggerException {
        if (job == null) {
            return false;
        }
        AbstractProject project = (AbstractProject) job;
        NugetGlobalConfiguration configuration = GlobalConfiguration.all().get(NugetGlobalConfiguration.class);
        NugetUpdater updater = new NugetUpdater(project.getSomeWorkspace(), configuration, checkPrerelease, xtl);
        return updater.performUpdate();
    }

    @Override
    protected String getCause() {
        return Messages.NugetCause_Cause();
    }

    @Override
    public NugetTriggerDescriptor getDescriptor() {
        return (NugetTriggerDescriptor)super.getDescriptor();
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        if (job == null) {
            return Collections.emptyList();
        }
        return Collections.singleton(new NugetTriggerAction(job, getLogFile()));
    }

    @Extension(ordinal=1000)
    public static final class NugetTriggerDescriptor extends XTriggerDescriptor {
        private String nugetExe;

        public NugetTriggerDescriptor() {
            super();
            load();
        }

        @Override
        public String getDisplayName() {
            return Messages.NugetTrigger_DiplayName();
        }

        @Deprecated
        public String getNugetExe() {
            return nugetExe;
        }

        @Override
        public XmlFile getConfigFile() {
            Jenkins jenkins = Jenkins.getInstance();
            if (jenkins == null) {
                return null;
            }
            return new XmlFile(Items.XSTREAM2, new File(jenkins.getRootDir(), "com.jenkinsci.nuget.NugetTrigger.xml"));
        }
    }
}
