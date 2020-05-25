package com.company.service;

import org.apache.commons.io.IOUtils;
import org.appformer.maven.integration.MavenRepository;
import org.drools.core.io.impl.ClassPathResource;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import javax.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.stereotype.Component;

@Component
public class RepositoryInitializer {
    private static final Log logger = LogFactory.getLog(RepositoryInitializer.class);

    private static final String REPO_LOCATION = "../.m2/repository/";

    @Value("${onboarding.local.groupId}")
    private String onBoardingGroupId;

    @Value("${onboarding.local.artifactId}")
    private String onBoardingArtifactId;

    @Value("${onboarding.local.version}")
    private String onBoardingVersion;

    @Value("${creditriskrating.local.groupId}")
    private String creditriskratingGroupId;

    @Value("${creditriskrating.local.artifactId}")
    private String creditriskratingArtifactId;

    @Value("${creditriskrating.local.version}")
    private String creditriskratingVersion;



    @PostConstruct
    public void init() {

            logger.info("KIE remote repository is disabled, using local configuration");
            try {

                loadToMaven(creditriskratingGroupId,creditriskratingArtifactId,creditriskratingVersion);
                loadToMaven(onBoardingGroupId,onBoardingArtifactId,onBoardingVersion);
            } catch (IOException e) {
                logger.error("Error loading kjar from classpath", e);
            }


    }

    public void initializeDirectories() {


        File m2RepoDir = new File( REPO_LOCATION );
        if(!m2RepoDir.exists())
            m2RepoDir.mkdirs();
    }



    private ReleaseId loadToMaven(String groupId, String artifactId, String version) throws IOException {
        System.out.println("Writing to maven#############################"+groupId+artifactId+version);
        KieFileSystem kfs = KieServices.Factory.get().newKieFileSystem();
        ReleaseId id = KieServices.Factory.get().newReleaseId(groupId, artifactId, version);
        kfs.generateAndWritePomXML(id);
        byte[] pom = kfs.read("pom.xml");



        InputStream artifactJar = this.getClass().getClassLoader()
                .getResourceAsStream(String.format("META-INF/processes/"+artifactId+"-"+version+".jar"));



        byte[] kjar = IOUtils.toByteArray(artifactJar);

        MavenRepository.getMavenRepository().installArtifact(id, kjar, pom);
        System.setProperty("org.kie.task.insecure","true");
        System.setProperty("org.kie.server.bypass.auth.user","true");


        return id;

    }
}
