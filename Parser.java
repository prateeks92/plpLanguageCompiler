package cop5556fa17;



import static cop5556fa17.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.Arrays;

import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionApp;
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
import cop5556fa17.AST.Sink;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;

public class Parser{

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * @return 
	 * 
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException {
		//TODO  implement this
		Token ft = t;
		ArrayList<ASTNode> declarareStatement = new ArrayList<>();
		
		if(ft.kind==IDENTIFIER)
		{
			match(IDENTIFIER);

			while(t.kind == KW_int || t.kind==KW_boolean || t.kind==KW_image || 
				t.kind==KW_url || t.kind==KW_file || t.kind==IDENTIFIER) 
			{
			
				Kind temp = t.kind;
				switch (temp) 
				{
					case KW_int: case KW_boolean: case KW_image: 
					case KW_url: case KW_file:
						ASTNode declare = declaration();
						match(Kind.SEMI);
						declarareStatement.add(declare);
						break;
	
					case IDENTIFIER:
						ASTNode state = statement();
						match(Kind.SEMI);
						declarareStatement.add(state);
						break;
				}
			}
		}
		else
		{
			throw new SyntaxException(t, "Program Exception");
		}

		return new Program(ft, ft, declarareStatement);
	}
	
	
	ASTNode statement() throws SyntaxException
	{
		ASTNode state = null;
		Token ft = t;
		match(IDENTIFIER);

		if(t.kind== OP_RARROW)
		{
			state = statement_imageOut(ft);
		}
		else
			if(t.kind== OP_LARROW)
			{
				state= statement_imageIn(ft); 
			}
			else
				if(t.kind == LSQUARE|| t.kind== OP_ASSIGN)
				{
					state= statement_assignment(ft);
				}
				else
					{
						throw new SyntaxException(t, "Statement Exception");
					}
		return state;
		
	}
	
	ASTNode declaration() throws SyntaxException 
	{
		Kind temp = t.kind;
		ASTNode declr;	
		if (t.kind == KW_int || t.kind== KW_boolean)
		{
				declr =  declaration_variable();
		}
		else if (t.kind == KW_image)
			{
				declr = declaration_image();
			}
		else if(t.kind== KW_url ||t.kind== KW_file)
			{
				declr = declaration_sourcesink();
			}
		else
		{
					throw new SyntaxException(t, "Declaration Exception");
		}
		return declr;
	}
		
	
	public Token varType() throws SyntaxException
	{
		Token ft = t;
		if(ft.kind == KW_int )//|| t.kind == KW_boolean)
		{
			match(KW_int);
		}
		else 
			if(ft.kind == KW_boolean)
			{
				match(KW_boolean);
			}
			else
			{
				throw new SyntaxException(t, "VarType exception");
			}
		return ft;
	}

	
	Declaration_Variable declaration_variable() throws SyntaxException
	{
		Token ft = t;
		Token tokType = varType();
		
		Expression e = null;
		Token na = t;
		
		match(IDENTIFIER);
		if(t.kind == OP_ASSIGN)
		{
			match(OP_ASSIGN);
			e = expression();
		}
 		return new Declaration_Variable(ft, tokType, na, e);

	}
	
	
	public Declaration_Image declaration_image() throws SyntaxException
	{
		Token ft = t;
		Source s = null;
		Expression ex = null;
		Expression ey = null;


		match(KW_image);
		if(t.kind==LSQUARE)
		{
			match(LSQUARE);
			ex = expression();
			match(Kind.COMMA);
			ey = expression();
			match(RSQUARE);
		}
		
		Token na = t;
		match(IDENTIFIER);
		if(t.kind==OP_LARROW)
		{
			match(OP_LARROW);
			s = source();
		}

		return new Declaration_Image(ft, ex, ey, na, s);
	}
	
	
	public Source source() throws SyntaxException
	{
		Token ft = t;
		Source retSrc = null;
        if (t.kind==Kind.STRING_LITERAL) 
        {
        	String fileOrUrl = t.getText();
            match(Kind.STRING_LITERAL);
            retSrc = new Source_StringLiteral(ft,fileOrUrl);
        } 
        else if (t.kind==Kind.OP_AT) 
        {
            match(Kind.OP_AT);
            Expression expr1 = expression();
            retSrc = new Source_CommandLineParam(ft,expr1);
        } 
        else if (t.kind==Kind.IDENTIFIER) 
        {
        	Token name = t;  
            match(Kind.IDENTIFIER); 					 
            retSrc = new Source_Ident(ft,name);
        } 
        else 
            throw new SyntaxException(t, "Issue in source terminal");
        
        return retSrc;
	}
	
	
	Sink sink() throws SyntaxException
	{
		Token ft = t;
		Sink s = null;
		Kind temp = t.kind;
		
		switch (temp) 
		{
			case IDENTIFIER:
				match(IDENTIFIER);
				s = new Sink_Ident(ft, ft);
				break;
				
			case KW_SCREEN:
				match(KW_SCREEN);
				s = new Sink_SCREEN(ft);
				break;
				
			default:
				throw new SyntaxException(t, "Sink exception");
		}
		return s;
	}
	
	
	Declaration_SourceSink declaration_sourcesink() throws SyntaxException
	{
		Token ft = t;
		Token sst = sourceSinkType();
		Token na = t;
		match(IDENTIFIER);
		match(OP_ASSIGN);
		Source s = source();
		return new Declaration_SourceSink(ft, sst, na, s);
	}
	
	
	Token sourceSinkType() throws SyntaxException
	{
		Token ft = null;
		if(t.kind==KW_url) 
		{
			ft = t;
			match(KW_url);
		}
		else if(t.kind==KW_file)
		{
			ft = t;
			match(KW_file);
		}
		else
			throw new SyntaxException(t, "Illegal source sink type");
		return ft;
	}
	
	
	Index selector() throws SyntaxException{
		Token ft = t;
		Expression e = expression();
		match(COMMA);
		Expression e1 = expression();
		return new Index(ft, e, e1);
	}
	
