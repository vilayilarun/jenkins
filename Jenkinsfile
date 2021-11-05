#!/usr/bin/env groovy
library identifier: 'jenkins-shared-lib@master', retriever: modernSCM(
    [$class: 'GitSCMSource',
     remote: 'https://github.com/vilayilarun/jenkins-shared-lib.git',
     credentialsId: 'gihub'
    ]
)
// @Library('jenkins-shared-lib')
def gv
pipeline{
    agent any
    environment {
        IMAGE = '35.200.245.75:8083/myapp:1.0.0'
    }
    
    stages{
        stage("Loading groovi script"){
           steps{
              script{
              gv = load "script.groovy"
              }
           }
        }
        stage("Testing the code"){
            steps{
                script{
                gv.testapp()
                }
            }
        }
        stage("Build docker image"){
             //when{
              //expression{
              //  BRANCH_NAME == 'master'
              //}
            //}
            steps{
                script{
                   // env.IMAGE = input message: 'please enter the image tag with repository', parameters: [string(defaultValue: '',
        description: '', name: 'Image name')]
                   buildapp "${env.IMAGE}"
                }
            }
        }
        stage("push the image to nexus repository"){
             //when{
             // expression{
               // BRANCH_NAME == 'master'
              //}
            //}
            steps{
                script{
                   //  env.IMAGE = input message: 'please enter the image tag with repository', parameters: [string(defaultValue: '',
        description: '', name: 'Image name')]
                    pushapp "${env.IMAGE}"
                }
            }
        }
       stage("Deploy the image on production"){
           // when{
             // expression{
              // BRANCH_NAME == 'master'
           //}
          //} 
           steps{
              script{
                //  env.IMAGE = input message: 'please enter the image tag with repository', parameters: [string(defaultValue: '',
        description: '', name: 'Image name')]
                  deployapp "${env.IMAGE}"
             }  
          }  
         }
    } 
}
