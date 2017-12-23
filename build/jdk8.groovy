node {
  stage('Preparation') {
    println "MAVEN_HOME = " + MAVEN_HOME
    println "WORKSPACE = " + WORKSPACE
  }
  
  stage('Checkout') {
    checkout scm
  }
}
