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
import org.junit.Test;
import org.visualcti.core.XmlAware;
import org.visualcti.server.UnitRegistry;
import org.visualcti.server.core.executable.task.TaskPoolsManager;
import org.visualcti.server.core.system.SubSystem;
import org.visualcti.server.core.unit.exception.ServerUnitException;
import org.visualcti.server.task.TaskPoolsManagerAdapter;

public class TasksSubSystemTest {

    TasksSubSystem system =  new TasksSubSystem();

    @Test
    public void shouldGetTasksSubSystemXML() {
        // preparing test data
        String managerRootDirectoryName = "root-directory";
        assertThat(system.getSystemManager()).isInstanceOf(TaskPoolsManagerAdapter.class);
        ((TaskPoolsManagerAdapter)system.getSystemManager()).setRootDirectoryName(managerRootDirectoryName);

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
        String xmlString = "    <system>\n" +
                "      <Tasks>\n" +
                "        <!-- Server tasks control system -->\n" +
                "        <!-- The manage of tasks pools -->\n" +
                "        <Manager package=\"org.visualcti.server.task\" class=\"Manager\" extends=\"org.visualcti.server.core.executable.Engine\">\n" +
                "          <parent package=\"org.visualcti.server.task\" class=\"TaskManager\" method=\"getManager\">The realization of tasks controls manager</parent>\n" +
                "          <parameter name=\"directory\" type=\"string\" value=\"work/tasks\" />\n" +
                "        </Manager>\n" +
                "        <!-- Pool of public tasks -->\n" +
                "        <pool type=\"public\" name=\"library\" file=\"public.tasks.pool\" />\n" +
                "        <!-- Pool of concrete device's tasks -->\n" +
                "        <pool type=\"local\" name=\"stub_9\" file=\"stub_9.tasks.pool\" />\n" +
                "        <pool type=\"local\" name=\"VisualCTI/stub_1\" file=\"stub_1.tasks.pool\" />\n" +
                "        <pool type=\"local\" name=\"VisualCTI/stub_2\" file=\"stub_2.tasks.pool\" />\n" +
                "        <pool type=\"local\" name=\"VisualCTI/stub_3\" file=\"stub_3.tasks.pool\" />\n" +
                "        <pool type=\"local\" name=\"VisualCTI/stub_4\" file=\"stub_4.tasks.pool\" />\n" +
                "        <pool type=\"local\" name=\"Dialogic/dxxxB1C1\" file=\"dxxxB1C1.tasks.pool\" />\n" +
                "        <pool type=\"local\" name=\"Dialogic/dxxxB1C2\" file=\"dxxxB1C2.tasks.pool\" />\n" +
                "        <pool type=\"local\" name=\"Dialogic/dxxxB1C3\" file=\"dxxxB1C3.tasks.pool\" />\n" +
                "        <pool type=\"local\" name=\"Dialogic/dxxxB1C4\" file=\"dxxxB1C4.tasks.pool\" />\n" +
                "      </Tasks>\n" +
                "    </system>\n";
        Element xml = new XmlAware() {
        }.load(new ByteArrayInputStream(xmlString.getBytes()));

        // acting
        system.setXML(xml);

        // check results
        assertThat(system.getSystemManager()).isInstanceOf(SubSystem.Manager.class)
                .isInstanceOf(TaskPoolsManager.class);
        TaskPoolsManager manager = (TaskPoolsManager)system.getSystemManager();
        assertThat(manager.getRoot()).exists().isDirectory();
        assertThat(UnitRegistry.lookup(SubSystem.Manager.class)).isNotNull();
        assertThat(manager.publicTaskPool().getName()).isEqualTo("library");
    }
}