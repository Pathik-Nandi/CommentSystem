pipeline {
    agent any
    tools{
        maven 'maven'
    }

    stages{
        stage('Build Maven'){
            steps{
                checkout scmGit(branches: [[name: '*/comment-hub-release']], extensions: [], userRemoteConfigs: [[credentialsId: 'a8c817df-3929-48c4-b679-0b9f48206eb7', url: 'https://git.idc.tarento.com/nsdc/comment-hub.git']])
                sh 'mvn clean install'
            }
        }
        stage('Build docker image'){
            steps{
                script{
                    sh 'docker build -t pruthvi1902/comment-hub .'
                }
            }
        }
        stage('Push image to hub'){
            steps{
                script{
                    withCredentials([string(credentialsId: 'dockerhub', variable: 'dockerhubpwd')]) {
                    sh 'docker login -u pruthvi1902 -p ${dockerhubpwd}'

                    }
                    sh 'docker push pruthvi1902/comment-hub'
                }
            }
        }
}
}
