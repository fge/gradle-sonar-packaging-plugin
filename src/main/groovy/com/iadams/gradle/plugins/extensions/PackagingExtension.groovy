package com.iadams.gradle.plugins.extensions

import org.gradle.api.GradleException
import org.gradle.api.Project
/**
 * Created by iwarapter
 */
class PackagingExtension {

    /**
     * Address for the web server for the SonarApiRestartTask.
     */
    String serverUrl = 'http://localhost:9000'
    /**
     * Address path for the API call to restart Sonar server.
     * Note: I wouldn't expect this to change.
     */
    String restartApiPath = '/api/system/restart'

    /**
     * Directory to the local sonar server's plugin directory.
     */
    String pluginDir

    /**
     *
     */
    String pluginKey

    String pluginClass

    String pluginName

    String pluginDescription

    //TODO Add support for all the other Manifest info
    String pluginParent = ''
    String pluginLicense = ''
    String requirePlugins = ''
    String pluginUrl = ''
    String pluginIssueTrackerUrl = ''
    String pluginTermsConditionsUrl = ''
    String pluginSourceUrl = ''
    String pluginDevelopers = ''
    boolean skipDependenciesPackaging = false
    boolean useChildFirstClassLoader = false
    String basePlugin = ''

    PackagingOrganizationExtension organization = new PackagingOrganizationExtension()

    PackagingExtension( Project project){
        pluginName = project.name
        pluginDescription = project.description
    }
}
