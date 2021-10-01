#!/usr/bin/env groovy
@Library('jenkins-shared-lib')
def gv
pipeline{
    agent any
    stages{
        env.IMAGE = input message: 'please enter the image tag with repository', parameters: [string(defaultValue: '',
        description: '', name: 'Image name')]
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
                    pushapp "${env.IMAGE}"
                }
            }
        }
       stage("Deploy the image on production"){
            when{
              expression{
               BRANCH_NAME == 'master'
           }
          } 
           steps{
              script{
               gv.deployapp()
             }  
          }  
         }
    } 
}
