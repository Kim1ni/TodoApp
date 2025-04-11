pipeline {
    agent any

    environment {
        ANDROID_SDK_ROOT = "/home/gabriel-kimani/Android/Sdk"  // Update this path
    }

    tools {
        jdk 'JDK17' // Replace 'JDK17' with the name you gave to your JDK 17 configuration in Jenkins
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean assembleDebug'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test'
            }
            post {
                always {
                    junit '**/build/test-results/**/*.xml'
                }
            }
        }

        stage('Instrumented Tests') {
            steps {
                sh './gradlew connectedAndroidTest'
            }
            post {
                always {
                    junit '**/build/outputs/androidTest-results/**/*.xml'
                }
            }
        }

        stage('Static Code Analysis') {
            steps {
                sh './gradlew lint'
            }
            post {
                always {
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'app/build/reports',
                        reportFiles: 'index.html',
                        reportName: 'Lint Report'
                    ])
                }
            }
        }

        stage('Build APK') {
            steps {
                sh './gradlew assembleRelease'
                archiveArtifacts artifacts: 'app/build/outputs/apk/release/*.apk', fingerprint: true
            }
        }
    }
}
