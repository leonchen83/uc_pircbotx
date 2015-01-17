/**
 * Copyright © 2012-2013 Unity Coders
 *
 * This file is part of uc_pircbotx.
 *
 * uc_pircbotx is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * uc_pircbotx is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * uc_pircbotx. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.unitycoders.pircbotx.data.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import uk.co.unitycoders.pircbotx.profile.Profile;

/**
 *
 *
 */
public class ProfileModel {

    private final Connection conn;
    private final PreparedStatement createProfile;
    private final PreparedStatement createPerm;
    private final PreparedStatement deletePerm;
    private final PreparedStatement getProfile;
    private final PreparedStatement getPerms;

    public ProfileModel(Connection conn) throws SQLException {
        this.conn = conn;
        buildTable();
        createProfile = conn.prepareStatement("INSERT INTO profiles VALUES(?)");
        createPerm = conn.prepareStatement("INSERT INTO permissions VALUES (?,?)");
        deletePerm = conn.prepareStatement("DELETE FROM permissions WHERE user=? AND name=?");
        getPerms = conn.prepareStatement("SELECT * FROM permissions WHERE user=?");
        getProfile = conn.prepareStatement("SELECT * FROM profiles WHERE user=?");
    }

    private void buildTable() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS permissions (user TEXT, name TEXT)");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS profiles (user TEXT)");
    }

    public void createProfile(String name) throws SQLException {
        createProfile.clearParameters();
        createProfile.setString(1, name);
        createProfile.executeUpdate();
    }

    public void addPerm(String user, String perm) throws SQLException {
        createPerm.clearParameters();
        createPerm.setString(1, user);
        createPerm.setString(2, perm);
        createPerm.execute();
    }

    public void removePerm(String user, String perm) throws SQLException {
        deletePerm.clearParameters();
        deletePerm.setString(1, user);
        deletePerm.setString(2, perm);
        deletePerm.executeUpdate();
    }

    public String[] getPerms(String user) throws SQLException {
        Set<String> perms = new HashSet<String>();

        getPerms.clearParameters();
        getPerms.setString(1, user);
        ResultSet rs = getPerms.executeQuery();

        while (rs.next()) {
            perms.add(rs.getString(2));
        }

        rs.close();

        return perms.toArray(new String[perms.size()]);
    }

    public Profile getProfile(String profileName) throws SQLException {
        getProfile.clearParameters();
        getProfile.setString(1, profileName);
        ResultSet rs = getProfile.executeQuery();
        rs.next();

        Profile profile = new Profile(rs.getString(1));
        return profile;
    }
}
