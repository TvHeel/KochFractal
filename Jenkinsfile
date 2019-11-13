pipeline {
    agent any

    stages {
        stage ('clean') {
            steps {
                withMaven(maven : 'Maven 3.6.2') {
                    sh 'mvn clean'
                }
            }
        }
        stage ('compile') {
            steps {
                withMaven(maven : 'Maven 3.6.2') {
                    sh 'mvn compile'
                }
            }
        }
        stage ('test') {
            steps {
                withMaven(maven : 'Maven 3.6.2') {
                    sh 'mvn test'
                }
            }
        }
    }
}