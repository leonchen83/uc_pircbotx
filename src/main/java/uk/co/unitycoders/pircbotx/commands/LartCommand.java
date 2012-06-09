/**
 * Copyright © 2012 Bruce Cowan <bruce@bcowan.me.uk>
 *
 * This file is part of uc_PircBotX.
 *
 * uc_PircBotX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uc_PircBotX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with uc_PircBotX.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.unitycoders.pircbotx.commands;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import uk.co.unitycoders.pircbotx.data.db.DBConnection;
import uk.co.unitycoders.pircbotx.data.db.LartModel;
import uk.co.unitycoders.pircbotx.types.Lart;

/**
 * Insults the 1st argument.
 *
 * @author Bruce Cowan
 */
public class LartCommand extends ListenerAdapter<PircBotX>
{
	private LartModel model;
	private final Pattern re;

	/**
	 * Creates a {@link LartCommand}.
	 */
	public LartCommand()
	{
		try
		{
			this.model = DBConnection.getLartModel();
		} catch(Exception ex)
		{
			ex.printStackTrace();
		}

		this.re = Pattern.compile("!lart ([^ ]+) *(.*)?");
	}

	@Override
	public void onMessage(MessageEvent<PircBotX> event) throws Exception
	{
		String msg = event.getMessage();

		Matcher matcher = this.re.matcher(msg);

		if(matcher.matches())
		{
			String subcommand = matcher.group(1);
			String opts = matcher.group(2);

			if (subcommand.equals("add"))
			{
				try
				{
					int num = this.model.storeLart(event.getChannel(), event.getUser(), opts);
					event.respond("Lart #" + num + " added");
				} catch (IllegalArgumentException ex)
				{
					event.respond("No $who section given");
					return;
				}
			}
			else if (subcommand.equals("delete"))
			{
				try
				{
					int id = Integer.parseInt(opts);
					this.model.deleteLart(id);
					event.respond("Deleted lart #" + id);
				} catch (NumberFormatException ex)
				{
					event.respond("Couldn't parse number");
				} catch (SQLException ex)
				{
					event.respond("No such lart in database");
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
			else if (subcommand.equals("info"))
			{
				try
				{
					int id = Integer.parseInt(opts);
					Lart lart = this.model.getLart(id);
					String resp = "Channel: " + lart.getChannel() + " Nick: " + lart.getNick() + " Pattern: " + lart.getPattern();
					event.respond(resp);
				} catch (NumberFormatException ex)
				{
					event.respond("Couldn't parse number");
				} catch (SQLException ex)
				{
					event.respond("No such lart in database");
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
			else if (subcommand.equals("list"))
				event.respond("Not implemented"); // TODO add this subcommand
			else
				insult(event, subcommand);
		}
	}

	/**
	 * Insults someone.
	 * @param event the event from {@link #onMessage(MessageEvent)}.
	 * @param nick the nick to insult
	 */
	private void insult(MessageEvent<PircBotX> event, String nick)
	{
		String insult;
		try
		{
			String pattern = this.model.getRandomLart().getPattern();
			insult = pattern.replace("$who", nick);
			event.getBot().sendAction(event.getChannel(), insult);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
