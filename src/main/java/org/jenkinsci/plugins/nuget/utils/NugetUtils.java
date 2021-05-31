package org.jenkinsci.plugins.nuget.utils;

import hudson.util.ListBoxModel;

/**
 * Utility method for Nuget.
 *
 * @author Dennis Lundberg
 * @since 1.1
 */
public class NugetUtils {
  private static final String[] NUGET_VERBOSITIES = {
      "Normal",
      "Quiet",
      "Detailed"
  };

  public static ListBoxModel createVerbositiesListBoxModel() {
      ListBoxModel items = new ListBoxModel();

      for (String choice : NUGET_VERBOSITIES) {
          items.add(choice);
      }

      return items;
  }
}
