package cop5556fa17;

import java.util.ArrayList;
import java.awt.image.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
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
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.Scanner;
import cop5556fa17.Scanner.Kind;
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;
import cop5556fa17.TypeCheckVisitor.SemanticException;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	// ...............................................................................
	/**
	 * ASSIGNMENT 6 PREDEFINED VARIABLES STACK SLOTS
	 */

	int x_slot = 1;
	int y_slot = 2;
	int X_slot = 3;
	int Y_slot = 4;
	int r_slot = 5;
	int a_slot = 6;
	int R_slot = 7;
	int A_slot = 8;
	int DEF_X_slot = 9;
	int DEF_Y_slot = 10;
	int Z_slot = 11;

	// .................................................................................

	// .................................................................................

	/*
	 * METHOD SIGNATURES FROM IMAGE SUPPORT COPIED TO MAKE THINGS EASIER
	 */
	public static final String StringDesc = "Ljava/lang/String;";
	public final static String IntegerDesc = "Ljava/lang/Integer;";
	public static final String ImageDesc = "Ljava/awt/image/BufferedImage;";

	// .................................................................................

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		System.out.println("visitProgram");
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// if GRADE, generates code to add string to log

		// ASKED TO REMOVE
		// CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		// and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		// generates code to add string to log

		// ASKED TO REMOVE
		// CodeGenUtils.genLog(GRADE, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);

		// handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

		// ..........ADD LOCAL VARIABLES FOR ASSIGNMENT 6..................

		mv.visitLocalVariable("x", "I", null, mainStart, mainEnd, 1);
		mv.visitLocalVariable("y", "I", null, mainStart, mainEnd, 2);
		mv.visitLocalVariable("X", "I", null, mainStart, mainEnd, 3);
		mv.visitLocalVariable("Y", "I", null, mainStart, mainEnd, 4);
		mv.visitLocalVariable("r", "I", null, mainStart, mainEnd, 5);
		mv.visitLocalVariable("a", "I", null, mainStart, mainEnd, 6);
		mv.visitLocalVariable("R", "I", null, mainStart, mainEnd, 7);
		mv.visitLocalVariable("A", "I", null, mainStart, mainEnd, 8);

		mv.visitLocalVariable("DEF_X", "I", null, mainStart, mainEnd, 9);
		mv.visitLocalVariable("DEF_Y", "I", null, mainStart, mainEnd, 10);
		mv.visitLocalVariable("Z", "I", null, mainStart, mainEnd, 11);

		// NOT REQUIRED, DONE DIRECTLY IN EXPRESSION_PREDEFINED_NAME
		// mv.visitLdcInsn(new Integer(256));
		// mv.visitVarInsn(ISTORE,DEF_X_slot);
		//
		// mv.visitLdcInsn(new Integer(256));
		// mv.visitVarInsn(ISTORE,DEF_Y_slot);
		//
		// mv.visitLdcInsn(new Integer(16777215));
		// mv.visitVarInsn(ISTORE,Z_slot);

		// ................................................................

		// Sets max stack size and number of local vars.
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily set the parameter in the ClassWriter constructor to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		// TODO
		System.out.println("visitDeclaration_Variable");

		String fieldName, fieldType;
		FieldVisitor fv;
		fieldName = declaration_Variable.name;

		if (declaration_Variable.astType == Type.INTEGER) {
			fieldType = "I";
			// initValue = new Integer();
		} else { // if(declaration_Variable.type.kind == Kind.BOOLEAN_LITERAL) {
			fieldType = "Z";
			// initValue = new Boolean(false);
		}

		fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, null);

		// mv.visitVarInsn(ALOAD, 0);

		if (declaration_Variable.e != null) {
			declaration_Variable.e.visit(this, arg);

			mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		}
		// throw new UnsupportedOperationException();
		return null;
	}
	

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO
		System.out.println("visitExpression_Binary");

		Label l1, l2;
		expression_Binary.e0.visit(this, arg);
		expression_Binary.e1.visit(this, arg);

		Type e0T = expression_Binary.e0.astType;
		Type e1T = expression_Binary.e1.astType;

		Kind op = expression_Binary.op;

		switch (op) {
		case OP_PLUS:
			//if (e0T == Type.INTEGER && e1T == Type.INTEGER)
				mv.visitInsn(IADD);
			break;

		case OP_MINUS:
//			if (e0T == Type.INTEGER && e1T == Type.INTEGER)
				mv.visitInsn(ISUB);
			break;

		case OP_TIMES:
//			if (e0T == Type.INTEGER && e1T == Type.INTEGER)
				mv.visitInsn(IMUL);
			break;

		case OP_DIV:
	//		if (e0T == Type.INTEGER && e1T == Type.INTEGER)
				mv.visitInsn(IDIV);
			break;

		case OP_LT:
			l1 = new Label();
			mv.visitJumpInsn(IF_ICMPGE, l1);
			mv.visitInsn(ICONST_1);
			l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			break;

		case OP_LE:
			l1 = new Label();
			mv.visitJumpInsn(IF_ICMPGT, l1);
			mv.visitInsn(ICONST_1);
			l2 = new Label();
		 	mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			break;

		case OP_GT:
			l1 = new Label();
			mv.visitJumpInsn(IF_ICMPLE, l1);
			mv.visitInsn(ICONST_1);
			l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			break;

		case OP_GE:
			l1 = new Label();
			mv.visitJumpInsn(IF_ICMPLT, l1);
			mv.visitInsn(ICONST_1);
			l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			break;

		case OP_EQ:
			l1 = new Label();
			if (e0T == Type.BOOLEAN || e0T == Type.INTEGER)
				mv.visitJumpInsn(IF_ICMPNE, l1);

			mv.visitInsn(ICONST_1);
			l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			break;

		case OP_NEQ:
			l1 = new Label();
			if (e0T == Type.BOOLEAN || e0T == Type.INTEGER)
				mv.visitJumpInsn(IF_ICMPEQ, l1);

			mv.visitInsn(ICONST_1);
			l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			break;

		case OP_AND:
			mv.visitInsn(IAND);
			break;

		case OP_OR:
			mv.visitInsn(IOR);
			break;

		case OP_MOD:
			if (e1T == Type.INTEGER && e0T == Type.INTEGER)
				mv.visitInsn(IREM);
			break;

		default:
			break;
		}
		// throw new UnsupportedOperationException();

		// ASKED TO REMOVE
		// CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.getType());
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO
		System.out.println("visitExpression_Unary");

		Kind op = expression_Unary.op;
		Type eType = expression_Unary.astType;
		expression_Unary.e.visit(this, arg);
		switch (op) {
		case OP_PLUS:
			break;

		case OP_MINUS:
			if (eType == Type.INTEGER) {
				mv.visitLdcInsn(new Integer(-1));
				mv.visitInsn(IMUL);
			}
			break;

		case OP_EXCL:
			if (eType == Type.INTEGER) {
				mv.visitLdcInsn(new Integer(Integer.MAX_VALUE));
				mv.visitInsn(IXOR);
			} else {
				if (eType == Type.BOOLEAN) {
					mv.visitLdcInsn(new Integer(1));
					mv.visitInsn(IXOR);
				}
			}

		default:
			break;
		}
		// throw new UnsupportedOperationException();

		// ASKED TO REMOVE
		// CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.getType());
		return null;
	}

	// generate code to leave the two values on the stack

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		System.out.println("visitIndex");

		index.e0.visit(this, arg);
		index.e1.visit(this, arg);

		if (index.isCartesian() == false) {
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig,
					false);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig,
					false);
		}
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
		System.out.println("visitExpression_PixelSelector");

		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, "Ljava/awt/image/BufferedImage;");

		expression_PixelSelector.index.visit(this, arg);

		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", ImageSupport.getPixelSig, false);

		return null;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		System.out.println("visitExpression_Conditional");

		// TODO
		expression_Conditional.condition.visit(this, arg);// new Integer(INTEGER.parseInt());
		Label l1 = new Label();
		mv.visitJumpInsn(IFEQ, l1);
		expression_Conditional.trueExpression.visit(this, arg);
		Label l2 = new Label();
		mv.visitJumpInsn(GOTO, l2);
		mv.visitLabel(l1);
		expression_Conditional.falseExpression.visit(this, arg);
		mv.visitLabel(l2);
		return null;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6

		System.out.println("visitDeclaration_Image");

		String fieldName, fieldType;
		FieldVisitor fVisitor;

		fieldName = declaration_Image.name;
		fieldType = "Ljava/awt/image/BufferedImage;";

		fVisitor = cw.visitField(ACC_STATIC, fieldName, fieldType, null, null);

		if (declaration_Image.source != null) {
			declaration_Image.source.visit(this, arg);

			if (declaration_Image.xSize != null) {
				declaration_Image.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			} else {
				mv.visitInsn(ACONST_NULL);
			}

			if (declaration_Image.ySize != null) {
				declaration_Image.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			} else {
				mv.visitInsn(ACONST_NULL);
			}

			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage",
					ImageSupport.readImageSig, false);
		} else {

			if (declaration_Image.xSize != null) {
				declaration_Image.xSize.visit(this, arg);
			} else {
				mv.visitLdcInsn(new Integer(256));
			}

			if (declaration_Image.ySize != null) {
				declaration_Image.ySize.visit(this, arg);
			} else {
				mv.visitLdcInsn(new Integer(256));
			}

			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig, false);

		}
		mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		return null;
		// throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6

		System.out.println("visitSource_StringLiteral");

		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
		return null;

	}

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {

		System.out.println("visitSource_CommandLineParam");

		// TODO
		mv.visitVarInsn(ALOAD, 0);
		source_CommandLineParam.paramNum.visit(this, arg);
		mv.visitInsn(AALOAD);
		// throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
		System.out.println("visitSource_Ident");

		mv.visitLdcInsn(source_Ident.name);

		return null;
		// throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		System.out.println("visitDeclaration_SourceSink");

		FieldVisitor fv = null;

		String fieldName, fieldType;
		fieldName = declaration_SourceSink.name;

		fieldType = "Ljava/lang/String;";
		// what should be the fieldType

		fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, null);
		fv.visitEnd();

		if (declaration_SourceSink.source != null) {
			declaration_SourceSink.source.visit(this, arg);

			mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		}
		return null;
		// throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO
		System.out.println("visitExpression_IntLit");

		mv.visitLdcInsn(expression_IntLit.value);
		// throw new UnsupportedOperationException();

		// ASKED TO REMOVE
		// CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		// TODO HW6
		System.out.println("visitExpression_FunctionAppWithExprArg");

		expression_FunctionAppWithExprArg.arg.visit(this, arg);

		if (expression_FunctionAppWithExprArg.function == Kind.KW_abs) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig, false);
		}

		if (expression_FunctionAppWithExprArg.function == Kind.KW_log) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig, false);
		}
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6

		System.out.println("visitExpression_FunctionAppWithIndexArg");
		
		expression_FunctionAppWithIndexArg.arg.e0.visit(this, arg);
		expression_FunctionAppWithIndexArg.arg.e1.visit(this, arg);

		if (expression_FunctionAppWithIndexArg.function == Kind.KW_cart_x) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
		}

		if (expression_FunctionAppWithIndexArg.function == Kind.KW_cart_y) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		}

		if (expression_FunctionAppWithIndexArg.function == Kind.KW_polar_r) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		}

		if (expression_FunctionAppWithIndexArg.function == Kind.KW_polar_a) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
		}

		return null;
		// throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		System.out.println("visitExpression_PredefinedName");

		if(expression_PredefinedName.kind == Kind.KW_x){
			mv.visitVarInsn(ILOAD, x_slot);
		}
		if(expression_PredefinedName.kind == Kind.KW_y){
			mv.visitVarInsn(ILOAD, y_slot);
		}
		
		if(expression_PredefinedName.kind == Kind.KW_X){
			mv.visitVarInsn(ILOAD, X_slot);
		}
		if(expression_PredefinedName.kind == Kind.KW_Y){
			mv.visitVarInsn(ILOAD, Y_slot);
		}
		if(expression_PredefinedName.kind == Kind.KW_r)
		{
			mv.visitVarInsn(ILOAD, x_slot);
			mv.visitVarInsn(ILOAD, y_slot);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		}
		
		if(expression_PredefinedName.kind == Kind.KW_a)
		{
			mv.visitVarInsn(ILOAD, x_slot);
			mv.visitVarInsn(ILOAD, y_slot);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
		}
		
		if(expression_PredefinedName.kind == Kind.KW_R)
		{
			mv.visitVarInsn(ILOAD, R_slot);
		}
		
		if(expression_PredefinedName.kind == Kind.KW_A){
			mv.visitVarInsn(ILOAD, A_slot);
		}
		
		if(expression_PredefinedName.kind == Kind.KW_DEF_X){
			mv.visitLdcInsn(new Integer(256));
		}
		
		if(expression_PredefinedName.kind == Kind.KW_DEF_Y){
			mv.visitLdcInsn(new Integer(256));
		}
		
		if(expression_PredefinedName.kind == Kind.KW_Z){
			mv.visitLdcInsn(new Integer(16777215));
		}
		return null;
		
