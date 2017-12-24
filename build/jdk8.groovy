def changeMap = [:]
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
          sh 'echo ${fileEntry.value}'
        }
    }
  }
  if(changeMap.size() > 0) {
    stage('Build') {
      changeMap.each {
        key,value -> 
          echo '${ANT_HOME}'
      }
    }
  }
}