	Index selector_LHS () throws SyntaxException
	{
		Index i = null;
		match(Kind.LSQUARE);
		Kind temp = t.kind;
		
		switch (temp) 
		{
			case KW_x:
				i = selector_XY();
				break;
				
			case KW_r:
				i = selector_RA();
				break;

			default:
				throw new SyntaxException(t, "LhsSelector Exception");
		}
		match(Kind.RSQUARE);
		return i;
	}
	
	Index selector_XY() throws SyntaxException
	{
		Token ft = t;
		Expression e =  new Expression_PredefinedName(ft, Kind.KW_x);
		match(KW_x);
		match(COMMA);
		Expression e1 =  new Expression_PredefinedName(ft, Kind.KW_y);
		match(KW_y);
		return new Index(ft, e, e1);
	}
	
	
	Index selector_RA() throws SyntaxException
	{
		Token ft = t;
		Expression e =  new Expression_PredefinedName(ft, Kind.KW_r);
		match(KW_r);
		match(COMMA);
		Expression e1 =  new Expression_PredefinedName(ft, Kind.KW_a);
		match(KW_a);
		return new Index(ft, e, e1);
	}
	
	
	Kind function_Name() throws SyntaxException
	{
		Token ft = t;
		Kind temp = t.kind;
		
		switch (temp) 
		{
			case KW_sin: case KW_cos: case KW_atan: case KW_abs: case KW_cart_x: 
				case KW_cart_y: case KW_polar_a: case KW_polar_r:
					consume();
					break;
			
			default:
				throw new SyntaxException(t, "Illegal function Name");
		}
		return ft.kind;
	}
	
	
	Expression_FunctionApp function_application() throws SyntaxException
	{
		Token ft = t;
		Expression_FunctionApp fa = null;
		Kind f = function_Name();
		

		switch(t.kind)
		{
			case LPAREN:
				match(LPAREN);
				Expression e = expression();
				match(RPAREN);
				fa = new Expression_FunctionAppWithExprArg(ft, f, e);
				break;	

			case LSQUARE:
				match(LSQUARE);
				Index i = selector();
				match(RSQUARE);
				fa = new Expression_FunctionAppWithIndexArg(ft, f, i);
				break;
			default:
				throw new SyntaxException(t, "Exception in Function Application");
		}
		return fa;
	}
	
