package de.topobyte.gradle;

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.Sync

class LaunchAndInstallPlugin implements Plugin<Project> {

    void apply(Project project) {
        def extension = project.extensions.create("launchAndInstall", LaunchAndInstallExtension)

        project.task('setupScripts') {
            doLast {
                if (extension.installationPath == null) {
                    throw new InvalidUserDataException("You need to specify the installationPath")
                }
                def baseName = project.distributions.main.distributionBaseName.get()
                def installationPath = "\$HOME/share/$extension.installationPath/$baseName-snapshot"

                def file1 = new File("$project.buildDir/setup/install.sh")
                createInstallScript(project, file1, baseName, installationPath)

                def file2 = new File("$project.buildDir/setup/post-install.sh")
                createPostInstallScript(project, file2, installationPath)
            }
        }

        project.task('makeselfInstallScript') {
            doLast {
                if (extension.installationPath == null) {
                    throw new InvalidUserDataException("You need to specify the installationPath")
                }
                def baseName = project.distributions.main.distributionBaseName.get()

                def file = new File("$project.buildDir/makeself/install.sh")
                createFullInstallScript(project, file, "\$HOME/share/$extension.installationPath/$baseName-$project.version")
            }
        }

        project.tasks.create(name: "makeselfData", type: Sync) {
            with project.tasks.installDist
            into "$project.buildDir/makeself/files"
        }

        project.tasks.create(name: "makeselfArchive") {
            dependsOn "makeselfData"
            dependsOn "makeselfInstallScript"
            doLast {
                if (extension.makeselfLabel == null) {
                    throw new InvalidUserDataException("You need to specify the makeselfLabel")
                }
                makeself(project, extension.makeselfLabel)
            }
        }

        project.tasks.create(name: "makeself") {
            dependsOn "makeselfArchive"
        }
    }

    def makeself(project, label) {
        def baseName = project.distributions.main.distributionBaseName.get()
        project.exec { executable 'makeself' args "$project.buildDir/makeself",
                       "$project.buildDir/$baseName-${project.version}.run",
                       label, "./install.sh"}
    }

    def createPostInstallScript(project, file, path) {
        newFile(project, file)
        header(file)
        scriptLinks(project, file, path)
        makeExecutable(project, file)
    }

    def createInstallScript(project, file, baseName, path) {
        newFile(project, file)
        header(file)
        file.append("DIR=\$(dirname \$0)\n")
        file.append("\n")
        file.append("TARGET=\"$path\"\n")
        file.append("\n")
        file.append("mkdir -p \"\$TARGET\"\n")
        file.append("rsync -av --delete \"\$DIR/../install/$baseName/\" \"\$TARGET\"\n")
        makeExecutable(project, file)
    }

    def createFullInstallScript(project, file, path) {
        newFile(project, file)
        header(file)
        file.append("DIR=\$(dirname \$0)\n")
        file.append("\n")
        file.append("TARGET=\"$path\"\n")
        file.append("\n")
        file.append("mkdir -p \"\$TARGET\"\n")
        file.append("rsync -av --delete \"\$DIR/files/\" \"\$TARGET\"\n")
        file.append("\n")
        scriptLinks(project, file, path)
        makeExecutable(project, file)
    }

    def newFile(project, file) {
        def dir = file.getParent()
        project.mkdir dir
        project.delete file
    }

    def makeExecutable(project, file) {
        project.exec { executable 'chmod' args '+x', file  }
    }

    def header(file) {
        file.append("#!/bin/bash\n")
        file.append("\n")
    }

    def scriptLinks(project, file, path) {
        file.append("mkdir -p \"\$HOME/bin\"\n")
        file.append("\n")
        project.scriptNames.each {
            file.append("ln -fsr \"$path/bin/$it\" \"\$HOME/bin/$it\"\n")
        }
    }

}