//		if (expression_PredefinedName.kind == Kind.KW_x) {
//			mv.visitVarInsn(ILOAD, x_slot);
//		}
//
//		if (expression_PredefinedName.kind == Kind.KW_y) {
//			mv.visitVarInsn(ILOAD, y_slot);
//		}
//
//		if (expression_PredefinedName.kind == Kind.KW_X) {
//			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getX", ImageSupport.getXSig, false);
//		}
//
//		if (expression_PredefinedName.kind == Kind.KW_Y) {
//			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getY", ImageSupport.getYSig, false);
//		}
//
//		if (expression_PredefinedName.kind == Kind.KW_a) {
//			mv.visitVarInsn(ILOAD, a_slot);
//		}
//
//		if (expression_PredefinedName.kind == Kind.KW_r) {
//			mv.visitVarInsn(ILOAD, r_slot);
//		}
//
//		if (expression_PredefinedName.kind == Kind.KW_A) {
//			mv.visitInsn(ICONST_0);
//			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getY", ImageSupport.getYSig, false);
//			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_a", RuntimeFunctions.polar_aSig,
//					false);
//		}
//
//		if (expression_PredefinedName.kind == Kind.KW_R) {
//			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getX", ImageSupport.getXSig, false);
//			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getY", ImageSupport.getYSig, false);
//			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_r", RuntimeFunctions.polar_rSig,
//					false);
//		}
//
//		if (expression_PredefinedName.kind == Kind.KW_DEF_X) {
//			mv.visitLdcInsn(new Integer(256));
//		}
//
//		if (expression_PredefinedName.kind == Kind.KW_DEF_Y) {
//			mv.visitLdcInsn(new Integer(256));
//		}
//
//		if (expression_PredefinedName.kind == Kind.KW_Z) {
//			mv.visitLdcInsn(new Integer(16777215));
//		}
//
//		return null;

		// throw new UnsupportedOperationException();
	}

	/**
	 * For Integers and booleans, the only "sink"is the screen, so generate code to
	 * print to console. For Images, load the Image onto the stack and visit the
	 * Sink which will generate the code to handle the image.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5: only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		System.out.println("visitStatement_Out");

		// Type sType = statement_Out.sink.getType();
		// switch (sType)
		// {
		// case SCREEN:
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		if (statement_Out.getDec().astType == Type.INTEGER) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "I");
			CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
		}

		if (statement_Out.getDec().astType == Type.BOOLEAN) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Z");
			CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
		}

		// ..........................HW 6.........................
		if (statement_Out.getDec().astType == Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Ljava/awt/image/BufferedImage;");
			CodeGenUtils.genLogTOS(GRADE, mv, Type.IMAGE);
			statement_Out.sink.visit(this, arg);
		}

		return null;
		// throw new UnsupportedOperationException();
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 * In HW5, you only need to handle INTEGER and BOOLEAN Use
	 * java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean to convert
	 * String to actual type.
	 * 
	 * TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		// TODO (see comment )
		System.out.println("visitStatement_In");

		Type sType = statement_In.source.astType;
		statement_In.source.visit(this, arg);
		if (statement_In.getDec().astType == Type.INTEGER) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "I");
		} 
		else 
		{
			if (statement_In.getDec().astType == Type.BOOLEAN) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
				mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Z");
			} 
			else 
			{
				if (statement_In.getDec().astType == Type.IMAGE) {
					Declaration_Image declaration = (Declaration_Image) statement_In.getDec();
					if (declaration.xSize != null && declaration.ySize != null) {
						declaration.xSize.visit(this, null);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
						declaration.ySize.visit(this, null);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
					}
					else 
					{
						mv.visitInsn(ACONST_NULL);
						mv.visitInsn(ACONST_NULL);
					}
					mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig,
							false);
					mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Ljava/awt/image/BufferedImage;");
				}
			}
		}
		
		return null;
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	// @Override
	// public Object visitStatement_Transform(Statement_Assign statement_Assign,
	// Object arg) throws Exception {
	// //TODO (see comment)
	// throw new UnsupportedOperationException();
	// }

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO (see comment)
		System.out.println("visitLHS");

		Type lhsT = lhs.astType;
		switch (lhsT) {
		case INTEGER:
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "I");
			break;

		case BOOLEAN:
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "Z");
			break;

		case IMAGE:
			mv.visitFieldInsn(GETSTATIC, className, lhs.name, "Ljava/awt/image/BufferedImage;");
			mv.visitVarInsn(ILOAD, x_slot);
			mv.visitVarInsn(ILOAD, y_slot);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", ImageSupport.setPixelSig, false);
			break;

		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		// TODO HW6
		System.out.println("visitSink_SCREEN");

		mv.visitMethodInsn(INVOKESTATIC, ImageFrame.className , "makeFrame",
				ImageSupport.makeFrameSig, false);

		mv.visitInsn(POP);

		return null;

		// throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		// TODO HW6
		System.out.println("visitSink_Ident");

		mv.visitLdcInsn(sink_Ident.name);

		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", ImageSupport.writeSig, false);

		return null;
		// throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		// TODO
		System.out.println("visitExpression_BooleanLit");

		boolean expV = expression_BooleanLit.value;
		if (expV == true) {
			mv.visitInsn(ICONST_1);
		} else {
			mv.visitInsn(ICONST_0);
		}

		// ASKED TO REMOVE
		// CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident, Object arg) throws Exception {

		System.out.println("visitExpression_Ident");

		// TODO

		if (expression_Ident.astType == Type.BOOLEAN)
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "Z");
		else {
			if (expression_Ident.astType == Type.INTEGER)
				mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "I");
		}
		// throw new UnsupportedOperationException();

		// ASKED TO REMOVE
		// CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.getType());

		return null;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("visitStatement_Assign");
		FieldVisitor fv;

		if (statement_Assign.lhs.astType == TypeUtils.Type.INTEGER
				|| statement_Assign.lhs.astType == TypeUtils.Type.BOOLEAN) {

			statement_Assign.e.visit(this, null);
			statement_Assign.lhs.visit(this, null);
		} else if (statement_Assign.lhs.astType == Type.IMAGE) {
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, x_slot);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, y_slot);

			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, "Ljava/awt/image/BufferedImage;");
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig,
					false);
			mv.visitVarInsn(ISTORE, X_slot);
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, "Ljava/awt/image/BufferedImage;");
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitVarInsn(ISTORE, Y_slot);

			Label ll6 = new Label();
			mv.visitJumpInsn(GOTO, ll6);
			Label ll7 = new Label();
			mv.visitLabel(ll7);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, y_slot);
			Label ll8 = new Label();
			mv.visitJumpInsn(GOTO, ll8);
			Label ll9 = new Label();
			mv.visitLabel(ll9);
			mv.visitVarInsn(ILOAD, x_slot);
			mv.visitVarInsn(ILOAD, y_slot);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
			mv.visitVarInsn(ISTORE, r_slot);
			Label ll10 = new Label();
			mv.visitLabel(ll10);
			mv.visitVarInsn(ILOAD, x_slot);
			mv.visitVarInsn(ILOAD, y_slot);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
			mv.visitVarInsn(ISTORE, a_slot);
			Label ll11 = new Label();
			mv.visitLabel(ll11);
			statement_Assign.e.visit(this, null);
			statement_Assign.lhs.visit(this, null);
			Label ll12 = new Label();
			mv.visitLabel(ll12);
			mv.visitIincInsn(y_slot, 1);
			mv.visitLabel(ll8);
			mv.visitVarInsn(ILOAD, y_slot);
			mv.visitVarInsn(ILOAD, Y_slot);
			mv.visitJumpInsn(IF_ICMPLT, ll9);
			Label ll13 = new Label();
			mv.visitLabel(ll13);
			mv.visitIincInsn(x_slot, 1);
			mv.visitLabel(ll6);
			mv.visitVarInsn(ILOAD, x_slot);
			mv.visitVarInsn(ILOAD, X_slot);
			mv.visitJumpInsn(IF_ICMPLT, ll7);
			// }
		}
		// throw new UnsupportedOperationException();
		return null;
	}
}
