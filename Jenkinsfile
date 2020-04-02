#!/usr/bin/env groovy
@Library('pipeline-library')_

def schedule, sonarBranchName, sonarBranchTarget
switch (env.BRANCH_NAME) {
    case ~/.*master.*/:
        schedule = '@monthly'
        sonarBranchName = '-Dsonar.branch.name=master'
        sonarBranchTarget = ''
        break
    case ~/.*develop.*/:
        schedule = '@midnight'
        sonarBranchName = '-Dsonar.branch.name=develop'
        sonarBranchTarget = '-Dsonar.branch.target=master'
        break
    default:
        schedule = ''
        sonarBranchName = '-Dsonar.branch.name=' + env.BRANCH_NAME
        sonarBranchTarget = '-Dsonar.branch.target=develop'
        break
}
def DOWNLOADDIR = 'downloads'
def MVNINSTALLPLUGIN = 'org.apache.maven.plugins:maven-install-plugin:3.0.0-M1'

pipeline {

    agent any

    environment {
        JDK_VERSION = 'jdk-8-oracle'
    }

    options {
        ansiColor('xterm')
        buildDiscarder(logRotator(artifactNumToKeepStr: '1'))
        parallelsAlwaysFailFast()
        retry(1)
        skipStagesAfterUnstable()
        timeout(time: 1, unit: 'HOURS')
        timestamps()
    }

    triggers {
        cron(schedule)
    }

    tools {
        maven 'M3'
        jdk "${JDK_VERSION}"
    }

    stages {
        stage('Build') {
            options {
                retry(2)
            }
            stages {
                stage('Clean workspace') {
                    options {
                        timeout(time: 5, unit: 'MINUTES')
                    }
                    steps {
                        withMaven(jdk: "${JDK_VERSION}", maven: 'M3') {
                            sh "mvn --threads 2C --no-transfer-progress clean dependency:purge-local-repository -Dinclude=com.itextpdf -DresolutionFuzziness=groupId -DreResolve=false -Dmaven.repo.local=${env.WORKSPACE.replace('\\','/')}/.repository"
                        }
                        script {
                            if(fileExists(DOWNLOADDIR)) {
                                sh "rm -rf ${env.WORKSPACE.replace('\\','/')}/${DOWNLOADDIR}"
                            }
                        }
                    }
                }
                stage('Install branch dependencies') {
                    options {
                        timeout(time: 5, unit: 'MINUTES')
                    }
                    when {
                        not {
                            anyOf {
                                branch "master"
                                branch "develop"
                            }
                        }
                    }
                    steps {
                        script {
                            getAndConfigureJFrogCLI()
                            sh "./jfrog rt dl branch-artifacts/${env.JOB_BASE_NAME}/**/java/ ${DOWNLOADDIR}/"
                            if(fileExists(DOWNLOADDIR)) {
                                dir (DOWNLOADDIR) {
                                    def mainPomFiles = findFiles(glob: '**/main.pom')
                                    mainPomFiles.each{ pomFile ->
                                        pomPath = pomFile.path.replace("\\","/")
                                        sh "mvn ${MVNINSTALLPLUGIN}:install-file --quiet -Dmaven.repo.local=${env.WORKSPACE.replace('\\','/')}/.repository -Dpackaging=pom -Dfile=${pomPath} -DpomFile=${pomPath}"
                                    }
                                    def pomFiles = findFiles(glob: '**/*.pom')
                                    pomFiles.each{ pomFile ->
                                        if (pomFile.name != "main.pom") {
                                            pomPath = pomFile.path.replace("\\","/")
                                            sh "mvn ${MVNINSTALLPLUGIN}:install-file --quiet -Dmaven.repo.local=${env.WORKSPACE.replace('\\','/')}/.repository -Dpackaging=pom -Dfile=${pomPath} -DpomFile=${pomPath}"
                                        }
                                    }
                                    def jarFiles = findFiles(glob: '**/*.jar')
                                    jarFiles.each{ jarFile ->
                                        jarPath = jarFile.path.replace("\\","/")
                                        sh "mvn ${MVNINSTALLPLUGIN}:install-file --quiet -Dmaven.repo.local=${env.WORKSPACE.replace('\\','/')}/.repository -Dfile=${jarPath}"
                                    }
                                }
                            }
                        }
                    }
                }
                stage('Compile') {
                    options {
                        timeout(time: 10, unit: 'MINUTES')
                    }
                    steps {
                        withMaven(jdk: "${JDK_VERSION}", maven: 'M3') {
                            sh "mvn --threads 2C --no-transfer-progress package -Dmaven.test.skip=true -Dmaven.repo.local=${env.WORKSPACE.replace('\\','/')}/.repository"
                        }
                    }
                }
            }
            post {
                failure {
                    sleep time: 2, unit: 'MINUTES'
                }
                success {
                    script { currentBuild.result = 'SUCCESS' }
                }
            }
        }
        stage('Static Code Analysis') {
            options {
                timeout(time: 1, unit: 'HOURS')
            }
            steps {
                withMaven(jdk: "${JDK_VERSION}", maven: 'M3') {
                    sh "mvn --no-transfer-progress verify --activate-profiles qa -Dpmd.analysisCache=true -Dmaven.repo.local=${env.WORKSPACE.replace('\\','/')}/.repository"
                }
                recordIssues(tools: [
                        checkStyle(),
                        pmdParser(),
                        spotBugs(useRankAsPriority: true)
                ])
                dependencyCheckPublisher pattern: 'target/dependency-check-report.xml'
            }
        }
        stage('Run Tests') {
            options {
                timeout(time: 30, unit: 'MINUTES')
            }
            steps {
                withMaven(jdk: "${JDK_VERSION}", maven: 'M3') {
                    withSonarQubeEnv('Sonar') {
                        sh "mvn --no-transfer-progress --activate-profiles test -DgsExec=\"${gsExec}\" -DcompareExec=\"${compareExec}\" -Dmaven.main.skip=true -Dmaven.test.failure.ignore=false -Dmaven.repo.local=${env.WORKSPACE.replace('\\','/')}/.repository org.jacoco:jacoco-maven-plugin:prepare-agent verify org.jacoco:jacoco-maven-plugin:report -Dsonar.java.spotbugs.reportPaths=\"target/spotbugs.xml\" sonar:sonar " + sonarBranchName + " " + sonarBranchTarget
                    }
                }
            }
        }
        stage("Quality Gate") {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage("Wrap to exe") {
            steps {
                withMaven(jdk: "${JDK_VERSION}", maven: 'M3') {
                    sh "mvn --threads 2C --no-transfer-progress --activate-profiles exe " +
                            "build-helper:parse-version@parse-version " +
                            "com.akathist.maven.plugins.launch4j:launch4j-maven-plugin:launch4j@l4j-gui " +
                            "assembly:single@exe-archive " +
                            "-Dmaven.repo.local=${env.WORKSPACE.replace('\\','/')}/.repository"
                }
            }
        }
        stage('Artifactory Deploy') {
            options {
                timeout(time: 5, unit: 'MINUTES')
            }
            when {
                anyOf {
                    branch "master"
                    branch "develop"
                }
            }
            steps {
                withMaven(jdk: "${JDK_VERSION}", maven: 'M3') {
                    script {
                        def server = Artifactory.server('itext-artifactory')
                        def rtMaven = Artifactory.newMavenBuild()
                        rtMaven.deployer server: server, releaseRepo: 'releases', snapshotRepo: 'snapshot'
                        rtMaven.tool = 'M3'
                        def buildInfo = rtMaven.run pom: 'pom.xml',
                                goals: "--threads 2C --no-transfer-progress " +
                                        "install --activate-profiles artifactory,exe " +
                                        "build-helper:attach-artifact@attach-exe-artifact " +
                                        "-Dmaven.repo.local=${env.WORKSPACE.replace('\\','/')}/.repository".toString()
                        server.publishBuildInfo buildInfo
                    }
                }
            }
        }
        stage('Branch Artifactory Deploy') {
            options {
                timeout(time: 5, unit: 'MINUTES')
            }
            when {
                not {
                    anyOf {
                        branch "master"
                        branch "develop"
                    }
                }
            }
            steps {
                script {
                    if (env.GIT_URL) {
                        repoName = ("${env.GIT_URL}" =~ /(.*\/)(.*)(\.git)/)[ 0 ][ 2 ]
                        findFiles(glob: 'target/*.jar').each { item ->
                            if (!(item ==~ /.*\/[fs]b-contrib-.*?.jar/) && !(item ==~ /.*\/findsecbugs-plugin-.*?.jar/) && !(item ==~ /.*-sources.jar/) && !(item ==~ /.*-javadoc.jar/)) {
                                sh "./jfrog rt u \"${item.path}\" branch-artifacts/${env.BRANCH_NAME}/${repoName}/java/ --recursive=false --build-name ${env.BRANCH_NAME} --build-number ${env.BUILD_NUMBER} --props \"vcs.revision=${env.GIT_COMMIT};repo.name=${repoName}\""
                            }
                        }
                        findFiles(glob: '**/pom.xml').each { item ->
                            def pomPath = item.path.replace('\\','/')
                            if (!(pomPath ==~ /.*target.*/)) {
                                def resPomName = "main.pom"
                                def subDirMatcher = (pomPath =~ /^.*(?<=\/|^)(.*)\/pom\.xml/)
                                if (subDirMatcher.matches()) {
                                    resPomName = "${subDirMatcher[ 0 ][ 1 ]}.pom"
                                }
                                sh "./jfrog rt u \"${item.path}\" branch-artifacts/${env.BRANCH_NAME}/${repoName}/java/${resPomName} --recursive=false --build-name ${env.BRANCH_NAME} --build-number ${env.BUILD_NUMBER} --props \"vcs.revision=${env.GIT_COMMIT};repo.name=${repoName}\""
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'One way or another, I have finished \uD83E\uDD16'
        }
        success {
            echo 'I succeeeded! \u263A'
            cleanWs deleteDirs: true
        }
        unstable {
            echo 'I am unstable \uD83D\uDE2E'
        }
        failure {
            echo 'I failed \uD83D\uDCA9'
        }
        changed {
            echo 'Things were different before... \uD83E\uDD14'
        }
        fixed {
            script {
                if (env.BRANCH_NAME.contains('master') || env.BRANCH_NAME.contains('develop')) {
                    slackNotifier("#ci", currentBuild.currentResult, "${env.BRANCH_NAME} - Back to normal")
                }
            }
        }
        regression {
            script {
                if (env.BRANCH_NAME.contains('master') || env.BRANCH_NAME.contains('develop')) {
                    slackNotifier("#ci", currentBuild.currentResult, "${env.BRANCH_NAME} - First failure")
                }
            }
        }
    }

}
