def scmCheckout(repo) {
    if (repo == null || repo.trim().isEmpty()) {
        throw new IllegalArgumentException("Repository name must not be null or empty")
    }
    
    def repoPath = "${env.WORKSPACE}/${repo}"
    
    checkout([$class: 'GitSCM', branches: [[name: '*/main']],
              userRemoteConfigs: [[credentialsId: 'gitlab-automation', url: "https://gitlab.workz.com:31443/workz/${repo}.git"]],
              extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: repoPath]]])
}

def push_image(name) {
    def dockerPushCommand = [
        "docker",
        "push",
        "${env.DOCKER_REPO}/${name}:${env.IMAGE_TAG}"
    ]
    executeShellCommand(dockerPushCommand)
}

def buildImage(name, dockerfile, directory, buildArgs = '') {
    def targetDirectory = "${WORKSPACE}/${directory}"
    def dockerBuildCmd = [
        "docker",
        "build",
        "-t",
        "${env.DOCKER_REPO}/${name}:${env.IMAGE_TAG}",
        "-f", "${targetDirectory}/${dockerfile}",
        buildArgs,
        "." 
    ].findAll { it }
  
    try {
        executeShellCommand(dockerBuildCmd, targetDirectory)
    } catch (Exception e) {
        error "Failed to execute Docker build: ${e.message}"
    }
}

def trivyScan(imageName, filename) {
    echo "Starting Trivy vulnerability scanner ${imageName}"
    def trivyJsonCommand = ["trivy", "image", "${imageName}", "--format=json", "-o", "${WORKSPACE}/${filename}.json"]
    execCommand(trivyJsonCommand)

    def trivyHtmlCommand = ["trivy", "image", "${imageName}", "--format", "template", "--template", "@/var/lib/jenkins/contrib/html1.tpl", "-o", "${WORKSPACE}/${filename}.html"]
    execCommand(trivyHtmlCommand)
}

def execCommand(command) {
    def processBuilder = new ProcessBuilder(command.collect { it.toString() })
    processBuilder.redirectErrorStream(true)

    def process = processBuilder.start()
    def reader = new BufferedReader(new InputStreamReader(process.inputStream))
    String line
    while ((line = reader.readLine()) != null) {
        echo line
    }
    reader.close()
    int exitCode = process.waitFor()

    if (process.exitValue() != 0) {
        error("Failed to execute commands.")
    }
}


def Deploy(selectedSite) {
    // Define site-specific configurations
    def playbooks = [
        "Dubai": "dubai-playbook.yaml",
        "US": "us-playbook.yaml",
        "France": "france-playbook.yaml"
    ]

    def helmValues = [
        "Dubai": "dubai.yaml",
        "US": "us.yaml",
        "France": "france.yaml"
    ]

    // Retrieve configurations based on the selected site
    def ansiblePlaybook = playbooks[selectedSite]
    def customValuesFile = helmValues[selectedSite]

    if (!ansiblePlaybook || !customValuesFile) {
        error("Invalid site selection: ${selectedSite}. No playbook or Helm values file found.")
    }

    echo "Starting deployment for ${selectedSite} using ${ansiblePlaybook} and ${customValuesFile}."

    // Login to GCP
    def gcloudLoginCommand = ["gcloud", "config", "set", "project", "workz-eim"]
    executeShellCommand(gcloudLoginCommand)

    // Copy Helm chart and custom values file to the bastion host
    def helmChartCopyCommand = ["gcloud", "compute", "scp", "--zone=europe-west3-b", "${WORKSPACE}/eSIM-3.0.0.tgz", "app-cluster-bastion:/tmp"]
    executeShellCommand(helmChartCopyCommand)

    def helmValuesCopyCommand = ["gcloud", "compute", "scp", "--zone=europe-west3-b", "${WORKSPACE}/${customValuesFile}", "app-cluster-bastion:/tmp"]
    executeShellCommand(helmValuesCopyCommand)

    // Run Ansible playbook for the selected site
    def ansibleCommand = ["ansible-playbook", "${WORKSPACE}/${ansiblePlaybook}", "-e", "image_tag=${imageTag}"]
    executeShellCommand(ansibleCommand)

    // Deploy Helm release using the custom values file
    def helmDeployCommand = [
        "gcloud", "compute", "ssh", "app-cluster-bastion", "--zone=europe-west3-b", 
        "--command=sudo helm upgrade esim-iot /tmp/eSIM-3.0.0.tgz -f /tmp/${customValuesFile} -n esim-iot"
    ]
    executeShellCommand(helmDeployCommand)

    echo "Deployment for ${selectedSite} completed successfully."
}

