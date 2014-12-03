/**
 * Copyright © 2012 Joseph Walton-Rivers <webpigeon@unitycoders.co.uk>
 *
 * This file is part of uc_PircBotX.
 *
 * uc_PircBotX is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * uc_PircBotX is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * uc_PircBotX. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.unitycoders.pircbotx.commandprocessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import uk.co.unitycoders.pircbotx.security.*;
import uk.co.unitycoders.pircbotx.security.SecurityManager;

/**
 * centrally managed command parsing.
 *
 * This class is responsible to breaking IRC messages into commands which the
 * bot can understand. It contains a list of modules and the methods which are
 * tagged with the command annotation. Classes must be registered with the
 * Command Processor in order for the processor to recognise them as commands.
 *
 */
public class CommandProcessor {

    private final Pattern regex;
    private final Map<String, CommandNode> commands;
    private final SecurityManager security;

    /**
     * Create a new command processor.
     *
     * This will create a new command processor and will initialise the regex
     * pattern the bot will use to match commands. It will also create the maps
     * needed to store information about the commands.
     */
    public CommandProcessor(SecurityManager security) {
        this.regex = Pattern.compile("([a-z0-9]+)(?: ([a-z0-9]+))?(?: (.*))?");
        this.commands = new TreeMap<String, CommandNode>();
        this.security = security;
    }

    /**
     * Register a new module and extract it's commands.
     *
     * This method will look at target and process any method which has been
     * annotated with the command annotation. It will then remember the module
     * and command names for use when processing messages.
     *
     * The module's name will need to be put before any command. This is to
     * prevent two modules conflicting with the same named commands.
     *
     * @param name the name of the module
     * @param target the module object
     */
    public void register(String name, Object target) {
        CommandNode node = CommandNode.build(target);
        commands.put(name, node);
    }

    //proper aliasing of commands is now possible
    public void alias(String name, String oldName) {
        CommandNode node = commands.get(oldName);

        if (oldName == null) {
            throw new RuntimeException(oldName + " is not a loaded class");
        }

        CommandNode aliasNode = commands.get(name);
        if (aliasNode != null) {
            throw new RuntimeException(name + " is already a keyword!");
        }

        commands.put(name, node);
    }

    public void remove(String command) {
        commands.remove(command);
    }


    /**
     * Process an IRC message to see if the bot needs to respond.
     *
     * This method takes an IRC message and splits it into it's component parts.
     * if the action is valid it will then call the call method to process the
     * event.
     *
     * @param event the event to be processed
     * @throws Exception
     */
    public void invoke(Message event) throws Exception {
        Matcher matcher = regex.matcher(event.getMessage());

        // not valid command format
        if (!matcher.matches()) {
            return;
        }

        // XXX lart [thing], thing will be an action
        String command = matcher.group(1);
        String action = matcher.group(2);
        String args = matcher.group(3);

        System.out.println("[DEBUG] Command: " + command);
        System.out.println("[DEBUG] action: " + action);
        System.out.println("[DEBUG] args: " + args);

        try {
            CommandNode commandNode = commands.get(command);
            if (commandNode == null) {
                //TODO throw exception
                return;
            }

            if (action != null && commandNode.isValidAction(action)) {
                if (!checkPermissions(commandNode, action, event.getUser())){
                    throw new RuntimeException("you don't have permission");
                }
                commandNode.invoke(action, event);
            } else {
                if (!checkPermissions(commandNode, "default", event.getUser())){
                    throw new RuntimeException("you don't have permission");
                }
                commandNode.invoke("default", event);
            }
        } catch (InvocationTargetException ex) {
            Throwable real = ex.getCause();
            real.printStackTrace();
            event.respond("[cmd-error] " + real.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            event.respond("[error] " + ex.getMessage());
        }
    }

    private boolean checkPermissions(CommandNode node, String action, User user) {
        //check if security is disabled
        if (security == null) {
            return true;
        }

        String[] permissions = node.getRequiredPermissions(action);
        if (permissions != null) {
            Session session = security.getSession(user);
            return session != null && session.hasPermissions(permissions);
        }
        return true;
    }

    /**
     * Get a list of modules registered with the command processor.
     *
     * @return the list of module names
     */
    public Collection<String> getModules() {
        return Collections.unmodifiableCollection(commands.keySet());
    }


    /**
     * Gets a list of commands which are registered with the command processor
     *
     * @param moduleName the name of the module to get the commands from.
     * @return the list of command names, or null if command doesn't exist.
     */
    public Collection<String> getCommands(String moduleName) {
        if (moduleName == null) {
            return Collections.emptyList();
        }

        CommandNode command = commands.get(moduleName);

        if (command == null) {
            return Collections.emptyList();
        }

        return command.getActions();
    }
}
