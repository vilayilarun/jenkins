def testapp() {
    echo "Testing the code"
}

def buildapp() {
    //sh 'docker-compose -d --build'
    // sh 'docker build -t 192.168.179.131:8083/myapp:1.0 .'
   def customeImage = dockr.build("vilayilarun/max:helloworld-python-${env.BUILD_ID}")
}

def update_manifest() {
    def maniFestContent = readFile 'nginx-k8s.yaml'
    def newTag = '${customeImage}'
    for (def deployment in deployments) {
        if (deployment.contains('nginx')){
            def updatedDeployment = deployment.replaceAll(/image:\s*.*nginx:)(\S+)/, "\$1$newTag")
        }
    }
}

def pushapp() {
    // withCredentials([usernamePassword(credentialsId: 'github', usernameVariable: 'USER', passwordVariable: 'PWD')]){
    // sh "echo $PWD | docker login -u $USER --password-stdin "
    // doc
    // }
    docker.withRegistry('','docker'){
        customeImage.push();
    }
}
def deployapp() {
    sh 'echo deploying this'
}

return this 
