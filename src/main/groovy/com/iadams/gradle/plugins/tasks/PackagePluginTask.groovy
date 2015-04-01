package com.iadams.gradle.plugins.tasks

import com.iadams.gradle.plugins.SonarPackagingPlugin
import com.iadams.gradle.plugins.core.DependencyQuery
import com.iadams.gradle.plugins.core.PluginManifest
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by iwarapter
 */
class PackagePluginTask extends Jar {

    private static final String[] GWT_ARTIFACT_IDS = ["gwt-user", "gwt-dev", "sonar-gwt-api"]
    private static final String[] LOG_GROUP_IDS = ["log4j", "commons-logging"]

    @Input
    String pluginClass

    @Input
    String pluginDescription

    @Input
    String pluginDevelopers

    @Input
    String pluginUrl

    @Input
    String pluginIssueTrackerUrl

    @Input
    String pluginKey

    @Input
    String pluginLicense

    @Input
    String pluginName

    @Input
    String organizationName

    @Input
    String organizationUrl

    @Input
    String pluginSourceUrl

    @Input
    String pluginTermsConditionsUrl

    @Input
    Boolean useChildFirstClassLoader

    @Input
    String pluginParent

    @Input
    String requirePlugins

    @Input
    String basePlugin

    @Input
    boolean skipDependenciesPackaging

    @TaskAction
    protected void copy() {
        def query = new DependencyQuery(project)
        if (!skipDependenciesPackaging) {
            query.checkApiDependency()
            query.checkForDependencies(LOG_GROUP_IDS)
            query.checkForDependencies(GWT_ARTIFACT_IDS)
        }

        getLogger().info "-------------------------------------------------------"
        getLogger().info "Plugin definition in update center"
        manifest = new PluginManifest()
        manifest.addManifestProperty("Created-By", "Gradle")
        manifest.addManifestProperty("Built-By", System.getProperty('user.name'))
        manifest.addManifestProperty("Build-Jdk", System.getProperty('java.version'))
        manifest.addManifestProperty("Build-Time", new Date().format("yyyy-MM-dd'T'HH:mm:ssZ"))
        manifest.addManifestProperty(PluginManifest.BUILD_DATE, new Date().format("yyyy-MM-dd'T'HH:mm:ssZ"))
        manifest.addManifestProperty(PluginManifest.MAIN_CLASS, getPluginClass())
        manifest.addManifestProperty(PluginManifest.DESCRIPTION, getPluginDescription())
        manifest.addManifestProperty(PluginManifest.DEVELOPERS, getPluginDevelopers())
        manifest.addManifestProperty(PluginManifest.HOMEPAGE, getPluginUrl())
        manifest.addManifestProperty(PluginManifest.ISSUE_TRACKER_URL, getPluginIssueTrackerUrl())
        manifest.addManifestProperty(PluginManifest.KEY, getPluginKey())
        manifest.addManifestProperty(PluginManifest.LICENSE, getPluginLicense())
        manifest.addManifestProperty(PluginManifest.NAME, getPluginName())
        manifest.addManifestProperty(PluginManifest.ORGANIZATION, getOrganizationName())
        manifest.addManifestProperty(PluginManifest.ORGANIZATION_URL, getOrganizationUrl())
        manifest.addManifestProperty(PluginManifest.SOURCES_URL, getPluginSourceUrl())
        manifest.addManifestProperty(PluginManifest.TERMS_CONDITIONS_URL, getPluginTermsConditionsUrl())
        manifest.addManifestProperty(PluginManifest.VERSION, project.version)
        manifest.addManifestProperty(PluginManifest.SONAR_VERSION, new DependencyQuery(project).sonarPluginApiArtifact.moduleVersion)
        if (getUseChildFirstClassLoader()){
            manifest.addManifestProperty(PluginManifest.USE_CHILD_FIRST_CLASSLOADER, getUseChildFirstClassLoader().toString())
        }

        /**
         * If these extension points are null dont add to manifest
         */
        if(getPluginParent()) {
            manifest.addManifestProperty(PluginManifest.PARENT, getPluginParent())
        }
        if(getRequirePlugins()){
            manifest.addManifestProperty(PluginManifest.REQUIRE_PLUGINS, getRequirePlugins())
        }
        if(getBasePlugin()){
            manifest.addManifestProperty(PluginManifest.BASE_PLUGIN, getBasePlugin())
        }

        getLogger().info '-------------------------------------------------------'

        if(!skipDependenciesPackaging) {

            List<ResolvedDependency> dependencies = new DependencyQuery(project).getNotProvidedDependencies()
            manifest.addManifestProperty(PluginManifest.DEPENDENCIES, dependencies.collect{ "META-INF/lib/${it.moduleName}:${it.moduleVersion}.jar" }.join(' '))

            from project.sourceSets.main.output
            into('META-INF/lib') {
                List<File> artifacts = []
                dependencies.each{ ResolvedDependency dep->
                    dep.getModuleArtifacts().each{ artifacts.add(it.getFile().absoluteFile) }
                }
                from artifacts
            }
        }

        super.copy()
        getLogger().info('Sonar Plugin Created.')
    }
}
