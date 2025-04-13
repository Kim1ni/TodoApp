pipeline {
    agent any
    
    environment {
        // Define environment variables for this pipeline
        ANDROID_HOME = "/home/jenkins/android-sdk"
        JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64"
        GRADLE_OPTS = "-Dorg.gradle.daemon=false -Dorg.gradle.caching=true -Dorg.gradle.parallel=true"
        BUILD_NUMBER_FORMATTED = "${BUILD_NUMBER.padLeft(3, '0')}"
    }
    
    options {
        // Pipeline-specific options
        timeout(time: 1, unit: 'HOURS')
        ansiColor('xterm')
        disableConcurrentBuilds()
    }
    
    stages {
        stage('Environment Setup') {
            steps {
                sh '''
                    # Create workspace Android SDK directory
                    export ANDROID_SDK="${WORKSPACE}/android-sdk"
                    mkdir -p ${ANDROID_SDK}
                    
                    # Download and install Android command-line tools if needed
                    if [ ! -f "${ANDROID_SDK}/cmdline-tools/latest/bin/sdkmanager" ]; then
                        # Create temp directory
                        mkdir -p /tmp/android-sdk-download
                        cd /tmp/android-sdk-download
                        
                        # Download command-line tools
                        wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
                        
                        # Extract
                        unzip -o commandlinetools-linux-*_latest.zip
                        
                        # Create proper structure for Android SDK tools 
                        mkdir -p ${ANDROID_SDK}/cmdline-tools
                        # The key fix: Move contents to right place
                        mv cmdline-tools ${ANDROID_SDK}/cmdline-tools/latest
                        
                        # Cleanup
                        cd -
                        rm -rf /tmp/android-sdk-download
                    fi
                    
                    # Make sure SDK tools are executable
                    chmod +x ${ANDROID_SDK}/cmdline-tools/latest/bin/*
                    
                    # Install required SDK components
                    export PATH="${ANDROID_SDK}/cmdline-tools/latest/bin:${PATH}"
                    yes | ${ANDROID_SDK}/cmdline-tools/latest/bin/sdkmanager --licenses || true
                    ${ANDROID_SDK}/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.2"
                '''
            }
        }
        
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Clean') {
            steps {
                sh './gradlew clean'
            }
        }
        
        stage('Build Debug') {
            steps {
                sh './gradlew assembleDebug --stacktrace'
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh './gradlew test --stacktrace'
            }
            post {
                always {
                    junit '**/build/test-results/**/*.xml'
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'app/build/reports/tests/testDebugUnitTest',
                        reportFiles: 'index.html',
                        reportName: 'Unit Test Report'
                    ])
                }
            }
        }
        
        stage('Code Analysis') {
            parallel {
                stage('Lint') {
                    steps {
                        sh './gradlew lintDebug --stacktrace'
                    }
                    post {
                        always {
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'app/build/reports',
                                reportFiles: 'lint-results-debug.html',
                                reportName: 'Lint Report'
                            ])
                        }
                    }
                }
                
                stage('Detekt') {
                    steps {
                        sh './gradlew detekt --stacktrace || true'
                    }
                    post {
                        always {
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'app/build/reports/detekt',
                                reportFiles: 'detekt.html',
                                reportName: 'Detekt Report'
                            ])
                        }
                    }
                }
            }
        }
        
        stage('Start Emulator') {
            steps {
                sh '''
                    # Start the emulator in the background
                    nohup $ANDROID_HOME/emulator/emulator -avd test_emulator -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect &
                    
                    # Get the emulator process ID
                    EMU_PID=$!
                    
                    # Wait for emulator to boot completely (timeout after 180 seconds)
                    echo "Waiting for emulator to boot..."
                    BOOT_TIMEOUT=180
                    BOOT_COMPLETED=false
                    for i in $(seq 1 $BOOT_TIMEOUT); do
                        if $ANDROID_HOME/platform-tools/adb shell getprop sys.boot_completed | grep -q "1"; then
                            BOOT_COMPLETED=true
                            echo "Emulator booted successfully after $i seconds"
                            break
                        fi
                        sleep 1
                    done
                    
                    if [ "$BOOT_COMPLETED" = false ]; then
                        echo "Emulator failed to boot within timeout"
                        exit 1
                    fi
                    
                    # Unlock the screen
                    $ANDROID_HOME/platform-tools/adb shell input keyevent 82
                    
                    # Store the emulator PID for later cleanup
                    echo $EMU_PID > .emulator_pid
                '''
            }
        }
        
        stage('Instrumented Tests') {
            steps {
                sh './gradlew connectedAndroidTest --stacktrace'
            }
            post {
                always {
                    junit '**/build/outputs/androidTest-results/**/*.xml'
                    publishHTML([
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'app/build/reports/androidTests/connected',
                        reportFiles: 'index.html',
                        reportName: 'Instrumented Test Report'
                    ])
                    
                    // Stop the emulator
                    sh '''
                        if [ -f .emulator_pid ]; then
                            EMU_PID=$(cat .emulator_pid)
                            if kill -0 $EMU_PID 2>/dev/null; then
                                kill $EMU_PID
                                echo "Emulator stopped"
                            fi
                            rm .emulator_pid
                        fi
                        
                        # Additional cleanup just in case
                        $ANDROID_HOME/platform-tools/adb devices | grep emulator | cut -f1 | while read device; do
                            $ANDROID_HOME/platform-tools/adb -s $device emu kill
                        done
                    '''
                }
            }
        }
        
        stage('Build Release') {
            steps {
                sh './gradlew assembleRelease --stacktrace'
            }
        }
        
        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'app/build/outputs/apk/**/*.apk', fingerprint: true
                echo "Debug and Release APKs have been archived"
            }
        }
        
        stage('Deploy to S3') {
            when {
                branch 'master'
            }
            steps {
                withAWS(region: 'eu-north-1', credentials: 'aws-credentials') {
                    sh '''
                        # Create version name from git and build number
                        VERSION_NAME=$(git describe --always --tags)-b${BUILD_NUMBER_FORMATTED}
                        
                        # Upload to S3
                        aws s3 cp app/build/outputs/apk/release/app-release.apk s3://your-bucket-name/todo-app/$VERSION_NAME/app-release.apk
                        
                        # Save the latest version name to a file
                        echo $VERSION_NAME > latest_version.txt
                        aws s3 cp latest_version.txt s3://your-bucket-name/todo-app/latest_version.txt
                        
                        echo "Uploaded APK to S3 with version $VERSION_NAME"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            // Clean up workspace to save disk space
            cleanWs(cleanWhenNotBuilt: false,
                    deleteDirs: true,
                    disableDeferredWipeout: true,
                    notFailBuild: true,
                    patterns: [[pattern: 'app/build/outputs/apk/**', type: 'INCLUDE']])
        }
        
        success {
            echo 'Build and tests completed successfully!'
            /*
            // Slack notification example (requires Slack Notification plugin)
            slackSend(
                color: 'good',
                message: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})"
            )*/
        }
        
        failure {
            echo 'Build or tests failed!'
            /*
            // Slack notification example
            slackSend(
                color: 'danger',
                message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})"
            )*/
        }
    }
}