// def deployCommand(command) {
//     def processBuilder = new ProcessBuilder(command.collect { it.toString() })
//     processBuilder.redirectErrorStream(true)

//     def process = processBuilder.start()
//     def reader = new BufferedReader(new InputStreamReader(process.inputStream))
//     String line
//     while ((line = reader.readLine()) != null) {
//         echo line
//     }
//     reader.close()
//     int exitCode = process.waitFor()

//     if (process.exitValue() != 0) {
//         error("Deployment failed.")
//     }
// }

def feedback(){
    withCredentials([usernamePassword(credentialsId: 'ansible', usernameVariable: 'ANSIBLE_USERNAME', passwordVariable: 'ANSIBLE_PASSWORD')]){
        def ansibleUsername = env.ANSIBLE_USERNAME
        def ansiblePassword = env.ANSIBLE_PASSWORD       
        def ansibleCommand = [
            "ansible-playbook", "${WORKSPACE}/feedback-k8s.yaml"
        ]
        executeShellCommand(ansibleCommand)
    }   
}

def scmPush() {
    withCredentials([usernamePassword(credentialsId: 'gitlab-automation', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
        def gitUsername = env.GIT_USERNAME
        def gitPassword = env.GIT_PASSWORD
        def gitUrl = "https://${gitUsername}:${gitPassword}@gitlab.workz.com:31443/workz/esim-deployment"
        
        dir('${WORKSPACE}') {
            def gitAddCmd = 'git add custom-values.yaml'
            def gitCommitCmd = "git commit -m 'Updated'"
            def gitChekOut = "git checkout -b QA"
            def gitMerge = "git merge main"
            def gitPushCmd = 'git push origin QA'
            
            executeShellCommand(gitAddCmd)
            executeShellCommand(gitCommitCmd)
            executeShellCommand(gitChekOut)
            executeShellCommand(gitMerge)
            executeShellCommand(gitPushCmd)
        }
    }
}


// def helm() {
//     def buildCommand = ["helm", "dependency", "build", "${WORKSPACE}/mesh"]

//     executeCommand(buildCommand)

//     def packageCommand = ["helm", "package", "${WORKSPACE}/mesh", "--version", "2.0.${BUILD_ID}", "--destination", "${WORKSPACE}", "--app-version", "2.0.${BUILD_ID}"]
//     executeCommand(packageCommand)

//     withCredentials([usernamePassword(credentialsId: 'helm', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
//         def nexusUsername = env.NEXUS_USERNAME
//         def nexusPassword = env.NEXUS_PASSWORD
//         def helmCommand = [
//             "helm",  "nexus-push", "ota-staging", "-u" , nexusUsername, "-p", nexusPassword, "${WORKSPACE}/mesh-2.0.${BUILD_ID}.tgz"
//         ]
//         // echo "push command: ${helmCommand}"
//         executeShellCommand(helmCommand)
//     }
// }

def executeShellCommand(command, workingDir = null) {
    def processBuilder = new ProcessBuilder(command.collect { it.toString() })
    processBuilder.redirectErrorStream(true)
    
    if (workingDir) {
        processBuilder.directory(new File(workingDir))
    }

    def process = processBuilder.start()
    def reader = new BufferedReader(new InputStreamReader(process.inputStream))
    String line
    while ((line = reader.readLine()) != null) {
        echo line
    }
    reader.close()
    int exitCode = process.waitFor()

    if (process.exitValue() != 0) {
        throw new RuntimeException("Error executing command: ${command}\n${process.err.text}")
    }
}

return this