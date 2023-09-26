#!/usr/bin/env groovy
@Library('pipeline-library')_

def repoName = "rups"
def dependencyRegex = "itextcore"

automaticJavaBuild(repoName, dependencyRegex)

pipeline {
    agent any

    stages {
        stage('test') {
            steps {
                sh 'Xvfb -ac :99 -screen 0 1280x1024x16 & export DISPLAY=:99'
            }
        }
    }
}