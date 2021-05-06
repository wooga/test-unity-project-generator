/*
 * Copyright 2018 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.wooga.test.unity


import spock.lang.Shared
import spock.lang.Specification

class ProjectGeneratorRuleSpec extends Specification {

    @Shared
    File projectDir

    @Shared
    ProjectGeneratorRule rule


    def setup() {
        projectDir = File.createTempDir()
        rule = new ProjectGeneratorRule(projectDir)
    }

    def cleanup() {
        rule.after()
    }

    def "creates unity project before test"() {
        given:
        rule.before()

        expect:
        new File(projectDir, "Assets").exists()
        new File(projectDir, "ProjectSettings").exists()
        new File(projectDir, "ProjectSettings/ProjectVersion.txt").exists()
        new File(projectDir, "Library").exists()
        new File(projectDir, "Temp").exists()
        new File(projectDir, "Packages").exists()
    }

    def "deletes unity project after test"() {
        given: "rule with executed before block"
        rule.before()
        assert projectDir.listFiles().size() > 0

        when:
        rule.after()

        then:
        projectDir.listFiles().size() == 0
    }
    def "can set editor version before test"() {
        given: "set custom version"
        rule.projectVersion = version
        rule.before()

        expect:
        def versionFile = new File(projectDir, "ProjectSettings/ProjectVersion.txt")
        versionFile.text.readLines()[0] == "m_EditorVersion: ${version}"

        where:
        version = "2018.1.1b4"
    }

    def "can set editor version during test"() {
        given:
        rule.before()
        def versionFile = new File(projectDir, "ProjectSettings/ProjectVersion.txt")

        when: "set custom version"
        _

        then:
        versionFile.text.readLines()[0] == "m_EditorVersion: ${version1}"


        when:
        rule.projectVersion = version2

        then:
        versionFile.text.readLines()[0] == "m_EditorVersion: ${version2}"

        where:
        version1 = "2018.0.0f1"
        version2 = "2018.1.1b4"
    }

    def "can reset project directory"() {
        given:
        def newTmp = File.createTempDir()
        rule.projectDir = newTmp
        rule.before()

        expect:
        new File(newTmp, "Assets").exists()
        new File(newTmp, "ProjectSettings").exists()
        new File(newTmp, "ProjectSettings/ProjectVersion.txt").exists()
        new File(newTmp, "Library").exists()
        new File(newTmp, "Temp").exists()
        new File(newTmp, "Packages").exists()
    }

    def "can reset project dir during test"() {
        given:
        rule.before()
        def newTmp = File.createTempDir()
        rule.projectDir = newTmp

        expect:
        new File(newTmp, "Assets").exists()
        new File(newTmp, "ProjectSettings").exists()
        new File(newTmp, "ProjectSettings/ProjectVersion.txt").exists()
        new File(newTmp, "Library").exists()
        new File(newTmp, "Temp").exists()
        new File(newTmp, "Packages").exists()
    }

    def "projectDir is readable"() {
        given:
        rule.before()

        expect:
        rule.projectDir == projectDir
    }
}
