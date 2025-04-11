pipeline {
    agent any

    environment {
        ANDROID_SDK_ROOT = "/path/to/android/sdk"  // Update this path
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
                        reportDir: 'app/build/reports/lint-results-debug',
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