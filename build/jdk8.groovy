import java.awt.Shape

import groovy.json.internal.Value

def mvnHome
def antHome
def javaHome
def changeMap = [:]
def projectMap = [:]
def hasBuildFile = false
def delClsFlg = false
def hasFailureFile = false
def failureFileName = "failure"
def failureProjectList = []
def buildAll = build_all
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
		def projectFile = readFile encoding: 'utf-8', file: 'build/project_list'
		def projectList = projectFile.split("\n")
		for(int i = 0; i < projectList.size(); i++) {
			def projectName = projectList[i]
			if(!projectName.trim().isEmpty()) {
				projectMap."${projectName}" = projectName
			}
		}
		projectMap.each {
			key,Value -> 
				println "Project Name --- " + key
		}
		if(buildAll.equals("true")) {
			hasFailureFile = fileExists WORKSPACE + failureFileName
			if(hasFailureFile) {
				sh 'rm -rf ' + WORKSPACE + failureFileName
			}
		} else {
			hasFailureFile = fileExists WORKSPACE + failureFileName
			if(hasFailureFile) {
				def failureFile = readFile WORKSPACE + failureFileName
				failureProjectList = failureFile.split("\n")
				sh 'rm -rf ' + WORKSPACE + failureFileName
			}
		}
	}

	stage('Checkout') {
		if(buildAll.equals("true")) {
			deleteDir()
		}
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
					println projectName
					if(!projectMap.containsKey(projectName)) {
						continue
					}
					def filePath = file.path.substring(file.path.indexOf("/"))
					def editType = file.editType.name
					println projectName + " --- " + filePath + " --- " + editType
					if(!changeMap.containsKey(projectName)) {
						changeMap."${projectName}" = [:]
						changeMap."${projectName}"."${filePath}" = editType
					} else {
						changeMap."${projectName}"."${filePath}" = editType
					}
				}
			}
		}
	}
	stage('Change File List') {
		if(changeMap.size() > 0) {
			changeMap.each { 
				projectNameEntry ->
					println projectNameEntry.key
					projectNameEntry.value.each { key,value ->
						println key
						println value
				}
			}
		} else {
			println 'No file changed.'
		}
	}

	stage('Build') {
		if(buildAll.equals("true")) {
			projectMap.each { key,value ->
				hasBuildFile = fileExists WORKSPACE + key + '/build.xml'
				if(hasBuildFile) {
					println key + " is Builded."
					sh 'ant -f ${WORKSPACE}/' + key + '/build.xml'
				} else {
					println key + " does not have build.xml."
				}
			}
		} else {
			if(changeMap.size() > 0) {
				for(int i = 0; i < failureProjectList.size(); i++) {
					def projectName = failureProjectList[i]
					if(!changeMap.containsKey(projectName)) {
						changeMap."${projectName}" = [:]
					}
				}
				changeMap.each { key,value ->
					hasBuildFile = fileExists WORKSPACE + key + '/build.xml'
					if(hasBuildFile) {
						println key + " is Builded."
						sh 'ant -f ${WORKSPACE}/' + key + '/build.xml || echo ' + key + ' >> ' + WORKSPACE + failureFileName
					} else {
						println key + " does not have build.xml."
					}
				}
				hasFailureFile = fileExists WORKSPACE + failureFileName
				if(hasFailureFile) {
					sh 'please check the build.'
				}
			} else {
				println "No project changed."
			}
		}
	}
	stage('Test') {
		def m = [:]
		def pn = "aa";
		if(!m.containsKey(pn)) {
			println "1"
			m."${pn}" = [:]
			m."${pn}".f1 = "file1"
		} else {
			println "3"
			m."${pn}".f3 = "file3"
		}

		if(!m.containsKey(pn)) {
			println "2"
			m."${pn}" = [:]
			m."${pn}".f2 = "file2"
		} else {
			println "4"
			m."${pn}".f4 = "file4"
		}

		m.each { mE ->
			println mE.key
			mE.value.each { key,value ->
				println key
				println value
			}
		}

		pn = "ant_sample"
		env."${pn}_delete_jar" = true
		println env.ant_sample_delete_jar
	}
}