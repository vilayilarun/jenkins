pipeline {
    agent any
    parameters {
        string(name: 'ENV', description: 'Environment Variable for Backend API', defaultValue: "/api")
    }
    environment {
        DOCKER_REPO = "europe-west3-docker.pkg.dev/workz-eim/esim-iot"
        DOCKER_REPO_DUBAI = "nexus:8080"
        DOCKER_REPO_US = "earuop-us"
        DOCKER_REPO_FRANCE = "france-registry"
        GIT_BRANCH = 'main'
        JAVA_OPTS = '-Dhudson.model.ParametersAction.keepUndefinedParameters=true'
        TIMESTAMP = new Date().format("yyyyMMddHHmmss", TimeZone.getTimeZone("UTC"))
        IMAGE_TAG = "${env.GIT_BRANCH}-${env.BUILD_ID}"
        GIT_REPO_URL = "https://gitlab.workz.com:31443/workz/esim-deployment"
        REPO_DIR = "${env.WORKSPACE}"
        HELM_CHART = "frontend-helm-chart"
        ANSIBLE_PLAYBOOK = "deploy.yml"
        DEPLOY_SITE_ORDER = 'Dubai,US,France'
    }  
    stages {
        stage('Check for Running Builds') {
            steps {
                script {
                    def currentJob = Jenkins.instance.getItemByFullName(env.JOB_NAME)
                    for (build in currentJob.builds) {
                        if (build.isBuilding() && build != currentBuild.rawBuild) {
                            echo "Aborting running build #${build.number}"
                            build.doKill()
                        }
                    }
                }
            }
        }
        stage('Approval to Begin') {
            steps {
                script {
                    def emailSubject = "eSIM-IOT ${env.GIT_BRANCH} Pipeline Begin Approval Request"
                    def emailBody = """
                        Hello,<br><br>

                        We kindly request your approval to initiate the CI/CD pipeline for the latest changes.

                        Please select the order in which you would like the images to be built: <br><br>
                        - Dubai
                        - US
                        - France

                        <br><br>
                        Please click the link below to approve or reject the deployment: <br><br>
                        <a href='${BUILD_URL}input'>Click here to approve or reject</a> <br><br>
                        
                        Thank you,<br>
                        Jenkins
                    """

                    emailext(
                        subject: emailSubject,
                        body: emailBody,
                        to: 'devopsvilayil@gmail.com',
                        mimeType: 'text/html'
                    )
                }
            }
        }

        stage('Approval wait') {
            steps {
                script {
                    // Ask user to select the site order
                    def approval = input(
                        id: 'deploymentApproval',
                        message: 'Please approve or reject the CI/CD and select the site order:',
                        parameters: [
                            choice(name: 'DEPLOY_SITE_ORDER', choices: ['Dubai, US, France', 'Dubai, France, US', 'US, Dubai, France', 'US, France, Dubai', 'France, Dubai, US', 'France, US, Dubai'], description: 'Select the build order')
                        ]
                    )

                    // Store the selected deployment order
                    def selectedOrder = approval?.DEPLOY_SITE_ORDER?.split(',')?.collect { it.trim() }
                    env.SITE_ORDER = selectedOrder.join(',')

                    if (approval == 'Approve') {
                        resultIsApproved = true
                    } else {
                        resultIsApproved = false
                        def emailSubject = "Jenkins Job eSIM-IOT ${env.BUILD_ID} "
                        def emailBody = """
                        <html>
                        <head>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                margin: 20px;
                            }
                            h1 {
                                color: #333;
                            }
                        </style>
                        </head>
                        <body>
                            <h1>Ohoo!<br><br> </h1>
                            Hello All,<br><br> 
                            The Jenkins Job ${env.BUILD_ID} for eSIM-IOT has been rejected by the approver.<br><br> 
                            IT Team
                        </body>
                        </html>
                        """
                        
                        emailext(
                            subject: emailSubject,
                            body: emailBody,
                            to: 'it.support@workz.com',
                            mimeType: 'text/html'
                        )
                    }
                }
            }
        }


        stage("Load Groovy script") {
            when{
                expression { resultIsApproved }
            }
            steps {
                script {
                    echo "Loading the groovy script"
                    gv = load "script.groovy"
                }
            }
        }
        stage("Retrive previous build_id") {
            steps {
                script {
                    def previousBuild = currentBuild.previousBuild
                    if (previousBuild != null) {
                        def previousBuildId = previousBuild.id
                        echo "Previous Build ID: ${previousBuildId}"
                        env.PREVIOUS_BUILD_ID = previousBuildId
                    } else {
                        echo "No previous build found"
                        env.PREVIOUS_BUILD_ID = "N/A"
                    }                    
                }
            }
        }
        stage("SCM Checkout") {
            steps {
                script {
                    def repositories = [
                        ["name": "eSIM-IoT-frontend", "url": "https://gitlab.workz.com:31443/workz/eSIM-IoT-frontend.git"],
                        ["name": "eSIM-IOT-API-BACKEND", "url": "https://gitlab.workz.com:31443/workz/esim-iot-backend.git"],
                        ["name": "eSIM-IOT-CORE-SERVER", "url": "https://gitlab.workz.com:31443/workz/LPAe-IOT-Server.git"],
                        ["name": "eSIM-IOT-DB-JOB", "url": "https://gitlab.workz.com:31443/workz/esim-iot-db-job.git"],
                        ["name": "eSIM-IOT-API-WORKER", "url": "https://gitlab.workz.com:31443/workz/esim-iot-backend.git"],
                        ["name": "eSIM-IOT-API-NGINX-BACKEND", "url": "https://gitlab.workz.com:31443/workz/esim-iot-backend.git"]
                    ]
                    
                    for (def repo in repositories) {
                        dir(repo.name) {
                            def changes = checkout([$class: 'GitSCM', branches: [[name: '*/main']], userRemoteConfigs: [[credentialsId: 'gitlab-automation', url: repo.url]]]).GIT_COMMIT
                            if (changes != null && changes != '') {
                                currentBuild.description = "${repo.name} changes detected"
                            }
                        }
                    }
                }
            }
        }
        stage("Image Build") {
            steps {
                script {
                    def repositories = [
                        ["name": "eSIM-IoT-frontend", "dockerfile": "Dockerfile", "image": "esim-iot-nginx-frontend", "buildArgs": "--build-arg ENV_BACKEND_API_URL=${env.ENV}"],
                        ["name": "eSIM-IOT-API-BACKEND", "dockerfile": "Dockerfile", "image": "esim-iot-api-backend"],
                        ["name": "eSIM-IOT-API-WORKER", "dockerfile": "Dockerfile_worker", "image": "esim-iot-api-worker"],
                        ["name": "eSIM-IOT-API-NGINX-BACKEND", "dockerfile": "Dockerfile_nginx", "image": "esim-iot-api-nginx-backend"],
                        ["name": "eSIM-IOT-CORE-SERVER", "dockerfile": "Dockerfile", "image": "esim-iot-core-server"],
                        ["name": "eSIM-IOT-DB-JOB", "dockerfile": "Dockerfile", "image": "esim-iot-db-job"]
                    ]
                    
                    // Process each site in the selected order
                    def selectedSites = env.SITE_ORDER.split(',')
                    
                    for (def site in selectedSites) {
                        // Build and push images for each site
                        echo "Building images for ${site} site"
                        
                        for (def repo in repositories) {
                            dir(repo.name) {
                                def changes = currentBuild.description.contains("${repo.name}")
                                
                                if (!changes) {
                                    currentBuild.description += " ${repo.name}"
                                    changes = true
                                }

                                if (changes) {
                                    echo "Building and pushing ${repo.image} image for ${site}"
                                    try {
                                        // Use buildArgs if defined, otherwise set it to an empty string
                                        def buildArgs = repo.buildArgs ? repo.buildArgs : ""
                                        docker.build("${env."DOCKER_REPO_${site}"}:${repo.image}:${env.IMAGE_TAG}", "-f ${repo.dockerfile} ${buildArgs} .")
                                        env."${repo.name}_BUILD_SUCCESS" = true
                                        docker.push("${env."DOCKER_REPO_${site}"}:${repo.image}:${env.IMAGE_TAG}")
                                    } catch (Exception e) {
                                        echo "Image build for ${repo.image} in ${site} failed: ${e.getMessage()}"
                                        env."${repo.name}_BUILD_SUCCESS" = false
                                    }
                                } else {
                                    echo "No changes detected in ${repo.name}, skipping build."
                                }
                            }
                        }
                    }
                }
            }
        }

    }
   
}