/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */

package com.manydesigns.portofino.connections;

import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Password;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
public class JdbcConnectionProvider extends ConnectionProvider {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields (configured values)
    //**************************************************************************

    protected String driver;
    protected String url;
    protected String username;
    protected String password;


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public JdbcConnectionProvider() {
        super();
    }

    //**************************************************************************
    // Implementation of ConnectionProvider
    //**************************************************************************

    public String getDescription() {
        return MessageFormat.format(
                "JDBC connection to URL: {0}", url);
    }

    public Connection acquireConnection() throws Exception {
        Class.forName(driver);
        return DriverManager.getConnection(url,
                username, password);
    }

    public void releaseConnection(Connection conn) {
        DbUtils.closeQuietly(conn);
    }


    //**************************************************************************
    // Getters
    //**************************************************************************

    @XmlAttribute(required = true)
    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    @Label("connection URL")
    @XmlAttribute(required = true)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlAttribute(required = true)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Password
    @XmlAttribute(required = true)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    //**************************************************************************
    // Other methods
    //**************************************************************************

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("databaseName", databaseName)
                .append("driver", driver)
                .append("url", url)
                .append("username", username)
                .append("password", password)
                .toString();
    }
}