	public LHS LHS(Token na) throws SyntaxException
	{
		Index i = null;
		if(t.kind==LSQUARE)
		{
			match(LSQUARE);
			i = selector_LHS();
			match(RSQUARE);
		}
		return new LHS(na, na, i);

	}
	
	Expression selector_identOrPixelExpression() throws SyntaxException
	{
		Token ft = t;
		match(IDENTIFIER);
		Expression e = new Expression_Ident(ft, ft);
		if(t.kind==LSQUARE)
		{
			match(LSQUARE);
			Index i = selector();
			match(RSQUARE);
			e = new Expression_PixelSelector(ft, ft,i);
		}
		return e;
	}
	
	Expression primary() throws SyntaxException
	{
		Kind temp = t.kind;
		Token ft = t;
		Expression e = null;
		
		switch (temp) 
		{
			case INTEGER_LITERAL:
				e = new Expression_IntLit(ft, t.intVal());
				match(INTEGER_LITERAL);
				break;

			case LPAREN:
				match(LPAREN);
				e=expression();
				match(RPAREN);
				break;
				
			case KW_sin: case KW_cos: case KW_atan: case KW_abs: case KW_cart_x:
				case KW_cart_y: case KW_polar_a: case KW_polar_r:
					e=function_application();
					break;
			
			case BOOLEAN_LITERAL:
				boolean bv = false;
				if(ft.getText().equals("true"))
					bv = true;
				match(BOOLEAN_LITERAL);
				e = new Expression_BooleanLit(ft,bv);
				break;
			
			default:
				throw new SyntaxException(t, "Exception in Primary");
		}
		return e;
	}
	
	
	Expression unaryExpression() throws SyntaxException
	{
		
		Token cur = t;
		Expression expr1 = null;
		Token op = null;

		if(t.kind==Kind.OP_PLUS) 
		{
			op = t;
			match(Kind.OP_PLUS);
			expr1 = new Expression_Unary(cur, op, unaryExpression());

		}
		else if(t.kind==Kind.OP_MINUS)
		{

			op = t;

			match(OP_MINUS);
			expr1 = new Expression_Unary(cur, op, unaryExpression());
		}
		else{

			expr1 = unaryExpression_Other();
		}
		return expr1;
	}
	
	Expression unaryExpression_Other() throws SyntaxException
	{
		Token ft = t;
		Expression e = null;
		
		if(ft.kind==INTEGER_LITERAL||ft.kind==LPAREN||ft.kind==KW_sin||ft.kind==KW_cos||
				ft.kind==KW_atan||ft.kind==KW_abs||ft.kind==KW_cart_x||ft.kind==KW_cart_y||
				ft.kind==KW_polar_a||ft.kind==KW_polar_r||ft.kind==BOOLEAN_LITERAL)
		{
			return primary();
		}
		else
		{
			if(ft.kind==Kind.IDENTIFIER)
			{
				return selector_identOrPixelExpression();
			}
			else
			{
				if(t.kind==Kind.OP_EXCL)
				{
					Token op=t;
					match(OP_EXCL);
					Expression ex = unaryExpression();
					e = new Expression_Unary(ft, op, ex);
				}
				else
					if(t.kind==Kind.KW_x || t.kind==Kind.KW_y || t.kind==Kind.KW_r || t.kind==Kind.KW_a || t.kind==Kind.KW_X || t.kind==Kind.KW_Y || t.kind==Kind.KW_Z || t.kind==Kind.KW_A || t.kind==Kind.KW_R || t.kind==Kind.KW_DEF_X || t.kind==Kind.KW_DEF_Y)
					{
						consume();
					}
					else
						throw new SyntaxException(t,"Invalid Function Name");
			}
			if(e==null)
				return new Expression_PredefinedName(ft, ft.kind);
			else
				return e;
		}
	}
	
	
	Expression mult() throws SyntaxException
	{
		
		Token ft = t;
		Expression e = unaryExpression();
		Expression e1 = null;
	
		
		while(t.kind==OP_TIMES || t.kind==OP_DIV || t.kind==OP_MOD)
		{
			Token op = t;
			if(t.kind==Kind.OP_TIMES)
			{
				match(Kind.OP_TIMES);
			}
			else if(t.kind==Kind.OP_DIV)
			{
				match(Kind.OP_DIV);
			}
			else if(t.kind==Kind.OP_MOD)
			{
				match(Kind.OP_MOD);
			}	
			e1 = unaryExpression();
			e = new Expression_Binary(ft, e, op, e1);
		}
		return e;
	}
	
