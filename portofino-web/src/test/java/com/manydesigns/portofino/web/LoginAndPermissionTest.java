/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.web;

import com.manydesigns.portofino.CommonTestUtil;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebForm;
import com.meterware.servletunit.ServletUnitClient;
import junit.framework.Test;
import junit.framework.TestSuite;

import javax.servlet.http.HttpServletResponse;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class LoginAndPermissionTest extends CommonTestUtil {

    public static Test suite() {
        return new TestSuite(LoginAndPermissionTest.class);
    }

    public void testLogin1() throws Exception {
      System.out.println("testLinkHomePage:");

        ServletUnitClient client = servletRunner.newClient();

        String url = "http://127.0.0.1/Document.action";
        System.out.println(url);
        WebResponse resp = client.getResponse(url);
        String text = resp.getText();
        //Controllo il menu deve avere solo la Homepage
        assertEquals("Codice risposta.",
               HttpServletResponse.SC_OK, resp.getResponseCode());
        assertTrue(text.contains("        <div id=\"sidebar\" class=\"yui-b\">\n" +
                "            <ul><li class=\"selected\"><a href=\"/Document.action\" title=\"Homepage\">Homepage</a></li></ul>\n" +
                "        </div>\n" +
                "    </div>\n"));
        assertEquals("Homepage", resp.getElementWithID("sidebar").getText());

        //Clicco su login
        WebLink login = resp.getLinkWith("Log in");
        resp = login.click();
        assertEquals("Codice risposta.",
               HttpServletResponse.SC_OK, resp.getResponseCode());
        text = resp.getText();

        WebForm form = resp.getFormWithID("Login");
        assertEquals (text, "Login", form
                .getSubmitButtonWithID("loginButton").getValue());

        form.setParameter("userName", "admin");
        form.setParameter("pwd", "admin");

        //Il menu ora è popolato
        resp = form.submit();
        text = resp.getText();
        assertEquals("Codice risposta.",
               HttpServletResponse.SC_OK, resp.getResponseCode());

        assertEquals("HomepageModelSystem administrationProfile", resp.getElementWithID("sidebar").getText());

        assertTrue(text.contains("        <div id=\"sidebar\" class=\"yui-b\">\n" +
                "            <ul><li class=\"selected\"><a href=\"/Document.action\" title=\"Homepage\">Homepage</a></li><li><a href=\"/model/Index.action\" title=\"Model\">Model</a></li><li><a href=\"/system-admin/Index.action\" title=\"System administration\">System administration</a></li><li><a href=\"/Profile.action\" title=\"Profile\">Profile</a></li></ul>\n" +
                "        </div>"));
        
    }

    //Chiamo una pagina protetta e vengo reindirizzato prima alla login e poi alla stessa pagina
    public void testLogin2() throws Exception {
      System.out.println("test pagina protetta:");

        String url = "http://127.0.0.1/system-admin/ServerInfo.action";

        ServletUnitClient client = servletRunner.newClient();
        System.out.println(url);
        WebResponse resp = client.getResponse(url);
        String text = resp.getText();
        assertEquals("Codice risposta.",
               HttpServletResponse.SC_OK, resp.getResponseCode());
        
        WebForm form = resp.getFormWithID("Login");
        assertEquals (text, "Login", form
                .getSubmitButtonWithID("loginButton").getValue());

        form.setParameter("userName", "admin");
        form.setParameter("pwd", "admin");

        //Il menu ora è popolato
        resp = form.submit();
        text = resp.getText();
        assertEquals("Codice risposta.",
               HttpServletResponse.SC_OK, resp.getResponseCode());
        assertEquals("Server info",resp.getTitle());
    }

    //Chiamo una pagina protetta e vengo reindirizzato prima alla login e poi alla stessa pagina,
    // ma non ho accesso alla stessa
    public void testLogin3() throws Exception {
      System.out.println("test pagina protetta:");

        String url = "http://127.0.0.1/system-admin/ServerInfo.action";

        ServletUnitClient client = servletRunner.newClient();
        System.out.println(url);
        WebResponse resp = client.getResponse(url);
        String text = resp.getText();
        assertEquals("Codice risposta.",
               HttpServletResponse.SC_OK, resp.getResponseCode());

        WebForm form = resp.getFormWithID("Login");
        assertEquals (text, "Login", form
                .getSubmitButtonWithID("loginButton").getValue());

        form.setParameter("userName", "giampi");
        form.setParameter("pwd", "giampi");

        //Il menu ora è popolato
        resp = form.submit();
        text = resp.getText();
        assertEquals("Codice risposta.",
               HttpServletResponse.SC_UNAUTHORIZED, resp.getResponseCode());
    }

    // Accesso utente non amministratore
    public void testLogin4() throws Exception {
      System.out.println("testLinkHomePage:");

        ServletUnitClient client = servletRunner.newClient();

        String url = "http://127.0.0.1/Document.action";
        System.out.println(url);
        WebResponse resp = client.getResponse(url);
        String text = resp.getText();
        //Controllo il menu deve avere solo la Homepage
        assertEquals("Codice risposta.",
               HttpServletResponse.SC_OK, resp.getResponseCode());
        assertTrue(text.contains("        <div id=\"sidebar\" class=\"yui-b\">\n" +
                "            <ul><li class=\"selected\"><a href=\"/Document.action\" title=\"Homepage\">Homepage</a></li></ul>\n" +
                "        </div>\n" +
                "    </div>\n"));
        assertEquals("Homepage", resp.getElementWithID("sidebar").getText());

        //Clicco su login
        WebLink login = resp.getLinkWith("Log in");
        resp = login.click();
        assertEquals("Codice risposta.",
               HttpServletResponse.SC_OK, resp.getResponseCode());
        text = resp.getText();

        WebForm form = resp.getFormWithID("Login");
        assertEquals (text, "Login", form
                .getSubmitButtonWithID("loginButton").getValue());

        form.setParameter("userName", "giampi");
        form.setParameter("pwd", "giampi");

        //Il menu ora è popolato
        resp = form.submit();
        text = resp.getText();
        assertEquals("Codice risposta.",
               HttpServletResponse.SC_OK, resp.getResponseCode());

        assertEquals("HomepageProfile", resp.getElementWithID("sidebar").getText());

        assertTrue(text.contains("        <div id=\"sidebar\" class=\"yui-b\">\n" +
                "            <ul><li class=\"selected\"><a href=\"/Document.action\" title=\"Homepage\">Homepage</a></li><li><a href=\"/Profile.action\" title=\"Profile\">Profile</a></li></ul>\n" +
                "        </div>"));
    }
}