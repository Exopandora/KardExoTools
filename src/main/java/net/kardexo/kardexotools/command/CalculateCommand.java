package net.kardexo.kardexotools.command;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import ch.obermuhlner.math.big.BigDecimalMath;
import net.kardexo.kardexotools.command.CalculateCommand.Expression.ParseException;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class CalculateCommand
{
	private static final Map<UUID, BigDecimal> HISTORY = new HashMap<UUID, BigDecimal>();
	private static final DynamicCommandExceptionType PARSING_EXCEPTION = new DynamicCommandExceptionType(exception -> new LiteralMessage(String.valueOf(exception)));
	private static final SimpleCommandExceptionType NO_ANS_STORED = new SimpleCommandExceptionType(new LiteralMessage("No previous value stored for ans"));
	private static final DynamicCommandExceptionType ERROR_WHILE_COMPUTING = new DynamicCommandExceptionType(cause -> new LiteralMessage("Error while computing result (" + cause + ")"));
	private static final SimpleCommandExceptionType COMPUTATION_TIME_EXCEEDED = new SimpleCommandExceptionType(new LiteralMessage("Computation time exceeded"));
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.##########", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	private static final MathContext MATH_CONTEXT = new MathContext(100);
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralCommandNode<CommandSourceStack> calculate = dispatcher.register(Commands.literal("calculate")
				.then(Commands.argument("expression", StringArgumentType.greedyString())
						.executes(context -> calculate(context, MATH_CONTEXT, StringArgumentType.getString(context, "expression"))))
				.then(Commands.literal("precise")
						.then(Commands.argument("precision", IntegerArgumentType.integer(1))
								.then(Commands.argument("expression", StringArgumentType.greedyString())
										.executes(context -> calculate(context, new MathContext(IntegerArgumentType.getInteger(context, "precision")), StringArgumentType.getString(context, "expression")))))));
		dispatcher.register(Commands.literal("calc")
				.redirect(calculate));
	}
	
	private static int calculate(CommandContext<CommandSourceStack> context, MathContext mathContext, String expression) throws CommandSyntaxException
	{
		UUID uuid = context.getSource().getEntity() != null ? context.getSource().getEntity().getUUID() : Util.NIL_UUID;
		
		if(expression.contains("ans") && !HISTORY.containsKey(uuid))
		{
			throw NO_ANS_STORED.create();
		}
		
		try
		{
			return CompletableFuture.supplyAsync(() ->
			{
				try
				{
					BigDecimal x = new Expression(expression).eval(HISTORY.get(uuid), mathContext);
					context.getSource().sendSuccess(new TextComponent(expression + " = " + DECIMAL_FORMAT.format(x)), false);
					HISTORY.put(uuid, x);
					return x;
				}
				catch(ParseException e)
				{
					throw new CompletionException(e);
				}
			}).get(5, TimeUnit.SECONDS).intValue();
		}
		catch(CompletionException e)
		{
			throw PARSING_EXCEPTION.create(e.getCause().getMessage());
		}
		catch(TimeoutException e)
		{
			throw COMPUTATION_TIME_EXCEEDED.create();
		}
		catch(InterruptedException | ExecutionException e)
		{
			throw ERROR_WHILE_COMPUTING.create(e.getMessage());
		}
	}
	
	public static class Expression
	{
		private final String string;
		
		public Expression(String string)
		{
			this.string = string;
		}
		
		public BigDecimal eval() throws ParseException
		{
			return this.eval(null, MATH_CONTEXT);
		}
		
		public BigDecimal eval(BigDecimal ans, MathContext context) throws ParseException
		{
			Parser parser = new Parser(this.string, ans, context);
			BigDecimal x = parser.parseExpression();
			
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
			private final BigDecimal ans;
			private final MathContext context;
			private int position;
			private int character;
			
			public Parser(String string, BigDecimal ans, MathContext context)
			{
				this.string = string;
				this.ans = ans;
				this.context = context;
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
			
			public BigDecimal parseExpression() throws ParseException
			{
				BigDecimal x = this.parseTerm();
				
				while(true)
				{
					if(this.consume('+'))
					{
						x = x.add(this.parseTerm());
					}
					else if(this.consume('-'))
					{
						x = x.subtract(this.parseTerm());
					}
					else
					{
						return x;
					}
				}
			}
			
			private BigDecimal parseTerm() throws ParseException
			{
				BigDecimal x = this.parseFactor();
				
				while(true)
				{
					if(this.consume('*'))
					{
						x = x.multiply(this.parseFactor(), this.context);
					}
					else if(this.consume('/'))
					{
						BigDecimal y = this.parseFactor();
						
						if(y.equals(BigDecimal.ZERO))
						{
							throw new ParseException("Division by zero", this.position);
						}
						
						x = x.divide(y, this.context);
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
							x = x.remainder(this.parseExpression(), this.context);
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
			
			private BigDecimal parseArgument() throws ParseException
			{
				this.consumeExpected('(');
				BigDecimal x = this.parseExpression();
				this.consumeExpected(')');
				return x;
			}
			
			private BigDecimal parseArguments(BiFunction<BigDecimal, BigDecimal, BigDecimal> function, boolean multipleArgs) throws ParseException
			{
				this.consumeExpected('(');
				BigDecimal x = this.parseExpression();
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
			
			private BigDecimal parseFactor() throws ParseException
			{
				if(this.consume('+'))
				{
					return this.parseFactor();
				}
				
				if(this.consume('-'))
				{
					return this.parseFactor().negate(this.context);
				}
				
				BigDecimal x;
				
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
						x = BigDecimalMath.toBigDecimal(this.string.substring(start, this.getPosition()), this.context);
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
							x = BigDecimalMath.e(this.context);
							break;
						case "pi":
							x = BigDecimalMath.pi(this.context);
							break;
						case "ans":
							if(this.ans == null)
							{
								throw new ParseException("No value for ans", start);
							}
							x = this.ans;
							break;
						case "sqrt":
							x = this.parseArgument();
							
							try
							{
								x = BigDecimalMath.sqrt(x, this.context);
							}
							catch(ArithmeticException e)
							{
								throw new ParseException("Undefined input for function sqrt: " + DECIMAL_FORMAT.format(x), start);
							}
							
							break;
						case "ceil":
							x = this.parseArgument().round(new MathContext(this.context.getPrecision(), RoundingMode.CEILING));
							break;
						case "floor":
							x = this.parseArgument().round(new MathContext(this.context.getPrecision(), RoundingMode.FLOOR));
							break;
						case "rad":
							x = this.parseArgument().multiply(BigDecimalMath.pi(this.context).divide(new BigDecimal(180), this.context), this.context);
							break;
						case "deg":
							x = this.parseArgument().multiply(new BigDecimal(180).divide(BigDecimalMath.pi(this.context), this.context), this.context);
							break;
						case "round":
							x = this.parseArgument().round(this.context);
							break;
						case "log":
							x = this.parseArgument();
							
							try
							{
								x = BigDecimalMath.log10(x, this.context);
							}
							catch(ArithmeticException e)
							{
								throw new ParseException("Undefined input for function log: " + DECIMAL_FORMAT.format(x), start);
							}
							
							break;
						case "ln":
							x = this.parseArgument();
							
							try
							{
								x = BigDecimalMath.log(x, this.context);
							}
							catch(ArithmeticException e)
							{
								throw new ParseException("Undefined input for function ln: " + DECIMAL_FORMAT.format(x), start);
							}
							
							break;
						case "sin":
							x = BigDecimalMath.sin(this.parseArgument(), this.context);
							break;
						case "cos":
							x = BigDecimalMath.cos(this.parseArgument(), this.context);
							break;
						case "tan":
							x = BigDecimalMath.tan(this.parseArgument(), this.context);
							break;
						case "sinh":
							x = BigDecimalMath.sinh(this.parseArgument(), this.context);
							break;
						case "cosh":
							x = BigDecimalMath.cosh(this.parseArgument(), this.context);
							break;
						case "tanh":
							x = BigDecimalMath.tanh(this.parseArgument(), this.context);
							break;
						case "asin":
							x = this.parseArgument();
							
							try
							{
								x = BigDecimalMath.asin(x, this.context);
							}
							catch(ArithmeticException e)
							{
								throw new ParseException("Undefined input for function asin: " + DECIMAL_FORMAT.format(x), start);
							}
							
							break;
						case "acos":
							x = this.parseArgument();
							
							try
							{
								x = BigDecimalMath.acos(x, this.context);
							}
							catch(ArithmeticException e)
							{
								throw new ParseException("Undefined input for function acos: " + DECIMAL_FORMAT.format(x), start);
							}
							
							break;
						case "atan":
							x = BigDecimalMath.atan(this.parseArgument(), this.context);
							break;
						case "asinh":
							x = BigDecimalMath.asinh(this.parseArgument(), this.context);
							break;
						case "acosh":
							x = BigDecimalMath.acosh(this.parseArgument(), this.context);
							break;
						case "atanh":
							x = BigDecimalMath.atanh(this.parseArgument(), this.context);
							break;
						case "min":
							x = this.parseArguments((a, b) -> a.compareTo(b) < 0 ? a : b, true);
							break;
						case "max":
							x = this.parseArguments((a, b) -> a.compareTo(b) < 0 ? b : a, true);
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
					x = BigDecimalMath.pow(x, this.parseFactor(), this.context);
				}
				else if(this.consume('!'))
				{
					x = BigDecimalMath.factorial(x, this.context);
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
