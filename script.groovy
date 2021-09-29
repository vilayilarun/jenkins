def testapp() {
    echo "Testing the code"
}

//def buildapp() {
    //sh 'docker-compose -d --build'
   // sh 'docker build -t 192.168.179.131:8083/myapp:1.0 .'
//}

//def pushapp() {
   // withCredentials([usernamePassword(credentialsId: 'Nexus-repo', usernameVariable: 'USER', passwordVariable: 'PWD')]){
    //sh "echo $PWD | docker login -u $USER --password-stdin 192.168.179.131:8083"
    //sh 'docker push 192.168.179.131:8083/myapp:1.0'
    //}
//}
def deployapp() {
    sh 'echo deploying this'
}

return this 
