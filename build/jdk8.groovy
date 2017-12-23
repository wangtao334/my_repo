node {
  def mvnHome
  stage('Preparation') {
    println "${MAVEN_HOME}"
    mvnHome = tool 'M3'
    println "mvnHome = " + mvnHome
  }
}
