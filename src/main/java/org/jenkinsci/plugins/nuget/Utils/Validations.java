package org.jenkinsci.plugins.nuget.Utils;

import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.nuget.Messages;

/**
 * @author Arnaud TAMAILLON
 */
public class Validations {
    public static FormValidation mandatory(String value) {
        return StringUtils.isBlank(value) ? FormValidation.error(Messages.NugetGlobalConfiguration_MandatoryProperty()) : FormValidation.ok();
    }
}
