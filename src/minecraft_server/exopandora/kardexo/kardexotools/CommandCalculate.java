package exopandora.kardexo.kardexotools;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandCalculate extends CommandBase
{
	private static final Map<String, String> FUNCTIONS = new HashMap<String, String>();
	private static final Map<String, String> EXPRESSIONS = new HashMap<String, String>();
	private static final Map<String, String> SPECIAL_FUNCTIONS = new HashMap<String, String>();
	private static final Map<String, String> HISTORY = new HashMap<String, String>();
	private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("JavaScript");
	private static final String TERM;
	
	static
	{
		FUNCTIONS.put("sin", "Math.sin");
		FUNCTIONS.put("sinh", "Math.sinh");
		FUNCTIONS.put("asin", "Math.asin");
		FUNCTIONS.put("asinh", "Math.asinh");
		
		FUNCTIONS.put("cos", "Math.cos");
		FUNCTIONS.put("cosh", "Math.cosh");
		FUNCTIONS.put("acos", "Math.acos");
		FUNCTIONS.put("acosh", "Math.acosh");
		
		FUNCTIONS.put("tan", "Math.tan");
		FUNCTIONS.put("tanh", "Math.tanh");
		FUNCTIONS.put("atan", "Math.atan");
		FUNCTIONS.put("atanh", "Math.atanh");
		
		FUNCTIONS.put("log", "java.lang.Math.log10");
		FUNCTIONS.put("ln", "java.lang.Math.log");
		
		FUNCTIONS.put("ceil", "Math.ceil");
		FUNCTIONS.put("floor", "Math.floor");
		FUNCTIONS.put("round", "Math.round");
		
		FUNCTIONS.put("min", "Math.min");
		FUNCTIONS.put("max", "Math.max");
		
		FUNCTIONS.put("pow", "Math.pow");
		FUNCTIONS.put("exp", "Math.exp");
		FUNCTIONS.put("sqrt", "Math.sqrt");
		
		EXPRESSIONS.put("\\|.*\\|", "Math.abs\\($1\\)");
		EXPRESSIONS.put("e", "Math.E");
		EXPRESSIONS.put("pi", "Math.PI");
		EXPRESSIONS.put("mod", "%");
		
		String expressions = String.join("|", EXPRESSIONS.keySet());
		String functions = String.join("|", FUNCTIONS.keySet());
		String regexFunctions = String.join("|", FUNCTIONS.keySet().stream().map(function -> function + "\\(.*\\)").collect(Collectors.toList()));
		String regexExpression = "([0-9]*\\.[0-9]+|[0-9]+|ans|\\(.*\\)|" + regexFunctions + "|" + expressions + ")";
		
		SPECIAL_FUNCTIONS.put(regexExpression + "\\^" + regexExpression, "pow($1,$2)");
		TERM = "([0-9]|\\.|\\(|\\)|\\+|-|\\*|/|%|,|\\^| |ans|" + expressions + "|" + functions + ")*";
	}
	
	@Override
	public String getName()
	{
		return "calc";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/calc <expression>";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		String input = String.join(" ", args).toLowerCase();
		
		if(!input.isEmpty() && input.matches(TERM))
		{
			String script = input;
			
			if(script.contains("ans") && !HISTORY.containsKey(sender.getName()))
			{
				throw new NumberInvalidException("No previous value stored for ans in " + input);
			}
			
			script = script.replaceAll("ans", HISTORY.get(sender.getName()));
			
			for(String function : SPECIAL_FUNCTIONS.keySet())
			{
				script = script.replaceAll(function, SPECIAL_FUNCTIONS.get(function));
			}
			
			for(String expression : EXPRESSIONS.keySet())
			{
				script = script.replaceAll(expression, EXPRESSIONS.get(expression));
			}
			
			for(String function : FUNCTIONS.keySet())
			{
				script = script.replaceAll("(?<![A-Za-z])" + function, FUNCTIONS.get(function));
			}
			
			try
			{
				String solution = SCRIPT_ENGINE.eval(script).toString();
				sender.sendMessage(new TextComponentString(input + " = " + solution));
				
				HISTORY.put(sender.getName(), solution);
			}
			catch(ScriptException e)
			{
				throw new SyntaxErrorException("Invalid expression");
			}
		}
		else
		{
			throw new WrongUsageException(this.getUsage(sender));
		}
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}
}
