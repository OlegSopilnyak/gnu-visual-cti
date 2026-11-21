/*
##############################################################################
##
##  DO NOT REMOVE THIS LICENSE AND COPYRIGHT NOTICE FOR ANY REASON
##
##############################################################################

GNU VisualCTI - A Java multi-platform Computer Telephony Application Server
Copyright (C) 2002 by Oleg Sopilnyak.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

Contact oleg.sopilnyak@gmail.com or gennady@visualcti.org for more information.

Ukraine point of contact: Oleg Sopilnyak - oleg.sopilnyak@gmail.com
Home Phone:	+380-63-8420220 (russian)

USA point of contact: Justin Kuntz - jkuntz@prominic.com
Prominic Technologies, Inc.
PO Box 3233
Champaign, IL 61826-3233
Fax number: 217-356-3356
##############################################################################

*/
package org.visualcti.server.system;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.visualcti.core.XmlAware;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.executable.task.TaskPoolsManager;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.exception.ServerUnitException;
import org.visualcti.server.system.manager.TasksManager;
import org.visualcti.server.task.AbstractTaskPoolsManager;

public class TasksSubSystemTest {
    final String testXml = "tasks.xml";
    String configurationXmlString;
    final XmlAware xmlEngine = new XmlAware() {
    };
    TasksSubSystem system =  new TasksSubSystem();

    @Before
    public void setUp() throws Exception {
        Element xml = xmlEngine.load(TasksSubSystem.class.getResourceAsStream(testXml));
        // preparing default sub-system Xml configuration text
        configurationXmlString = xmlEngine.asString(xml);
        UnitRegistry.clear();
    }

    @After
    public void tearDown() throws Exception {
        system.getSystemManager().Stop();
    }

    @Test
    public void shouldGetTasksSubSystemXML() throws IOException {
        // preparing test data
        UnitRegistry.register(TasksManager.getInstance());
        String managerRootDirectoryName = "root-directory";
        assertThat(system.getSystemManager()).isInstanceOf(AbstractTaskPoolsManager.class);
        system.getSystemManager().setRootDirectoryName(managerRootDirectoryName);

        // acting
        Element xml = system.getXML();

        // check results
        assertThat(xml).isNotNull();
        assertThat(xml.getName()).isEqualTo("system");
        Element tasks = xml.getChild("Tasks");
        assertThat(tasks).isNotNull();
        Element manager = tasks.getChild("Manager");
        assertThat(manager).isNotNull();
        Element managerRootDirectory = manager.getChild("parameter");
        assertThat(managerRootDirectory).isNotNull();
        assertThat(managerRootDirectory.getAttributeValue("name")).isEqualTo("directory");
        assertThat(managerRootDirectory.getAttributeValue("type")).isEqualTo("string");
        assertThat(managerRootDirectory.getAttributeValue("value")).isEqualTo(managerRootDirectoryName);
    }

    @Test
    public void shouldSetTasksSubSystemXML_fromXmlString() throws IOException, DataConversionException, ServerUnitException {
        // preparing test data
        Element xml = xmlEngine.load(new ByteArrayInputStream(configurationXmlString.getBytes()));

        // acting
        system.setXML(xml);

        // check results
        assertThat(system.getSystemManager()).isInstanceOf(SubSystem.Manager.class).isInstanceOf(TaskPoolsManager.class);
        assertThat(UnitRegistry.lookup(SubSystem.Manager.class)).isInstanceOf(TaskPoolsManager.class);
        TaskPoolsManager manager = system.getSystemManager();
        assertThat(manager.getRoot()).exists().isDirectory();
        assertThat(UnitRegistry.lookup(SubSystem.Manager.class)).isNotNull();
        assertThat(manager.publicTaskPool().getName()).isEqualTo("library");
        assertThat(xmlEngine.asString(system.getXML())).isEqualTo(configurationXmlString);
    }

    @Test
    public void shouldStartSystem() throws IOException, DataConversionException {
        // preparing test data
        Element xml = xmlEngine.load(new ByteArrayInputStream(configurationXmlString.getBytes()));
        system.setXML(xml);
        assertThat(system.getSystemManager()).isInstanceOf(TaskPoolsManager.class);
        assertThat(system.getSystemManager().canStartUnit()).isTrue();

        // acting
        system.getSystemManager().Start();

        // check results
        assertThat(system.getSystemManager().isStarted()).isTrue();
        TaskPoolsManager manager = system.getSystemManager();
        assertThat(manager.publicTaskPool().isStarted()).isTrue();
        manager.localTaskPoolStream().forEach(pool -> {
            if (pool.canStartUnit()) {
                assertThat(pool.isStarted()).isTrue();
            } else {
                System.out.println("=== Tasks Pool :[" + pool.getName() + "] can't start");
            }
        });
    }
}
