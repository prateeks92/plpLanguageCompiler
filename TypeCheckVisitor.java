package cop5556fa17;

import java.net.URL;
import java.util.HashMap;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeCheckVisitor.SemanticException;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.TypeUtils;
import cop5556fa17.TypeUtils.Type;
import java.net.*;

public class TypeCheckVisitor implements ASTVisitor {
	

		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}
		
		public HashMap<String, ASTNode> symbolTable = new HashMap<>();		
		
		
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		if(declaration_Variable.e != null) {
			declaration_Variable.e.astType= (TypeUtils.Type) declaration_Variable.e.visit(this, arg);
		}
		
		if (symbolTable.get(declaration_Variable.name) != null)
			throw new SemanticException(declaration_Variable.firstToken,"duplicate Variable declaration.");
		
		symbolTable.put(declaration_Variable.name, declaration_Variable);
		declaration_Variable.astType = TypeUtils.getType(declaration_Variable.firstToken);
		if (declaration_Variable.e != null)
			if (declaration_Variable.astType != declaration_Variable.e.astType)
				throw new SemanticException(declaration_Variable.firstToken, "Declaration Variable mismatch: expression is :"+ declaration_Variable.e.astType+ " and variable is :" + declaration_Variable.astType);
		return declaration_Variable;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		expression_Binary.e0.astType= (TypeUtils.Type) expression_Binary.e0.visit(this, null);
		expression_Binary.e1.astType= (TypeUtils.Type) expression_Binary.e1.visit(this, null);

		Kind temp = expression_Binary.op;
			
		if(temp.equals(Kind.OP_EQ) || temp.equals(Kind.OP_NEQ)) {
			expression_Binary.astType = Type.BOOLEAN;
		}
		else 
			if((temp.equals(Kind.OP_GE) || temp.equals(Kind.OP_GT) || temp.equals(Kind.OP_LT) 
				|| temp.equals(Kind.OP_LE)) && expression_Binary.e0.astType == Type.INTEGER)
		{
			expression_Binary.astType = Type.BOOLEAN;
		}
		else 
			if ((temp.equals(Kind.OP_OR) ||temp.equals(Kind.OP_AND)) 
				&& (expression_Binary.e0.astType == Type.INTEGER || expression_Binary.e0.astType == Type.BOOLEAN)) 
			{
				expression_Binary.astType = expression_Binary.e0.astType;
			}
			else 
				if((temp.equals(Kind.OP_DIV) || temp.equals(Kind.OP_MINUS)
						|| temp.equals(Kind.OP_MOD) || temp.equals(Kind.OP_PLUS)
						|| temp.equals(Kind.OP_POWER) || temp.equals(Kind.OP_TIMES)) 
						&& expression_Binary.e0.astType == Type.INTEGER)
				{
					expression_Binary.astType = Type.INTEGER;
				}
				else
					expression_Binary.astType = null;
		
		if (!(expression_Binary.e0.astType==expression_Binary.e1.astType && expression_Binary.astType!=null))
			throw new SemanticException(expression_Binary.firstToken, "Expressions' type not same or binary expression type null");
	
		return expression_Binary.astType;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception 
	{
		expression_Unary.e.astType= (TypeUtils.Type) expression_Unary.e.visit(this, null);
		
		if(expression_Unary.op == Kind.OP_EXCL)
		{
			if(expression_Unary.e.astType == TypeUtils.Type.BOOLEAN || expression_Unary.e.astType == TypeUtils.Type.INTEGER)
				expression_Unary.astType = expression_Unary.e.astType;
		} 
		else 
			if(expression_Unary.op == Kind.OP_PLUS || expression_Unary.op == Kind.OP_MINUS)
			{
				if(expression_Unary.e.astType == TypeUtils.Type.INTEGER)
					expression_Unary.astType = TypeUtils.Type.INTEGER;
			} 
			else 
			{
				throw new SemanticException(expression_Unary.firstToken, "unary expression type mismatch.");
			}
		return expression_Unary.astType;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception 
	{	
		index.e0.astType = (TypeUtils.Type) index.e0.visit(this, null);
		index.e1.astType = (TypeUtils.Type) index.e1.visit(this, null);
	
		if(index.e0.astType == TypeUtils.Type.INTEGER && index.e1.astType == TypeUtils.Type.INTEGER)
		{
			index.setCartesian(!(index.e0.firstToken.kind == Kind.KW_r && index.e1.firstToken.kind == Kind.KW_a));
			return index.astType;
		}
		else
		{
			throw new SemanticException(index.firstToken, "index type mismatch. E0 is:" +index.e0.astType.toString()+"and E1 is :"+index.e1.astType.toString());
		}
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception 
	{
		if(!(symbolTable.get(expression_PixelSelector.name) == null))
		{
			TypeUtils.Type name = symbolTable.get(expression_PixelSelector.name).astType;
			expression_PixelSelector.index.astType = (TypeUtils.Type) expression_PixelSelector.index.visit(this, null);
			
			if (name == TypeUtils.Type.IMAGE) 
			{
				expression_PixelSelector.astType= TypeUtils.Type.INTEGER;
			} 
			else 
				if (expression_PixelSelector.index == null) 
				{
					expression_PixelSelector.astType = name;
				}
				else 
				{
					throw new SemanticException(expression_PixelSelector.firstToken, "pixel selector expression can't be null");
				}

			return expression_PixelSelector.astType;
		}
		else 
		{
			throw new SemanticException(expression_PixelSelector.firstToken, expression_PixelSelector.name + " not declared.");
		}
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception 
	{
		expression_Conditional.condition.astType = (TypeUtils.Type) expression_Conditional.condition.visit(this, null);
		expression_Conditional.trueExpression.astType = (TypeUtils.Type)expression_Conditional.trueExpression.visit(this, null);
		expression_Conditional.falseExpression.astType =(TypeUtils.Type) expression_Conditional.falseExpression.visit(this, null);
		
		if(expression_Conditional.condition.astType == TypeUtils.Type.BOOLEAN && expression_Conditional.trueExpression.astType == expression_Conditional.falseExpression.astType)
		{
			expression_Conditional.astType = expression_Conditional.trueExpression.astType;
			return expression_Conditional.astType;
		} 
		else
		{
			throw new SemanticException(expression_Conditional.firstToken, "conditional expression type mismatch");
		}
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception 
	{
		if(declaration_Image.source != null) 
			declaration_Image.source.visit(this, arg);
//		if(declaration_Image.xSize != null) 
//			declaration_Image.xSize.visit(this, arg);
//		if(declaration_Image.ySize != null) 
//			declaration_Image.ySize.visit(this, arg);
		
		if(declaration_Image.xSize != null && declaration_Image.ySize!=null)
		{
			declaration_Image.xSize.visit(this, arg);
			declaration_Image.ySize.visit(this, arg);
			
			if(declaration_Image.xSize.astType != TypeUtils.Type.INTEGER || declaration_Image.ySize.astType != TypeUtils.Type.INTEGER)
				throw new SemanticException(declaration_Image.firstToken, "xSize and ySize type error");
		}		
		if (symbolTable.get(declaration_Image.name)!= null)
			throw new SemanticException(declaration_Image.firstToken,"declaration image already in table.");
		
		declaration_Image.astType = Type.IMAGE;   
		symbolTable.put(declaration_Image.name, declaration_Image);
		
		return declaration_Image;
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(isValidURL(source_StringLiteral.fileOrUrl))
			source_StringLiteral.astType = Type.URL;
		else
			source_StringLiteral.astType = Type.FILE;
		
		return source_StringLiteral.astType;
		//throw new UnsupportedOperationException();
	}
	
	public boolean isValidURL(String pUrl) 
	{
		URL u;
        try 
        {
            u = new URL(pUrl);
        } 
        catch (MalformedURLException e) 
        {
            return false;
        }
        
        try 
        {
            u.toURI();
        } 
        catch (URISyntaxException e) 
        {
            return false;
        }
        return true;
    }
	
	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception 
	{
		source_CommandLineParam.paramNum.visit(this, null);
		source_CommandLineParam.astType = null;
		if(source_CommandLineParam.paramNum.astType != TypeUtils.Type.INTEGER)
		{
			throw new SemanticException(source_CommandLineParam.firstToken, "command line params type mismatch");
		}
		return source_CommandLineParam;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception 
	{
		if(symbolTable.get(source_Ident.name) !=null)
		{
			source_Ident.astType = symbolTable.get(source_Ident.name).astType;
			if(source_Ident.astType != TypeUtils.Type.FILE && source_Ident.astType != TypeUtils.Type.URL)
			{
				throw new SemanticException(source_Ident.firstToken, "source ident type mismatch");
			}
			return source_Ident.astType;
		} 
		else
		{
			throw new SemanticException(source_Ident.firstToken, "source ident " + source_Ident.name + " not declared.");
		}
	}


	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO Auto-generated method stub		
		
		if(declaration_SourceSink.source != null)
			declaration_SourceSink.source.visit(this, null);
		else
			throw new SemanticException(declaration_SourceSink.firstToken, "no source for source sink");
		
		if(symbolTable.get(declaration_SourceSink.name)!=null)
			throw new SemanticException(declaration_SourceSink.firstToken, "exception in source sink");
		
		symbolTable.put(declaration_SourceSink.name, declaration_SourceSink);
		declaration_SourceSink.astType = TypeUtils.getType(declaration_SourceSink.firstToken);
		
		
		
		//4 change 6
		if ((declaration_SourceSink.source.astType == declaration_SourceSink.astType) || (declaration_SourceSink.source.astType == null)) {
			return declaration_SourceSink;
		} 
		else {
			throw new SemanticException(declaration_SourceSink.firstToken, "type mismatch in source sink");
		}
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception 
	{
		expression_IntLit.astType = TypeUtils.Type.INTEGER;
		return expression_IntLit.astType;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception 
	{
		expression_FunctionAppWithExprArg.arg.astType = (TypeUtils.Type) expression_FunctionAppWithExprArg.arg.visit(this, null);
		if(expression_FunctionAppWithExprArg.arg.astType == TypeUtils.Type.INTEGER)
		{
			expression_FunctionAppWithExprArg.astType = TypeUtils.Type.INTEGER;
			return expression_FunctionAppWithExprArg.astType;
		} 
		else
		{
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, "function app with arg type mismatch. arg should be int");
		}
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception 
	{
		expression_FunctionAppWithIndexArg.arg.astType = (TypeUtils.Type) expression_FunctionAppWithIndexArg.arg.visit(this, null);
		expression_FunctionAppWithIndexArg.astType = TypeUtils.Type.INTEGER;
		return expression_FunctionAppWithIndexArg.astType;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception 
	{
		expression_PredefinedName.astType = TypeUtils.Type.INTEGER;
		return expression_PredefinedName.astType;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception 
	{
		if(symbolTable.get(statement_Out.name) !=null)
		{
			ASTNode node = symbolTable.get(statement_Out.name);
			statement_Out.setDec((Declaration) node);
			statement_Out.sink.astType = (TypeUtils.Type) statement_Out.sink.visit(this, null);
			
			TypeUtils.Type nT = node.astType;
			
			switch (nT) 
			{
				case INTEGER: case BOOLEAN:
					if(statement_Out.sink.astType == TypeUtils.Type.SCREEN)
						return statement_Out.getDec().astType;
				
				case IMAGE:
					if(statement_Out.sink.astType == TypeUtils.Type.FILE || statement_Out.sink.astType == TypeUtils.Type.SCREEN)
						return statement_Out.getDec().astType;
						

				default:
					throw new SemanticException(statement_Out.firstToken, "statement out type mismatch");
			}
		}
		else 
		{
			throw new SemanticException(statement_Out.firstToken, "statement out variable " + statement_Out.name + "not declared.");
		}
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception 
	{
//		if(symbolTable.get(statement_In.name) != null)
	//	{
			ASTNode node = symbolTable.get(statement_In.name);
			statement_In.setDec((Declaration)node);
			statement_In.source.visit(this, null);
			
//			if(node.astType != statement_In.source.astType)
//			{
//				throw new SemanticException(statement_In.source.firstToken, "statement in type mismatch");
//			}
			return statement_In.getDec().astType;
//		}
//		else
//		{
//			throw new SemanticException(statement_In.firstToken, "statemnet in variable " + statement_In.name + "not declared.");
//		}
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception 
	{
		 statement_Assign.lhs.astType = (TypeUtils.Type) statement_Assign.lhs.visit(this, null);
		 statement_Assign.e.astType = (TypeUtils.Type) statement_Assign.e.visit(this, null);
		 
		 if((statement_Assign.lhs.astType == statement_Assign.e.astType) || 
				 ((statement_Assign.lhs.astType == TypeUtils.Type.IMAGE) && statement_Assign.e.astType == TypeUtils.Type.INTEGER))
		 {
			statement_Assign.setCartesian(statement_Assign.lhs.isCartesian);
			return statement_Assign.astType;
		} else{
			throw new SemanticException(statement_Assign.firstToken, "statement assign type mismatch");
		}
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception 
	{
		if(symbolTable.get(lhs.name) != null)
		{
			Declaration declr = (Declaration) symbolTable.get(lhs.name);
			lhs.astType = declr.astType;
			
			if(lhs.index != null)
			{
				lhs.index.astType = (TypeUtils.Type) lhs.index.visit(this, null);
				lhs.isCartesian = lhs.index.isCartesian();
			} 
			else
			{
				lhs.isCartesian = false;
			}
			return lhs.astType;
		} 
		else
		{
			throw new SemanticException(lhs.firstToken, "LHS variable " + lhs.name + "not declared.");
		}	
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception 
	{
		sink_SCREEN.astType = Type.SCREEN;
		return sink_SCREEN.astType;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception 
	{
		if(symbolTable.get(sink_Ident.name) != null)
		{
			sink_Ident.astType = symbolTable.get(sink_Ident.name).astType;

			if(sink_Ident.astType != TypeUtils.Type.FILE)
			{
				throw new SemanticException(sink_Ident.firstToken,"sink ident"+ sink_Ident.name + " should be FILE, but is : "+sink_Ident.astType.toString());
			}
			return sink_Ident.astType;
		} 
		else 
		{
			throw new SemanticException(sink_Ident.firstToken, "sink ident" + sink_Ident.name + " not declared.");
		}
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception 
	{
		expression_BooleanLit.astType = TypeUtils.Type.BOOLEAN;
		return expression_BooleanLit.astType;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception 
	{
		if(symbolTable.get(expression_Ident.name) != null)
		{
			expression_Ident.astType = symbolTable.get(expression_Ident.name).astType;
			return expression_Ident.astType;
		} 
		else
		{
			throw new SemanticException(expression_Ident.firstToken, "expression ident " + expression_Ident.name + " not declared.");
		}
	}
}
