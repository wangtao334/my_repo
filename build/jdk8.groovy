node {
  def mvnHome
  stage('Preparation') {
    mvnHome = tool 'M3'
    println "mvnHome = " + mvnHome
  }
}
