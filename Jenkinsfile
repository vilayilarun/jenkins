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
                  ${env.BRANCH_NAME} == 'master'
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
                 ${env.BRANCH_NAME} == 'master'
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
                ${env.BRANCH_NAME} == 'master'
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
