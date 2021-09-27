pipeline{
    agent any
    }
    stages{
        stage("Testing the code"){
           steps{
              script{
              echo "testing the code on $BRANCH_NAME."
              }
           }
        }
        stage("building the code "){
            when{
              expression{
                BRANCH_NAME == 'master'
              }
            }
            steps{
                script{
                    echo 'Building the jar file'
                    //sh 'mvn package'
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
                   // gv.imagebuild()
                   //sh 'docker build -t 192.168.179.131:8083/java-manen-app:1.1 .'
                  echo "Building the docker image" 
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
                   echo "Depoying the build"
                }
            }
        }
}

