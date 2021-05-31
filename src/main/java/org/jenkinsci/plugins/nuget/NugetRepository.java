package org.jenkinsci.plugins.nuget;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * This represents a Nuget repository that can be used to restore Nuget packages
 * from.
 *
 * @author Dennis Lundberg
 * @since 1.1
 */public class NugetRepository implements Serializable {
    private final String name;
    private final String url;

    @DataBoundConstructor
    public NugetRepository(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public static final List<NugetRepository> all() {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins != null) {
            NugetGlobalConfiguration nugetGlobalConfiguration = jenkins.getDescriptorByType(NugetGlobalConfiguration.class);
            return nugetGlobalConfiguration.getNugetRepositories();
        }
        return Collections.emptyList();
    }

    public static final NugetRepository get(String name) {
        List<NugetRepository> available = all();
        for (NugetRepository nugetRepository : available) {
            if (StringUtils.equals(name, nugetRepository.getName())) {
                return nugetRepository;
            }
        }
        return null;
    }
}
