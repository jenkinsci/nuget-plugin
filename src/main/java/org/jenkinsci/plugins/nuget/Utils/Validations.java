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

    public static FormValidation urlPath(String value) {
        String trimmedValue = StringUtils.trim(value);
        if ( StringUtils.startsWith(trimmedValue, "/") || StringUtils.endsWith(trimmedValue, "/") ) {
            return FormValidation.error(Messages.NugetPublisher_DontStartOrEndWithSlash());
        } else if ( StringUtils.containsAny(trimmedValue, "\\")) {
            return FormValidation.error(Messages.NugetPublisher_BackSlash());
        } else {
            return FormValidation.ok();
        } 
    }
}
