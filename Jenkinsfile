#!/usr/bin/env groovy
@Library('jenkins-shared-lib')
def gv
pipeline{
    agent any
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
                   env.IMAGE = input message: 'please enter the image tag with repository', parameters: [string(defaultValue: '',
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
                    env.IMAGE = input message: 'please enter the image tag with repository', parameters: [string(defaultValue: '',
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
                  env.IMAGE = input message: 'please enter the image tag with repository', parameters: [string(defaultValue: '',
        description: '', name: 'Image name')]
                  deployapp "${env.IMAGE}"
             }  
          }  
         }
    } 
}
