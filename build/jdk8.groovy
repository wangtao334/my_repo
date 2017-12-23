node {
  stage('Preparation') {
    println "MAVEN_HOME = " + MAVEN_HOME
    println "WORKSPACE = " + WORKSPACE
    sh 'mvn -version'
  }
  
  stage('Checkout') {
    checkout scm
  }
}
