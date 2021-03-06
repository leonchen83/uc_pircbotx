/**
 * Copyright © 2012-2015 Unity Coders
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
package uk.co.unitycoders.pircbotx.commands;

import java.util.Map;

import uk.co.unitycoders.pircbotx.commandprocessor.Command;
import uk.co.unitycoders.pircbotx.commandprocessor.Message;
import uk.co.unitycoders.pircbotx.data.db.DBConnection;
import uk.co.unitycoders.pircbotx.data.db.JoinModel;
import uk.co.unitycoders.pircbotx.modules.AnnotationModule;

/**
 * Keeps a list of joins, and gives a list of nicks and number of joins.
 *
 * @author Bruce Cowan
 */
public class JoinsCommand extends AnnotationModule {

    private JoinModel model;

    /**
     * Creates a {@link JoinsCommand}.
     */
    public JoinsCommand() throws Exception {
    	super("joins");
        this.model = DBConnection.getJoinModel();
    }

    @Command
    public void onJoins(Message event) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, Integer> entry : this.model.getAllJoins().entrySet()) {
            String nick = entry.getKey();
            String value = entry.getValue().toString();
            builder.append(nick);
            builder.append(" = ");
            builder.append(value);
            builder.append("; ");
        }

        event.respond(builder.substring(0, builder.length() - 2));
    }
}
