node {
  stage('Preparation') {
    println "MAVEN_HOME = " + MAVEN_HOME
    println "WORKSPACE = " + WORKSPACE
    sh 'maven -version'
  }
  
  stage('Checkout') {
    checkout scm
  }
}
