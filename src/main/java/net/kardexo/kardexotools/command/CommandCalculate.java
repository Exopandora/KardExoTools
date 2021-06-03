package net.kardexo.kardexotools.command;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.kardexo.kardexotools.command.CommandCalculate.Expression.ParseException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class CommandCalculate
{
	private static final Map<String, String> HISTORY = new HashMap<String, String>();
	private static final DynamicCommandExceptionType PARSING_EXCEPTION = new DynamicCommandExceptionType(exception -> new LiteralMessage(String.valueOf(exception)));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralCommandNode<CommandSource> watch2gether = dispatcher.register(Commands.literal("calculate")
				.then(Commands.argument("expression", StringArgumentType.greedyString())
						.executes(context -> calculate(context, StringArgumentType.getString(context, "expression")))));
		dispatcher.register(Commands.literal("calc")
				.redirect(watch2gether));
	}
	
	private static int calculate(CommandContext<CommandSource> context, String expression) throws CommandSyntaxException
	{
		try
		{
			String name = context.getSource().getTextName();
			
			if(expression.contains("ans") && !HISTORY.containsKey(name))
			{
				throw CommandBase.exception("No previous value stored for ans");
			}
			
			double x = new Expression(expression.replaceAll("ans", HISTORY.get(name))).eval();
			
			if(Double.isFinite(x))
			{
				HISTORY.put(name, String.valueOf(x));
			}
			
			context.getSource().sendSuccess(new StringTextComponent(expression + " = " + x), false);
			return (int) x;
		}
		catch(ParseException e)
		{
			throw PARSING_EXCEPTION.create(e.getMessage());
		}
	}
	
	public static class Expression
	{
		private final String string;
		
		public Expression(String string)
		{
			this.string = string;
		}
		
		public double eval() throws ParseException
		{
			Parser parser = new Parser(this.string);
			double x = parser.parseExpression();
			
			if(parser.getPosition() < this.string.length())
			{
				throw new ParseException("Unexpected: " + (char) parser.getChar(), parser.getPosition());
			}
			
			return x;
		}
		
		@Override
		public String toString()
		{
			return this.string;
		}
		
		private static class Parser
		{
			private final String string;
			private int position;
			private int character;
			
			public Parser(String string)
			{
				this.string = string;
				this.position = 0;
				this.character = this.string.isEmpty() ? -1 : this.string.charAt(0);
			}
			
			private boolean consume(int c)
			{
				while(this.getChar() == ' ')
				{
					this.next();
				}
				
				if(this.getChar() == c)
				{
					this.next();
					return true;
				}
				
				return false;
			}
			
			private void consumeExpected(char c) throws ParseException
			{
				if(!this.consume(c))
				{
					throw new ParseException("Expected: " + c, this.getPosition());
				}
			}
			
			public double parseExpression() throws ParseException
			{
				double x = this.parseTerm();
				
				while(true)
				{
					if(this.consume('+'))
					{
						x += this.parseTerm();
					}
					else if(this.consume('-'))
					{
						x -= this.parseTerm();
					}
					else
					{
						return x;
					}
				}
			}
			
			private double parseTerm() throws ParseException
			{
				double x = this.parseFactor();
				
				while(true)
				{
					if(this.consume('*'))
					{
						x *= this.parseFactor();
					}
					else if(this.consume('/'))
					{
						x /= this.parseFactor();
					}
					else if(this.getChar() >= 'a' && this.getChar() <= 'z')
					{
						int start = this.getPosition();
						
						while(this.getChar() >= 'a' && this.getChar() <= 'z')
						{
							this.next();
						}
						
						String function = this.string.substring(start, this.getPosition());
						
						if(function.equals("mod"))
						{
							x %= this.parseExpression();
						}
						else
						{
							throw new ParseException("Unknown operator: " + function, start);
						}
					}
					else
					{
						return x;
					}
				}
			}
			
			private double parseArgument() throws ParseException
			{
				this.consumeExpected('(');
				double x = this.parseExpression();
				this.consumeExpected(')');
				return x;
			}
			
			private double parseArguments(BiFunction<Double, Double, Double> function, boolean multipleArgs) throws ParseException
			{
				this.consumeExpected('(');
				double x = this.parseExpression();
				this.consumeExpected(',');
				x = function.apply(x, this.parseExpression());
				
				if(multipleArgs)
				{
					while(this.consume(','))
					{
						x = function.apply(x, this.parseExpression());
					}
				}
				
				this.consumeExpected(')');
				return x;
			}
			
			private double parseFactor() throws ParseException
			{
				if(this.consume('+'))
				{
					return this.parseFactor();
				}
				
				if(this.consume('-'))
				{
					return -this.parseFactor();
				}
				
				double x;
				
				if(this.consume('('))
				{
					x = this.parseExpression();
					this.consumeExpected(')');
				}
				else if((this.character >= '0' && this.character <= '9') || this.character == '.')
				{
					int start = this.getPosition();
					
					while((this.character >= '0' && this.character <= '9') || this.character == '.')
					{
						this.next();
					}
					
					try
					{
						x = Double.parseDouble(this.string.substring(start, this.getPosition()));
					}
					catch(NumberFormatException e)
					{
						throw new ParseException(e.getMessage(), start);
					}
				}
				else if(this.getChar() >= 'a' && this.getChar() <= 'z')
				{
					int start = this.getPosition();
					
					while(this.getChar() >= 'a' && this.getChar() <= 'z')
					{
						this.next();
					}
					
					String function = this.string.substring(start, this.getPosition());
					
					switch(function)
					{
						case "e":
							x = Math.E;
							break;
						case "pi":
							x = Math.PI;
							break;
						case "sqrt":
							x = Math.sqrt(this.parseArgument());
							break;
						case "ceil":
							x = Math.ceil(this.parseArgument());
							break;
						case "floor":
							x = Math.floor(this.parseArgument());
							break;
						case "rad":
							x = Math.toRadians(this.parseArgument());
							break;
						case "deg":
							x = Math.toDegrees(this.parseArgument());
							break;
						case "round":
							x = Math.round(this.parseArgument());
							break;
						case "log":
							x = Math.log10(this.parseArgument());
							break;
						case "ln":
							x = Math.log(this.parseArgument());
							break;
						case "sin":
							x = Math.sin(this.parseArgument());
							break;
						case "cos":
							x = Math.cos(this.parseArgument());
							break;
						case "tan":
							x = Math.tan(this.parseArgument());
							break;
						case "sinh":
							x = Math.sinh(this.parseArgument());
							break;
						case "cosh":
							x = Math.cosh(this.parseArgument());
							break;
						case "tanh":
							x = Math.tanh(this.parseArgument());
							break;
						case "asin":
							x = Math.asin(this.parseArgument());
							break;
						case "acos":
							x = Math.acos(this.parseArgument());
							break;
						case "atan":
							x = Math.atan(this.parseArgument());
							break;
						case "asinh":
							x = Parser.asinh(this.parseArgument());
							break;
						case "acosh":
							x = Parser.acosh(this.parseArgument());
							break;
						case "atanh":
							x = Parser.atanh(this.parseArgument());
							break;
						case "min":
							x = this.parseArguments(Math::min, true);
							break;
						case "max":
							x = this.parseArguments(Math::max, true);
							break;
						default:
							throw new ParseException("Unknown function: " + function, start);
					}
				}
				else
				{
					throw new ParseException("Unexpected: " + (char) this.getChar(), this.getPosition());
				}
				
				if(this.consume('^'))
				{
					x = Math.pow(x, this.parseFactor());
				}
				
				return x;
			}
			
			public int getPosition()
			{
				return this.position;
			}
			
			public int getChar()
			{
				return this.character;
			}
			
			public void next()
			{
				this.character = (++this.position < this.string.length()) ? this.string.charAt(this.position) : -1;
			}
			
			private static double asinh(double x)
			{
				return Math.log(x + Math.sqrt(x * x + 1.0));
			}
			
			private static double acosh(double x)
			{
				return Math.log(x + Math.sqrt(x * x - 1.0));
			}
			
			private static double atanh(double x)
			{
				return 0.5 * Math.log((x + 1.0) / (x - 1.0));
			}
		}
		
		public static class ParseException extends Exception
		{
			private static final long serialVersionUID = -7352867054559488848L;
			
			public ParseException(String message, int position)
			{
				super(message + " at position " + position);
			}
		}
	}
}
