/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.appformer.maven.integration;

import java.util.Collections;

import org.apache.maven.project.MavenProject;
import org.appformer.maven.integration.embedder.MavenSettings;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.Test;

import static org.appformer.maven.integration.embedder.MavenSettings.CUSTOM_SETTINGS_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AetherTest {

    private final String SETTINGS_WITH_PROXY =
            "<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\"\n" +
            "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "      xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0\n" +
            "                          http://maven.apache.org/xsd/settings-1.0.0.xsd\">" +
            "  <proxies>\n" +
            "    <proxy>\n" +
            "      <id>MyProxy</id>\n" +
            "      <active>true</active>\n" +
            "      <protocol>http</protocol>\n" +
            "      <host>localhost</host>\n" +
            "      <port>8888</port>\n" +
            "    </proxy>\n" +
            "  </proxies>" +
            "</settings>\n";

    @Test
    public void testProxies() {
        String oldSettingsXmlPath = System.getProperty( CUSTOM_SETTINGS_PROPERTY );
        try {
            if (oldSettingsXmlPath != null) {
                System.clearProperty( CUSTOM_SETTINGS_PROPERTY );
            }
            MavenSettings.reinitSettingsFromString( SETTINGS_WITH_PROXY );

            Aether aether = Aether.getAether();
            RemoteRepository remoteRepository = new RemoteRepository.Builder( "local", "default", "http://myserver.com" ).build();
            Proxy proxy = aether.getSession().getProxySelector().getProxy( remoteRepository );
            assertEquals("http", proxy.getType());
            assertEquals("localhost", proxy.getHost());
            assertEquals(8888, proxy.getPort());

        } finally {
            if (oldSettingsXmlPath != null) {
                System.setProperty( CUSTOM_SETTINGS_PROPERTY, oldSettingsXmlPath );
            }
            MavenSettings.reinitSettings();
        }
    }

    @Test
    public void testForcedOffline() {
        final RemoteRepository central = new RemoteRepository.Builder( "central", "default", "http://repo1.maven.org/maven2/" ).build();

        final MavenProject mavenProject = mock(MavenProject.class);
        when(mavenProject.getRemoteProjectRepositories()).thenReturn(Collections.singletonList(central));

        final Aether aether = new Aether(mavenProject) {
            @Override
            boolean isForcedOffline() {
                return true;
            }
        };
        assertThat(aether.getRepositories()).doesNotContain(central);
    }

    @Test
    public void testNotOffline() {
        final RemoteRepository central = new RemoteRepository.Builder( "central", "default", "http://repo1.maven.org/maven2/" ).build();

        final MavenProject mavenProject = mock(MavenProject.class);
        when(mavenProject.getRemoteProjectRepositories()).thenReturn(Collections.singletonList(central));

        final Aether aether = new Aether(mavenProject) {
            @Override
            boolean isForcedOffline() {
                return false;
            }
        };

        assertThat(aether.getRepositories()).contains(central);
    }

}
