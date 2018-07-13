package com.avast.gradle.dockercompose

import spock.lang.Ignore
import spock.lang.Specification

class DockerExecutorTest extends Specification {

    def "reads Docker platform"() {
        def f = Fixture.plain()
        when:
        String dockerPlatform = f.extension.dockerExecutor.getDockerPlatform()
        then:
        noExceptionThrown()
        !dockerPlatform.empty
    }

    def "reads network gateway"() {
        def f = Fixture.withNginx()
        when:
        f.project.tasks.composeUp.up()
        ServiceInfo serviceInfo = f.project.tasks.composeUp.servicesInfos.find().value
        String networkName = serviceInfo.firstContainer.inspection.NetworkSettings.Networks.find().key
        String networkGateway = f.extension.dockerExecutor.getNetworkGateway(networkName)
        then:
        noExceptionThrown()
        !networkGateway.empty
        cleanup:
        f.project.tasks.composeDown.down()
        f.close()
    }

    def "reads container logs"() {
        def f = Fixture.withHelloWorld()
        f.project.tasks.composeUp.up()
        String containerId = f.extension.servicesInfos.hello.firstContainer.containerId
        when:
        String output = f.extension.dockerExecutor.getContainerLogs(containerId)
        then:
        output.contains('Hello from Docker')
        cleanup:
        f.project.tasks.composeDown.down()
        f.close()
    }

    def "runs docker-compose with verbose flag"() {
        given:
        def f = Fixture.withHelloWorld()
        f.extension.verbose = true
        when:
        f.project.tasks.composePull.pull()
        then:
        true
        cleanup:
        f.close()
    }
}
