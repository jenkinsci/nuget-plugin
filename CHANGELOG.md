CHANGELOG
=========
1.0 (13 Nov 2019)
------
* Removed incorrect binding directive leading to job dsl failures [JENKINS-47495]

0.7
------
* Fixed a circular dependency issue at Jenkins startup [JENKINS-43260]
* Add a verbose logging option for nuget trigger [JENKINS-38022]

0.6
------
* Fixed a Null Pointer Exception at polling time [JENKINS-25277]
* Allow triggering on new pre-release versions [JENKINS-34420]
* Allow publishing to a custom path [JENKINS-36343]
* Fail the build when nupkg does not exist [JENKINS-35316]

0.5
------
* Added publication to NuGet repositories
* Added French localization

0.4
------
* Fixed NuGet polling on remote slave

0.3
------
* Added support for Run Condition Plugin, so Nuget trigger can be an option in the Conditional BuildStep Plugin or Flexible Publish Plugin

0.2
------
* Fixed issue where NuGet exe path wasn't loaded on Jenkins restart

0.1
------
* Initial release

[JENKINS-47495]: https://issues.jenkins-ci.org/browse/JENKINS-47495
[JENKINS-43260]: https://issues.jenkins-ci.org/browse/JENKINS-43260
[JENKINS-38022]: https://issues.jenkins-ci.org/browse/JENKINS-38022
[JENKINS-36343]: https://issues.jenkins-ci.org/browse/JENKINS-36343
[JENKINS-35316]: https://issues.jenkins-ci.org/browse/JENKINS-35316
[JENKINS-34420]: https://issues.jenkins-ci.org/browse/JENKINS-34420
[JENKINS-25277]: https://issues.jenkins-ci.org/browse/JENKINS-25277