	Expression add() throws SyntaxException
	{
		Token ft = t;
		Expression e = null;
		Expression e1 = null;
		e = mult();
		while(t.kind == OP_PLUS || t.kind == OP_MINUS)
		{
			Token op = t;
			if(t.kind==Kind.OP_PLUS)
			{
				match(Kind.OP_PLUS);
			}
			else if(t.kind==Kind.OP_MINUS)
			{
				match(Kind.OP_MINUS);	
			}
			e1 = mult();
			e = new Expression_Binary(ft, e, op, e1);
		}
		return e;
	}
	
	Expression rel() throws SyntaxException
	{

		Token ft = t;
		Expression e = null;
		Expression e1 = null;
		
		e = add();
		while(t.kind==OP_LT || t.kind==OP_GT || t.kind==OP_LE || t.kind==OP_GE)
		{
			Token op = t;
			if(t.kind==Kind.OP_LT)
			{
				match(Kind.OP_LT);
			}
			else if(t.kind==Kind.OP_GT)
			{
				match(Kind.OP_GT);
			}
			else if(t.kind==Kind.OP_LE)
			{
				match(Kind.OP_LE);
			}
			else if(t.kind==Kind.OP_GE)
			{
				match(Kind.OP_GE);
			}

			e1 = add();
			e = new Expression_Binary(ft, e, op, e1);
		}
		return e;
	}
	
	Expression eq() throws SyntaxException
	{

		Token ft = t;
		Expression e = rel();
		Expression e1 = null;

		while(t.kind==OP_EQ || t.kind==OP_NEQ)
		{
			Token op = t;
			if(t.kind==OP_EQ)
				match(OP_EQ);
			else
				if(t.kind==OP_NEQ)
					match(OP_NEQ);

			e1 = rel();
			e = new Expression_Binary(ft, e, op, e1);
		}
		return e;
	}
	
	Expression and() throws SyntaxException
	{

		Token ft = t;
		Expression e = eq();
		Expression e1 = null;

		while(t.kind==OP_AND)
		{
			Token op = t;
			match(OP_AND);
			e1 = eq();
			e = new Expression_Binary(ft, e, op, e1);
		}
		return e;
	}
	
	Expression or() throws SyntaxException
	{

		Token ft = t;
		Expression e = and();
		Expression e1 = null;
		
		while(t.kind==OP_OR)
		{
			Token op = t;
			match(OP_OR);
			e1 = and();
			e = new Expression_Binary(ft, e, op, e1);
		}
		return e;
	}

	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */	
	public Expression expression() throws SyntaxException 
	{

		//TODO implement this.
		Token ft = t;
		Expression exp_condi = or();
		if(t.kind==OP_Q)
		{
			match(OP_Q);
			Expression exp_true = expression();
			match(OP_COLON);
			Expression exp_false = expression();
			return new Expression_Conditional(ft, exp_condi, exp_true, exp_false);
		}
		return exp_condi;
	}
	
	
	public Statement statement_assignment(Token na) throws SyntaxException
	{
		LHS l = LHS(na);
		match(OP_ASSIGN);
		Expression e = expression();
		return new Statement_Assign(na, l, e);
	}
	
	public Statement statement_imageIn(Token na) throws SyntaxException
	{
		match(OP_LARROW);
		Source s = source();
		return new Statement_In(na, na, s);
	}
	
	public Statement statement_imageOut(Token na) throws SyntaxException
	{
		match(OP_RARROW);
		Sink s = sink();
		return new Statement_Out(na, na, s);
	}
	
	Token consume() {
		return t = scanner.nextToken();
	}
	
	
	void match(Kind k) throws SyntaxException
	{
		if(t.kind==k)
			consume();
		else
			throw new SyntaxException(t, "Match exception");
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
	
	
}
