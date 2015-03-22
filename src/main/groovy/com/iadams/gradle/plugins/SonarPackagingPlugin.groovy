package com.iadams.gradle.plugins

import com.iadams.gradle.plugins.extensions.PackagingExtension
import com.iadams.gradle.plugins.tasks.SonarApiRestartTask
import com.iadams.gradle.plugins.tasks.SonarPluginDeployTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.file.IdentityFileResolver
import org.gradle.api.java.archives.internal.DefaultManifest
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by iwarapter
 */
class SonarPackagingPlugin implements Plugin<Project> {

    static final SONAR_PACKAGING_EXTENSION = 'sonarpackaging'
    static final SONAR_PLUGIN_LOCAL_DEPLOY_TASK = 'localDeploy'
    static final SONAR_API_RESTART_TASK = 'restartServer'

    @Override
    void apply(Project project) {

        project.plugins.apply JavaPlugin
        project.extensions.create( SONAR_PACKAGING_EXTENSION, PackagingExtension, project)
        project.configurations.create('sonarqube')

        addTasks(project)
    }

    /**
     * Adds the plugin deploy and server restart tasks.
     *
     * @param project
     */
    void addTasks(Project project){
        def extension = project.extensions.findByName(SONAR_PACKAGING_EXTENSION)

        project.tasks.withType(Jar) {
            doFirst {
                manifest = new DefaultManifest(new IdentityFileResolver())
                manifest.attributes(
                        "Created-By": "Gradle",
                        "Built-By": System.getProperty('user.name'),
                        "Build-Jdk": System.getProperty('java.version'),
                        "Build-Time": new Date().format("yyyy-MM-dd'T'HH:mm:ssZ"),
                        "Plugin-BuildDate": new Date().format("yyyy-MM-dd'T'HH:mm:ssZ"),
                        "Plugin-Class": extension.pluginClass,
                        "Plugin-Dependencies": project.configurations.sonarqube.collect {
                            "META-INF/lib/${it.name}"
                        }.join(' '),
                        "Plugin-Description": extension.pluginDescription,
                        //TODO Setup a way to handle developers
                        //"Plugin-Developers": 'iwarapter',
                        "Plugin-Key": extension.pluginKey,
                        "Plugin-Name": extension.pluginName,
                        //"Plugin-Version": version,
                        "Sonar-Version": 3.7
                )

                manifest.writeTo("${project.buildDir}/tmp/jar/MANIFEST.MF")
            }
            into ('META-INF/lib') {
                from project.configurations.sonarqube
            }
        }

        project.task( SONAR_PLUGIN_LOCAL_DEPLOY_TASK, type: SonarPluginDeployTask) {
            description = 'Copies the built plugin to the local server.'
            group = 'Sonar Packaging'

            conventionMapping.localServer = { project.file(extension.pluginDir) }
            conventionMapping.pluginJar = { project.file("${project.libsDir}/${project.archivesBaseName}-${project.version}.jar") }
        }

        project.task( SONAR_API_RESTART_TASK, type: SonarApiRestartTask) {
            description = 'Restarts a SonarQube server running in dev mode.'
            group = 'Sonar Packaging'

            conventionMapping.serverUrl = { extension.serverUrl }
            conventionMapping.restartApiPath = { extension.restartApiPath }
        }
    }
}
