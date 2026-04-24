pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // Checks out the source code from the repository linked to the Jenkins job
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building the Spring Boot application...'
                // Compiles the Java source code
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                echo 'Running Unit and Integration tests...'
                // Runs the tests to ensure the application works perfectly
                sh 'mvn test'
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging the application into a standalone JAR file...'
                // Packages the compiled code into a runnable .jar file
                sh 'mvn package -DskipTests'
            }
        }
    }

    post {
        success {
            echo 'Build completed successfully!'
            // Archives the built JAR file so you can download it directly from Jenkins
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
        failure {
            echo 'Build failed. Please check the Jenkins logs for errors.'
        }
        always {
            echo 'Pipeline execution finished.'
        }
    }
}
