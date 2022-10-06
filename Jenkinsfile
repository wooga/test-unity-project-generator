#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([string(credentialsId: 'snyk-wooga-frontend-integration-token', variable: 'SNYK_TOKEN')])
{
    buildJavaLibraryOSSRH()
}
