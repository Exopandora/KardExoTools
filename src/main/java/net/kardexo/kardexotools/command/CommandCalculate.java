package net.kardexo.kardexotools.command;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class CommandCalculate
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
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("calc")
				.then(Commands.argument("expression", StringArgumentType.greedyString())
					.executes(context -> calc(context.getSource(), StringArgumentType.getString(context, "expression")))));
	}
	
	private static int calc(CommandSource source, String term) throws CommandSyntaxException
	{
		String script = term;
		
		if(script.contains("ans") && !HISTORY.containsKey(source.getTextName()))
		{
			throw CommandBase.exception("No previous value stored for ans in " + term);
		}
		
		script = script.replaceAll("ans", HISTORY.get(source.getTextName()));
		
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
			source.sendSuccess(new StringTextComponent(term + " = " + solution), false);
			
			HISTORY.put(source.getTextName(), solution);
			
			return (int) Math.round(Double.parseDouble(solution));
		}
		catch(ScriptException e)
		{
			throw CommandBase.exception("Invalid expression");
		}
	}
	
	public static boolean isTerm(String expression)
	{
		return expression.matches(TERM);
	}
}
