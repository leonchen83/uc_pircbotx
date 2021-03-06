package uk.co.unitycoders.pircbotx.modules;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import uk.co.unitycoders.pircbotx.commandprocessor.Command;
import uk.co.unitycoders.pircbotx.commandprocessor.CommandNotFoundException;
import uk.co.unitycoders.pircbotx.commandprocessor.HelpText;
import uk.co.unitycoders.pircbotx.commandprocessor.Message;
import uk.co.unitycoders.pircbotx.security.Secured;

public class AnnotationModule implements Module {
	protected final String name;
	protected final Map<String, Node> nodes;
	protected final Object source;
	protected String helpText;
	
	/**
	 * Default constructor for inherited classes.
	 * 
	 * This is the constructor which subclasses should use. It will automatically register the methods which the subclass has tagged with the command interface. 
	 */
	public AnnotationModule(String name) {
		this.name = name;
		this.nodes = new TreeMap<>();
		this.source = this;
		processAnnotations();
	}
	
	/**
	 * Constructor for wrapped classes.
	 * 
	 * This is the constructor which allows classes with command annotations with does not implement the Module interface.
	 * This was the case for all plugins which are part of uc_pircbotx 0.2 or earlier, but presents problems for dynamic loading.
	 * 
	 * @param source the class which should be wrapped.
	 */
	public AnnotationModule(String name, Object source) {
		this.name = name;
		this.nodes = new TreeMap<>();
		this.source = source;
		processAnnotations();
	}
	
	@Override
	public void fire(Message message) throws Exception {
		String action = message.getArgument(Module.COMMAND_ARG, Module.DEFAULT_COMMAND);
		
		Method method = nodes.get(action).method;
        if (method == null) {
            throw new CommandNotFoundException(action);
        }

        method.invoke(source, message);
	}
	
	@Override
	public String[] getRequiredPermissions(String action) {
		Node node = nodes.get(action);
		if (node == null) {
			return new String[0];
		}
		return node.permissions;
	}
	
	@Override
	public boolean isValidAction(String action) {
		return nodes.containsKey(action);
	}
	
	protected void processAnnotations() {
        Class<?> clazz = source.getClass();
        HelpText clazzHelp = clazz.getAnnotation(HelpText.class);
        if (clazzHelp != null) {
        	helpText = clazzHelp.value();
        }
        
        for (Method method : clazz.getMethods()) {

            Command c = method.getAnnotation(Command.class);
            if (c != null ) {

                // check the class params match our spec
                assert ModuleUtils.isValidParams(method) : "first parameter of a command must be Message";

                HelpText help = method.getAnnotation(HelpText.class);
                String helpText = "";
                if (help != null) {
                	helpText = help.value();
                }
                
                String[] keywords = c.value();
                for (String keyword : keywords) {
                    registerAction(keyword,method, helpText);
                }
            }
        }
	}
	
    protected void registerAction(String action, Method method, String helpText) {
    	Node node = new Node();
    	node.method = method;
    	node.helpText = helpText;
        nodes.put(action, node);
        
        //Permissions annotation
        Secured permissionsRequired = method.getAnnotation(Secured.class);
        if (permissionsRequired != null) {
            node.permissions = permissionsRequired.value();
        }
    }

	@Override
	public Collection<String> getActions() {
		return Collections.unmodifiableCollection(nodes.keySet());
	}
	
	static class Node {
		protected Method method;
		protected String[] permissions;
		protected String helpText;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getHelp(String command) {
		Node node = nodes.get(command);
		if (node == null) {
			return null;
		}
		
		return node.helpText;
	}

	@Override
	public String getModuleHelp() {
		return helpText;
	}
}
