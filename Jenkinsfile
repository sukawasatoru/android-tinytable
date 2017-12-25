/*
 * Copyright 2017 sukawasatoru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

pipeline {
    agent {
        label 'android'
    }

    stages {
        stage('Build') {
            steps {
                sh './gradlew --info --profile build'
            }
        }

        stage('Report') {
            steps {
                withCredentials([string(credentialsId: '5f36d99d-2541-46a5-9ee1-94a9e2ea338e', variable: 'SONAR_LOGIN')]) {
                    sh """
                        ./gradlew --info \
                        -Dsonar.organization=sukawasatoru-github \
                        -Dsonar.host.url=https://sonarcloud.io \
                        -Dsonar.login=${SONAR_LOGIN} \
                        -Dsonar.projectName=android-tinytable \
                        -Dsonar.projectKey=android-tinytable \
                        -Dsonar.branch.name=${env.BRANCH_NAME} \
                        -Dsonar.verbose=true \
                        sonarqube
                        """
                }
            }
        }
    }
}
