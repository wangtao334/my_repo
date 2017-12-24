node {
  stage('Preparation') {
    println "MAVEN_HOME = " + MAVEN_HOME
    sh 'mvn -version'
    println "ANT_HOME = " + ANT_HOME
    sh 'ant -version'
    println "JAVA_HOME = " + JAVA_HOME
    sh 'java -version'
  }
  
  stage('Checkout') {
    deleteDir()
    checkout scm
    def changeMap = [:]
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
          if(projectName.startsWith()) {
            continue
          }
          println "projectName = " + projectName
          def filePath = file.path.substring(file.path.indexOf("/"))
          println "filePath = " + filePath
          def editType = file.editType.name
          println "editType = " + editType
          if(!changeMap.containsKey("${projectName}")) {
            changeMap."${projectName}" = [:]
            changeMap."${projectName}"."${filePath}" = editType
          } else {
            changeMap."${projectName}"."${filePath}" = editType
          }
        }
      }
    }
    changeMap.each {
      projectNameEntry -> 
        println projectNameEntry.key
        println projectNameEntry.value
        projectNameEntry.value.each {
          fileEntry -> 
            println fileEntry.key
            println fileEntry.value
        }
    }
  }
}
