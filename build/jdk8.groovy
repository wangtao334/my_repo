def changeMap = [:]
def mvnHome
def antHome
def javaHome
def delClsFlg = false
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
          if(projectName.startsWith("build")) {
            continue
          }
          def filePath = file.path.substring(file.path.indexOf("/"))
          def editType = file.editType.name
          if(editType.equals("delete") && filePath.startsWith("/src") && filePath.endsWith(".java")) {
            delClsFlg = true
          }
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
        projectNameEntry.value.each {
          fileEntry -> 
            println projectNameEntry.key + fileEntry.value + " : " +fileEntry.key
        }
    }
  }
  stage('Delete Class File') {
    if(delClsFlg) {
      changeMap.each {
        projectNameEntry -> 
          projectNameEntry.value.each {
            key,value -> 
              if(value.equals("delete") && key.startsWith("/src") && key.endsWith(".java")) {
                sh 'rm -rf ${WORKSPACE}/' + projectNameEntry.key + key.replaceFirst("/src", "/classes").replace(".java", ".class")
                sh 'rm -rf ${WORKSPACE}/' + projectNameEntry.key + key.replaceFirst("/src", "/classes").replace(".java", "\\\\$.class")
              }
          }
      }
    } else {
      println "No java file deleted."
    }
  }
  
  stage('Build') {
    if(changeMap.size() > 0) {
      changeMap.each {
        key,value ->
          sh 'ant -f ${WORKSPACE}/' + key + '/build.xml'
      }
    } else {
      println "No project changed."
    }
  }
}
