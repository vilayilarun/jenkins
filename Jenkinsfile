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
             when{
              expression{
                BRANCH_NAME == 'master'
              }
            }
            steps{
                script{
                   gv.buildapp()
                }
            }
        }
        stage("push the image to nexus repository"){
             when{
              expression{
                BRANCH_NAME == 'master'
              }
            }
            steps{
                script{
                   gv.pushapp()
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
