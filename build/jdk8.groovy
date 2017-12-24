def changeMap = [:]
def mvnHome
def antHome
def javaHome
node {
  stage('Preparation') {
    mvnHome = MAVEN_HOME
    println "MAVEN_HOME = " + mvnHome
    sh 'mvn -version'
    antHome = ANT_HOME
    println "ANT_HOME = " + antHome
    sh 'ant -version'
    javaHome = JAVA_HOME
    println "JAVA_HOME = " + javaHome
    sh 'java -version'
  }
  
  stage('Checkout') {
    checkout scm    
    // check change.
    def changeLogSets = currentBuild.changeSets
    for(int i = 0; i < changeLogSets.size(); i++) {
      def entries = changeLogSets[i].items
      for(int j =0; j < entries.length; j++) {
        def entry = entries[j]
        def files = new ArrayList(entry.affectedFiles)
        for(int k = 0; k < files.size(); k++) {
          def file = files[k]
          def projectName = file.path.substring(0, file.path.indexOf("/"))
          if(projectName.startsWith("build00")) {
            continue
          }
          def filePath = file.path.substring(file.path.indexOf("/"))
          def editType = file.editType.name
          if(!changeMap.containsKey("${projectName}")) {
            changeMap."${projectName}" = [:]
            changeMap."${projectName}"."${filePath}" = editType
          } else {
            changeMap."${projectName}"."${filePath}" = editType
          }
        }
      }
    }
    println "Change Project Count : " + changeMap.size()
    changeMap.each {
      projectNameEntry -> 
        println projectNameEntry.key
        projectNameEntry.value.each {
          fileEntry -> 
            println fileEntry.value + " : " +fileEntry.key
        }
    }
  }
  if(changeMap.size() > 0) {
    stage('Build') {
      changeMap.each {
        key,value ->
          println key
          sh 'ant -f ${WORKSPACE}/"${key}"/build.xml'
      }
    }
  }
}
