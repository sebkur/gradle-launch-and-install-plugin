package de.topobyte.gradle;

import org.gradle.jvm.application.tasks.CreateStartScripts
import org.gradle.api.file.DuplicatesStrategy

class LaunchAndInstallExtension {

    String installationPath
    String makeselfLabel

    def createScript(project, mainClass, name) {
        createScript(project, mainClass, name, null)
    }

    def createScript(project, mainClass, name, options) {
        project.tasks.create(name: name, type: CreateStartScripts) {
            outputDir = new File(project.buildDir, 'scripts')
            mainClassName = mainClass
            applicationName = name
            classpath = project.tasks.startScripts.classpath
            if (!project.hasProperty('scriptNames')) {
                project.ext.scriptNames = []
            }
            project.ext.scriptNames.add(name)
            if (options != null) {
                defaultJvmOpts = options
                doLast {
                    unixScript.text = unixScript.text.replaceAll('APP_HOME_PLACEHOLDER', '\\\$APP_HOME')
                }
            }
        }

        project.tasks[name].dependsOn(project.jar)

        project.distributions.main.contents.from(project.tasks[name]) {
             duplicatesStrategy = DuplicatesStrategy.EXCLUDE
             into 'bin'
        }
    }

}
