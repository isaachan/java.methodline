package com.thoughtworks.jsa;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
/** A Java 1.5 grammar for ANTLR v3 derived from the spec
 *
 *  This is a very close representation of the spec; the changes
 *  are comestic (remove left recursion) and also fixes (the spec
 *  isn't exactly perfect).  I have run this on the 1.4.2 source
 *  and some nasty looking enums from 1.5, but have not really
 *  tested for 1.5 compatibility.
 *
 *  I built this with: java -Xmx100M org.antlr.Tool java.g 
 *  and got two errors that are ok (for now):
 *  java.g:691:9: Decision can match input such as
 *    "'0'..'9'{'E', 'e'}{'+', '-'}'0'..'9'{'D', 'F', 'd', 'f'}"
 *    using multiple alternatives: 3, 4
 *  As a result, alternative(s) 4 were disabled for that input
 *  java.g:734:35: Decision can match input such as "{'$', 'A'..'Z',
 *    '_', 'a'..'z', '\u00C0'..'\u00D6', '\u00D8'..'\u00F6',
 *    '\u00F8'..'\u1FFF', '\u3040'..'\u318F', '\u3300'..'\u337F',
 *    '\u3400'..'\u3D2D', '\u4E00'..'\u9FFF', '\uF900'..'\uFAFF'}"
 *    using multiple alternatives: 1, 2
 *  As a result, alternative(s) 2 were disabled for that input
 *
 *  You can turn enum on/off as a keyword :)
 *
 *  Version 1.0 -- initial release July 5, 2006 (requires 3.0b2 or higher)
 *
 *  Primary author: Terence Parr, July 2006
 *
 *  Version 1.0.1 -- corrections by Koen Vanderkimpen & Marko van Dooren,
 *      October 25, 2006;
 *      fixed normalInterfaceDeclaration: now uses typeParameters instead
 *          of typeParameter (according to JLS, 3rd edition)
 *      fixed castExpression: no longer allows expression next to type
 *          (according to semantics in JLS, in contrast with syntax in JLS)
 *
 *  Version 1.0.2 -- Terence Parr, Nov 27, 2006
 *      java spec I built this from had some bizarre for-loop control.
 *          Looked weird and so I looked elsewhere...Yep, it's messed up.
 *          simplified.
 *
 *  Version 1.0.3 -- Chris Hogue, Feb 26, 2007
 *      Factored out an annotationName rule and used it in the annotation rule.
 *          Not sure why, but typeName wasn't recognizing references to inner
 *          annotations (e.g. @InterfaceName.InnerAnnotation())
 *      Factored out the elementValue section of an annotation reference.  Created 
 *          elementValuePair and elementValuePairs rules, then used them in the 
 *          annotation rule.  Allows it to recognize annotation references with 
 *          multiple, comma separated attributes.
 *      Updated elementValueArrayInitializer so that it allows multiple elements.
 *          (It was only allowing 0 or 1 element).
 *      Updated localVariableDeclaration to allow annotations.  Interestingly the JLS
 *          doesn't appear to indicate this is legal, but it does work as of at least
 *          JDK 1.5.0_06.
 *      Moved the Identifier portion of annotationTypeElementRest to annotationMethodRest.
 *          Because annotationConstantRest already references variableDeclarator which 
 *          has the Identifier portion in it, the parser would fail on constants in 
 *          annotation definitions because it expected two identifiers.  
 *      Added optional trailing ';' to the alternatives in annotationTypeElementRest.
 *          Wouldn't handle an inner interface that has a trailing ';'.
 *      Swapped the expression and type rule reference order in castExpression to 
 *          make it check for genericized casts first.  It was failing to recognize a
 *          statement like  "Class<Byte> TYPE = (Class<Byte>)...;" because it was seeing
 *          'Class<Byte' in the cast expression as a less than expression, then failing 
 *          on the '>'.
 *      Changed createdName to use typeArguments instead of nonWildcardTypeArguments.
 *      Changed the 'this' alternative in primary to allow 'identifierSuffix' rather than
 *          just 'arguments'.  The case it couldn't handle was a call to an explicit
 *          generic method invocation (e.g. this.<E>doSomething()).  Using identifierSuffix
 *          may be overly aggressive--perhaps should create a more constrained thisSuffix rule?
 *      
 *  Version 1.0.4 -- Hiroaki Nakamura, May 3, 2007
 *
 *  Fixed formalParameterDecls, localVariableDeclaration, forInit,
 *  and forVarControl to use variableModifier* not 'final'? (annotation)?
 *
 *  Version 1.0.5 -- Terence, June 21, 2007
 *  --a[i].foo didn't work. Fixed unaryExpression
 *
 *  Version 1.0.6 -- John Ridgway, March 17, 2008
 *      Made "assert" a switchable keyword like "enum".
 *      Fixed compilationUnit to disallow "annotation importDeclaration ...".
 *      Changed "Identifier ('.' Identifier)*" to "qualifiedName" in more 
 *          places.
 *      Changed modifier* and/or variableModifier* to classOrInterfaceModifiers,
 *          modifiers or variableModifiers, as appropriate.
 *      Renamed "bound" to "typeBound" to better match language in the JLS.
 *      Added "memberDeclaration" which rewrites to methodDeclaration or 
 *      fieldDeclaration and pulled type into memberDeclaration.  So we parse 
 *          type and then move on to decide whether we're dealing with a field
 *          or a method.
 *      Modified "constructorDeclaration" to use "constructorBody" instead of
 *          "methodBody".  constructorBody starts with explicitConstructorInvocation,
 *          then goes on to blockStatement*.  Pulling explicitConstructorInvocation
 *          out of expressions allowed me to simplify "primary".
 *      Changed variableDeclarator to simplify it.
 *      Changed type to use classOrInterfaceType, thus simplifying it; of course
 *          I then had to add classOrInterfaceType, but it is used in several 
 *          places.
 *      Fixed annotations, old version allowed "@X(y,z)", which is illegal.
 *      Added optional comma to end of "elementValueArrayInitializer"; as per JLS.
 *      Changed annotationTypeElementRest to use normalClassDeclaration and 
 *          normalInterfaceDeclaration rather than classDeclaration and 
 *          interfaceDeclaration, thus getting rid of a couple of grammar ambiguities.
 *      Split localVariableDeclaration into localVariableDeclarationStatement
 *          (includes the terminating semi-colon) and localVariableDeclaration.  
 *          This allowed me to use localVariableDeclaration in "forInit" clauses,
 *           simplifying them.
 *      Changed switchBlockStatementGroup to use multiple labels.  This adds an
 *          ambiguity, but if one uses appropriately greedy parsing it yields the
 *           parse that is closest to the meaning of the switch statement.
 *      Renamed "forVarControl" to "enhancedForControl" -- JLS language.
 *      Added semantic predicates to test for shift operations rather than other
 *          things.  Thus, for instance, the string "< <" will never be treated
 *          as a left-shift operator.
 *      In "creator" we rule out "nonWildcardTypeArguments" on arrayCreation, 
 *          which are illegal.
 *      Moved "nonWildcardTypeArguments into innerCreator.
 *      Removed 'super' superSuffix from explicitGenericInvocation, since that
 *          is only used in explicitConstructorInvocation at the beginning of a
 *           constructorBody.  (This is part of the simplification of expressions
 *           mentioned earlier.)
 *      Simplified primary (got rid of those things that are only used in
 *          explicitConstructorInvocation).
 *      Lexer -- removed "Exponent?" from FloatingPointLiteral choice 4, since it
 *          led to an ambiguity.
 *
 *      This grammar successfully parses every .java file in the JDK 1.5 source 
 *          tree (excluding those whose file names include '-', which are not
 *          valid Java compilation units).
 *
 *  June 26, 2008
 *
 *	conditionalExpression had wrong precedence x?y:z.
 *
 *  Known remaining problems:
 *      "Letter" and "JavaIDDigit" are wrong.  The actual specification of
 *      "Letter" should be "a character for which the method
 *      Character.isJavaIdentifierStart(int) returns true."  A "Java 
 *      letter-or-digit is a character for which the method 
 *      Character.isJavaIdentifierPart(int) returns true."
 */
public class JavaParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "Identifier", "ENUM", "FloatingPointLiteral", "CharacterLiteral", "StringLiteral", "HexLiteral", "OctalLiteral", "DecimalLiteral", "ASSERT", "HexDigit", "IntegerTypeSuffix", "Exponent", "FloatTypeSuffix", "EscapeSequence", "UnicodeEscape", "OctalEscape", "Letter", "JavaIDDigit", "WS", "COMMENT", "LINE_COMMENT", "'package'", "';'", "'import'", "'static'", "'.'", "'*'", "'public'", "'protected'", "'private'", "'abstract'", "'final'", "'strictfp'", "'class'", "'extends'", "'implements'", "'<'", "','", "'>'", "'&'", "'{'", "'}'", "'interface'", "'void'", "'['", "']'", "'throws'", "'='", "'native'", "'synchronized'", "'transient'", "'volatile'", "'boolean'", "'char'", "'byte'", "'short'", "'int'", "'long'", "'float'", "'double'", "'?'", "'super'", "'('", "')'", "'...'", "'this'", "'null'", "'true'", "'false'", "'@'", "'default'", "':'", "'if'", "'else'", "'for'", "'while'", "'do'", "'try'", "'finally'", "'switch'", "'return'", "'throw'", "'break'", "'continue'", "'catch'", "'case'", "'+='", "'-='", "'*='", "'/='", "'&='", "'|='", "'^='", "'%='", "'||'", "'&&'", "'|'", "'^'", "'=='", "'!='", "'instanceof'", "'+'", "'-'", "'/'", "'%'", "'++'", "'--'", "'~'", "'!'", "'new'"
    };
    public static final int T__42=42;
    public static final int HexDigit=13;
    public static final int T__47=47;
    public static final int T__109=109;
    public static final int T__73=73;
    public static final int T__72=72;
    public static final int T__70=70;
    public static final int T__39=39;
    public static final int Letter=20;
    public static final int T__30=30;
    public static final int T__46=46;
    public static final int T__96=96;
    public static final int T__49=49;
    public static final int ASSERT=12;
    public static final int T__108=108;
    public static final int T__112=112;
    public static final int T__54=54;
    public static final int T__48=48;
    public static final int FloatTypeSuffix=16;
    public static final int T__113=113;
    public static final int IntegerTypeSuffix=14;
    public static final int Identifier=4;
    public static final int T__89=89;
    public static final int WS=22;
    public static final int T__79=79;
    public static final int T__64=64;
    public static final int T__44=44;
    public static final int T__66=66;
    public static final int T__92=92;
    public static final int T__88=88;
    public static final int LINE_COMMENT=24;
    public static final int T__90=90;
    public static final int UnicodeEscape=18;
    public static final int HexLiteral=9;
    public static final int T__63=63;
    public static final int T__110=110;
    public static final int T__43=43;
    public static final int T__91=91;
    public static final int T__40=40;
    public static final int DecimalLiteral=11;
    public static final int T__85=85;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__60=60;
    public static final int T__41=41;
    public static final int T__93=93;
    public static final int T__86=86;
    public static final int T__28=28;
    public static final int OctalLiteral=10;
    public static final int T__57=57;
    public static final int T__94=94;
    public static final int T__51=51;
    public static final int T__80=80;
    public static final int T__100=100;
    public static final int T__69=69;
    public static final int T__95=95;
    public static final int T__50=50;
    public static final int T__65=65;
    public static final int T__101=101;
    public static final int T__104=104;
    public static final int T__107=107;
    public static final int T__67=67;
    public static final int T__87=87;
    public static final int T__106=106;
    public static final int T__74=74;
    public static final int T__52=52;
    public static final int T__68=68;
    public static final int T__62=62;
    public static final int EscapeSequence=17;
    public static final int T__27=27;
    public static final int T__61=61;
    public static final int T__59=59;
    public static final int T__34=34;
    public static final int FloatingPointLiteral=6;
    public static final int T__98=98;
    public static final int T__56=56;
    public static final int ENUM=5;
    public static final int T__35=35;
    public static final int Exponent=15;
    public static final int T__78=78;
    public static final int T__36=36;
    public static final int CharacterLiteral=7;
    public static final int T__58=58;
    public static final int COMMENT=23;
    public static final int T__99=99;
    public static final int StringLiteral=8;
    public static final int T__33=33;
    public static final int T__77=77;
    public static final int T__55=55;
    public static final int T__45=45;
    public static final int T__29=29;
    public static final int T__103=103;
    public static final int JavaIDDigit=21;
    public static final int T__84=84;
    public static final int T__97=97;
    public static final int T__75=75;
    public static final int T__105=105;
    public static final int T__111=111;
    public static final int T__31=31;
    public static final int EOF=-1;
    public static final int T__53=53;
    public static final int T__32=32;
    public static final int T__38=38;
    public static final int T__37=37;
    public static final int T__76=76;
    public static final int T__82=82;
    public static final int OctalEscape=19;
    public static final int T__81=81;
    public static final int T__83=83;
    public static final int T__71=71;
    public static final int T__102=102;
	private String fileName;
	private String fileNameAbsPath;

    // delegates
    // delegators


        public JavaParser(TokenStream input, String fileNameAbsPath) {
            this(input, new RecognizerSharedState());
			this.fileNameAbsPath = fileNameAbsPath;
        }
        
        public JavaParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        
        public JavaParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
            this.state.ruleMemo = new HashMap[407+1];
        }
        

    public String[] getTokenNames() { return JavaParser.tokenNames; }
    public String getGrammarFileName() { return "Java.g"; }



    // $ANTLR start "compilationUnit"
    // Java.g:179:1: compilationUnit : ( annotations ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* ) | ( packageDeclaration )? ( importDeclaration )* ( typeDeclaration )* );
    public final void compilationUnit() throws RecognitionException {
        int compilationUnit_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 1) ) { return ; }
            // Java.g:180:5: ( annotations ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* ) | ( packageDeclaration )? ( importDeclaration )* ( typeDeclaration )* )
            int alt8=2;
            alt8 = dfa8.predict(input);
            switch (alt8) {
                case 1 :
                    // Java.g:180:9: annotations ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* )
                    {
                    pushFollow(FOLLOW_annotations_in_compilationUnit44);
                    annotations();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:181:9: ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* )
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0==25) ) {
                        alt4=1;
                    }
                    else if ( (LA4_0==ENUM||LA4_0==28||(LA4_0>=31 && LA4_0<=37)||LA4_0==46||LA4_0==73) ) {
                        alt4=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 4, 0, input);

                        throw nvae;
                    }
                    switch (alt4) {
                        case 1 :
                            // Java.g:181:13: packageDeclaration ( importDeclaration )* ( typeDeclaration )*
                            {
                            pushFollow(FOLLOW_packageDeclaration_in_compilationUnit58);
                            packageDeclaration();

                            state._fsp--;
                            if (state.failed) return ;
                            // Java.g:181:32: ( importDeclaration )*
                            loop1:
                            do {
                                int alt1=2;
                                int LA1_0 = input.LA(1);

                                if ( (LA1_0==27) ) {
                                    alt1=1;
                                }


                                switch (alt1) {
                            	case 1 :
                            	    // Java.g:0:0: importDeclaration
                            	    {
                            	    pushFollow(FOLLOW_importDeclaration_in_compilationUnit60);
                            	    importDeclaration();

                            	    state._fsp--;
                            	    if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop1;
                                }
                            } while (true);

                            // Java.g:181:51: ( typeDeclaration )*
                            loop2:
                            do {
                                int alt2=2;
                                int LA2_0 = input.LA(1);

                                if ( (LA2_0==ENUM||LA2_0==26||LA2_0==28||(LA2_0>=31 && LA2_0<=37)||LA2_0==46||LA2_0==73) ) {
                                    alt2=1;
                                }


                                switch (alt2) {
                            	case 1 :
                            	    // Java.g:0:0: typeDeclaration
                            	    {
                            	    pushFollow(FOLLOW_typeDeclaration_in_compilationUnit63);
                            	    typeDeclaration();

                            	    state._fsp--;
                            	    if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop2;
                                }
                            } while (true);


                            }
                            break;
                        case 2 :
                            // Java.g:182:13: classOrInterfaceDeclaration ( typeDeclaration )*
                            {
                            pushFollow(FOLLOW_classOrInterfaceDeclaration_in_compilationUnit78);
                            classOrInterfaceDeclaration();

                            state._fsp--;
                            if (state.failed) return ;
                            // Java.g:182:41: ( typeDeclaration )*
                            loop3:
                            do {
                                int alt3=2;
                                int LA3_0 = input.LA(1);

                                if ( (LA3_0==ENUM||LA3_0==26||LA3_0==28||(LA3_0>=31 && LA3_0<=37)||LA3_0==46||LA3_0==73) ) {
                                    alt3=1;
                                }


                                switch (alt3) {
                            	case 1 :
                            	    // Java.g:0:0: typeDeclaration
                            	    {
                            	    pushFollow(FOLLOW_typeDeclaration_in_compilationUnit80);
                            	    typeDeclaration();

                            	    state._fsp--;
                            	    if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop3;
                                }
                            } while (true);


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // Java.g:184:9: ( packageDeclaration )? ( importDeclaration )* ( typeDeclaration )*
                    {
                    // Java.g:184:9: ( packageDeclaration )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);

                    if ( (LA5_0==25) ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // Java.g:0:0: packageDeclaration
                            {
                            pushFollow(FOLLOW_packageDeclaration_in_compilationUnit101);
                            packageDeclaration();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }

                    // Java.g:184:29: ( importDeclaration )*
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==27) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // Java.g:0:0: importDeclaration
                    	    {
                    	    pushFollow(FOLLOW_importDeclaration_in_compilationUnit104);
                    	    importDeclaration();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop6;
                        }
                    } while (true);

                    // Java.g:184:48: ( typeDeclaration )*
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( (LA7_0==ENUM||LA7_0==26||LA7_0==28||(LA7_0>=31 && LA7_0<=37)||LA7_0==46||LA7_0==73) ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // Java.g:0:0: typeDeclaration
                    	    {
                    	    pushFollow(FOLLOW_typeDeclaration_in_compilationUnit107);
                    	    typeDeclaration();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop7;
                        }
                    } while (true);


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 1, compilationUnit_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "compilationUnit"


    // $ANTLR start "packageDeclaration"
    // Java.g:187:1: packageDeclaration : 'package' qualifiedName ';' ;
    public final void packageDeclaration() throws RecognitionException {
        int packageDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 2) ) { return ; }
            // Java.g:188:5: ( 'package' qualifiedName ';' )
            // Java.g:188:9: 'package' qualifiedName ';'
            {
            match(input,25,FOLLOW_25_in_packageDeclaration127); if (state.failed) return ;
            pushFollow(FOLLOW_qualifiedName_in_packageDeclaration129);
            qualifiedName();

            state._fsp--;
            if (state.failed) return ;
            match(input,26,FOLLOW_26_in_packageDeclaration131); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 2, packageDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "packageDeclaration"


    // $ANTLR start "importDeclaration"
    // Java.g:191:1: importDeclaration : 'import' ( 'static' )? qualifiedName ( '.' '*' )? ';' ;
    public final void importDeclaration() throws RecognitionException {
        int importDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 3) ) { return ; }
            // Java.g:192:5: ( 'import' ( 'static' )? qualifiedName ( '.' '*' )? ';' )
            // Java.g:192:9: 'import' ( 'static' )? qualifiedName ( '.' '*' )? ';'
            {
            match(input,27,FOLLOW_27_in_importDeclaration154); if (state.failed) return ;
            // Java.g:192:18: ( 'static' )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==28) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // Java.g:0:0: 'static'
                    {
                    match(input,28,FOLLOW_28_in_importDeclaration156); if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_qualifiedName_in_importDeclaration159);
            qualifiedName();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:192:42: ( '.' '*' )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==29) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // Java.g:192:43: '.' '*'
                    {
                    match(input,29,FOLLOW_29_in_importDeclaration162); if (state.failed) return ;
                    match(input,30,FOLLOW_30_in_importDeclaration164); if (state.failed) return ;

                    }
                    break;

            }

            match(input,26,FOLLOW_26_in_importDeclaration168); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 3, importDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "importDeclaration"


    // $ANTLR start "typeDeclaration"
    // Java.g:195:1: typeDeclaration : ( classOrInterfaceDeclaration | ';' );
    public final void typeDeclaration() throws RecognitionException {
        int typeDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 4) ) { return ; }
            // Java.g:196:5: ( classOrInterfaceDeclaration | ';' )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==ENUM||LA11_0==28||(LA11_0>=31 && LA11_0<=37)||LA11_0==46||LA11_0==73) ) {
                alt11=1;
            }
            else if ( (LA11_0==26) ) {
                alt11=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // Java.g:196:9: classOrInterfaceDeclaration
                    {
                    pushFollow(FOLLOW_classOrInterfaceDeclaration_in_typeDeclaration191);
                    classOrInterfaceDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:197:9: ';'
                    {
                    match(input,26,FOLLOW_26_in_typeDeclaration201); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 4, typeDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "typeDeclaration"


    // $ANTLR start "classOrInterfaceDeclaration"
    // Java.g:200:1: classOrInterfaceDeclaration : classOrInterfaceModifiers ( classDeclaration | interfaceDeclaration ) ;
    public final void classOrInterfaceDeclaration() throws RecognitionException {
        int classOrInterfaceDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 5) ) { return ; }
            // Java.g:201:5: ( classOrInterfaceModifiers ( classDeclaration | interfaceDeclaration ) )
            // Java.g:201:9: classOrInterfaceModifiers ( classDeclaration | interfaceDeclaration )
            {
            pushFollow(FOLLOW_classOrInterfaceModifiers_in_classOrInterfaceDeclaration224);
            classOrInterfaceModifiers();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:201:35: ( classDeclaration | interfaceDeclaration )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==ENUM||LA12_0==37) ) {
                alt12=1;
            }
            else if ( (LA12_0==46||LA12_0==73) ) {
                alt12=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // Java.g:201:36: classDeclaration
                    {
                    pushFollow(FOLLOW_classDeclaration_in_classOrInterfaceDeclaration227);
                    classDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:201:55: interfaceDeclaration
                    {
                    pushFollow(FOLLOW_interfaceDeclaration_in_classOrInterfaceDeclaration231);
                    interfaceDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 5, classOrInterfaceDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "classOrInterfaceDeclaration"


    // $ANTLR start "classOrInterfaceModifiers"
    // Java.g:204:1: classOrInterfaceModifiers : ( classOrInterfaceModifier )* ;
    public final void classOrInterfaceModifiers() throws RecognitionException {
        int classOrInterfaceModifiers_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 6) ) { return ; }
            // Java.g:205:5: ( ( classOrInterfaceModifier )* )
            // Java.g:205:9: ( classOrInterfaceModifier )*
            {
            // Java.g:205:9: ( classOrInterfaceModifier )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==73) ) {
                    int LA13_2 = input.LA(2);

                    if ( (LA13_2==Identifier) ) {
                        alt13=1;
                    }


                }
                else if ( (LA13_0==28||(LA13_0>=31 && LA13_0<=36)) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // Java.g:0:0: classOrInterfaceModifier
            	    {
            	    pushFollow(FOLLOW_classOrInterfaceModifier_in_classOrInterfaceModifiers255);
            	    classOrInterfaceModifier();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 6, classOrInterfaceModifiers_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "classOrInterfaceModifiers"


    // $ANTLR start "classOrInterfaceModifier"
    // Java.g:208:1: classOrInterfaceModifier : ( annotation | 'public' | 'protected' | 'private' | 'abstract' | 'static' | 'final' | 'strictfp' );
    public final void classOrInterfaceModifier() throws RecognitionException {
        int classOrInterfaceModifier_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 7) ) { return ; }
            // Java.g:209:5: ( annotation | 'public' | 'protected' | 'private' | 'abstract' | 'static' | 'final' | 'strictfp' )
            int alt14=8;
            switch ( input.LA(1) ) {
            case 73:
                {
                alt14=1;
                }
                break;
            case 31:
                {
                alt14=2;
                }
                break;
            case 32:
                {
                alt14=3;
                }
                break;
            case 33:
                {
                alt14=4;
                }
                break;
            case 34:
                {
                alt14=5;
                }
                break;
            case 28:
                {
                alt14=6;
                }
                break;
            case 35:
                {
                alt14=7;
                }
                break;
            case 36:
                {
                alt14=8;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;
            }

            switch (alt14) {
                case 1 :
                    // Java.g:209:9: annotation
                    {
                    pushFollow(FOLLOW_annotation_in_classOrInterfaceModifier275);
                    annotation();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:210:9: 'public'
                    {
                    match(input,31,FOLLOW_31_in_classOrInterfaceModifier288); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // Java.g:211:9: 'protected'
                    {
                    match(input,32,FOLLOW_32_in_classOrInterfaceModifier303); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // Java.g:212:9: 'private'
                    {
                    match(input,33,FOLLOW_33_in_classOrInterfaceModifier315); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // Java.g:213:9: 'abstract'
                    {
                    match(input,34,FOLLOW_34_in_classOrInterfaceModifier329); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // Java.g:214:9: 'static'
                    {
                    match(input,28,FOLLOW_28_in_classOrInterfaceModifier342); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    // Java.g:215:9: 'final'
                    {
                    match(input,35,FOLLOW_35_in_classOrInterfaceModifier357); if (state.failed) return ;

                    }
                    break;
                case 8 :
                    // Java.g:216:9: 'strictfp'
                    {
                    match(input,36,FOLLOW_36_in_classOrInterfaceModifier373); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 7, classOrInterfaceModifier_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "classOrInterfaceModifier"


    // $ANTLR start "modifiers"
    // Java.g:219:1: modifiers : ( modifier )* ;
    public final void modifiers() throws RecognitionException {
        int modifiers_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 8) ) { return ; }
            // Java.g:220:5: ( ( modifier )* )
            // Java.g:220:9: ( modifier )*
            {
            // Java.g:220:9: ( modifier )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==73) ) {
                    int LA15_2 = input.LA(2);

                    if ( (LA15_2==Identifier) ) {
                        alt15=1;
                    }


                }
                else if ( (LA15_0==28||(LA15_0>=31 && LA15_0<=36)||(LA15_0>=52 && LA15_0<=55)) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // Java.g:0:0: modifier
            	    {
            	    pushFollow(FOLLOW_modifier_in_modifiers395);
            	    modifier();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 8, modifiers_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "modifiers"


    // $ANTLR start "classDeclaration"
    // Java.g:223:1: classDeclaration : ( normalClassDeclaration | enumDeclaration );
    public final void classDeclaration() throws RecognitionException {
        int classDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 9) ) { return ; }
            // Java.g:224:5: ( normalClassDeclaration | enumDeclaration )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==37) ) {
                alt16=1;
            }
            else if ( (LA16_0==ENUM) ) {
                alt16=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // Java.g:224:9: normalClassDeclaration
                    {
                    pushFollow(FOLLOW_normalClassDeclaration_in_classDeclaration415);
                    normalClassDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:225:9: enumDeclaration
                    {
                    pushFollow(FOLLOW_enumDeclaration_in_classDeclaration425);
                    enumDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 9, classDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "classDeclaration"


    // $ANTLR start "normalClassDeclaration"
    // Java.g:228:1: normalClassDeclaration : 'class' Identifier ( typeParameters )? ( 'extends' type )? ( 'implements' typeList )? classBody ;
    public final void normalClassDeclaration() throws RecognitionException {
        int normalClassDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 10) ) { return ; }
            // Java.g:229:5: ( 'class' Identifier ( typeParameters )? ( 'extends' type )? ( 'implements' typeList )? classBody )
            // Java.g:229:9: 'class' Identifier ( typeParameters )? ( 'extends' type )? ( 'implements' typeList )? classBody
            {
            match(input,37,FOLLOW_37_in_normalClassDeclaration448); if (state.failed) return ;
            match(input,Identifier,FOLLOW_Identifier_in_normalClassDeclaration450); if (state.failed) return ;
            // Java.g:229:28: ( typeParameters )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==40) ) {
                alt17=1;
            }
            switch (alt17) {
                case 1 :
                    // Java.g:0:0: typeParameters
                    {
                    pushFollow(FOLLOW_typeParameters_in_normalClassDeclaration452);
                    typeParameters();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // Java.g:230:9: ( 'extends' type )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==38) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // Java.g:230:10: 'extends' type
                    {
                    match(input,38,FOLLOW_38_in_normalClassDeclaration464); if (state.failed) return ;
                    pushFollow(FOLLOW_type_in_normalClassDeclaration466);
                    type();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // Java.g:231:9: ( 'implements' typeList )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==39) ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // Java.g:231:10: 'implements' typeList
                    {
                    match(input,39,FOLLOW_39_in_normalClassDeclaration479); if (state.failed) return ;
                    pushFollow(FOLLOW_typeList_in_normalClassDeclaration481);
                    typeList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_classBody_in_normalClassDeclaration493);
            classBody();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 10, normalClassDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "normalClassDeclaration"


    // $ANTLR start "typeParameters"
    // Java.g:235:1: typeParameters : '<' typeParameter ( ',' typeParameter )* '>' ;
    public final void typeParameters() throws RecognitionException {
        int typeParameters_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 11) ) { return ; }
            // Java.g:236:5: ( '<' typeParameter ( ',' typeParameter )* '>' )
            // Java.g:236:9: '<' typeParameter ( ',' typeParameter )* '>'
            {
            match(input,40,FOLLOW_40_in_typeParameters516); if (state.failed) return ;
            pushFollow(FOLLOW_typeParameter_in_typeParameters518);
            typeParameter();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:236:27: ( ',' typeParameter )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==41) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // Java.g:236:28: ',' typeParameter
            	    {
            	    match(input,41,FOLLOW_41_in_typeParameters521); if (state.failed) return ;
            	    pushFollow(FOLLOW_typeParameter_in_typeParameters523);
            	    typeParameter();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);

            match(input,42,FOLLOW_42_in_typeParameters527); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 11, typeParameters_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "typeParameters"


    // $ANTLR start "typeParameter"
    // Java.g:239:1: typeParameter : Identifier ( 'extends' typeBound )? ;
    public final void typeParameter() throws RecognitionException {
        int typeParameter_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 12) ) { return ; }
            // Java.g:240:5: ( Identifier ( 'extends' typeBound )? )
            // Java.g:240:9: Identifier ( 'extends' typeBound )?
            {
            match(input,Identifier,FOLLOW_Identifier_in_typeParameter546); if (state.failed) return ;
            // Java.g:240:20: ( 'extends' typeBound )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==38) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // Java.g:240:21: 'extends' typeBound
                    {
                    match(input,38,FOLLOW_38_in_typeParameter549); if (state.failed) return ;
                    pushFollow(FOLLOW_typeBound_in_typeParameter551);
                    typeBound();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 12, typeParameter_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "typeParameter"


    // $ANTLR start "typeBound"
    // Java.g:243:1: typeBound : type ( '&' type )* ;
    public final void typeBound() throws RecognitionException {
        int typeBound_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 13) ) { return ; }
            // Java.g:244:5: ( type ( '&' type )* )
            // Java.g:244:9: type ( '&' type )*
            {
            pushFollow(FOLLOW_type_in_typeBound580);
            type();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:244:14: ( '&' type )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( (LA22_0==43) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // Java.g:244:15: '&' type
            	    {
            	    match(input,43,FOLLOW_43_in_typeBound583); if (state.failed) return ;
            	    pushFollow(FOLLOW_type_in_typeBound585);
            	    type();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 13, typeBound_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "typeBound"


    // $ANTLR start "enumDeclaration"
    // Java.g:247:1: enumDeclaration : ENUM Identifier ( 'implements' typeList )? enumBody ;
    public final void enumDeclaration() throws RecognitionException {
        int enumDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 14) ) { return ; }
            // Java.g:248:5: ( ENUM Identifier ( 'implements' typeList )? enumBody )
            // Java.g:248:9: ENUM Identifier ( 'implements' typeList )? enumBody
            {
            match(input,ENUM,FOLLOW_ENUM_in_enumDeclaration606); if (state.failed) return ;
            match(input,Identifier,FOLLOW_Identifier_in_enumDeclaration608); if (state.failed) return ;
            // Java.g:248:25: ( 'implements' typeList )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==39) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // Java.g:248:26: 'implements' typeList
                    {
                    match(input,39,FOLLOW_39_in_enumDeclaration611); if (state.failed) return ;
                    pushFollow(FOLLOW_typeList_in_enumDeclaration613);
                    typeList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_enumBody_in_enumDeclaration617);
            enumBody();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 14, enumDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "enumDeclaration"


    // $ANTLR start "enumBody"
    // Java.g:251:1: enumBody : '{' ( enumConstants )? ( ',' )? ( enumBodyDeclarations )? '}' ;
    public final void enumBody() throws RecognitionException {
        int enumBody_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 15) ) { return ; }
            // Java.g:252:5: ( '{' ( enumConstants )? ( ',' )? ( enumBodyDeclarations )? '}' )
            // Java.g:252:9: '{' ( enumConstants )? ( ',' )? ( enumBodyDeclarations )? '}'
            {
            match(input,44,FOLLOW_44_in_enumBody636); if (state.failed) return ;
            // Java.g:252:13: ( enumConstants )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==Identifier||LA24_0==73) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // Java.g:0:0: enumConstants
                    {
                    pushFollow(FOLLOW_enumConstants_in_enumBody638);
                    enumConstants();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // Java.g:252:28: ( ',' )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==41) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // Java.g:0:0: ','
                    {
                    match(input,41,FOLLOW_41_in_enumBody641); if (state.failed) return ;

                    }
                    break;

            }

            // Java.g:252:33: ( enumBodyDeclarations )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==26) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // Java.g:0:0: enumBodyDeclarations
                    {
                    pushFollow(FOLLOW_enumBodyDeclarations_in_enumBody644);
                    enumBodyDeclarations();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            match(input,45,FOLLOW_45_in_enumBody647); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 15, enumBody_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "enumBody"


    // $ANTLR start "enumConstants"
    // Java.g:255:1: enumConstants : enumConstant ( ',' enumConstant )* ;
    public final void enumConstants() throws RecognitionException {
        int enumConstants_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 16) ) { return ; }
            // Java.g:256:5: ( enumConstant ( ',' enumConstant )* )
            // Java.g:256:9: enumConstant ( ',' enumConstant )*
            {
            pushFollow(FOLLOW_enumConstant_in_enumConstants666);
            enumConstant();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:256:22: ( ',' enumConstant )*
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==41) ) {
                    int LA27_1 = input.LA(2);

                    if ( (LA27_1==Identifier||LA27_1==73) ) {
                        alt27=1;
                    }


                }


                switch (alt27) {
            	case 1 :
            	    // Java.g:256:23: ',' enumConstant
            	    {
            	    match(input,41,FOLLOW_41_in_enumConstants669); if (state.failed) return ;
            	    pushFollow(FOLLOW_enumConstant_in_enumConstants671);
            	    enumConstant();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop27;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 16, enumConstants_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "enumConstants"


    // $ANTLR start "enumConstant"
    // Java.g:259:1: enumConstant : ( annotations )? Identifier ( arguments )? ( classBody )? ;
    public final void enumConstant() throws RecognitionException {
        int enumConstant_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 17) ) { return ; }
            // Java.g:260:5: ( ( annotations )? Identifier ( arguments )? ( classBody )? )
            // Java.g:260:9: ( annotations )? Identifier ( arguments )? ( classBody )?
            {
            // Java.g:260:9: ( annotations )?
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==73) ) {
                alt28=1;
            }
            switch (alt28) {
                case 1 :
                    // Java.g:0:0: annotations
                    {
                    pushFollow(FOLLOW_annotations_in_enumConstant696);
                    annotations();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            match(input,Identifier,FOLLOW_Identifier_in_enumConstant699); if (state.failed) return ;
            // Java.g:260:33: ( arguments )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==66) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // Java.g:0:0: arguments
                    {
                    pushFollow(FOLLOW_arguments_in_enumConstant701);
                    arguments();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // Java.g:260:44: ( classBody )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==44) ) {
                alt30=1;
            }
            switch (alt30) {
                case 1 :
                    // Java.g:0:0: classBody
                    {
                    pushFollow(FOLLOW_classBody_in_enumConstant704);
                    classBody();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 17, enumConstant_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "enumConstant"


    // $ANTLR start "enumBodyDeclarations"
    // Java.g:263:1: enumBodyDeclarations : ';' ( classBodyDeclaration )* ;
    public final void enumBodyDeclarations() throws RecognitionException {
        int enumBodyDeclarations_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 18) ) { return ; }
            // Java.g:264:5: ( ';' ( classBodyDeclaration )* )
            // Java.g:264:9: ';' ( classBodyDeclaration )*
            {
            match(input,26,FOLLOW_26_in_enumBodyDeclarations728); if (state.failed) return ;
            // Java.g:264:13: ( classBodyDeclaration )*
            loop31:
            do {
                int alt31=2;
                int LA31_0 = input.LA(1);

                if ( ((LA31_0>=Identifier && LA31_0<=ENUM)||LA31_0==26||LA31_0==28||(LA31_0>=31 && LA31_0<=37)||LA31_0==40||LA31_0==44||(LA31_0>=46 && LA31_0<=47)||(LA31_0>=52 && LA31_0<=63)||LA31_0==73) ) {
                    alt31=1;
                }


                switch (alt31) {
            	case 1 :
            	    // Java.g:264:14: classBodyDeclaration
            	    {
            	    pushFollow(FOLLOW_classBodyDeclaration_in_enumBodyDeclarations731);
            	    classBodyDeclaration();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 18, enumBodyDeclarations_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "enumBodyDeclarations"


    // $ANTLR start "interfaceDeclaration"
    // Java.g:267:1: interfaceDeclaration : ( normalInterfaceDeclaration | annotationTypeDeclaration );
    public final void interfaceDeclaration() throws RecognitionException {
        int interfaceDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 19) ) { return ; }
            // Java.g:268:5: ( normalInterfaceDeclaration | annotationTypeDeclaration )
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==46) ) {
                alt32=1;
            }
            else if ( (LA32_0==73) ) {
                alt32=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }
            switch (alt32) {
                case 1 :
                    // Java.g:268:9: normalInterfaceDeclaration
                    {
                    pushFollow(FOLLOW_normalInterfaceDeclaration_in_interfaceDeclaration756);
                    normalInterfaceDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:269:9: annotationTypeDeclaration
                    {
                    pushFollow(FOLLOW_annotationTypeDeclaration_in_interfaceDeclaration766);
                    annotationTypeDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 19, interfaceDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "interfaceDeclaration"


    // $ANTLR start "normalInterfaceDeclaration"
    // Java.g:272:1: normalInterfaceDeclaration : 'interface' Identifier ( typeParameters )? ( 'extends' typeList )? interfaceBody ;
    public final void normalInterfaceDeclaration() throws RecognitionException {
        int normalInterfaceDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 20) ) { return ; }
            // Java.g:273:5: ( 'interface' Identifier ( typeParameters )? ( 'extends' typeList )? interfaceBody )
            // Java.g:273:9: 'interface' Identifier ( typeParameters )? ( 'extends' typeList )? interfaceBody
            {
            match(input,46,FOLLOW_46_in_normalInterfaceDeclaration789); if (state.failed) return ;
            match(input,Identifier,FOLLOW_Identifier_in_normalInterfaceDeclaration791); if (state.failed) return ;
            // Java.g:273:32: ( typeParameters )?
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==40) ) {
                alt33=1;
            }
            switch (alt33) {
                case 1 :
                    // Java.g:0:0: typeParameters
                    {
                    pushFollow(FOLLOW_typeParameters_in_normalInterfaceDeclaration793);
                    typeParameters();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // Java.g:273:48: ( 'extends' typeList )?
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==38) ) {
                alt34=1;
            }
            switch (alt34) {
                case 1 :
                    // Java.g:273:49: 'extends' typeList
                    {
                    match(input,38,FOLLOW_38_in_normalInterfaceDeclaration797); if (state.failed) return ;
                    pushFollow(FOLLOW_typeList_in_normalInterfaceDeclaration799);
                    typeList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_interfaceBody_in_normalInterfaceDeclaration803);
            interfaceBody();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 20, normalInterfaceDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "normalInterfaceDeclaration"


    // $ANTLR start "typeList"
    // Java.g:276:1: typeList : type ( ',' type )* ;
    public final void typeList() throws RecognitionException {
        int typeList_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 21) ) { return ; }
            // Java.g:277:5: ( type ( ',' type )* )
            // Java.g:277:9: type ( ',' type )*
            {
            pushFollow(FOLLOW_type_in_typeList826);
            type();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:277:14: ( ',' type )*
            loop35:
            do {
                int alt35=2;
                int LA35_0 = input.LA(1);

                if ( (LA35_0==41) ) {
                    alt35=1;
                }


                switch (alt35) {
            	case 1 :
            	    // Java.g:277:15: ',' type
            	    {
            	    match(input,41,FOLLOW_41_in_typeList829); if (state.failed) return ;
            	    pushFollow(FOLLOW_type_in_typeList831);
            	    type();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop35;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 21, typeList_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "typeList"


    // $ANTLR start "classBody"
    // Java.g:280:1: classBody : '{' ( classBodyDeclaration )* '}' ;
    public final void classBody() throws RecognitionException {
        int classBody_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 22) ) { return ; }
            // Java.g:281:5: ( '{' ( classBodyDeclaration )* '}' )
            // Java.g:281:9: '{' ( classBodyDeclaration )* '}'
            {
            match(input,44,FOLLOW_44_in_classBody856); if (state.failed) return ;
            // Java.g:281:13: ( classBodyDeclaration )*
            loop36:
            do {
                int alt36=2;
                int LA36_0 = input.LA(1);

                if ( ((LA36_0>=Identifier && LA36_0<=ENUM)||LA36_0==26||LA36_0==28||(LA36_0>=31 && LA36_0<=37)||LA36_0==40||LA36_0==44||(LA36_0>=46 && LA36_0<=47)||(LA36_0>=52 && LA36_0<=63)||LA36_0==73) ) {
                    alt36=1;
                }


                switch (alt36) {
            	case 1 :
            	    // Java.g:0:0: classBodyDeclaration
            	    {
            	    pushFollow(FOLLOW_classBodyDeclaration_in_classBody858);
            	    classBodyDeclaration();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop36;
                }
            } while (true);

            match(input,45,FOLLOW_45_in_classBody861); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 22, classBody_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "classBody"


    // $ANTLR start "interfaceBody"
    // Java.g:284:1: interfaceBody : '{' ( interfaceBodyDeclaration )* '}' ;
    public final void interfaceBody() throws RecognitionException {
        int interfaceBody_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 23) ) { return ; }
            // Java.g:285:5: ( '{' ( interfaceBodyDeclaration )* '}' )
            // Java.g:285:9: '{' ( interfaceBodyDeclaration )* '}'
            {
            match(input,44,FOLLOW_44_in_interfaceBody884); if (state.failed) return ;
            // Java.g:285:13: ( interfaceBodyDeclaration )*
            loop37:
            do {
                int alt37=2;
                int LA37_0 = input.LA(1);

                if ( ((LA37_0>=Identifier && LA37_0<=ENUM)||LA37_0==26||LA37_0==28||(LA37_0>=31 && LA37_0<=37)||LA37_0==40||(LA37_0>=46 && LA37_0<=47)||(LA37_0>=52 && LA37_0<=63)||LA37_0==73) ) {
                    alt37=1;
                }


                switch (alt37) {
            	case 1 :
            	    // Java.g:0:0: interfaceBodyDeclaration
            	    {
            	    pushFollow(FOLLOW_interfaceBodyDeclaration_in_interfaceBody886);
            	    interfaceBodyDeclaration();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop37;
                }
            } while (true);

            match(input,45,FOLLOW_45_in_interfaceBody889); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 23, interfaceBody_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "interfaceBody"


    // $ANTLR start "classBodyDeclaration"
    // Java.g:288:1: classBodyDeclaration : ( ';' | ( 'static' )? block | modifiers memberDecl );
    public final void classBodyDeclaration() throws RecognitionException {
        int classBodyDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 24) ) { return ; }
            // Java.g:289:5: ( ';' | ( 'static' )? block | modifiers memberDecl )
            int alt39=3;
            switch ( input.LA(1) ) {
            case 26:
                {
                alt39=1;
                }
                break;
            case 28:
                {
                int LA39_2 = input.LA(2);

                if ( ((LA39_2>=Identifier && LA39_2<=ENUM)||LA39_2==28||(LA39_2>=31 && LA39_2<=37)||LA39_2==40||(LA39_2>=46 && LA39_2<=47)||(LA39_2>=52 && LA39_2<=63)||LA39_2==73) ) {
                    alt39=3;
                }
                else if ( (LA39_2==44) ) {
                    alt39=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 39, 2, input);

                    throw nvae;
                }
                }
                break;
            case 44:
                {
                alt39=2;
                }
                break;
            case Identifier:
            case ENUM:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 40:
            case 46:
            case 47:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 73:
                {
                alt39=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }

            switch (alt39) {
                case 1 :
                    // Java.g:289:9: ';'
                    {
                    match(input,26,FOLLOW_26_in_classBodyDeclaration908); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:290:9: ( 'static' )? block
                    {
                    // Java.g:290:9: ( 'static' )?
                    int alt38=2;
                    int LA38_0 = input.LA(1);

                    if ( (LA38_0==28) ) {
                        alt38=1;
                    }
                    switch (alt38) {
                        case 1 :
                            // Java.g:0:0: 'static'
                            {
                            match(input,28,FOLLOW_28_in_classBodyDeclaration918); if (state.failed) return ;

                            }
                            break;

                    }

                    pushFollow(FOLLOW_block_in_classBodyDeclaration921);
                    block();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // Java.g:291:9: modifiers memberDecl
                    {
                    pushFollow(FOLLOW_modifiers_in_classBodyDeclaration931);
                    modifiers();

                    state._fsp--;
                    if (state.failed) return ;
                    pushFollow(FOLLOW_memberDecl_in_classBodyDeclaration933);
                    memberDecl();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 24, classBodyDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "classBodyDeclaration"


    // $ANTLR start "memberDecl"
    // Java.g:294:1: memberDecl : ( genericMethodOrConstructorDecl | memberDeclaration | 'void' Identifier voidMethodDeclaratorRest | Identifier constructorDeclaratorRest | interfaceDeclaration | classDeclaration );
    public final void memberDecl() throws RecognitionException {
        int memberDecl_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 25) ) { return ; }
            // Java.g:295:5: ( genericMethodOrConstructorDecl | memberDeclaration | 'void' Identifier voidMethodDeclaratorRest | Identifier constructorDeclaratorRest | interfaceDeclaration | classDeclaration )
            int alt40=6;
            switch ( input.LA(1) ) {
            case 40:
                {
                alt40=1;
                }
                break;
            case Identifier:
                {
                int LA40_2 = input.LA(2);

                if ( (LA40_2==Identifier||LA40_2==29||LA40_2==40||LA40_2==48) ) {
                    alt40=2;
                }
                else if ( (LA40_2==66) ) {
                    alt40=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 40, 2, input);

                    throw nvae;
                }
                }
                break;
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
                {
                alt40=2;
                }
                break;
            case 47:
                {
                alt40=3;
                }
                break;
            case 46:
            case 73:
                {
                alt40=5;
                }
                break;
            case ENUM:
            case 37:
                {
                alt40=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;
            }

            switch (alt40) {
                case 1 :
                    // Java.g:295:9: genericMethodOrConstructorDecl
                    {
                    MethodInfo info = new MethodInfo(this.fileNameAbsPath, input.get(input.index()).getText());
                    currentInfos.add(info);
                    
                    pushFollow(FOLLOW_genericMethodOrConstructorDecl_in_memberDecl956);
                    genericMethodOrConstructorDecl();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:296:9: memberDeclaration
                    {
                    pushFollow(FOLLOW_memberDeclaration_in_memberDecl966);
                    memberDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // Java.g:297:9: 'void' Identifier voidMethodDeclaratorRest
                    {
                        
                    match(input,47,FOLLOW_47_in_memberDecl976); if (state.failed) return ;
                    
                    MethodInfo info = new MethodInfo(this.fileNameAbsPath, input.get(input.index()).getText());
                    currentInfos.add(info);
                    
                    match(input,Identifier,FOLLOW_Identifier_in_memberDecl978); if (state.failed) return ;
                    pushFollow(FOLLOW_voidMethodDeclaratorRest_in_memberDecl980);
                    voidMethodDeclaratorRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // Java.g:298:9: Identifier constructorDeclaratorRest
                    {

                    
                    MethodInfo info = new MethodInfo(this.fileNameAbsPath, input.get(input.index()).getText());
                    currentInfos.add(info);
                    
                    match(input,Identifier,FOLLOW_Identifier_in_memberDecl990); if (state.failed) return ;
                    
                    pushFollow(FOLLOW_constructorDeclaratorRest_in_memberDecl992);
                    constructorDeclaratorRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // Java.g:299:9: interfaceDeclaration
                    {
                    pushFollow(FOLLOW_interfaceDeclaration_in_memberDecl1002);
                    interfaceDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // Java.g:300:9: classDeclaration
                    {
                    pushFollow(FOLLOW_classDeclaration_in_memberDecl1012);
                    classDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 25, memberDecl_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "memberDecl"


    // $ANTLR start "memberDeclaration"
    // Java.g:303:1: memberDeclaration : type ( methodDeclaration | fieldDeclaration ) ;
    public final void memberDeclaration() throws RecognitionException {
        int memberDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 26) ) { return ; }
            // Java.g:304:5: ( type ( methodDeclaration | fieldDeclaration ) )
            // Java.g:304:9: type ( methodDeclaration | fieldDeclaration )
            {
            pushFollow(FOLLOW_type_in_memberDeclaration1035);
            type();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:304:14: ( methodDeclaration | fieldDeclaration )
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==Identifier) ) {
                int LA41_1 = input.LA(2);

                if ( (LA41_1==66) ) {
                    alt41=1;
                }
                else if ( (LA41_1==26||LA41_1==41||LA41_1==48||LA41_1==51) ) {
                    alt41=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 41, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;
            }
            switch (alt41) {
                case 1 :
                    // Java.g:304:15: methodDeclaration
                    {
                    pushFollow(FOLLOW_methodDeclaration_in_memberDeclaration1038);
                    methodDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:304:35: fieldDeclaration
                    {
                    pushFollow(FOLLOW_fieldDeclaration_in_memberDeclaration1042);
                    fieldDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 26, memberDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "memberDeclaration"


    // $ANTLR start "genericMethodOrConstructorDecl"
    // Java.g:307:1: genericMethodOrConstructorDecl : typeParameters genericMethodOrConstructorRest ;
    public final void genericMethodOrConstructorDecl() throws RecognitionException {
        int genericMethodOrConstructorDecl_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 27) ) { return ; }
            // Java.g:308:5: ( typeParameters genericMethodOrConstructorRest )
            // Java.g:308:9: typeParameters genericMethodOrConstructorRest
            {
            pushFollow(FOLLOW_typeParameters_in_genericMethodOrConstructorDecl1062);
            typeParameters();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_genericMethodOrConstructorRest_in_genericMethodOrConstructorDecl1064);
            genericMethodOrConstructorRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 27, genericMethodOrConstructorDecl_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "genericMethodOrConstructorDecl"


    // $ANTLR start "genericMethodOrConstructorRest"
    // Java.g:311:1: genericMethodOrConstructorRest : ( ( type | 'void' ) Identifier methodDeclaratorRest | Identifier constructorDeclaratorRest );
    public final void genericMethodOrConstructorRest() throws RecognitionException {
        int genericMethodOrConstructorRest_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 28) ) { return ; }
            // Java.g:312:5: ( ( type | 'void' ) Identifier methodDeclaratorRest | Identifier constructorDeclaratorRest )
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==Identifier) ) {
                int LA43_1 = input.LA(2);

                if ( (LA43_1==66) ) {
                    alt43=2;
                }
                else if ( (LA43_1==Identifier||LA43_1==29||LA43_1==40||LA43_1==48) ) {
                    alt43=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 43, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA43_0==47||(LA43_0>=56 && LA43_0<=63)) ) {
                alt43=1;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;
            }
            switch (alt43) {
                case 1 :
                    // Java.g:312:9: ( type | 'void' ) Identifier methodDeclaratorRest
                    {
                    // Java.g:312:9: ( type | 'void' )
                    int alt42=2;
                    int LA42_0 = input.LA(1);

                    if ( (LA42_0==Identifier||(LA42_0>=56 && LA42_0<=63)) ) {
                        alt42=1;
                    }
                    else if ( (LA42_0==47) ) {
                        alt42=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 42, 0, input);

                        throw nvae;
                    }
                    switch (alt42) {
                        case 1 :
                            // Java.g:312:10: type
                            {
                            pushFollow(FOLLOW_type_in_genericMethodOrConstructorRest1088);
                            type();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // Java.g:312:17: 'void'
                            {
                            match(input,47,FOLLOW_47_in_genericMethodOrConstructorRest1092); if (state.failed) return ;

                            }
                            break;

                    }
                  
                    MethodInfo info = new MethodInfo(this.fileNameAbsPath, input.get(input.index()).getText());
                    currentInfos.add(info);
                    
                    match(input,Identifier,FOLLOW_Identifier_in_genericMethodOrConstructorRest1095); if (state.failed) return ;
                    pushFollow(FOLLOW_methodDeclaratorRest_in_genericMethodOrConstructorRest1097);
                    methodDeclaratorRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:313:9: Identifier constructorDeclaratorRest
                    {
                    match(input,Identifier,FOLLOW_Identifier_in_genericMethodOrConstructorRest1107); if (state.failed) return ;
                    pushFollow(FOLLOW_constructorDeclaratorRest_in_genericMethodOrConstructorRest1109);
                    constructorDeclaratorRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 28, genericMethodOrConstructorRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "genericMethodOrConstructorRest"


    // $ANTLR start "methodDeclaration"
    // Java.g:316:1: methodDeclaration : Identifier methodDeclaratorRest ;
    public final void methodDeclaration() throws RecognitionException {
        int methodDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 29) ) { return ; }
            // Java.g:317:5: ( Identifier methodDeclaratorRest )
            // Java.g:317:9: Identifier methodDeclaratorRest
            {
            MethodInfo info = new MethodInfo(this.fileNameAbsPath, input.get(input.index()).getText());
            currentInfos.add(info);
            match(input,Identifier,FOLLOW_Identifier_in_methodDeclaration1128); if (state.failed) return ;
            pushFollow(FOLLOW_methodDeclaratorRest_in_methodDeclaration1130);
            methodDeclaratorRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 29, methodDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "methodDeclaration"


    // $ANTLR start "fieldDeclaration"
    // Java.g:320:1: fieldDeclaration : variableDeclarators ';' ;
    public final void fieldDeclaration() throws RecognitionException {
        int fieldDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 30) ) { return ; }
            // Java.g:321:5: ( variableDeclarators ';' )
            // Java.g:321:9: variableDeclarators ';'
            {
            pushFollow(FOLLOW_variableDeclarators_in_fieldDeclaration1149);
            variableDeclarators();

            state._fsp--;
            if (state.failed) return ;
            match(input,26,FOLLOW_26_in_fieldDeclaration1151); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 30, fieldDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "fieldDeclaration"


    // $ANTLR start "interfaceBodyDeclaration"
    // Java.g:324:1: interfaceBodyDeclaration : ( modifiers interfaceMemberDecl | ';' );
    public final void interfaceBodyDeclaration() throws RecognitionException {
        int interfaceBodyDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 31) ) { return ; }
            // Java.g:325:5: ( modifiers interfaceMemberDecl | ';' )
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( ((LA44_0>=Identifier && LA44_0<=ENUM)||LA44_0==28||(LA44_0>=31 && LA44_0<=37)||LA44_0==40||(LA44_0>=46 && LA44_0<=47)||(LA44_0>=52 && LA44_0<=63)||LA44_0==73) ) {
                alt44=1;
            }
            else if ( (LA44_0==26) ) {
                alt44=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 44, 0, input);

                throw nvae;
            }
            switch (alt44) {
                case 1 :
                    // Java.g:325:9: modifiers interfaceMemberDecl
                    {
                    pushFollow(FOLLOW_modifiers_in_interfaceBodyDeclaration1178);
                    modifiers();

                    state._fsp--;
                    if (state.failed) return ;
                    pushFollow(FOLLOW_interfaceMemberDecl_in_interfaceBodyDeclaration1180);
                    interfaceMemberDecl();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:326:9: ';'
                    {
                    match(input,26,FOLLOW_26_in_interfaceBodyDeclaration1190); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 31, interfaceBodyDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "interfaceBodyDeclaration"


    // $ANTLR start "interfaceMemberDecl"
    // Java.g:329:1: interfaceMemberDecl : ( interfaceMethodOrFieldDecl | interfaceGenericMethodDecl | 'void' Identifier voidInterfaceMethodDeclaratorRest | interfaceDeclaration | classDeclaration );
    public final void interfaceMemberDecl() throws RecognitionException {
        int interfaceMemberDecl_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 32) ) { return ; }
            // Java.g:330:5: ( interfaceMethodOrFieldDecl | interfaceGenericMethodDecl | 'void' Identifier voidInterfaceMethodDeclaratorRest | interfaceDeclaration | classDeclaration )
            int alt45=5;
            switch ( input.LA(1) ) {
            case Identifier:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
                {
                alt45=1;
                }
                break;
            case 40:
                {
                alt45=2;
                }
                break;
            case 47:
                {
                alt45=3;
                }
                break;
            case 46:
            case 73:
                {
                alt45=4;
                }
                break;
            case ENUM:
            case 37:
                {
                alt45=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 45, 0, input);

                throw nvae;
            }

            switch (alt45) {
                case 1 :
                    // Java.g:330:9: interfaceMethodOrFieldDecl
                    {
                    pushFollow(FOLLOW_interfaceMethodOrFieldDecl_in_interfaceMemberDecl1209);
                    interfaceMethodOrFieldDecl();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:331:9: interfaceGenericMethodDecl
                    {
                    pushFollow(FOLLOW_interfaceGenericMethodDecl_in_interfaceMemberDecl1219);
                    interfaceGenericMethodDecl();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // Java.g:332:9: 'void' Identifier voidInterfaceMethodDeclaratorRest
                    {
                    match(input,47,FOLLOW_47_in_interfaceMemberDecl1229); if (state.failed) return ;
                    match(input,Identifier,FOLLOW_Identifier_in_interfaceMemberDecl1231); if (state.failed) return ;
                    pushFollow(FOLLOW_voidInterfaceMethodDeclaratorRest_in_interfaceMemberDecl1233);
                    voidInterfaceMethodDeclaratorRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // Java.g:333:9: interfaceDeclaration
                    {
                    pushFollow(FOLLOW_interfaceDeclaration_in_interfaceMemberDecl1243);
                    interfaceDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // Java.g:334:9: classDeclaration
                    {
                    pushFollow(FOLLOW_classDeclaration_in_interfaceMemberDecl1253);
                    classDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 32, interfaceMemberDecl_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "interfaceMemberDecl"


    // $ANTLR start "interfaceMethodOrFieldDecl"
    // Java.g:337:1: interfaceMethodOrFieldDecl : type Identifier interfaceMethodOrFieldRest ;
    public final void interfaceMethodOrFieldDecl() throws RecognitionException {
        int interfaceMethodOrFieldDecl_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 33) ) { return ; }
            // Java.g:338:5: ( type Identifier interfaceMethodOrFieldRest )
            // Java.g:338:9: type Identifier interfaceMethodOrFieldRest
            {
            pushFollow(FOLLOW_type_in_interfaceMethodOrFieldDecl1276);
            type();

            state._fsp--;
            if (state.failed) return ;
            match(input,Identifier,FOLLOW_Identifier_in_interfaceMethodOrFieldDecl1278); if (state.failed) return ;
            pushFollow(FOLLOW_interfaceMethodOrFieldRest_in_interfaceMethodOrFieldDecl1280);
            interfaceMethodOrFieldRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 33, interfaceMethodOrFieldDecl_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "interfaceMethodOrFieldDecl"


    // $ANTLR start "interfaceMethodOrFieldRest"
    // Java.g:341:1: interfaceMethodOrFieldRest : ( constantDeclaratorsRest ';' | interfaceMethodDeclaratorRest );
    public final void interfaceMethodOrFieldRest() throws RecognitionException {
        int interfaceMethodOrFieldRest_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 34) ) { return ; }
            // Java.g:342:5: ( constantDeclaratorsRest ';' | interfaceMethodDeclaratorRest )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==48||LA46_0==51) ) {
                alt46=1;
            }
            else if ( (LA46_0==66) ) {
                alt46=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;
            }
            switch (alt46) {
                case 1 :
                    // Java.g:342:9: constantDeclaratorsRest ';'
                    {
                    pushFollow(FOLLOW_constantDeclaratorsRest_in_interfaceMethodOrFieldRest1303);
                    constantDeclaratorsRest();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,26,FOLLOW_26_in_interfaceMethodOrFieldRest1305); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:343:9: interfaceMethodDeclaratorRest
                    {
                    pushFollow(FOLLOW_interfaceMethodDeclaratorRest_in_interfaceMethodOrFieldRest1315);
                    interfaceMethodDeclaratorRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 34, interfaceMethodOrFieldRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "interfaceMethodOrFieldRest"


    // $ANTLR start "methodDeclaratorRest"
    // Java.g:346:1: methodDeclaratorRest : formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ( methodBody | ';' ) ;
    public final void methodDeclaratorRest() throws RecognitionException {
        int methodDeclaratorRest_StartIndex = input.index();
        
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 35) ) { return ; }
            // Java.g:347:5: ( formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ( methodBody | ';' ) )
            // Java.g:347:9: formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ( methodBody | ';' )
            {

            
            pushFollow(FOLLOW_formalParameters_in_methodDeclaratorRest1338);
            formalParameters();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:347:26: ( '[' ']' )*
            loop47:
            do {
                int alt47=2;
                int LA47_0 = input.LA(1);

                if ( (LA47_0==48) ) {
                    alt47=1;
                }


                switch (alt47) {
            	case 1 :
            	    // Java.g:347:27: '[' ']'
            	    {
            	    match(input,48,FOLLOW_48_in_methodDeclaratorRest1341); if (state.failed) return ;
            	    match(input,49,FOLLOW_49_in_methodDeclaratorRest1343); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop47;
                }
            } while (true);

            // Java.g:348:9: ( 'throws' qualifiedNameList )?
            int alt48=2;
            int LA48_0 = input.LA(1);

            if ( (LA48_0==50) ) {
                alt48=1;
            }
            switch (alt48) {
                case 1 :
                    // Java.g:348:10: 'throws' qualifiedNameList
                    {
                    match(input,50,FOLLOW_50_in_methodDeclaratorRest1356); if (state.failed) return ;
                    
                    pushFollow(FOLLOW_qualifiedNameList_in_methodDeclaratorRest1358);
                    qualifiedNameList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // Java.g:349:9: ( methodBody | ';' )
            int alt49=2;
            int LA49_0 = input.LA(1);

            if ( (LA49_0==44) ) {
                alt49=1;
            }
            else if ( (LA49_0==26) ) {
                alt49=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 49, 0, input);

                throw nvae;
            }
            switch (alt49) {
                case 1 :
                    // Java.g:349:13: methodBody
                    {
                    pushFollow(FOLLOW_methodBody_in_methodDeclaratorRest1374);
                    methodBody();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:350:13: ';'
                    {
                    match(input,26,FOLLOW_26_in_methodDeclaratorRest1388); if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 35, methodDeclaratorRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "methodDeclaratorRest"


    // $ANTLR start "voidMethodDeclaratorRest"
    // Java.g:354:1: voidMethodDeclaratorRest : formalParameters ( 'throws' qualifiedNameList )? ( methodBody | ';' ) ;
    public final void voidMethodDeclaratorRest() throws RecognitionException {
        int voidMethodDeclaratorRest_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 36) ) { return ; }
            // Java.g:355:5: ( formalParameters ( 'throws' qualifiedNameList )? ( methodBody | ';' ) )
            // Java.g:355:9: formalParameters ( 'throws' qualifiedNameList )? ( methodBody | ';' )
            {
            pushFollow(FOLLOW_formalParameters_in_voidMethodDeclaratorRest1421);
            formalParameters();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:355:26: ( 'throws' qualifiedNameList )?
            int alt50=2;
            int LA50_0 = input.LA(1);

            if ( (LA50_0==50) ) {
                alt50=1;
            }
            switch (alt50) {
                case 1 :
                    // Java.g:355:27: 'throws' qualifiedNameList
                    {
                    match(input,50,FOLLOW_50_in_voidMethodDeclaratorRest1424); if (state.failed) return ;
                    pushFollow(FOLLOW_qualifiedNameList_in_voidMethodDeclaratorRest1426);
                    qualifiedNameList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // Java.g:356:9: ( methodBody | ';' )
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( (LA51_0==44) ) {
                alt51=1;
            }
            else if ( (LA51_0==26) ) {
                alt51=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 51, 0, input);

                throw nvae;
            }
            switch (alt51) {
                case 1 :
                    // Java.g:356:13: methodBody
                    {
                    pushFollow(FOLLOW_methodBody_in_voidMethodDeclaratorRest1442);
                    methodBody();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:357:13: ';'
                    {
                    match(input,26,FOLLOW_26_in_voidMethodDeclaratorRest1456); if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 36, voidMethodDeclaratorRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "voidMethodDeclaratorRest"


    // $ANTLR start "interfaceMethodDeclaratorRest"
    // Java.g:361:1: interfaceMethodDeclaratorRest : formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ';' ;
    public final void interfaceMethodDeclaratorRest() throws RecognitionException {
        int interfaceMethodDeclaratorRest_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 37) ) { return ; }
            // Java.g:362:5: ( formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ';' )
            // Java.g:362:9: formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ';'
            {
            pushFollow(FOLLOW_formalParameters_in_interfaceMethodDeclaratorRest1489);
            formalParameters();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:362:26: ( '[' ']' )*
            loop52:
            do {
                int alt52=2;
                int LA52_0 = input.LA(1);

                if ( (LA52_0==48) ) {
                    alt52=1;
                }


                switch (alt52) {
            	case 1 :
            	    // Java.g:362:27: '[' ']'
            	    {
            	    match(input,48,FOLLOW_48_in_interfaceMethodDeclaratorRest1492); if (state.failed) return ;
            	    match(input,49,FOLLOW_49_in_interfaceMethodDeclaratorRest1494); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop52;
                }
            } while (true);

            // Java.g:362:37: ( 'throws' qualifiedNameList )?
            int alt53=2;
            int LA53_0 = input.LA(1);

            if ( (LA53_0==50) ) {
                alt53=1;
            }
            switch (alt53) {
                case 1 :
                    // Java.g:362:38: 'throws' qualifiedNameList
                    {
                    match(input,50,FOLLOW_50_in_interfaceMethodDeclaratorRest1499); if (state.failed) return ;
                    pushFollow(FOLLOW_qualifiedNameList_in_interfaceMethodDeclaratorRest1501);
                    qualifiedNameList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            match(input,26,FOLLOW_26_in_interfaceMethodDeclaratorRest1505); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 37, interfaceMethodDeclaratorRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "interfaceMethodDeclaratorRest"


    // $ANTLR start "interfaceGenericMethodDecl"
    // Java.g:365:1: interfaceGenericMethodDecl : typeParameters ( type | 'void' ) Identifier interfaceMethodDeclaratorRest ;
    public final void interfaceGenericMethodDecl() throws RecognitionException {
        int interfaceGenericMethodDecl_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 38) ) { return ; }
            // Java.g:366:5: ( typeParameters ( type | 'void' ) Identifier interfaceMethodDeclaratorRest )
            // Java.g:366:9: typeParameters ( type | 'void' ) Identifier interfaceMethodDeclaratorRest
            {
            pushFollow(FOLLOW_typeParameters_in_interfaceGenericMethodDecl1528);
            typeParameters();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:366:24: ( type | 'void' )
            int alt54=2;
            int LA54_0 = input.LA(1);

            if ( (LA54_0==Identifier||(LA54_0>=56 && LA54_0<=63)) ) {
                alt54=1;
            }
            else if ( (LA54_0==47) ) {
                alt54=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 54, 0, input);

                throw nvae;
            }
            switch (alt54) {
                case 1 :
                    // Java.g:366:25: type
                    {
                    pushFollow(FOLLOW_type_in_interfaceGenericMethodDecl1531);
                    type();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:366:32: 'void'
                    {
                    match(input,47,FOLLOW_47_in_interfaceGenericMethodDecl1535); if (state.failed) return ;

                    }
                    break;

            }

            match(input,Identifier,FOLLOW_Identifier_in_interfaceGenericMethodDecl1538); if (state.failed) return ;
            pushFollow(FOLLOW_interfaceMethodDeclaratorRest_in_interfaceGenericMethodDecl1548);
            interfaceMethodDeclaratorRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 38, interfaceGenericMethodDecl_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "interfaceGenericMethodDecl"


    // $ANTLR start "voidInterfaceMethodDeclaratorRest"
    // Java.g:370:1: voidInterfaceMethodDeclaratorRest : formalParameters ( 'throws' qualifiedNameList )? ';' ;
    public final void voidInterfaceMethodDeclaratorRest() throws RecognitionException {
        int voidInterfaceMethodDeclaratorRest_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 39) ) { return ; }
            // Java.g:371:5: ( formalParameters ( 'throws' qualifiedNameList )? ';' )
            // Java.g:371:9: formalParameters ( 'throws' qualifiedNameList )? ';'
            {
            pushFollow(FOLLOW_formalParameters_in_voidInterfaceMethodDeclaratorRest1571);
            formalParameters();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:371:26: ( 'throws' qualifiedNameList )?
            int alt55=2;
            int LA55_0 = input.LA(1);

            if ( (LA55_0==50) ) {
                alt55=1;
            }
            switch (alt55) {
                case 1 :
                    // Java.g:371:27: 'throws' qualifiedNameList
                    {
                    match(input,50,FOLLOW_50_in_voidInterfaceMethodDeclaratorRest1574); if (state.failed) return ;
                    pushFollow(FOLLOW_qualifiedNameList_in_voidInterfaceMethodDeclaratorRest1576);
                    qualifiedNameList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            match(input,26,FOLLOW_26_in_voidInterfaceMethodDeclaratorRest1580); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 39, voidInterfaceMethodDeclaratorRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "voidInterfaceMethodDeclaratorRest"


    // $ANTLR start "constructorDeclaratorRest"
    // Java.g:374:1: constructorDeclaratorRest : formalParameters ( 'throws' qualifiedNameList )? constructorBody ;
    public final void constructorDeclaratorRest() throws RecognitionException {
        int constructorDeclaratorRest_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 40) ) { return ; }
            // Java.g:375:5: ( formalParameters ( 'throws' qualifiedNameList )? constructorBody )
            // Java.g:375:9: formalParameters ( 'throws' qualifiedNameList )? constructorBody
            {
            pushFollow(FOLLOW_formalParameters_in_constructorDeclaratorRest1603);
            formalParameters();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:375:26: ( 'throws' qualifiedNameList )?
            int alt56=2;
            int LA56_0 = input.LA(1);

            if ( (LA56_0==50) ) {
                alt56=1;
            }
            switch (alt56) {
                case 1 :
                    // Java.g:375:27: 'throws' qualifiedNameList
                    {
                    match(input,50,FOLLOW_50_in_constructorDeclaratorRest1606); if (state.failed) return ;
                    pushFollow(FOLLOW_qualifiedNameList_in_constructorDeclaratorRest1608);
                    qualifiedNameList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_constructorBody_in_constructorDeclaratorRest1612);
            constructorBody();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 40, constructorDeclaratorRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "constructorDeclaratorRest"


    // $ANTLR start "constantDeclarator"
    // Java.g:378:1: constantDeclarator : Identifier constantDeclaratorRest ;
    public final void constantDeclarator() throws RecognitionException {
        int constantDeclarator_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 41) ) { return ; }
            // Java.g:379:5: ( Identifier constantDeclaratorRest )
            // Java.g:379:9: Identifier constantDeclaratorRest
            {
            match(input,Identifier,FOLLOW_Identifier_in_constantDeclarator1631); if (state.failed) return ;
            pushFollow(FOLLOW_constantDeclaratorRest_in_constantDeclarator1633);
            constantDeclaratorRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 41, constantDeclarator_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "constantDeclarator"


    // $ANTLR start "variableDeclarators"
    // Java.g:382:1: variableDeclarators : variableDeclarator ( ',' variableDeclarator )* ;
    public final void variableDeclarators() throws RecognitionException {
        int variableDeclarators_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 42) ) { return ; }
            // Java.g:383:5: ( variableDeclarator ( ',' variableDeclarator )* )
            // Java.g:383:9: variableDeclarator ( ',' variableDeclarator )*
            {
            pushFollow(FOLLOW_variableDeclarator_in_variableDeclarators1656);
            variableDeclarator();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:383:28: ( ',' variableDeclarator )*
            loop57:
            do {
                int alt57=2;
                int LA57_0 = input.LA(1);

                if ( (LA57_0==41) ) {
                    alt57=1;
                }


                switch (alt57) {
            	case 1 :
            	    // Java.g:383:29: ',' variableDeclarator
            	    {
            	    match(input,41,FOLLOW_41_in_variableDeclarators1659); if (state.failed) return ;
            	    pushFollow(FOLLOW_variableDeclarator_in_variableDeclarators1661);
            	    variableDeclarator();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop57;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 42, variableDeclarators_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "variableDeclarators"


    // $ANTLR start "variableDeclarator"
    // Java.g:386:1: variableDeclarator : variableDeclaratorId ( '=' variableInitializer )? ;
    public final void variableDeclarator() throws RecognitionException {
        int variableDeclarator_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 43) ) { return ; }
            // Java.g:387:5: ( variableDeclaratorId ( '=' variableInitializer )? )
            // Java.g:387:9: variableDeclaratorId ( '=' variableInitializer )?
            {
            pushFollow(FOLLOW_variableDeclaratorId_in_variableDeclarator1682);
            variableDeclaratorId();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:387:30: ( '=' variableInitializer )?
            int alt58=2;
            int LA58_0 = input.LA(1);

            if ( (LA58_0==51) ) {
                alt58=1;
            }
            switch (alt58) {
                case 1 :
                    // Java.g:387:31: '=' variableInitializer
                    {
                    match(input,51,FOLLOW_51_in_variableDeclarator1685); if (state.failed) return ;
                    pushFollow(FOLLOW_variableInitializer_in_variableDeclarator1687);
                    variableInitializer();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 43, variableDeclarator_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "variableDeclarator"


    // $ANTLR start "constantDeclaratorsRest"
    // Java.g:390:1: constantDeclaratorsRest : constantDeclaratorRest ( ',' constantDeclarator )* ;
    public final void constantDeclaratorsRest() throws RecognitionException {
        int constantDeclaratorsRest_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 44) ) { return ; }
            // Java.g:391:5: ( constantDeclaratorRest ( ',' constantDeclarator )* )
            // Java.g:391:9: constantDeclaratorRest ( ',' constantDeclarator )*
            {
            pushFollow(FOLLOW_constantDeclaratorRest_in_constantDeclaratorsRest1712);
            constantDeclaratorRest();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:391:32: ( ',' constantDeclarator )*
            loop59:
            do {
                int alt59=2;
                int LA59_0 = input.LA(1);

                if ( (LA59_0==41) ) {
                    alt59=1;
                }


                switch (alt59) {
            	case 1 :
            	    // Java.g:391:33: ',' constantDeclarator
            	    {
            	    match(input,41,FOLLOW_41_in_constantDeclaratorsRest1715); if (state.failed) return ;
            	    pushFollow(FOLLOW_constantDeclarator_in_constantDeclaratorsRest1717);
            	    constantDeclarator();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop59;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 44, constantDeclaratorsRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "constantDeclaratorsRest"


    // $ANTLR start "constantDeclaratorRest"
    // Java.g:394:1: constantDeclaratorRest : ( '[' ']' )* '=' variableInitializer ;
    public final void constantDeclaratorRest() throws RecognitionException {
        int constantDeclaratorRest_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 45) ) { return ; }
            // Java.g:395:5: ( ( '[' ']' )* '=' variableInitializer )
            // Java.g:395:9: ( '[' ']' )* '=' variableInitializer
            {
            // Java.g:395:9: ( '[' ']' )*
            loop60:
            do {
                int alt60=2;
                int LA60_0 = input.LA(1);

                if ( (LA60_0==48) ) {
                    alt60=1;
                }


                switch (alt60) {
            	case 1 :
            	    // Java.g:395:10: '[' ']'
            	    {
            	    match(input,48,FOLLOW_48_in_constantDeclaratorRest1739); if (state.failed) return ;
            	    match(input,49,FOLLOW_49_in_constantDeclaratorRest1741); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop60;
                }
            } while (true);

            match(input,51,FOLLOW_51_in_constantDeclaratorRest1745); if (state.failed) return ;
            pushFollow(FOLLOW_variableInitializer_in_constantDeclaratorRest1747);
            variableInitializer();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 45, constantDeclaratorRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "constantDeclaratorRest"


    // $ANTLR start "variableDeclaratorId"
    // Java.g:398:1: variableDeclaratorId : Identifier ( '[' ']' )* ;
    public final void variableDeclaratorId() throws RecognitionException {
        int variableDeclaratorId_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 46) ) { return ; }
            // Java.g:399:5: ( Identifier ( '[' ']' )* )
            // Java.g:399:9: Identifier ( '[' ']' )*
            {
            match(input,Identifier,FOLLOW_Identifier_in_variableDeclaratorId1770); if (state.failed) return ;
            // Java.g:399:20: ( '[' ']' )*
            loop61:
            do {
                int alt61=2;
                int LA61_0 = input.LA(1);

                if ( (LA61_0==48) ) {
                    alt61=1;
                }


                switch (alt61) {
            	case 1 :
            	    // Java.g:399:21: '[' ']'
            	    {
            	    match(input,48,FOLLOW_48_in_variableDeclaratorId1773); if (state.failed) return ;
            	    match(input,49,FOLLOW_49_in_variableDeclaratorId1775); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop61;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 46, variableDeclaratorId_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "variableDeclaratorId"


    // $ANTLR start "variableInitializer"
    // Java.g:402:1: variableInitializer : ( arrayInitializer | expression );
    public final void variableInitializer() throws RecognitionException {
        int variableInitializer_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 47) ) { return ; }
            // Java.g:403:5: ( arrayInitializer | expression )
            int alt62=2;
            int LA62_0 = input.LA(1);

            if ( (LA62_0==44) ) {
                alt62=1;
            }
            else if ( (LA62_0==Identifier||(LA62_0>=FloatingPointLiteral && LA62_0<=DecimalLiteral)||LA62_0==47||(LA62_0>=56 && LA62_0<=63)||(LA62_0>=65 && LA62_0<=66)||(LA62_0>=69 && LA62_0<=72)||(LA62_0>=105 && LA62_0<=106)||(LA62_0>=109 && LA62_0<=113)) ) {
                alt62=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 62, 0, input);

                throw nvae;
            }
            switch (alt62) {
                case 1 :
                    // Java.g:403:9: arrayInitializer
                    {
                    pushFollow(FOLLOW_arrayInitializer_in_variableInitializer1796);
                    arrayInitializer();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:404:9: expression
                    {
                    pushFollow(FOLLOW_expression_in_variableInitializer1806);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 47, variableInitializer_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "variableInitializer"


    // $ANTLR start "arrayInitializer"
    // Java.g:407:1: arrayInitializer : '{' ( variableInitializer ( ',' variableInitializer )* ( ',' )? )? '}' ;
    public final void arrayInitializer() throws RecognitionException {
        int arrayInitializer_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 48) ) { return ; }
            // Java.g:408:5: ( '{' ( variableInitializer ( ',' variableInitializer )* ( ',' )? )? '}' )
            // Java.g:408:9: '{' ( variableInitializer ( ',' variableInitializer )* ( ',' )? )? '}'
            {
            match(input,44,FOLLOW_44_in_arrayInitializer1833); if (state.failed) return ;
            // Java.g:408:13: ( variableInitializer ( ',' variableInitializer )* ( ',' )? )?
            int alt65=2;
            int LA65_0 = input.LA(1);

            if ( (LA65_0==Identifier||(LA65_0>=FloatingPointLiteral && LA65_0<=DecimalLiteral)||LA65_0==44||LA65_0==47||(LA65_0>=56 && LA65_0<=63)||(LA65_0>=65 && LA65_0<=66)||(LA65_0>=69 && LA65_0<=72)||(LA65_0>=105 && LA65_0<=106)||(LA65_0>=109 && LA65_0<=113)) ) {
                alt65=1;
            }
            switch (alt65) {
                case 1 :
                    // Java.g:408:14: variableInitializer ( ',' variableInitializer )* ( ',' )?
                    {
                    pushFollow(FOLLOW_variableInitializer_in_arrayInitializer1836);
                    variableInitializer();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:408:34: ( ',' variableInitializer )*
                    loop63:
                    do {
                        int alt63=2;
                        int LA63_0 = input.LA(1);

                        if ( (LA63_0==41) ) {
                            int LA63_1 = input.LA(2);

                            if ( (LA63_1==Identifier||(LA63_1>=FloatingPointLiteral && LA63_1<=DecimalLiteral)||LA63_1==44||LA63_1==47||(LA63_1>=56 && LA63_1<=63)||(LA63_1>=65 && LA63_1<=66)||(LA63_1>=69 && LA63_1<=72)||(LA63_1>=105 && LA63_1<=106)||(LA63_1>=109 && LA63_1<=113)) ) {
                                alt63=1;
                            }


                        }


                        switch (alt63) {
                    	case 1 :
                    	    // Java.g:408:35: ',' variableInitializer
                    	    {
                    	    match(input,41,FOLLOW_41_in_arrayInitializer1839); if (state.failed) return ;
                    	    pushFollow(FOLLOW_variableInitializer_in_arrayInitializer1841);
                    	    variableInitializer();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop63;
                        }
                    } while (true);

                    // Java.g:408:61: ( ',' )?
                    int alt64=2;
                    int LA64_0 = input.LA(1);

                    if ( (LA64_0==41) ) {
                        alt64=1;
                    }
                    switch (alt64) {
                        case 1 :
                            // Java.g:408:62: ','
                            {
                            match(input,41,FOLLOW_41_in_arrayInitializer1846); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }

            match(input,45,FOLLOW_45_in_arrayInitializer1853); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 48, arrayInitializer_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "arrayInitializer"


    // $ANTLR start "modifier"
    // Java.g:411:1: modifier : ( annotation | 'public' | 'protected' | 'private' | 'static' | 'abstract' | 'final' | 'native' | 'synchronized' | 'transient' | 'volatile' | 'strictfp' );
    public final void modifier() throws RecognitionException {
        int modifier_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 49) ) { return ; }
            // Java.g:412:5: ( annotation | 'public' | 'protected' | 'private' | 'static' | 'abstract' | 'final' | 'native' | 'synchronized' | 'transient' | 'volatile' | 'strictfp' )
            int alt66=12;
            switch ( input.LA(1) ) {
            case 73:
                {
                alt66=1;
                }
                break;
            case 31:
                {
                alt66=2;
                }
                break;
            case 32:
                {
                alt66=3;
                }
                break;
            case 33:
                {
                alt66=4;
                }
                break;
            case 28:
                {
                alt66=5;
                }
                break;
            case 34:
                {
                alt66=6;
                }
                break;
            case 35:
                {
                alt66=7;
                }
                break;
            case 52:
                {
                alt66=8;
                }
                break;
            case 53:
                {
                alt66=9;
                }
                break;
            case 54:
                {
                alt66=10;
                }
                break;
            case 55:
                {
                alt66=11;
                }
                break;
            case 36:
                {
                alt66=12;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 66, 0, input);

                throw nvae;
            }

            switch (alt66) {
                case 1 :
                    // Java.g:412:9: annotation
                    {
                    pushFollow(FOLLOW_annotation_in_modifier1872);
                    annotation();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:413:9: 'public'
                    {
                    match(input,31,FOLLOW_31_in_modifier1882); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // Java.g:414:9: 'protected'
                    {
                    match(input,32,FOLLOW_32_in_modifier1892); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // Java.g:415:9: 'private'
                    {
                    match(input,33,FOLLOW_33_in_modifier1902); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // Java.g:416:9: 'static'
                    {
                    match(input,28,FOLLOW_28_in_modifier1912); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // Java.g:417:9: 'abstract'
                    {
                    match(input,34,FOLLOW_34_in_modifier1922); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    // Java.g:418:9: 'final'
                    {
                    match(input,35,FOLLOW_35_in_modifier1932); if (state.failed) return ;

                    }
                    break;
                case 8 :
                    // Java.g:419:9: 'native'
                    {
                    match(input,52,FOLLOW_52_in_modifier1942); if (state.failed) return ;

                    }
                    break;
                case 9 :
                    // Java.g:420:9: 'synchronized'
                    {
                    match(input,53,FOLLOW_53_in_modifier1952); if (state.failed) return ;

                    }
                    break;
                case 10 :
                    // Java.g:421:9: 'transient'
                    {
                    match(input,54,FOLLOW_54_in_modifier1962); if (state.failed) return ;

                    }
                    break;
                case 11 :
                    // Java.g:422:9: 'volatile'
                    {
                    match(input,55,FOLLOW_55_in_modifier1972); if (state.failed) return ;

                    }
                    break;
                case 12 :
                    // Java.g:423:9: 'strictfp'
                    {
                    match(input,36,FOLLOW_36_in_modifier1982); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 49, modifier_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "modifier"


    // $ANTLR start "packageOrTypeName"
    // Java.g:426:1: packageOrTypeName : qualifiedName ;
    public final void packageOrTypeName() throws RecognitionException {
        int packageOrTypeName_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 50) ) { return ; }
            // Java.g:427:5: ( qualifiedName )
            // Java.g:427:9: qualifiedName
            {
            pushFollow(FOLLOW_qualifiedName_in_packageOrTypeName2001);
            qualifiedName();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 50, packageOrTypeName_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "packageOrTypeName"


    // $ANTLR start "enumConstantName"
    // Java.g:430:1: enumConstantName : Identifier ;
    public final void enumConstantName() throws RecognitionException {
        int enumConstantName_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 51) ) { return ; }
            // Java.g:431:5: ( Identifier )
            // Java.g:431:9: Identifier
            {
            match(input,Identifier,FOLLOW_Identifier_in_enumConstantName2020); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 51, enumConstantName_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "enumConstantName"


    // $ANTLR start "typeName"
    // Java.g:434:1: typeName : qualifiedName ;
    public final void typeName() throws RecognitionException {
        int typeName_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 52) ) { return ; }
            // Java.g:435:5: ( qualifiedName )
            // Java.g:435:9: qualifiedName
            {
            pushFollow(FOLLOW_qualifiedName_in_typeName2039);
            qualifiedName();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 52, typeName_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "typeName"


    // $ANTLR start "type"
    // Java.g:438:1: type : ( classOrInterfaceType ( '[' ']' )* | primitiveType ( '[' ']' )* );
    public final void type() throws RecognitionException {
        int type_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 53) ) { return ; }
            // Java.g:439:2: ( classOrInterfaceType ( '[' ']' )* | primitiveType ( '[' ']' )* )
            int alt69=2;
            int LA69_0 = input.LA(1);

            if ( (LA69_0==Identifier) ) {
                alt69=1;
            }
            else if ( ((LA69_0>=56 && LA69_0<=63)) ) {
                alt69=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 69, 0, input);

                throw nvae;
            }
            switch (alt69) {
                case 1 :
                    // Java.g:439:4: classOrInterfaceType ( '[' ']' )*
                    {
                    pushFollow(FOLLOW_classOrInterfaceType_in_type2053);
                    classOrInterfaceType();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:439:25: ( '[' ']' )*
                    loop67:
                    do {
                        int alt67=2;
                        int LA67_0 = input.LA(1);

                        if ( (LA67_0==48) ) {
                            alt67=1;
                        }


                        switch (alt67) {
                    	case 1 :
                    	    // Java.g:439:26: '[' ']'
                    	    {
                    	    match(input,48,FOLLOW_48_in_type2056); if (state.failed) return ;
                    	    match(input,49,FOLLOW_49_in_type2058); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop67;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // Java.g:440:4: primitiveType ( '[' ']' )*
                    {
                    pushFollow(FOLLOW_primitiveType_in_type2065);
                    primitiveType();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:440:18: ( '[' ']' )*
                    loop68:
                    do {
                        int alt68=2;
                        int LA68_0 = input.LA(1);

                        if ( (LA68_0==48) ) {
                            alt68=1;
                        }


                        switch (alt68) {
                    	case 1 :
                    	    // Java.g:440:19: '[' ']'
                    	    {
                    	    match(input,48,FOLLOW_48_in_type2068); if (state.failed) return ;
                    	    match(input,49,FOLLOW_49_in_type2070); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop68;
                        }
                    } while (true);


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 53, type_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "type"


    // $ANTLR start "classOrInterfaceType"
    // Java.g:443:1: classOrInterfaceType : Identifier ( typeArguments )? ( '.' Identifier ( typeArguments )? )* ;
    public final void classOrInterfaceType() throws RecognitionException {
        int classOrInterfaceType_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 54) ) { return ; }
            // Java.g:444:2: ( Identifier ( typeArguments )? ( '.' Identifier ( typeArguments )? )* )
            // Java.g:444:4: Identifier ( typeArguments )? ( '.' Identifier ( typeArguments )? )*
            {
            match(input,Identifier,FOLLOW_Identifier_in_classOrInterfaceType2083); if (state.failed) return ;
            // Java.g:444:15: ( typeArguments )?
            int alt70=2;
            int LA70_0 = input.LA(1);

            if ( (LA70_0==40) ) {
                int LA70_1 = input.LA(2);

                if ( (LA70_1==Identifier||(LA70_1>=56 && LA70_1<=64)) ) {
                    alt70=1;
                }
            }
            switch (alt70) {
                case 1 :
                    // Java.g:0:0: typeArguments
                    {
                    pushFollow(FOLLOW_typeArguments_in_classOrInterfaceType2085);
                    typeArguments();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // Java.g:444:30: ( '.' Identifier ( typeArguments )? )*
            loop72:
            do {
                int alt72=2;
                int LA72_0 = input.LA(1);

                if ( (LA72_0==29) ) {
                    alt72=1;
                }


                switch (alt72) {
            	case 1 :
            	    // Java.g:444:31: '.' Identifier ( typeArguments )?
            	    {
            	    match(input,29,FOLLOW_29_in_classOrInterfaceType2089); if (state.failed) return ;
            	    match(input,Identifier,FOLLOW_Identifier_in_classOrInterfaceType2091); if (state.failed) return ;
            	    // Java.g:444:46: ( typeArguments )?
            	    int alt71=2;
            	    int LA71_0 = input.LA(1);

            	    if ( (LA71_0==40) ) {
            	        int LA71_1 = input.LA(2);

            	        if ( (LA71_1==Identifier||(LA71_1>=56 && LA71_1<=64)) ) {
            	            alt71=1;
            	        }
            	    }
            	    switch (alt71) {
            	        case 1 :
            	            // Java.g:0:0: typeArguments
            	            {
            	            pushFollow(FOLLOW_typeArguments_in_classOrInterfaceType2093);
            	            typeArguments();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop72;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 54, classOrInterfaceType_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "classOrInterfaceType"


    // $ANTLR start "primitiveType"
    // Java.g:447:1: primitiveType : ( 'boolean' | 'char' | 'byte' | 'short' | 'int' | 'long' | 'float' | 'double' );
    public final void primitiveType() throws RecognitionException {
        int primitiveType_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 55) ) { return ; }
            // Java.g:448:5: ( 'boolean' | 'char' | 'byte' | 'short' | 'int' | 'long' | 'float' | 'double' )
            // Java.g:
            {
            if ( (input.LA(1)>=56 && input.LA(1)<=63) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 55, primitiveType_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "primitiveType"


    // $ANTLR start "variableModifier"
    // Java.g:458:1: variableModifier : ( 'final' | annotation );
    public final void variableModifier() throws RecognitionException {
        int variableModifier_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 56) ) { return ; }
            // Java.g:459:5: ( 'final' | annotation )
            int alt73=2;
            int LA73_0 = input.LA(1);

            if ( (LA73_0==35) ) {
                alt73=1;
            }
            else if ( (LA73_0==73) ) {
                alt73=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 73, 0, input);

                throw nvae;
            }
            switch (alt73) {
                case 1 :
                    // Java.g:459:9: 'final'
                    {
                    match(input,35,FOLLOW_35_in_variableModifier2202); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:460:9: annotation
                    {
                    pushFollow(FOLLOW_annotation_in_variableModifier2212);
                    annotation();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 56, variableModifier_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "variableModifier"


    // $ANTLR start "typeArguments"
    // Java.g:463:1: typeArguments : '<' typeArgument ( ',' typeArgument )* '>' ;
    public final void typeArguments() throws RecognitionException {
        int typeArguments_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 57) ) { return ; }
            // Java.g:464:5: ( '<' typeArgument ( ',' typeArgument )* '>' )
            // Java.g:464:9: '<' typeArgument ( ',' typeArgument )* '>'
            {
            match(input,40,FOLLOW_40_in_typeArguments2231); if (state.failed) return ;
            pushFollow(FOLLOW_typeArgument_in_typeArguments2233);
            typeArgument();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:464:26: ( ',' typeArgument )*
            loop74:
            do {
                int alt74=2;
                int LA74_0 = input.LA(1);

                if ( (LA74_0==41) ) {
                    alt74=1;
                }


                switch (alt74) {
            	case 1 :
            	    // Java.g:464:27: ',' typeArgument
            	    {
            	    match(input,41,FOLLOW_41_in_typeArguments2236); if (state.failed) return ;
            	    pushFollow(FOLLOW_typeArgument_in_typeArguments2238);
            	    typeArgument();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop74;
                }
            } while (true);

            match(input,42,FOLLOW_42_in_typeArguments2242); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 57, typeArguments_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "typeArguments"


    // $ANTLR start "typeArgument"
    // Java.g:467:1: typeArgument : ( type | '?' ( ( 'extends' | 'super' ) type )? );
    public final void typeArgument() throws RecognitionException {
        int typeArgument_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 58) ) { return ; }
            // Java.g:468:5: ( type | '?' ( ( 'extends' | 'super' ) type )? )
            int alt76=2;
            int LA76_0 = input.LA(1);

            if ( (LA76_0==Identifier||(LA76_0>=56 && LA76_0<=63)) ) {
                alt76=1;
            }
            else if ( (LA76_0==64) ) {
                alt76=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 76, 0, input);

                throw nvae;
            }
            switch (alt76) {
                case 1 :
                    // Java.g:468:9: type
                    {
                    pushFollow(FOLLOW_type_in_typeArgument2265);
                    type();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:469:9: '?' ( ( 'extends' | 'super' ) type )?
                    {
                    match(input,64,FOLLOW_64_in_typeArgument2275); if (state.failed) return ;
                    // Java.g:469:13: ( ( 'extends' | 'super' ) type )?
                    int alt75=2;
                    int LA75_0 = input.LA(1);

                    if ( (LA75_0==38||LA75_0==65) ) {
                        alt75=1;
                    }
                    switch (alt75) {
                        case 1 :
                            // Java.g:469:14: ( 'extends' | 'super' ) type
                            {
                            if ( input.LA(1)==38||input.LA(1)==65 ) {
                                input.consume();
                                state.errorRecovery=false;state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                throw mse;
                            }

                            pushFollow(FOLLOW_type_in_typeArgument2286);
                            type();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 58, typeArgument_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "typeArgument"


    // $ANTLR start "qualifiedNameList"
    // Java.g:472:1: qualifiedNameList : qualifiedName ( ',' qualifiedName )* ;
    public final void qualifiedNameList() throws RecognitionException {
        int qualifiedNameList_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 59) ) { return ; }
            // Java.g:473:5: ( qualifiedName ( ',' qualifiedName )* )
            // Java.g:473:9: qualifiedName ( ',' qualifiedName )*
            {
            pushFollow(FOLLOW_qualifiedName_in_qualifiedNameList2311);
            qualifiedName();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:473:23: ( ',' qualifiedName )*
            loop77:
            do {
                int alt77=2;
                int LA77_0 = input.LA(1);

                if ( (LA77_0==41) ) {
                    alt77=1;
                }


                switch (alt77) {
            	case 1 :
            	    // Java.g:473:24: ',' qualifiedName
            	    {
            	    match(input,41,FOLLOW_41_in_qualifiedNameList2314); if (state.failed) return ;
            	    pushFollow(FOLLOW_qualifiedName_in_qualifiedNameList2316);
            	    qualifiedName();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop77;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 59, qualifiedNameList_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "qualifiedNameList"


    // $ANTLR start "formalParameters"
    // Java.g:476:1: formalParameters : '(' ( formalParameterDecls )? ')' ;
    public final void formalParameters() throws RecognitionException {
        int formalParameters_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 60) ) { return ; }
            // Java.g:477:5: ( '(' ( formalParameterDecls )? ')' )
            // Java.g:477:9: '(' ( formalParameterDecls )? ')'
            {
            match(input,66,FOLLOW_66_in_formalParameters2337); if (state.failed) return ;
            // Java.g:477:13: ( formalParameterDecls )?
            int alt78=2;
            int LA78_0 = input.LA(1);

            if ( (LA78_0==Identifier||LA78_0==35||(LA78_0>=56 && LA78_0<=63)||LA78_0==73) ) {
                alt78=1;
            }
            switch (alt78) {
                case 1 :
                    // Java.g:0:0: formalParameterDecls
                    {
                    pushFollow(FOLLOW_formalParameterDecls_in_formalParameters2339);
                    formalParameterDecls();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            match(input,67,FOLLOW_67_in_formalParameters2342); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 60, formalParameters_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "formalParameters"


    // $ANTLR start "formalParameterDecls"
    // Java.g:480:1: formalParameterDecls : variableModifiers type formalParameterDeclsRest ;
    public final void formalParameterDecls() throws RecognitionException {
        int formalParameterDecls_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 61) ) { return ; }
            // Java.g:481:5: ( variableModifiers type formalParameterDeclsRest )
            // Java.g:481:9: variableModifiers type formalParameterDeclsRest
            {
            pushFollow(FOLLOW_variableModifiers_in_formalParameterDecls2365);
            variableModifiers();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_type_in_formalParameterDecls2367);
            type();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_formalParameterDeclsRest_in_formalParameterDecls2369);
            formalParameterDeclsRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 61, formalParameterDecls_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "formalParameterDecls"


    // $ANTLR start "formalParameterDeclsRest"
    // Java.g:484:1: formalParameterDeclsRest : ( variableDeclaratorId ( ',' formalParameterDecls )? | '...' variableDeclaratorId );
    public final void formalParameterDeclsRest() throws RecognitionException {
        int formalParameterDeclsRest_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 62) ) { return ; }
            // Java.g:485:5: ( variableDeclaratorId ( ',' formalParameterDecls )? | '...' variableDeclaratorId )
            int alt80=2;
            int LA80_0 = input.LA(1);

            if ( (LA80_0==Identifier) ) {
                alt80=1;
            }
            else if ( (LA80_0==68) ) {
                alt80=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 80, 0, input);

                throw nvae;
            }
            switch (alt80) {
                case 1 :
                    // Java.g:485:9: variableDeclaratorId ( ',' formalParameterDecls )?
                    {
                    pushFollow(FOLLOW_variableDeclaratorId_in_formalParameterDeclsRest2392);
                    variableDeclaratorId();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:485:30: ( ',' formalParameterDecls )?
                    int alt79=2;
                    int LA79_0 = input.LA(1);

                    if ( (LA79_0==41) ) {
                        alt79=1;
                    }
                    switch (alt79) {
                        case 1 :
                            // Java.g:485:31: ',' formalParameterDecls
                            {
                            match(input,41,FOLLOW_41_in_formalParameterDeclsRest2395); if (state.failed) return ;
                            pushFollow(FOLLOW_formalParameterDecls_in_formalParameterDeclsRest2397);
                            formalParameterDecls();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // Java.g:486:9: '...' variableDeclaratorId
                    {
                    match(input,68,FOLLOW_68_in_formalParameterDeclsRest2409); if (state.failed) return ;
                    pushFollow(FOLLOW_variableDeclaratorId_in_formalParameterDeclsRest2411);
                    variableDeclaratorId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 62, formalParameterDeclsRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "formalParameterDeclsRest"

    public List<MethodInfo> lines = new ArrayList<MethodInfo>();
    
    // $ANTLR start "methodBody"
    // Java.g:489:1: methodBody : block ;
    public final void methodBody() throws RecognitionException {
        int methodBody_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 63) ) { return ; }
            // Java.g:490:5: ( block )
            // Java.g:490:9: block
            {
            int startLine = input.get(methodBody_StartIndex).getLine();
            pushFollow(FOLLOW_block_in_methodBody2434);
            int endLine = block();
            
            MethodInfo latestInfo = currentInfos.remove(currentInfos.size() - 1);
            latestInfo.line = endLine - startLine + 1; 
            lines.add(latestInfo);
            
            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 63, methodBody_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "methodBody"


    // $ANTLR start "constructorBody"
    // Java.g:493:1: constructorBody : '{' ( explicitConstructorInvocation )? ( blockStatement )* '}' ;
    public final void constructorBody() throws RecognitionException {
        int constructorBody_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 64) ) { return ; }
            // Java.g:494:5: ( '{' ( explicitConstructorInvocation )? ( blockStatement )* '}' )
            // Java.g:494:9: '{' ( explicitConstructorInvocation )? ( blockStatement )* '}'
            {
            match(input,44,FOLLOW_44_in_constructorBody2453); if (state.failed) return ;
            // Java.g:494:13: ( explicitConstructorInvocation )?
            int alt81=2;
            alt81 = dfa81.predict(input);
            switch (alt81) {
                case 1 :
                    // Java.g:0:0: explicitConstructorInvocation
                    {
                    pushFollow(FOLLOW_explicitConstructorInvocation_in_constructorBody2455);
                    explicitConstructorInvocation();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // Java.g:494:44: ( blockStatement )*
            loop82:
            do {
                int alt82=2;
                int LA82_0 = input.LA(1);

                if ( ((LA82_0>=Identifier && LA82_0<=ASSERT)||LA82_0==26||LA82_0==28||(LA82_0>=31 && LA82_0<=37)||LA82_0==44||(LA82_0>=46 && LA82_0<=47)||LA82_0==53||(LA82_0>=56 && LA82_0<=63)||(LA82_0>=65 && LA82_0<=66)||(LA82_0>=69 && LA82_0<=73)||LA82_0==76||(LA82_0>=78 && LA82_0<=81)||(LA82_0>=83 && LA82_0<=87)||(LA82_0>=105 && LA82_0<=106)||(LA82_0>=109 && LA82_0<=113)) ) {
                    alt82=1;
                }


                switch (alt82) {
            	case 1 :
            	    // Java.g:0:0: blockStatement
            	    {
            	    pushFollow(FOLLOW_blockStatement_in_constructorBody2458);
            	    blockStatement();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop82;
                }
            } while (true);

            match(input,45,FOLLOW_45_in_constructorBody2461); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 64, constructorBody_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "constructorBody"


    // $ANTLR start "explicitConstructorInvocation"
    // Java.g:497:1: explicitConstructorInvocation : ( ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';' | primary '.' ( nonWildcardTypeArguments )? 'super' arguments ';' );
    public final void explicitConstructorInvocation() throws RecognitionException {
        int explicitConstructorInvocation_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 65) ) { return ; }
            // Java.g:498:5: ( ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';' | primary '.' ( nonWildcardTypeArguments )? 'super' arguments ';' )
            int alt85=2;
            alt85 = dfa85.predict(input);
            switch (alt85) {
                case 1 :
                    // Java.g:498:9: ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';'
                    {
                    // Java.g:498:9: ( nonWildcardTypeArguments )?
                    int alt83=2;
                    int LA83_0 = input.LA(1);

                    if ( (LA83_0==40) ) {
                        alt83=1;
                    }
                    switch (alt83) {
                        case 1 :
                            // Java.g:0:0: nonWildcardTypeArguments
                            {
                            pushFollow(FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation2480);
                            nonWildcardTypeArguments();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }

                    if ( input.LA(1)==65||input.LA(1)==69 ) {
                        input.consume();
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    pushFollow(FOLLOW_arguments_in_explicitConstructorInvocation2491);
                    arguments();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,26,FOLLOW_26_in_explicitConstructorInvocation2493); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:499:9: primary '.' ( nonWildcardTypeArguments )? 'super' arguments ';'
                    {
                    pushFollow(FOLLOW_primary_in_explicitConstructorInvocation2503);
                    primary();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,29,FOLLOW_29_in_explicitConstructorInvocation2505); if (state.failed) return ;
                    // Java.g:499:21: ( nonWildcardTypeArguments )?
                    int alt84=2;
                    int LA84_0 = input.LA(1);

                    if ( (LA84_0==40) ) {
                        alt84=1;
                    }
                    switch (alt84) {
                        case 1 :
                            // Java.g:0:0: nonWildcardTypeArguments
                            {
                            pushFollow(FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation2507);
                            nonWildcardTypeArguments();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }

                    match(input,65,FOLLOW_65_in_explicitConstructorInvocation2510); if (state.failed) return ;
                    pushFollow(FOLLOW_arguments_in_explicitConstructorInvocation2512);
                    arguments();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,26,FOLLOW_26_in_explicitConstructorInvocation2514); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 65, explicitConstructorInvocation_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "explicitConstructorInvocation"


    // $ANTLR start "qualifiedName"
    // Java.g:503:1: qualifiedName : Identifier ( '.' Identifier )* ;
    public final void qualifiedName() throws RecognitionException {
        int qualifiedName_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 66) ) { return ; }
            // Java.g:504:5: ( Identifier ( '.' Identifier )* )
            // Java.g:504:9: Identifier ( '.' Identifier )*
            {
            match(input,Identifier,FOLLOW_Identifier_in_qualifiedName2534); if (state.failed) return ;
            // Java.g:504:20: ( '.' Identifier )*
            loop86:
            do {
                int alt86=2;
                int LA86_0 = input.LA(1);

                if ( (LA86_0==29) ) {
                    int LA86_2 = input.LA(2);

                    if ( (LA86_2==Identifier) ) {
                        alt86=1;
                    }


                }


                switch (alt86) {
            	case 1 :
            	    // Java.g:504:21: '.' Identifier
            	    {
            	    match(input,29,FOLLOW_29_in_qualifiedName2537); if (state.failed) return ;
            	    match(input,Identifier,FOLLOW_Identifier_in_qualifiedName2539); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop86;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 66, qualifiedName_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "qualifiedName"


    // $ANTLR start "literal"
    // Java.g:507:1: literal : ( integerLiteral | FloatingPointLiteral | CharacterLiteral | StringLiteral | booleanLiteral | 'null' );
    public final void literal() throws RecognitionException {
        int literal_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 67) ) { return ; }
            // Java.g:508:5: ( integerLiteral | FloatingPointLiteral | CharacterLiteral | StringLiteral | booleanLiteral | 'null' )
            int alt87=6;
            switch ( input.LA(1) ) {
            case HexLiteral:
            case OctalLiteral:
            case DecimalLiteral:
                {
                alt87=1;
                }
                break;
            case FloatingPointLiteral:
                {
                alt87=2;
                }
                break;
            case CharacterLiteral:
                {
                alt87=3;
                }
                break;
            case StringLiteral:
                {
                alt87=4;
                }
                break;
            case 71:
            case 72:
                {
                alt87=5;
                }
                break;
            case 70:
                {
                alt87=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 87, 0, input);

                throw nvae;
            }

            switch (alt87) {
                case 1 :
                    // Java.g:508:9: integerLiteral
                    {
                    pushFollow(FOLLOW_integerLiteral_in_literal2565);
                    integerLiteral();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:509:9: FloatingPointLiteral
                    {
                    match(input,FloatingPointLiteral,FOLLOW_FloatingPointLiteral_in_literal2575); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // Java.g:510:9: CharacterLiteral
                    {
                    match(input,CharacterLiteral,FOLLOW_CharacterLiteral_in_literal2585); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // Java.g:511:9: StringLiteral
                    {
                    match(input,StringLiteral,FOLLOW_StringLiteral_in_literal2595); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // Java.g:512:9: booleanLiteral
                    {
                    pushFollow(FOLLOW_booleanLiteral_in_literal2605);
                    booleanLiteral();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // Java.g:513:9: 'null'
                    {
                    match(input,70,FOLLOW_70_in_literal2615); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 67, literal_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "literal"


    // $ANTLR start "integerLiteral"
    // Java.g:516:1: integerLiteral : ( HexLiteral | OctalLiteral | DecimalLiteral );
    public final void integerLiteral() throws RecognitionException {
        int integerLiteral_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 68) ) { return ; }
            // Java.g:517:5: ( HexLiteral | OctalLiteral | DecimalLiteral )
            // Java.g:
            {
            if ( (input.LA(1)>=HexLiteral && input.LA(1)<=DecimalLiteral) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 68, integerLiteral_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "integerLiteral"


    // $ANTLR start "booleanLiteral"
    // Java.g:522:1: booleanLiteral : ( 'true' | 'false' );
    public final void booleanLiteral() throws RecognitionException {
        int booleanLiteral_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 69) ) { return ; }
            // Java.g:523:5: ( 'true' | 'false' )
            // Java.g:
            {
            if ( (input.LA(1)>=71 && input.LA(1)<=72) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 69, booleanLiteral_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "booleanLiteral"


    // $ANTLR start "annotations"
    // Java.g:529:1: annotations : ( annotation )+ ;
    public final void annotations() throws RecognitionException {
        int annotations_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 70) ) { return ; }
            // Java.g:530:5: ( ( annotation )+ )
            // Java.g:530:9: ( annotation )+
            {
            // Java.g:530:9: ( annotation )+
            int cnt88=0;
            loop88:
            do {
                int alt88=2;
                int LA88_0 = input.LA(1);

                if ( (LA88_0==73) ) {
                    int LA88_2 = input.LA(2);

                    if ( (LA88_2==Identifier) ) {
                        int LA88_3 = input.LA(3);

                        if ( (synpred128_Java()) ) {
                            alt88=1;
                        }


                    }


                }


                switch (alt88) {
            	case 1 :
            	    // Java.g:0:0: annotation
            	    {
            	    pushFollow(FOLLOW_annotation_in_annotations2704);
            	    annotation();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt88 >= 1 ) break loop88;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(88, input);
                        throw eee;
                }
                cnt88++;
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 70, annotations_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "annotations"


    // $ANTLR start "annotation"
    // Java.g:533:1: annotation : '@' annotationName ( '(' ( elementValuePairs | elementValue )? ')' )? ;
    public final void annotation() throws RecognitionException {
        int annotation_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 71) ) { return ; }
            // Java.g:534:5: ( '@' annotationName ( '(' ( elementValuePairs | elementValue )? ')' )? )
            // Java.g:534:9: '@' annotationName ( '(' ( elementValuePairs | elementValue )? ')' )?
            {
            match(input,73,FOLLOW_73_in_annotation2724); if (state.failed) return ;
            pushFollow(FOLLOW_annotationName_in_annotation2726);
            annotationName();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:534:28: ( '(' ( elementValuePairs | elementValue )? ')' )?
            int alt90=2;
            int LA90_0 = input.LA(1);

            if ( (LA90_0==66) ) {
                alt90=1;
            }
            switch (alt90) {
                case 1 :
                    // Java.g:534:30: '(' ( elementValuePairs | elementValue )? ')'
                    {
                    match(input,66,FOLLOW_66_in_annotation2730); if (state.failed) return ;
                    // Java.g:534:34: ( elementValuePairs | elementValue )?
                    int alt89=3;
                    int LA89_0 = input.LA(1);

                    if ( (LA89_0==Identifier) ) {
                        int LA89_1 = input.LA(2);

                        if ( (LA89_1==51) ) {
                            alt89=1;
                        }
                        else if ( ((LA89_1>=29 && LA89_1<=30)||LA89_1==40||(LA89_1>=42 && LA89_1<=43)||LA89_1==48||LA89_1==64||(LA89_1>=66 && LA89_1<=67)||(LA89_1>=98 && LA89_1<=110)) ) {
                            alt89=2;
                        }
                    }
                    else if ( ((LA89_0>=FloatingPointLiteral && LA89_0<=DecimalLiteral)||LA89_0==44||LA89_0==47||(LA89_0>=56 && LA89_0<=63)||(LA89_0>=65 && LA89_0<=66)||(LA89_0>=69 && LA89_0<=73)||(LA89_0>=105 && LA89_0<=106)||(LA89_0>=109 && LA89_0<=113)) ) {
                        alt89=2;
                    }
                    switch (alt89) {
                        case 1 :
                            // Java.g:534:36: elementValuePairs
                            {
                            pushFollow(FOLLOW_elementValuePairs_in_annotation2734);
                            elementValuePairs();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // Java.g:534:56: elementValue
                            {
                            pushFollow(FOLLOW_elementValue_in_annotation2738);
                            elementValue();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }

                    match(input,67,FOLLOW_67_in_annotation2743); if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 71, annotation_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "annotation"


    // $ANTLR start "annotationName"
    // Java.g:537:1: annotationName : Identifier ( '.' Identifier )* ;
    public final void annotationName() throws RecognitionException {
        int annotationName_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 72) ) { return ; }
            // Java.g:538:5: ( Identifier ( '.' Identifier )* )
            // Java.g:538:7: Identifier ( '.' Identifier )*
            {
            match(input,Identifier,FOLLOW_Identifier_in_annotationName2767); if (state.failed) return ;
            // Java.g:538:18: ( '.' Identifier )*
            loop91:
            do {
                int alt91=2;
                int LA91_0 = input.LA(1);

                if ( (LA91_0==29) ) {
                    alt91=1;
                }


                switch (alt91) {
            	case 1 :
            	    // Java.g:538:19: '.' Identifier
            	    {
            	    match(input,29,FOLLOW_29_in_annotationName2770); if (state.failed) return ;
            	    match(input,Identifier,FOLLOW_Identifier_in_annotationName2772); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop91;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 72, annotationName_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "annotationName"


    // $ANTLR start "elementValuePairs"
    // Java.g:541:1: elementValuePairs : elementValuePair ( ',' elementValuePair )* ;
    public final void elementValuePairs() throws RecognitionException {
        int elementValuePairs_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 73) ) { return ; }
            // Java.g:542:5: ( elementValuePair ( ',' elementValuePair )* )
            // Java.g:542:9: elementValuePair ( ',' elementValuePair )*
            {
            pushFollow(FOLLOW_elementValuePair_in_elementValuePairs2793);
            elementValuePair();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:542:26: ( ',' elementValuePair )*
            loop92:
            do {
                int alt92=2;
                int LA92_0 = input.LA(1);

                if ( (LA92_0==41) ) {
                    alt92=1;
                }


                switch (alt92) {
            	case 1 :
            	    // Java.g:542:27: ',' elementValuePair
            	    {
            	    match(input,41,FOLLOW_41_in_elementValuePairs2796); if (state.failed) return ;
            	    pushFollow(FOLLOW_elementValuePair_in_elementValuePairs2798);
            	    elementValuePair();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop92;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 73, elementValuePairs_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "elementValuePairs"


    // $ANTLR start "elementValuePair"
    // Java.g:545:1: elementValuePair : Identifier '=' elementValue ;
    public final void elementValuePair() throws RecognitionException {
        int elementValuePair_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 74) ) { return ; }
            // Java.g:546:5: ( Identifier '=' elementValue )
            // Java.g:546:9: Identifier '=' elementValue
            {
            match(input,Identifier,FOLLOW_Identifier_in_elementValuePair2819); if (state.failed) return ;
            match(input,51,FOLLOW_51_in_elementValuePair2821); if (state.failed) return ;
            pushFollow(FOLLOW_elementValue_in_elementValuePair2823);
            elementValue();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 74, elementValuePair_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "elementValuePair"


    // $ANTLR start "elementValue"
    // Java.g:549:1: elementValue : ( conditionalExpression | annotation | elementValueArrayInitializer );
    public final void elementValue() throws RecognitionException {
        int elementValue_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 75) ) { return ; }
            // Java.g:550:5: ( conditionalExpression | annotation | elementValueArrayInitializer )
            int alt93=3;
            switch ( input.LA(1) ) {
            case Identifier:
            case FloatingPointLiteral:
            case CharacterLiteral:
            case StringLiteral:
            case HexLiteral:
            case OctalLiteral:
            case DecimalLiteral:
            case 47:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 65:
            case 66:
            case 69:
            case 70:
            case 71:
            case 72:
            case 105:
            case 106:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
                {
                alt93=1;
                }
                break;
            case 73:
                {
                alt93=2;
                }
                break;
            case 44:
                {
                alt93=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 93, 0, input);

                throw nvae;
            }

            switch (alt93) {
                case 1 :
                    // Java.g:550:9: conditionalExpression
                    {
                    pushFollow(FOLLOW_conditionalExpression_in_elementValue2846);
                    conditionalExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:551:9: annotation
                    {
                    pushFollow(FOLLOW_annotation_in_elementValue2856);
                    annotation();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // Java.g:552:9: elementValueArrayInitializer
                    {
                    pushFollow(FOLLOW_elementValueArrayInitializer_in_elementValue2866);
                    elementValueArrayInitializer();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 75, elementValue_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "elementValue"


    // $ANTLR start "elementValueArrayInitializer"
    // Java.g:555:1: elementValueArrayInitializer : '{' ( elementValue ( ',' elementValue )* )? ( ',' )? '}' ;
    public final void elementValueArrayInitializer() throws RecognitionException {
        int elementValueArrayInitializer_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 76) ) { return ; }
            // Java.g:556:5: ( '{' ( elementValue ( ',' elementValue )* )? ( ',' )? '}' )
            // Java.g:556:9: '{' ( elementValue ( ',' elementValue )* )? ( ',' )? '}'
            {
            match(input,44,FOLLOW_44_in_elementValueArrayInitializer2889); if (state.failed) return ;
            // Java.g:556:13: ( elementValue ( ',' elementValue )* )?
            int alt95=2;
            int LA95_0 = input.LA(1);

            if ( (LA95_0==Identifier||(LA95_0>=FloatingPointLiteral && LA95_0<=DecimalLiteral)||LA95_0==44||LA95_0==47||(LA95_0>=56 && LA95_0<=63)||(LA95_0>=65 && LA95_0<=66)||(LA95_0>=69 && LA95_0<=73)||(LA95_0>=105 && LA95_0<=106)||(LA95_0>=109 && LA95_0<=113)) ) {
                alt95=1;
            }
            switch (alt95) {
                case 1 :
                    // Java.g:556:14: elementValue ( ',' elementValue )*
                    {
                    pushFollow(FOLLOW_elementValue_in_elementValueArrayInitializer2892);
                    elementValue();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:556:27: ( ',' elementValue )*
                    loop94:
                    do {
                        int alt94=2;
                        int LA94_0 = input.LA(1);

                        if ( (LA94_0==41) ) {
                            int LA94_1 = input.LA(2);

                            if ( (LA94_1==Identifier||(LA94_1>=FloatingPointLiteral && LA94_1<=DecimalLiteral)||LA94_1==44||LA94_1==47||(LA94_1>=56 && LA94_1<=63)||(LA94_1>=65 && LA94_1<=66)||(LA94_1>=69 && LA94_1<=73)||(LA94_1>=105 && LA94_1<=106)||(LA94_1>=109 && LA94_1<=113)) ) {
                                alt94=1;
                            }


                        }


                        switch (alt94) {
                    	case 1 :
                    	    // Java.g:556:28: ',' elementValue
                    	    {
                    	    match(input,41,FOLLOW_41_in_elementValueArrayInitializer2895); if (state.failed) return ;
                    	    pushFollow(FOLLOW_elementValue_in_elementValueArrayInitializer2897);
                    	    elementValue();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop94;
                        }
                    } while (true);


                    }
                    break;

            }

            // Java.g:556:49: ( ',' )?
            int alt96=2;
            int LA96_0 = input.LA(1);

            if ( (LA96_0==41) ) {
                alt96=1;
            }
            switch (alt96) {
                case 1 :
                    // Java.g:556:50: ','
                    {
                    match(input,41,FOLLOW_41_in_elementValueArrayInitializer2904); if (state.failed) return ;

                    }
                    break;

            }

            match(input,45,FOLLOW_45_in_elementValueArrayInitializer2908); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 76, elementValueArrayInitializer_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "elementValueArrayInitializer"


    // $ANTLR start "annotationTypeDeclaration"
    // Java.g:559:1: annotationTypeDeclaration : '@' 'interface' Identifier annotationTypeBody ;
    public final void annotationTypeDeclaration() throws RecognitionException {
        int annotationTypeDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 77) ) { return ; }
            // Java.g:560:5: ( '@' 'interface' Identifier annotationTypeBody )
            // Java.g:560:9: '@' 'interface' Identifier annotationTypeBody
            {
            match(input,73,FOLLOW_73_in_annotationTypeDeclaration2931); if (state.failed) return ;
            match(input,46,FOLLOW_46_in_annotationTypeDeclaration2933); if (state.failed) return ;
            match(input,Identifier,FOLLOW_Identifier_in_annotationTypeDeclaration2935); if (state.failed) return ;
            pushFollow(FOLLOW_annotationTypeBody_in_annotationTypeDeclaration2937);
            annotationTypeBody();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 77, annotationTypeDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "annotationTypeDeclaration"


    // $ANTLR start "annotationTypeBody"
    // Java.g:563:1: annotationTypeBody : '{' ( annotationTypeElementDeclaration )* '}' ;
    public final void annotationTypeBody() throws RecognitionException {
        int annotationTypeBody_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 78) ) { return ; }
            // Java.g:564:5: ( '{' ( annotationTypeElementDeclaration )* '}' )
            // Java.g:564:9: '{' ( annotationTypeElementDeclaration )* '}'
            {
            match(input,44,FOLLOW_44_in_annotationTypeBody2960); if (state.failed) return ;
            // Java.g:564:13: ( annotationTypeElementDeclaration )*
            loop97:
            do {
                int alt97=2;
                int LA97_0 = input.LA(1);

                if ( ((LA97_0>=Identifier && LA97_0<=ENUM)||LA97_0==28||(LA97_0>=31 && LA97_0<=37)||LA97_0==40||(LA97_0>=46 && LA97_0<=47)||(LA97_0>=52 && LA97_0<=63)||LA97_0==73) ) {
                    alt97=1;
                }


                switch (alt97) {
            	case 1 :
            	    // Java.g:564:14: annotationTypeElementDeclaration
            	    {
            	    pushFollow(FOLLOW_annotationTypeElementDeclaration_in_annotationTypeBody2963);
            	    annotationTypeElementDeclaration();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop97;
                }
            } while (true);

            match(input,45,FOLLOW_45_in_annotationTypeBody2967); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 78, annotationTypeBody_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "annotationTypeBody"


    // $ANTLR start "annotationTypeElementDeclaration"
    // Java.g:567:1: annotationTypeElementDeclaration : modifiers annotationTypeElementRest ;
    public final void annotationTypeElementDeclaration() throws RecognitionException {
        int annotationTypeElementDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 79) ) { return ; }
            // Java.g:568:5: ( modifiers annotationTypeElementRest )
            // Java.g:568:9: modifiers annotationTypeElementRest
            {
            pushFollow(FOLLOW_modifiers_in_annotationTypeElementDeclaration2990);
            modifiers();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_annotationTypeElementRest_in_annotationTypeElementDeclaration2992);
            annotationTypeElementRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 79, annotationTypeElementDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "annotationTypeElementDeclaration"


    // $ANTLR start "annotationTypeElementRest"
    // Java.g:571:1: annotationTypeElementRest : ( type annotationMethodOrConstantRest ';' | normalClassDeclaration ( ';' )? | normalInterfaceDeclaration ( ';' )? | enumDeclaration ( ';' )? | annotationTypeDeclaration ( ';' )? );
    public final void annotationTypeElementRest() throws RecognitionException {
        int annotationTypeElementRest_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 80) ) { return ; }
            // Java.g:572:5: ( type annotationMethodOrConstantRest ';' | normalClassDeclaration ( ';' )? | normalInterfaceDeclaration ( ';' )? | enumDeclaration ( ';' )? | annotationTypeDeclaration ( ';' )? )
            int alt102=5;
            switch ( input.LA(1) ) {
            case Identifier:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
                {
                alt102=1;
                }
                break;
            case 37:
                {
                alt102=2;
                }
                break;
            case 46:
                {
                alt102=3;
                }
                break;
            case ENUM:
                {
                alt102=4;
                }
                break;
            case 73:
                {
                alt102=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 102, 0, input);

                throw nvae;
            }

            switch (alt102) {
                case 1 :
                    // Java.g:572:9: type annotationMethodOrConstantRest ';'
                    {
                    pushFollow(FOLLOW_type_in_annotationTypeElementRest3015);
                    type();

                    state._fsp--;
                    if (state.failed) return ;
                    pushFollow(FOLLOW_annotationMethodOrConstantRest_in_annotationTypeElementRest3017);
                    annotationMethodOrConstantRest();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,26,FOLLOW_26_in_annotationTypeElementRest3019); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:573:9: normalClassDeclaration ( ';' )?
                    {
                    pushFollow(FOLLOW_normalClassDeclaration_in_annotationTypeElementRest3029);
                    normalClassDeclaration();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:573:32: ( ';' )?
                    int alt98=2;
                    int LA98_0 = input.LA(1);

                    if ( (LA98_0==26) ) {
                        alt98=1;
                    }
                    switch (alt98) {
                        case 1 :
                            // Java.g:0:0: ';'
                            {
                            match(input,26,FOLLOW_26_in_annotationTypeElementRest3031); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // Java.g:574:9: normalInterfaceDeclaration ( ';' )?
                    {
                    pushFollow(FOLLOW_normalInterfaceDeclaration_in_annotationTypeElementRest3042);
                    normalInterfaceDeclaration();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:574:36: ( ';' )?
                    int alt99=2;
                    int LA99_0 = input.LA(1);

                    if ( (LA99_0==26) ) {
                        alt99=1;
                    }
                    switch (alt99) {
                        case 1 :
                            // Java.g:0:0: ';'
                            {
                            match(input,26,FOLLOW_26_in_annotationTypeElementRest3044); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 4 :
                    // Java.g:575:9: enumDeclaration ( ';' )?
                    {
                    pushFollow(FOLLOW_enumDeclaration_in_annotationTypeElementRest3055);
                    enumDeclaration();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:575:25: ( ';' )?
                    int alt100=2;
                    int LA100_0 = input.LA(1);

                    if ( (LA100_0==26) ) {
                        alt100=1;
                    }
                    switch (alt100) {
                        case 1 :
                            // Java.g:0:0: ';'
                            {
                            match(input,26,FOLLOW_26_in_annotationTypeElementRest3057); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 5 :
                    // Java.g:576:9: annotationTypeDeclaration ( ';' )?
                    {
                    pushFollow(FOLLOW_annotationTypeDeclaration_in_annotationTypeElementRest3068);
                    annotationTypeDeclaration();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:576:35: ( ';' )?
                    int alt101=2;
                    int LA101_0 = input.LA(1);

                    if ( (LA101_0==26) ) {
                        alt101=1;
                    }
                    switch (alt101) {
                        case 1 :
                            // Java.g:0:0: ';'
                            {
                            match(input,26,FOLLOW_26_in_annotationTypeElementRest3070); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 80, annotationTypeElementRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "annotationTypeElementRest"


    // $ANTLR start "annotationMethodOrConstantRest"
    // Java.g:579:1: annotationMethodOrConstantRest : ( annotationMethodRest | annotationConstantRest );
    public final void annotationMethodOrConstantRest() throws RecognitionException {
        int annotationMethodOrConstantRest_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 81) ) { return ; }
            // Java.g:580:5: ( annotationMethodRest | annotationConstantRest )
            int alt103=2;
            int LA103_0 = input.LA(1);

            if ( (LA103_0==Identifier) ) {
                int LA103_1 = input.LA(2);

                if ( (LA103_1==66) ) {
                    alt103=1;
                }
                else if ( (LA103_1==26||LA103_1==41||LA103_1==48||LA103_1==51) ) {
                    alt103=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 103, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 103, 0, input);

                throw nvae;
            }
            switch (alt103) {
                case 1 :
                    // Java.g:580:9: annotationMethodRest
                    {
                    pushFollow(FOLLOW_annotationMethodRest_in_annotationMethodOrConstantRest3094);
                    annotationMethodRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:581:9: annotationConstantRest
                    {
                    pushFollow(FOLLOW_annotationConstantRest_in_annotationMethodOrConstantRest3104);
                    annotationConstantRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 81, annotationMethodOrConstantRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "annotationMethodOrConstantRest"


    // $ANTLR start "annotationMethodRest"
    // Java.g:584:1: annotationMethodRest : Identifier '(' ')' ( defaultValue )? ;
    public final void annotationMethodRest() throws RecognitionException {
        int annotationMethodRest_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 82) ) { return ; }
            // Java.g:585:5: ( Identifier '(' ')' ( defaultValue )? )
            // Java.g:585:9: Identifier '(' ')' ( defaultValue )?
            {
            match(input,Identifier,FOLLOW_Identifier_in_annotationMethodRest3127); if (state.failed) return ;
            match(input,66,FOLLOW_66_in_annotationMethodRest3129); if (state.failed) return ;
            match(input,67,FOLLOW_67_in_annotationMethodRest3131); if (state.failed) return ;
            // Java.g:585:28: ( defaultValue )?
            int alt104=2;
            int LA104_0 = input.LA(1);

            if ( (LA104_0==74) ) {
                alt104=1;
            }
            switch (alt104) {
                case 1 :
                    // Java.g:0:0: defaultValue
                    {
                    pushFollow(FOLLOW_defaultValue_in_annotationMethodRest3133);
                    defaultValue();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 82, annotationMethodRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "annotationMethodRest"


    // $ANTLR start "annotationConstantRest"
    // Java.g:588:1: annotationConstantRest : variableDeclarators ;
    public final void annotationConstantRest() throws RecognitionException {
        int annotationConstantRest_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 83) ) { return ; }
            // Java.g:589:5: ( variableDeclarators )
            // Java.g:589:9: variableDeclarators
            {
            pushFollow(FOLLOW_variableDeclarators_in_annotationConstantRest3157);
            variableDeclarators();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 83, annotationConstantRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "annotationConstantRest"


    // $ANTLR start "defaultValue"
    // Java.g:592:1: defaultValue : 'default' elementValue ;
    public final void defaultValue() throws RecognitionException {
        int defaultValue_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 84) ) { return ; }
            // Java.g:593:5: ( 'default' elementValue )
            // Java.g:593:9: 'default' elementValue
            {
            match(input,74,FOLLOW_74_in_defaultValue3180); if (state.failed) return ;
            pushFollow(FOLLOW_elementValue_in_defaultValue3182);
            elementValue();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 84, defaultValue_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "defaultValue"


    // $ANTLR start "block"
    // Java.g:598:1: block : '{' ( blockStatement )* '}' ;
    public final int block() throws RecognitionException {
        int block_StartIndex = input.index();
        int endLine = 0;
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 85) ) { return 0; }
            // Java.g:599:5: ( '{' ( blockStatement )* '}' )
            // Java.g:599:9: '{' ( blockStatement )* '}'
            {
            match(input,44,FOLLOW_44_in_block3203); if (state.failed) return 0;
            // Java.g:599:13: ( blockStatement )*
            loop105:
            do {
                int alt105=2;
                int LA105_0 = input.LA(1);

                if ( ((LA105_0>=Identifier && LA105_0<=ASSERT)||LA105_0==26||LA105_0==28||(LA105_0>=31 && LA105_0<=37)||LA105_0==44||(LA105_0>=46 && LA105_0<=47)||LA105_0==53||(LA105_0>=56 && LA105_0<=63)||(LA105_0>=65 && LA105_0<=66)||(LA105_0>=69 && LA105_0<=73)||LA105_0==76||(LA105_0>=78 && LA105_0<=81)||(LA105_0>=83 && LA105_0<=87)||(LA105_0>=105 && LA105_0<=106)||(LA105_0>=109 && LA105_0<=113)) ) {
                    alt105=1;
                }


                switch (alt105) {
            	case 1 :
            	    // Java.g:0:0: blockStatement
            	    {
            	    pushFollow(FOLLOW_blockStatement_in_block3205);
            	    blockStatement();

            	    state._fsp--;
            	    if (state.failed) return 0;

            	    }
            	    break;

            	default :
            	    break loop105;
                }
            } while (true);
            
            
            endLine = input.get(input.index()).getLine();
        	
            match(input,45,FOLLOW_45_in_block3208); if (state.failed) return endLine;
            
            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        	if ( state.backtracking>0 ) { memoize(input, 85, block_StartIndex); }
        }
        
        return endLine;
    }
    // $ANTLR end "block"


    // $ANTLR start "blockStatement"
    // Java.g:602:1: blockStatement : ( localVariableDeclarationStatement | classOrInterfaceDeclaration | statement );
    public final void blockStatement() throws RecognitionException {
        int blockStatement_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 86) ) { return ; }
            // Java.g:603:5: ( localVariableDeclarationStatement | classOrInterfaceDeclaration | statement )
            int alt106=3;
            alt106 = dfa106.predict(input);
            switch (alt106) {
                case 1 :
                    // Java.g:603:9: localVariableDeclarationStatement
                    {
                    pushFollow(FOLLOW_localVariableDeclarationStatement_in_blockStatement3231);
                    localVariableDeclarationStatement();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:604:9: classOrInterfaceDeclaration
                    {
                    pushFollow(FOLLOW_classOrInterfaceDeclaration_in_blockStatement3241);
                    classOrInterfaceDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // Java.g:605:9: statement
                    {
                    pushFollow(FOLLOW_statement_in_blockStatement3251);
                    statement();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 86, blockStatement_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "blockStatement"


    // $ANTLR start "localVariableDeclarationStatement"
    // Java.g:608:1: localVariableDeclarationStatement : localVariableDeclaration ';' ;
    public final void localVariableDeclarationStatement() throws RecognitionException {
        int localVariableDeclarationStatement_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 87) ) { return ; }
            // Java.g:609:5: ( localVariableDeclaration ';' )
            // Java.g:609:10: localVariableDeclaration ';'
            {
            pushFollow(FOLLOW_localVariableDeclaration_in_localVariableDeclarationStatement3275);
            localVariableDeclaration();

            state._fsp--;
            if (state.failed) return ;
            match(input,26,FOLLOW_26_in_localVariableDeclarationStatement3277); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 87, localVariableDeclarationStatement_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "localVariableDeclarationStatement"


    // $ANTLR start "localVariableDeclaration"
    // Java.g:612:1: localVariableDeclaration : variableModifiers type variableDeclarators ;
    public final void localVariableDeclaration() throws RecognitionException {
        int localVariableDeclaration_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 88) ) { return ; }
            // Java.g:613:5: ( variableModifiers type variableDeclarators )
            // Java.g:613:9: variableModifiers type variableDeclarators
            {
            pushFollow(FOLLOW_variableModifiers_in_localVariableDeclaration3296);
            variableModifiers();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_type_in_localVariableDeclaration3298);
            type();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_variableDeclarators_in_localVariableDeclaration3300);
            variableDeclarators();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 88, localVariableDeclaration_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "localVariableDeclaration"


    // $ANTLR start "variableModifiers"
    // Java.g:616:1: variableModifiers : ( variableModifier )* ;
    public final void variableModifiers() throws RecognitionException {
        int variableModifiers_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 89) ) { return ; }
            // Java.g:617:5: ( ( variableModifier )* )
            // Java.g:617:9: ( variableModifier )*
            {
            // Java.g:617:9: ( variableModifier )*
            loop107:
            do {
                int alt107=2;
                int LA107_0 = input.LA(1);

                if ( (LA107_0==35||LA107_0==73) ) {
                    alt107=1;
                }


                switch (alt107) {
            	case 1 :
            	    // Java.g:0:0: variableModifier
            	    {
            	    pushFollow(FOLLOW_variableModifier_in_variableModifiers3323);
            	    variableModifier();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop107;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 89, variableModifiers_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "variableModifiers"


    // $ANTLR start "statement"
    // Java.g:620:1: statement : ( block | ASSERT expression ( ':' expression )? ';' | 'if' parExpression statement ( options {k=1; } : 'else' statement )? | 'for' '(' forControl ')' statement | 'while' parExpression statement | 'do' statement 'while' parExpression ';' | 'try' block ( catches 'finally' block | catches | 'finally' block ) | 'switch' parExpression '{' switchBlockStatementGroups '}' | 'synchronized' parExpression block | 'return' ( expression )? ';' | 'throw' expression ';' | 'break' ( Identifier )? ';' | 'continue' ( Identifier )? ';' | ';' | statementExpression ';' | Identifier ':' statement );
    public final void statement() throws RecognitionException {
        int statement_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 90) ) { return ; }
            // Java.g:621:5: ( block | ASSERT expression ( ':' expression )? ';' | 'if' parExpression statement ( options {k=1; } : 'else' statement )? | 'for' '(' forControl ')' statement | 'while' parExpression statement | 'do' statement 'while' parExpression ';' | 'try' block ( catches 'finally' block | catches | 'finally' block ) | 'switch' parExpression '{' switchBlockStatementGroups '}' | 'synchronized' parExpression block | 'return' ( expression )? ';' | 'throw' expression ';' | 'break' ( Identifier )? ';' | 'continue' ( Identifier )? ';' | ';' | statementExpression ';' | Identifier ':' statement )
            int alt114=16;
            alt114 = dfa114.predict(input);
            switch (alt114) {
                case 1 :
                    // Java.g:621:7: block
                    {
                    pushFollow(FOLLOW_block_in_statement3341);
                    block();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:622:9: ASSERT expression ( ':' expression )? ';'
                    {
                    match(input,ASSERT,FOLLOW_ASSERT_in_statement3351); if (state.failed) return ;
                    pushFollow(FOLLOW_expression_in_statement3353);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:622:27: ( ':' expression )?
                    int alt108=2;
                    int LA108_0 = input.LA(1);

                    if ( (LA108_0==75) ) {
                        alt108=1;
                    }
                    switch (alt108) {
                        case 1 :
                            // Java.g:622:28: ':' expression
                            {
                            match(input,75,FOLLOW_75_in_statement3356); if (state.failed) return ;
                            pushFollow(FOLLOW_expression_in_statement3358);
                            expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }

                    match(input,26,FOLLOW_26_in_statement3362); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // Java.g:623:9: 'if' parExpression statement ( options {k=1; } : 'else' statement )?
                    {
                    match(input,76,FOLLOW_76_in_statement3372); if (state.failed) return ;
                    pushFollow(FOLLOW_parExpression_in_statement3374);
                    parExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    pushFollow(FOLLOW_statement_in_statement3376);
                    statement();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:623:38: ( options {k=1; } : 'else' statement )?
                    int alt109=2;
                    int LA109_0 = input.LA(1);

                    if ( (LA109_0==77) ) {
                        int LA109_2 = input.LA(2);

                        if ( (synpred157_Java()) ) {
                            alt109=1;
                        }
                    }
                    switch (alt109) {
                        case 1 :
                            // Java.g:623:54: 'else' statement
                            {
                            match(input,77,FOLLOW_77_in_statement3386); if (state.failed) return ;
                            pushFollow(FOLLOW_statement_in_statement3388);
                            statement();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 4 :
                    // Java.g:624:9: 'for' '(' forControl ')' statement
                    {
                    match(input,78,FOLLOW_78_in_statement3400); if (state.failed) return ;
                    match(input,66,FOLLOW_66_in_statement3402); if (state.failed) return ;
                    pushFollow(FOLLOW_forControl_in_statement3404);
                    forControl();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,67,FOLLOW_67_in_statement3406); if (state.failed) return ;
                    pushFollow(FOLLOW_statement_in_statement3408);
                    statement();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // Java.g:625:9: 'while' parExpression statement
                    {
                    match(input,79,FOLLOW_79_in_statement3418); if (state.failed) return ;
                    pushFollow(FOLLOW_parExpression_in_statement3420);
                    parExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    pushFollow(FOLLOW_statement_in_statement3422);
                    statement();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // Java.g:626:9: 'do' statement 'while' parExpression ';'
                    {
                    match(input,80,FOLLOW_80_in_statement3432); if (state.failed) return ;
                    pushFollow(FOLLOW_statement_in_statement3434);
                    statement();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,79,FOLLOW_79_in_statement3436); if (state.failed) return ;
                    pushFollow(FOLLOW_parExpression_in_statement3438);
                    parExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,26,FOLLOW_26_in_statement3440); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    // Java.g:627:9: 'try' block ( catches 'finally' block | catches | 'finally' block )
                    {
                    match(input,81,FOLLOW_81_in_statement3450); if (state.failed) return ;
                    pushFollow(FOLLOW_block_in_statement3452);
                    block();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:628:9: ( catches 'finally' block | catches | 'finally' block )
                    int alt110=3;
                    int LA110_0 = input.LA(1);

                    if ( (LA110_0==88) ) {
                        int LA110_1 = input.LA(2);

                        if ( (synpred162_Java()) ) {
                            alt110=1;
                        }
                        else if ( (synpred163_Java()) ) {
                            alt110=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 110, 1, input);

                            throw nvae;
                        }
                    }
                    else if ( (LA110_0==82) ) {
                        alt110=3;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 110, 0, input);

                        throw nvae;
                    }
                    switch (alt110) {
                        case 1 :
                            // Java.g:628:11: catches 'finally' block
                            {
                            pushFollow(FOLLOW_catches_in_statement3464);
                            catches();

                            state._fsp--;
                            if (state.failed) return ;
                            match(input,82,FOLLOW_82_in_statement3466); if (state.failed) return ;
                            pushFollow(FOLLOW_block_in_statement3468);
                            block();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // Java.g:629:11: catches
                            {
                            pushFollow(FOLLOW_catches_in_statement3480);
                            catches();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // Java.g:630:13: 'finally' block
                            {
                            match(input,82,FOLLOW_82_in_statement3494); if (state.failed) return ;
                            pushFollow(FOLLOW_block_in_statement3496);
                            block();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 8 :
                    // Java.g:632:9: 'switch' parExpression '{' switchBlockStatementGroups '}'
                    {
                    match(input,83,FOLLOW_83_in_statement3516); if (state.failed) return ;
                    pushFollow(FOLLOW_parExpression_in_statement3518);
                    parExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,44,FOLLOW_44_in_statement3520); if (state.failed) return ;
                    pushFollow(FOLLOW_switchBlockStatementGroups_in_statement3522);
                    switchBlockStatementGroups();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,45,FOLLOW_45_in_statement3524); if (state.failed) return ;

                    }
                    break;
                case 9 :
                    // Java.g:633:9: 'synchronized' parExpression block
                    {
                    match(input,53,FOLLOW_53_in_statement3534); if (state.failed) return ;
                    pushFollow(FOLLOW_parExpression_in_statement3536);
                    parExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    pushFollow(FOLLOW_block_in_statement3538);
                    block();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 10 :
                    // Java.g:634:9: 'return' ( expression )? ';'
                    {
                    match(input,84,FOLLOW_84_in_statement3548); if (state.failed) return ;
                    // Java.g:634:18: ( expression )?
                    int alt111=2;
                    int LA111_0 = input.LA(1);

                    if ( (LA111_0==Identifier||(LA111_0>=FloatingPointLiteral && LA111_0<=DecimalLiteral)||LA111_0==47||(LA111_0>=56 && LA111_0<=63)||(LA111_0>=65 && LA111_0<=66)||(LA111_0>=69 && LA111_0<=72)||(LA111_0>=105 && LA111_0<=106)||(LA111_0>=109 && LA111_0<=113)) ) {
                        alt111=1;
                    }
                    switch (alt111) {
                        case 1 :
                            // Java.g:0:0: expression
                            {
                            pushFollow(FOLLOW_expression_in_statement3550);
                            expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }

                    match(input,26,FOLLOW_26_in_statement3553); if (state.failed) return ;

                    }
                    break;
                case 11 :
                    // Java.g:635:9: 'throw' expression ';'
                    {
                    match(input,85,FOLLOW_85_in_statement3563); if (state.failed) return ;
                    pushFollow(FOLLOW_expression_in_statement3565);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,26,FOLLOW_26_in_statement3567); if (state.failed) return ;

                    }
                    break;
                case 12 :
                    // Java.g:636:9: 'break' ( Identifier )? ';'
                    {
                    match(input,86,FOLLOW_86_in_statement3577); if (state.failed) return ;
                    // Java.g:636:17: ( Identifier )?
                    int alt112=2;
                    int LA112_0 = input.LA(1);

                    if ( (LA112_0==Identifier) ) {
                        alt112=1;
                    }
                    switch (alt112) {
                        case 1 :
                            // Java.g:0:0: Identifier
                            {
                            match(input,Identifier,FOLLOW_Identifier_in_statement3579); if (state.failed) return ;

                            }
                            break;

                    }

                    match(input,26,FOLLOW_26_in_statement3582); if (state.failed) return ;

                    }
                    break;
                case 13 :
                    // Java.g:637:9: 'continue' ( Identifier )? ';'
                    {
                    match(input,87,FOLLOW_87_in_statement3592); if (state.failed) return ;
                    // Java.g:637:20: ( Identifier )?
                    int alt113=2;
                    int LA113_0 = input.LA(1);

                    if ( (LA113_0==Identifier) ) {
                        alt113=1;
                    }
                    switch (alt113) {
                        case 1 :
                            // Java.g:0:0: Identifier
                            {
                            match(input,Identifier,FOLLOW_Identifier_in_statement3594); if (state.failed) return ;

                            }
                            break;

                    }

                    match(input,26,FOLLOW_26_in_statement3597); if (state.failed) return ;

                    }
                    break;
                case 14 :
                    // Java.g:638:9: ';'
                    {
                    match(input,26,FOLLOW_26_in_statement3607); if (state.failed) return ;

                    }
                    break;
                case 15 :
                    // Java.g:639:9: statementExpression ';'
                    {
                    pushFollow(FOLLOW_statementExpression_in_statement3618);
                    statementExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,26,FOLLOW_26_in_statement3620); if (state.failed) return ;

                    }
                    break;
                case 16 :
                    // Java.g:640:9: Identifier ':' statement
                    {
                    match(input,Identifier,FOLLOW_Identifier_in_statement3630); if (state.failed) return ;
                    match(input,75,FOLLOW_75_in_statement3632); if (state.failed) return ;
                    pushFollow(FOLLOW_statement_in_statement3634);
                    statement();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 90, statement_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "statement"


    // $ANTLR start "catches"
    // Java.g:643:1: catches : catchClause ( catchClause )* ;
    public final void catches() throws RecognitionException {
        int catches_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 91) ) { return ; }
            // Java.g:644:5: ( catchClause ( catchClause )* )
            // Java.g:644:9: catchClause ( catchClause )*
            {
            pushFollow(FOLLOW_catchClause_in_catches3657);
            catchClause();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:644:21: ( catchClause )*
            loop115:
            do {
                int alt115=2;
                int LA115_0 = input.LA(1);

                if ( (LA115_0==88) ) {
                    alt115=1;
                }


                switch (alt115) {
            	case 1 :
            	    // Java.g:644:22: catchClause
            	    {
            	    pushFollow(FOLLOW_catchClause_in_catches3660);
            	    catchClause();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop115;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 91, catches_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "catches"


    // $ANTLR start "catchClause"
    // Java.g:647:1: catchClause : 'catch' '(' formalParameter ')' block ;
    public final void catchClause() throws RecognitionException {
        int catchClause_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 92) ) { return ; }
            // Java.g:648:5: ( 'catch' '(' formalParameter ')' block )
            // Java.g:648:9: 'catch' '(' formalParameter ')' block
            {
            match(input,88,FOLLOW_88_in_catchClause3685); if (state.failed) return ;
            match(input,66,FOLLOW_66_in_catchClause3687); if (state.failed) return ;
            pushFollow(FOLLOW_formalParameter_in_catchClause3689);
            formalParameter();

            state._fsp--;
            if (state.failed) return ;
            match(input,67,FOLLOW_67_in_catchClause3691); if (state.failed) return ;
            pushFollow(FOLLOW_block_in_catchClause3693);
            block();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 92, catchClause_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "catchClause"


    // $ANTLR start "formalParameter"
    // Java.g:651:1: formalParameter : variableModifiers type variableDeclaratorId ;
    public final void formalParameter() throws RecognitionException {
        int formalParameter_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 93) ) { return ; }
            // Java.g:652:5: ( variableModifiers type variableDeclaratorId )
            // Java.g:652:9: variableModifiers type variableDeclaratorId
            {
            pushFollow(FOLLOW_variableModifiers_in_formalParameter3712);
            variableModifiers();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_type_in_formalParameter3714);
            type();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_variableDeclaratorId_in_formalParameter3716);
            variableDeclaratorId();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 93, formalParameter_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "formalParameter"


    // $ANTLR start "switchBlockStatementGroups"
    // Java.g:655:1: switchBlockStatementGroups : ( switchBlockStatementGroup )* ;
    public final void switchBlockStatementGroups() throws RecognitionException {
        int switchBlockStatementGroups_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 94) ) { return ; }
            // Java.g:656:5: ( ( switchBlockStatementGroup )* )
            // Java.g:656:9: ( switchBlockStatementGroup )*
            {
            // Java.g:656:9: ( switchBlockStatementGroup )*
            loop116:
            do {
                int alt116=2;
                int LA116_0 = input.LA(1);

                if ( (LA116_0==74||LA116_0==89) ) {
                    alt116=1;
                }


                switch (alt116) {
            	case 1 :
            	    // Java.g:656:10: switchBlockStatementGroup
            	    {
            	    pushFollow(FOLLOW_switchBlockStatementGroup_in_switchBlockStatementGroups3744);
            	    switchBlockStatementGroup();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop116;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 94, switchBlockStatementGroups_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "switchBlockStatementGroups"


    // $ANTLR start "switchBlockStatementGroup"
    // Java.g:663:1: switchBlockStatementGroup : ( switchLabel )+ ( blockStatement )* ;
    public final void switchBlockStatementGroup() throws RecognitionException {
        int switchBlockStatementGroup_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 95) ) { return ; }
            // Java.g:664:5: ( ( switchLabel )+ ( blockStatement )* )
            // Java.g:664:9: ( switchLabel )+ ( blockStatement )*
            {
            // Java.g:664:9: ( switchLabel )+
            int cnt117=0;
            loop117:
            do {
                int alt117=2;
                int LA117_0 = input.LA(1);

                if ( (LA117_0==89) ) {
                    int LA117_2 = input.LA(2);

                    if ( (synpred178_Java()) ) {
                        alt117=1;
                    }


                }
                else if ( (LA117_0==74) ) {
                    int LA117_3 = input.LA(2);

                    if ( (synpred178_Java()) ) {
                        alt117=1;
                    }


                }


                switch (alt117) {
            	case 1 :
            	    // Java.g:0:0: switchLabel
            	    {
            	    pushFollow(FOLLOW_switchLabel_in_switchBlockStatementGroup3771);
            	    switchLabel();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt117 >= 1 ) break loop117;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(117, input);
                        throw eee;
                }
                cnt117++;
            } while (true);

            // Java.g:664:22: ( blockStatement )*
            loop118:
            do {
                int alt118=2;
                int LA118_0 = input.LA(1);

                if ( ((LA118_0>=Identifier && LA118_0<=ASSERT)||LA118_0==26||LA118_0==28||(LA118_0>=31 && LA118_0<=37)||LA118_0==44||(LA118_0>=46 && LA118_0<=47)||LA118_0==53||(LA118_0>=56 && LA118_0<=63)||(LA118_0>=65 && LA118_0<=66)||(LA118_0>=69 && LA118_0<=73)||LA118_0==76||(LA118_0>=78 && LA118_0<=81)||(LA118_0>=83 && LA118_0<=87)||(LA118_0>=105 && LA118_0<=106)||(LA118_0>=109 && LA118_0<=113)) ) {
                    alt118=1;
                }


                switch (alt118) {
            	case 1 :
            	    // Java.g:0:0: blockStatement
            	    {
            	    pushFollow(FOLLOW_blockStatement_in_switchBlockStatementGroup3774);
            	    blockStatement();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop118;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 95, switchBlockStatementGroup_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "switchBlockStatementGroup"


    // $ANTLR start "switchLabel"
    // Java.g:667:1: switchLabel : ( 'case' constantExpression ':' | 'case' enumConstantName ':' | 'default' ':' );
    public final void switchLabel() throws RecognitionException {
        int switchLabel_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 96) ) { return ; }
            // Java.g:668:5: ( 'case' constantExpression ':' | 'case' enumConstantName ':' | 'default' ':' )
            int alt119=3;
            int LA119_0 = input.LA(1);

            if ( (LA119_0==89) ) {
                int LA119_1 = input.LA(2);

                if ( ((LA119_1>=FloatingPointLiteral && LA119_1<=DecimalLiteral)||LA119_1==47||(LA119_1>=56 && LA119_1<=63)||(LA119_1>=65 && LA119_1<=66)||(LA119_1>=69 && LA119_1<=72)||(LA119_1>=105 && LA119_1<=106)||(LA119_1>=109 && LA119_1<=113)) ) {
                    alt119=1;
                }
                else if ( (LA119_1==Identifier) ) {
                    int LA119_4 = input.LA(3);

                    if ( ((LA119_4>=29 && LA119_4<=30)||LA119_4==40||(LA119_4>=42 && LA119_4<=43)||LA119_4==48||LA119_4==51||LA119_4==64||LA119_4==66||(LA119_4>=90 && LA119_4<=110)) ) {
                        alt119=1;
                    }
                    else if ( (LA119_4==75) ) {
                        int LA119_5 = input.LA(4);

                        if ( (synpred180_Java()) ) {
                            alt119=1;
                        }
                        else if ( (synpred181_Java()) ) {
                            alt119=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 119, 5, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 119, 4, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 119, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA119_0==74) ) {
                alt119=3;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 119, 0, input);

                throw nvae;
            }
            switch (alt119) {
                case 1 :
                    // Java.g:668:9: 'case' constantExpression ':'
                    {
                    match(input,89,FOLLOW_89_in_switchLabel3798); if (state.failed) return ;
                    pushFollow(FOLLOW_constantExpression_in_switchLabel3800);
                    constantExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,75,FOLLOW_75_in_switchLabel3802); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:669:9: 'case' enumConstantName ':'
                    {
                    match(input,89,FOLLOW_89_in_switchLabel3812); if (state.failed) return ;
                    pushFollow(FOLLOW_enumConstantName_in_switchLabel3814);
                    enumConstantName();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,75,FOLLOW_75_in_switchLabel3816); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // Java.g:670:9: 'default' ':'
                    {
                    match(input,74,FOLLOW_74_in_switchLabel3826); if (state.failed) return ;
                    match(input,75,FOLLOW_75_in_switchLabel3828); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 96, switchLabel_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "switchLabel"


    // $ANTLR start "forControl"
    // Java.g:673:1: forControl options {k=3; } : ( enhancedForControl | ( forInit )? ';' ( expression )? ';' ( forUpdate )? );
    public final void forControl() throws RecognitionException {
        int forControl_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 97) ) { return ; }
            // Java.g:675:5: ( enhancedForControl | ( forInit )? ';' ( expression )? ';' ( forUpdate )? )
            int alt123=2;
            alt123 = dfa123.predict(input);
            switch (alt123) {
                case 1 :
                    // Java.g:675:9: enhancedForControl
                    {
                    pushFollow(FOLLOW_enhancedForControl_in_forControl3859);
                    enhancedForControl();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:676:9: ( forInit )? ';' ( expression )? ';' ( forUpdate )?
                    {
                    // Java.g:676:9: ( forInit )?
                    int alt120=2;
                    int LA120_0 = input.LA(1);

                    if ( (LA120_0==Identifier||(LA120_0>=FloatingPointLiteral && LA120_0<=DecimalLiteral)||LA120_0==35||LA120_0==47||(LA120_0>=56 && LA120_0<=63)||(LA120_0>=65 && LA120_0<=66)||(LA120_0>=69 && LA120_0<=73)||(LA120_0>=105 && LA120_0<=106)||(LA120_0>=109 && LA120_0<=113)) ) {
                        alt120=1;
                    }
                    switch (alt120) {
                        case 1 :
                            // Java.g:0:0: forInit
                            {
                            pushFollow(FOLLOW_forInit_in_forControl3869);
                            forInit();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }

                    match(input,26,FOLLOW_26_in_forControl3872); if (state.failed) return ;
                    // Java.g:676:22: ( expression )?
                    int alt121=2;
                    int LA121_0 = input.LA(1);

                    if ( (LA121_0==Identifier||(LA121_0>=FloatingPointLiteral && LA121_0<=DecimalLiteral)||LA121_0==47||(LA121_0>=56 && LA121_0<=63)||(LA121_0>=65 && LA121_0<=66)||(LA121_0>=69 && LA121_0<=72)||(LA121_0>=105 && LA121_0<=106)||(LA121_0>=109 && LA121_0<=113)) ) {
                        alt121=1;
                    }
                    switch (alt121) {
                        case 1 :
                            // Java.g:0:0: expression
                            {
                            pushFollow(FOLLOW_expression_in_forControl3874);
                            expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }

                    match(input,26,FOLLOW_26_in_forControl3877); if (state.failed) return ;
                    // Java.g:676:38: ( forUpdate )?
                    int alt122=2;
                    int LA122_0 = input.LA(1);

                    if ( (LA122_0==Identifier||(LA122_0>=FloatingPointLiteral && LA122_0<=DecimalLiteral)||LA122_0==47||(LA122_0>=56 && LA122_0<=63)||(LA122_0>=65 && LA122_0<=66)||(LA122_0>=69 && LA122_0<=72)||(LA122_0>=105 && LA122_0<=106)||(LA122_0>=109 && LA122_0<=113)) ) {
                        alt122=1;
                    }
                    switch (alt122) {
                        case 1 :
                            // Java.g:0:0: forUpdate
                            {
                            pushFollow(FOLLOW_forUpdate_in_forControl3879);
                            forUpdate();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 97, forControl_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "forControl"


    // $ANTLR start "forInit"
    // Java.g:679:1: forInit : ( localVariableDeclaration | expressionList );
    public final void forInit() throws RecognitionException {
        int forInit_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 98) ) { return ; }
            // Java.g:680:5: ( localVariableDeclaration | expressionList )
            int alt124=2;
            alt124 = dfa124.predict(input);
            switch (alt124) {
                case 1 :
                    // Java.g:680:9: localVariableDeclaration
                    {
                    pushFollow(FOLLOW_localVariableDeclaration_in_forInit3899);
                    localVariableDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:681:9: expressionList
                    {
                    pushFollow(FOLLOW_expressionList_in_forInit3909);
                    expressionList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 98, forInit_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "forInit"


    // $ANTLR start "enhancedForControl"
    // Java.g:684:1: enhancedForControl : variableModifiers type Identifier ':' expression ;
    public final void enhancedForControl() throws RecognitionException {
        int enhancedForControl_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 99) ) { return ; }
            // Java.g:685:5: ( variableModifiers type Identifier ':' expression )
            // Java.g:685:9: variableModifiers type Identifier ':' expression
            {
            pushFollow(FOLLOW_variableModifiers_in_enhancedForControl3932);
            variableModifiers();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_type_in_enhancedForControl3934);
            type();

            state._fsp--;
            if (state.failed) return ;
            match(input,Identifier,FOLLOW_Identifier_in_enhancedForControl3936); if (state.failed) return ;
            match(input,75,FOLLOW_75_in_enhancedForControl3938); if (state.failed) return ;
            pushFollow(FOLLOW_expression_in_enhancedForControl3940);
            expression();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 99, enhancedForControl_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "enhancedForControl"


    // $ANTLR start "forUpdate"
    // Java.g:688:1: forUpdate : expressionList ;
    public final void forUpdate() throws RecognitionException {
        int forUpdate_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 100) ) { return ; }
            // Java.g:689:5: ( expressionList )
            // Java.g:689:9: expressionList
            {
            pushFollow(FOLLOW_expressionList_in_forUpdate3959);
            expressionList();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 100, forUpdate_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "forUpdate"


    // $ANTLR start "parExpression"
    // Java.g:694:1: parExpression : '(' expression ')' ;
    public final void parExpression() throws RecognitionException {
        int parExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 101) ) { return ; }
            // Java.g:695:5: ( '(' expression ')' )
            // Java.g:695:9: '(' expression ')'
            {
            match(input,66,FOLLOW_66_in_parExpression3980); if (state.failed) return ;
            pushFollow(FOLLOW_expression_in_parExpression3982);
            expression();

            state._fsp--;
            if (state.failed) return ;
            match(input,67,FOLLOW_67_in_parExpression3984); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 101, parExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "parExpression"


    // $ANTLR start "expressionList"
    // Java.g:698:1: expressionList : expression ( ',' expression )* ;
    public final void expressionList() throws RecognitionException {
        int expressionList_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 102) ) { return ; }
            // Java.g:699:5: ( expression ( ',' expression )* )
            // Java.g:699:9: expression ( ',' expression )*
            {
            pushFollow(FOLLOW_expression_in_expressionList4007);
            expression();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:699:20: ( ',' expression )*
            loop125:
            do {
                int alt125=2;
                int LA125_0 = input.LA(1);

                if ( (LA125_0==41) ) {
                    alt125=1;
                }


                switch (alt125) {
            	case 1 :
            	    // Java.g:699:21: ',' expression
            	    {
            	    match(input,41,FOLLOW_41_in_expressionList4010); if (state.failed) return ;
            	    pushFollow(FOLLOW_expression_in_expressionList4012);
            	    expression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop125;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 102, expressionList_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "expressionList"


    // $ANTLR start "statementExpression"
    // Java.g:702:1: statementExpression : expression ;
    public final void statementExpression() throws RecognitionException {
        int statementExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 103) ) { return ; }
            // Java.g:703:5: ( expression )
            // Java.g:703:9: expression
            {
            pushFollow(FOLLOW_expression_in_statementExpression4033);
            expression();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 103, statementExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "statementExpression"


    // $ANTLR start "constantExpression"
    // Java.g:706:1: constantExpression : expression ;
    public final void constantExpression() throws RecognitionException {
        int constantExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 104) ) { return ; }
            // Java.g:707:5: ( expression )
            // Java.g:707:9: expression
            {
            pushFollow(FOLLOW_expression_in_constantExpression4056);
            expression();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 104, constantExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "constantExpression"


    // $ANTLR start "expression"
    // Java.g:710:1: expression : conditionalExpression ( assignmentOperator expression )? ;
    public final void expression() throws RecognitionException {
        int expression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 105) ) { return ; }
            // Java.g:711:5: ( conditionalExpression ( assignmentOperator expression )? )
            // Java.g:711:9: conditionalExpression ( assignmentOperator expression )?
            {
            pushFollow(FOLLOW_conditionalExpression_in_expression4079);
            conditionalExpression();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:711:31: ( assignmentOperator expression )?
            int alt126=2;
            int LA126_0 = input.LA(1);

            if ( (LA126_0==40||LA126_0==42||LA126_0==51||(LA126_0>=90 && LA126_0<=97)) ) {
                alt126=1;
            }
            switch (alt126) {
                case 1 :
                    // Java.g:711:32: assignmentOperator expression
                    {
                    pushFollow(FOLLOW_assignmentOperator_in_expression4082);
                    assignmentOperator();

                    state._fsp--;
                    if (state.failed) return ;
                    pushFollow(FOLLOW_expression_in_expression4084);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 105, expression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "expression"


    // $ANTLR start "assignmentOperator"
    // Java.g:714:1: assignmentOperator : ( '=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '%=' | ( '<' '<' '=' )=>t1= '<' t2= '<' t3= '=' {...}? | ( '>' '>' '>' '=' )=>t1= '>' t2= '>' t3= '>' t4= '=' {...}? | ( '>' '>' '=' )=>t1= '>' t2= '>' t3= '=' {...}?);
    public final void assignmentOperator() throws RecognitionException {
        int assignmentOperator_StartIndex = input.index();
        Token t1=null;
        Token t2=null;
        Token t3=null;
        Token t4=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 106) ) { return ; }
            // Java.g:715:5: ( '=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '%=' | ( '<' '<' '=' )=>t1= '<' t2= '<' t3= '=' {...}? | ( '>' '>' '>' '=' )=>t1= '>' t2= '>' t3= '>' t4= '=' {...}? | ( '>' '>' '=' )=>t1= '>' t2= '>' t3= '=' {...}?)
            int alt127=12;
            alt127 = dfa127.predict(input);
            switch (alt127) {
                case 1 :
                    // Java.g:715:9: '='
                    {
                    match(input,51,FOLLOW_51_in_assignmentOperator4109); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:716:9: '+='
                    {
                    match(input,90,FOLLOW_90_in_assignmentOperator4119); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // Java.g:717:9: '-='
                    {
                    match(input,91,FOLLOW_91_in_assignmentOperator4129); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // Java.g:718:9: '*='
                    {
                    match(input,92,FOLLOW_92_in_assignmentOperator4139); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // Java.g:719:9: '/='
                    {
                    match(input,93,FOLLOW_93_in_assignmentOperator4149); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // Java.g:720:9: '&='
                    {
                    match(input,94,FOLLOW_94_in_assignmentOperator4159); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    // Java.g:721:9: '|='
                    {
                    match(input,95,FOLLOW_95_in_assignmentOperator4169); if (state.failed) return ;

                    }
                    break;
                case 8 :
                    // Java.g:722:9: '^='
                    {
                    match(input,96,FOLLOW_96_in_assignmentOperator4179); if (state.failed) return ;

                    }
                    break;
                case 9 :
                    // Java.g:723:9: '%='
                    {
                    match(input,97,FOLLOW_97_in_assignmentOperator4189); if (state.failed) return ;

                    }
                    break;
                case 10 :
                    // Java.g:724:9: ( '<' '<' '=' )=>t1= '<' t2= '<' t3= '=' {...}?
                    {
                    t1=(Token)match(input,40,FOLLOW_40_in_assignmentOperator4210); if (state.failed) return ;
                    t2=(Token)match(input,40,FOLLOW_40_in_assignmentOperator4214); if (state.failed) return ;
                    t3=(Token)match(input,51,FOLLOW_51_in_assignmentOperator4218); if (state.failed) return ;
                    if ( !(( t1.getLine() == t2.getLine() &&
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() && 
                              t2.getLine() == t3.getLine() && 
                              t2.getCharPositionInLine() + 1 == t3.getCharPositionInLine() )) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "assignmentOperator", " $t1.getLine() == $t2.getLine() &&\n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() && \n          $t2.getLine() == $t3.getLine() && \n          $t2.getCharPositionInLine() + 1 == $t3.getCharPositionInLine() ");
                    }

                    }
                    break;
                case 11 :
                    // Java.g:729:9: ( '>' '>' '>' '=' )=>t1= '>' t2= '>' t3= '>' t4= '=' {...}?
                    {
                    t1=(Token)match(input,42,FOLLOW_42_in_assignmentOperator4252); if (state.failed) return ;
                    t2=(Token)match(input,42,FOLLOW_42_in_assignmentOperator4256); if (state.failed) return ;
                    t3=(Token)match(input,42,FOLLOW_42_in_assignmentOperator4260); if (state.failed) return ;
                    t4=(Token)match(input,51,FOLLOW_51_in_assignmentOperator4264); if (state.failed) return ;
                    if ( !(( t1.getLine() == t2.getLine() && 
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() &&
                              t2.getLine() == t3.getLine() && 
                              t2.getCharPositionInLine() + 1 == t3.getCharPositionInLine() &&
                              t3.getLine() == t4.getLine() && 
                              t3.getCharPositionInLine() + 1 == t4.getCharPositionInLine() )) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "assignmentOperator", " $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() &&\n          $t2.getLine() == $t3.getLine() && \n          $t2.getCharPositionInLine() + 1 == $t3.getCharPositionInLine() &&\n          $t3.getLine() == $t4.getLine() && \n          $t3.getCharPositionInLine() + 1 == $t4.getCharPositionInLine() ");
                    }

                    }
                    break;
                case 12 :
                    // Java.g:736:9: ( '>' '>' '=' )=>t1= '>' t2= '>' t3= '=' {...}?
                    {
                    t1=(Token)match(input,42,FOLLOW_42_in_assignmentOperator4295); if (state.failed) return ;
                    t2=(Token)match(input,42,FOLLOW_42_in_assignmentOperator4299); if (state.failed) return ;
                    t3=(Token)match(input,51,FOLLOW_51_in_assignmentOperator4303); if (state.failed) return ;
                    if ( !(( t1.getLine() == t2.getLine() && 
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() && 
                              t2.getLine() == t3.getLine() && 
                              t2.getCharPositionInLine() + 1 == t3.getCharPositionInLine() )) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "assignmentOperator", " $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() && \n          $t2.getLine() == $t3.getLine() && \n          $t2.getCharPositionInLine() + 1 == $t3.getCharPositionInLine() ");
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 106, assignmentOperator_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "assignmentOperator"


    // $ANTLR start "conditionalExpression"
    // Java.g:743:1: conditionalExpression : conditionalOrExpression ( '?' conditionalExpression ':' conditionalExpression )? ;
    public final void conditionalExpression() throws RecognitionException {
        int conditionalExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 107) ) { return ; }
            // Java.g:744:5: ( conditionalOrExpression ( '?' conditionalExpression ':' conditionalExpression )? )
            // Java.g:744:9: conditionalOrExpression ( '?' conditionalExpression ':' conditionalExpression )?
            {
            pushFollow(FOLLOW_conditionalOrExpression_in_conditionalExpression4332);
            conditionalOrExpression();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:744:33: ( '?' conditionalExpression ':' conditionalExpression )?
            int alt128=2;
            int LA128_0 = input.LA(1);

            if ( (LA128_0==64) ) {
                alt128=1;
            }
            switch (alt128) {
                case 1 :
                    // Java.g:744:35: '?' conditionalExpression ':' conditionalExpression
                    {
                    match(input,64,FOLLOW_64_in_conditionalExpression4336); if (state.failed) return ;
                    pushFollow(FOLLOW_conditionalExpression_in_conditionalExpression4338);
                    conditionalExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,75,FOLLOW_75_in_conditionalExpression4340); if (state.failed) return ;
                    pushFollow(FOLLOW_conditionalExpression_in_conditionalExpression4342);
                    conditionalExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 107, conditionalExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "conditionalExpression"


    // $ANTLR start "conditionalOrExpression"
    // Java.g:747:1: conditionalOrExpression : conditionalAndExpression ( '||' conditionalAndExpression )* ;
    public final void conditionalOrExpression() throws RecognitionException {
        int conditionalOrExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 108) ) { return ; }
            // Java.g:748:5: ( conditionalAndExpression ( '||' conditionalAndExpression )* )
            // Java.g:748:9: conditionalAndExpression ( '||' conditionalAndExpression )*
            {
            pushFollow(FOLLOW_conditionalAndExpression_in_conditionalOrExpression4364);
            conditionalAndExpression();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:748:34: ( '||' conditionalAndExpression )*
            loop129:
            do {
                int alt129=2;
                int LA129_0 = input.LA(1);

                if ( (LA129_0==98) ) {
                    alt129=1;
                }


                switch (alt129) {
            	case 1 :
            	    // Java.g:748:36: '||' conditionalAndExpression
            	    {
            	    match(input,98,FOLLOW_98_in_conditionalOrExpression4368); if (state.failed) return ;
            	    pushFollow(FOLLOW_conditionalAndExpression_in_conditionalOrExpression4370);
            	    conditionalAndExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop129;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 108, conditionalOrExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "conditionalOrExpression"


    // $ANTLR start "conditionalAndExpression"
    // Java.g:751:1: conditionalAndExpression : inclusiveOrExpression ( '&&' inclusiveOrExpression )* ;
    public final void conditionalAndExpression() throws RecognitionException {
        int conditionalAndExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 109) ) { return ; }
            // Java.g:752:5: ( inclusiveOrExpression ( '&&' inclusiveOrExpression )* )
            // Java.g:752:9: inclusiveOrExpression ( '&&' inclusiveOrExpression )*
            {
            pushFollow(FOLLOW_inclusiveOrExpression_in_conditionalAndExpression4392);
            inclusiveOrExpression();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:752:31: ( '&&' inclusiveOrExpression )*
            loop130:
            do {
                int alt130=2;
                int LA130_0 = input.LA(1);

                if ( (LA130_0==99) ) {
                    alt130=1;
                }


                switch (alt130) {
            	case 1 :
            	    // Java.g:752:33: '&&' inclusiveOrExpression
            	    {
            	    match(input,99,FOLLOW_99_in_conditionalAndExpression4396); if (state.failed) return ;
            	    pushFollow(FOLLOW_inclusiveOrExpression_in_conditionalAndExpression4398);
            	    inclusiveOrExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop130;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 109, conditionalAndExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "conditionalAndExpression"


    // $ANTLR start "inclusiveOrExpression"
    // Java.g:755:1: inclusiveOrExpression : exclusiveOrExpression ( '|' exclusiveOrExpression )* ;
    public final void inclusiveOrExpression() throws RecognitionException {
        int inclusiveOrExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 110) ) { return ; }
            // Java.g:756:5: ( exclusiveOrExpression ( '|' exclusiveOrExpression )* )
            // Java.g:756:9: exclusiveOrExpression ( '|' exclusiveOrExpression )*
            {
            pushFollow(FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression4420);
            exclusiveOrExpression();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:756:31: ( '|' exclusiveOrExpression )*
            loop131:
            do {
                int alt131=2;
                int LA131_0 = input.LA(1);

                if ( (LA131_0==100) ) {
                    alt131=1;
                }


                switch (alt131) {
            	case 1 :
            	    // Java.g:756:33: '|' exclusiveOrExpression
            	    {
            	    match(input,100,FOLLOW_100_in_inclusiveOrExpression4424); if (state.failed) return ;
            	    pushFollow(FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression4426);
            	    exclusiveOrExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop131;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 110, inclusiveOrExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "inclusiveOrExpression"


    // $ANTLR start "exclusiveOrExpression"
    // Java.g:759:1: exclusiveOrExpression : andExpression ( '^' andExpression )* ;
    public final void exclusiveOrExpression() throws RecognitionException {
        int exclusiveOrExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 111) ) { return ; }
            // Java.g:760:5: ( andExpression ( '^' andExpression )* )
            // Java.g:760:9: andExpression ( '^' andExpression )*
            {
            pushFollow(FOLLOW_andExpression_in_exclusiveOrExpression4448);
            andExpression();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:760:23: ( '^' andExpression )*
            loop132:
            do {
                int alt132=2;
                int LA132_0 = input.LA(1);

                if ( (LA132_0==101) ) {
                    alt132=1;
                }


                switch (alt132) {
            	case 1 :
            	    // Java.g:760:25: '^' andExpression
            	    {
            	    match(input,101,FOLLOW_101_in_exclusiveOrExpression4452); if (state.failed) return ;
            	    pushFollow(FOLLOW_andExpression_in_exclusiveOrExpression4454);
            	    andExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop132;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 111, exclusiveOrExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "exclusiveOrExpression"


    // $ANTLR start "andExpression"
    // Java.g:763:1: andExpression : equalityExpression ( '&' equalityExpression )* ;
    public final void andExpression() throws RecognitionException {
        int andExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 112) ) { return ; }
            // Java.g:764:5: ( equalityExpression ( '&' equalityExpression )* )
            // Java.g:764:9: equalityExpression ( '&' equalityExpression )*
            {
            pushFollow(FOLLOW_equalityExpression_in_andExpression4476);
            equalityExpression();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:764:28: ( '&' equalityExpression )*
            loop133:
            do {
                int alt133=2;
                int LA133_0 = input.LA(1);

                if ( (LA133_0==43) ) {
                    alt133=1;
                }


                switch (alt133) {
            	case 1 :
            	    // Java.g:764:30: '&' equalityExpression
            	    {
            	    match(input,43,FOLLOW_43_in_andExpression4480); if (state.failed) return ;
            	    pushFollow(FOLLOW_equalityExpression_in_andExpression4482);
            	    equalityExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop133;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 112, andExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "andExpression"


    // $ANTLR start "equalityExpression"
    // Java.g:767:1: equalityExpression : instanceOfExpression ( ( '==' | '!=' ) instanceOfExpression )* ;
    public final void equalityExpression() throws RecognitionException {
        int equalityExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 113) ) { return ; }
            // Java.g:768:5: ( instanceOfExpression ( ( '==' | '!=' ) instanceOfExpression )* )
            // Java.g:768:9: instanceOfExpression ( ( '==' | '!=' ) instanceOfExpression )*
            {
            pushFollow(FOLLOW_instanceOfExpression_in_equalityExpression4504);
            instanceOfExpression();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:768:30: ( ( '==' | '!=' ) instanceOfExpression )*
            loop134:
            do {
                int alt134=2;
                int LA134_0 = input.LA(1);

                if ( ((LA134_0>=102 && LA134_0<=103)) ) {
                    alt134=1;
                }


                switch (alt134) {
            	case 1 :
            	    // Java.g:768:32: ( '==' | '!=' ) instanceOfExpression
            	    {
            	    if ( (input.LA(1)>=102 && input.LA(1)<=103) ) {
            	        input.consume();
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_instanceOfExpression_in_equalityExpression4516);
            	    instanceOfExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop134;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 113, equalityExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "equalityExpression"


    // $ANTLR start "instanceOfExpression"
    // Java.g:771:1: instanceOfExpression : relationalExpression ( 'instanceof' type )? ;
    public final void instanceOfExpression() throws RecognitionException {
        int instanceOfExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 114) ) { return ; }
            // Java.g:772:5: ( relationalExpression ( 'instanceof' type )? )
            // Java.g:772:9: relationalExpression ( 'instanceof' type )?
            {
            pushFollow(FOLLOW_relationalExpression_in_instanceOfExpression4538);
            relationalExpression();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:772:30: ( 'instanceof' type )?
            int alt135=2;
            int LA135_0 = input.LA(1);

            if ( (LA135_0==104) ) {
                alt135=1;
            }
            switch (alt135) {
                case 1 :
                    // Java.g:772:31: 'instanceof' type
                    {
                    match(input,104,FOLLOW_104_in_instanceOfExpression4541); if (state.failed) return ;
                    pushFollow(FOLLOW_type_in_instanceOfExpression4543);
                    type();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 114, instanceOfExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "instanceOfExpression"


    // $ANTLR start "relationalExpression"
    // Java.g:775:1: relationalExpression : shiftExpression ( relationalOp shiftExpression )* ;
    public final void relationalExpression() throws RecognitionException {
        int relationalExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 115) ) { return ; }
            // Java.g:776:5: ( shiftExpression ( relationalOp shiftExpression )* )
            // Java.g:776:9: shiftExpression ( relationalOp shiftExpression )*
            {
            pushFollow(FOLLOW_shiftExpression_in_relationalExpression4564);
            shiftExpression();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:776:25: ( relationalOp shiftExpression )*
            loop136:
            do {
                int alt136=2;
                int LA136_0 = input.LA(1);

                if ( (LA136_0==40) ) {
                    int LA136_2 = input.LA(2);

                    if ( (LA136_2==Identifier||(LA136_2>=FloatingPointLiteral && LA136_2<=DecimalLiteral)||LA136_2==47||LA136_2==51||(LA136_2>=56 && LA136_2<=63)||(LA136_2>=65 && LA136_2<=66)||(LA136_2>=69 && LA136_2<=72)||(LA136_2>=105 && LA136_2<=106)||(LA136_2>=109 && LA136_2<=113)) ) {
                        alt136=1;
                    }


                }
                else if ( (LA136_0==42) ) {
                    int LA136_3 = input.LA(2);

                    if ( (LA136_3==Identifier||(LA136_3>=FloatingPointLiteral && LA136_3<=DecimalLiteral)||LA136_3==47||LA136_3==51||(LA136_3>=56 && LA136_3<=63)||(LA136_3>=65 && LA136_3<=66)||(LA136_3>=69 && LA136_3<=72)||(LA136_3>=105 && LA136_3<=106)||(LA136_3>=109 && LA136_3<=113)) ) {
                        alt136=1;
                    }


                }


                switch (alt136) {
            	case 1 :
            	    // Java.g:776:27: relationalOp shiftExpression
            	    {
            	    pushFollow(FOLLOW_relationalOp_in_relationalExpression4568);
            	    relationalOp();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    pushFollow(FOLLOW_shiftExpression_in_relationalExpression4570);
            	    shiftExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop136;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 115, relationalExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "relationalExpression"


    // $ANTLR start "relationalOp"
    // Java.g:779:1: relationalOp : ( ( '<' '=' )=>t1= '<' t2= '=' {...}? | ( '>' '=' )=>t1= '>' t2= '=' {...}? | '<' | '>' );
    public final void relationalOp() throws RecognitionException {
        int relationalOp_StartIndex = input.index();
        Token t1=null;
        Token t2=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 116) ) { return ; }
            // Java.g:780:5: ( ( '<' '=' )=>t1= '<' t2= '=' {...}? | ( '>' '=' )=>t1= '>' t2= '=' {...}? | '<' | '>' )
            int alt137=4;
            int LA137_0 = input.LA(1);

            if ( (LA137_0==40) ) {
                int LA137_1 = input.LA(2);

                if ( (LA137_1==51) && (synpred211_Java())) {
                    alt137=1;
                }
                else if ( (LA137_1==Identifier||(LA137_1>=FloatingPointLiteral && LA137_1<=DecimalLiteral)||LA137_1==47||(LA137_1>=56 && LA137_1<=63)||(LA137_1>=65 && LA137_1<=66)||(LA137_1>=69 && LA137_1<=72)||(LA137_1>=105 && LA137_1<=106)||(LA137_1>=109 && LA137_1<=113)) ) {
                    alt137=3;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 137, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA137_0==42) ) {
                int LA137_2 = input.LA(2);

                if ( (LA137_2==51) && (synpred212_Java())) {
                    alt137=2;
                }
                else if ( (LA137_2==Identifier||(LA137_2>=FloatingPointLiteral && LA137_2<=DecimalLiteral)||LA137_2==47||(LA137_2>=56 && LA137_2<=63)||(LA137_2>=65 && LA137_2<=66)||(LA137_2>=69 && LA137_2<=72)||(LA137_2>=105 && LA137_2<=106)||(LA137_2>=109 && LA137_2<=113)) ) {
                    alt137=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 137, 2, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 137, 0, input);

                throw nvae;
            }
            switch (alt137) {
                case 1 :
                    // Java.g:780:9: ( '<' '=' )=>t1= '<' t2= '=' {...}?
                    {
                    t1=(Token)match(input,40,FOLLOW_40_in_relationalOp4605); if (state.failed) return ;
                    t2=(Token)match(input,51,FOLLOW_51_in_relationalOp4609); if (state.failed) return ;
                    if ( !(( t1.getLine() == t2.getLine() && 
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() )) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "relationalOp", " $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() ");
                    }

                    }
                    break;
                case 2 :
                    // Java.g:783:9: ( '>' '=' )=>t1= '>' t2= '=' {...}?
                    {
                    t1=(Token)match(input,42,FOLLOW_42_in_relationalOp4639); if (state.failed) return ;
                    t2=(Token)match(input,51,FOLLOW_51_in_relationalOp4643); if (state.failed) return ;
                    if ( !(( t1.getLine() == t2.getLine() && 
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() )) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "relationalOp", " $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() ");
                    }

                    }
                    break;
                case 3 :
                    // Java.g:786:9: '<'
                    {
                    match(input,40,FOLLOW_40_in_relationalOp4664); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // Java.g:787:9: '>'
                    {
                    match(input,42,FOLLOW_42_in_relationalOp4675); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 116, relationalOp_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "relationalOp"


    // $ANTLR start "shiftExpression"
    // Java.g:790:1: shiftExpression : additiveExpression ( shiftOp additiveExpression )* ;
    public final void shiftExpression() throws RecognitionException {
        int shiftExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 117) ) { return ; }
            // Java.g:791:5: ( additiveExpression ( shiftOp additiveExpression )* )
            // Java.g:791:9: additiveExpression ( shiftOp additiveExpression )*
            {
            pushFollow(FOLLOW_additiveExpression_in_shiftExpression4695);
            additiveExpression();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:791:28: ( shiftOp additiveExpression )*
            loop138:
            do {
                int alt138=2;
                int LA138_0 = input.LA(1);

                if ( (LA138_0==40) ) {
                    int LA138_1 = input.LA(2);

                    if ( (LA138_1==40) ) {
                        int LA138_4 = input.LA(3);

                        if ( (LA138_4==Identifier||(LA138_4>=FloatingPointLiteral && LA138_4<=DecimalLiteral)||LA138_4==47||(LA138_4>=56 && LA138_4<=63)||(LA138_4>=65 && LA138_4<=66)||(LA138_4>=69 && LA138_4<=72)||(LA138_4>=105 && LA138_4<=106)||(LA138_4>=109 && LA138_4<=113)) ) {
                            alt138=1;
                        }


                    }


                }
                else if ( (LA138_0==42) ) {
                    int LA138_2 = input.LA(2);

                    if ( (LA138_2==42) ) {
                        int LA138_5 = input.LA(3);

                        if ( (LA138_5==42) ) {
                            int LA138_7 = input.LA(4);

                            if ( (LA138_7==Identifier||(LA138_7>=FloatingPointLiteral && LA138_7<=DecimalLiteral)||LA138_7==47||(LA138_7>=56 && LA138_7<=63)||(LA138_7>=65 && LA138_7<=66)||(LA138_7>=69 && LA138_7<=72)||(LA138_7>=105 && LA138_7<=106)||(LA138_7>=109 && LA138_7<=113)) ) {
                                alt138=1;
                            }


                        }
                        else if ( (LA138_5==Identifier||(LA138_5>=FloatingPointLiteral && LA138_5<=DecimalLiteral)||LA138_5==47||(LA138_5>=56 && LA138_5<=63)||(LA138_5>=65 && LA138_5<=66)||(LA138_5>=69 && LA138_5<=72)||(LA138_5>=105 && LA138_5<=106)||(LA138_5>=109 && LA138_5<=113)) ) {
                            alt138=1;
                        }


                    }


                }


                switch (alt138) {
            	case 1 :
            	    // Java.g:791:30: shiftOp additiveExpression
            	    {
            	    pushFollow(FOLLOW_shiftOp_in_shiftExpression4699);
            	    shiftOp();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    pushFollow(FOLLOW_additiveExpression_in_shiftExpression4701);
            	    additiveExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop138;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 117, shiftExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "shiftExpression"


    // $ANTLR start "shiftOp"
    // Java.g:794:1: shiftOp : ( ( '<' '<' )=>t1= '<' t2= '<' {...}? | ( '>' '>' '>' )=>t1= '>' t2= '>' t3= '>' {...}? | ( '>' '>' )=>t1= '>' t2= '>' {...}?);
    public final void shiftOp() throws RecognitionException {
        int shiftOp_StartIndex = input.index();
        Token t1=null;
        Token t2=null;
        Token t3=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 118) ) { return ; }
            // Java.g:795:5: ( ( '<' '<' )=>t1= '<' t2= '<' {...}? | ( '>' '>' '>' )=>t1= '>' t2= '>' t3= '>' {...}? | ( '>' '>' )=>t1= '>' t2= '>' {...}?)
            int alt139=3;
            alt139 = dfa139.predict(input);
            switch (alt139) {
                case 1 :
                    // Java.g:795:9: ( '<' '<' )=>t1= '<' t2= '<' {...}?
                    {
                    t1=(Token)match(input,40,FOLLOW_40_in_shiftOp4732); if (state.failed) return ;
                    t2=(Token)match(input,40,FOLLOW_40_in_shiftOp4736); if (state.failed) return ;
                    if ( !(( t1.getLine() == t2.getLine() && 
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() )) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "shiftOp", " $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() ");
                    }

                    }
                    break;
                case 2 :
                    // Java.g:798:9: ( '>' '>' '>' )=>t1= '>' t2= '>' t3= '>' {...}?
                    {
                    t1=(Token)match(input,42,FOLLOW_42_in_shiftOp4768); if (state.failed) return ;
                    t2=(Token)match(input,42,FOLLOW_42_in_shiftOp4772); if (state.failed) return ;
                    t3=(Token)match(input,42,FOLLOW_42_in_shiftOp4776); if (state.failed) return ;
                    if ( !(( t1.getLine() == t2.getLine() && 
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() &&
                              t2.getLine() == t3.getLine() && 
                              t2.getCharPositionInLine() + 1 == t3.getCharPositionInLine() )) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "shiftOp", " $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() &&\n          $t2.getLine() == $t3.getLine() && \n          $t2.getCharPositionInLine() + 1 == $t3.getCharPositionInLine() ");
                    }

                    }
                    break;
                case 3 :
                    // Java.g:803:9: ( '>' '>' )=>t1= '>' t2= '>' {...}?
                    {
                    t1=(Token)match(input,42,FOLLOW_42_in_shiftOp4806); if (state.failed) return ;
                    t2=(Token)match(input,42,FOLLOW_42_in_shiftOp4810); if (state.failed) return ;
                    if ( !(( t1.getLine() == t2.getLine() && 
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() )) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "shiftOp", " $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() ");
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 118, shiftOp_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "shiftOp"


    // $ANTLR start "additiveExpression"
    // Java.g:809:1: additiveExpression : multiplicativeExpression ( ( '+' | '-' ) multiplicativeExpression )* ;
    public final void additiveExpression() throws RecognitionException {
        int additiveExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 119) ) { return ; }
            // Java.g:810:5: ( multiplicativeExpression ( ( '+' | '-' ) multiplicativeExpression )* )
            // Java.g:810:9: multiplicativeExpression ( ( '+' | '-' ) multiplicativeExpression )*
            {
            pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression4840);
            multiplicativeExpression();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:810:34: ( ( '+' | '-' ) multiplicativeExpression )*
            loop140:
            do {
                int alt140=2;
                int LA140_0 = input.LA(1);

                if ( ((LA140_0>=105 && LA140_0<=106)) ) {
                    alt140=1;
                }


                switch (alt140) {
            	case 1 :
            	    // Java.g:810:36: ( '+' | '-' ) multiplicativeExpression
            	    {
            	    if ( (input.LA(1)>=105 && input.LA(1)<=106) ) {
            	        input.consume();
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression4852);
            	    multiplicativeExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop140;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 119, additiveExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "additiveExpression"


    // $ANTLR start "multiplicativeExpression"
    // Java.g:813:1: multiplicativeExpression : unaryExpression ( ( '*' | '/' | '%' ) unaryExpression )* ;
    public final void multiplicativeExpression() throws RecognitionException {
        int multiplicativeExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 120) ) { return ; }
            // Java.g:814:5: ( unaryExpression ( ( '*' | '/' | '%' ) unaryExpression )* )
            // Java.g:814:9: unaryExpression ( ( '*' | '/' | '%' ) unaryExpression )*
            {
            pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression4874);
            unaryExpression();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:814:25: ( ( '*' | '/' | '%' ) unaryExpression )*
            loop141:
            do {
                int alt141=2;
                int LA141_0 = input.LA(1);

                if ( (LA141_0==30||(LA141_0>=107 && LA141_0<=108)) ) {
                    alt141=1;
                }


                switch (alt141) {
            	case 1 :
            	    // Java.g:814:27: ( '*' | '/' | '%' ) unaryExpression
            	    {
            	    if ( input.LA(1)==30||(input.LA(1)>=107 && input.LA(1)<=108) ) {
            	        input.consume();
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression4892);
            	    unaryExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop141;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 120, multiplicativeExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "multiplicativeExpression"


    // $ANTLR start "unaryExpression"
    // Java.g:817:1: unaryExpression : ( '+' unaryExpression | '-' unaryExpression | '++' unaryExpression | '--' unaryExpression | unaryExpressionNotPlusMinus );
    public final void unaryExpression() throws RecognitionException {
        int unaryExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 121) ) { return ; }
            // Java.g:818:5: ( '+' unaryExpression | '-' unaryExpression | '++' unaryExpression | '--' unaryExpression | unaryExpressionNotPlusMinus )
            int alt142=5;
            switch ( input.LA(1) ) {
            case 105:
                {
                alt142=1;
                }
                break;
            case 106:
                {
                alt142=2;
                }
                break;
            case 109:
                {
                alt142=3;
                }
                break;
            case 110:
                {
                alt142=4;
                }
                break;
            case Identifier:
            case FloatingPointLiteral:
            case CharacterLiteral:
            case StringLiteral:
            case HexLiteral:
            case OctalLiteral:
            case DecimalLiteral:
            case 47:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 65:
            case 66:
            case 69:
            case 70:
            case 71:
            case 72:
            case 111:
            case 112:
            case 113:
                {
                alt142=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 142, 0, input);

                throw nvae;
            }

            switch (alt142) {
                case 1 :
                    // Java.g:818:9: '+' unaryExpression
                    {
                    match(input,105,FOLLOW_105_in_unaryExpression4918); if (state.failed) return ;
                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression4920);
                    unaryExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:819:9: '-' unaryExpression
                    {
                    match(input,106,FOLLOW_106_in_unaryExpression4930); if (state.failed) return ;
                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression4932);
                    unaryExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // Java.g:820:9: '++' unaryExpression
                    {
                    match(input,109,FOLLOW_109_in_unaryExpression4942); if (state.failed) return ;
                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression4944);
                    unaryExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // Java.g:821:9: '--' unaryExpression
                    {
                    match(input,110,FOLLOW_110_in_unaryExpression4954); if (state.failed) return ;
                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression4956);
                    unaryExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // Java.g:822:9: unaryExpressionNotPlusMinus
                    {
                    pushFollow(FOLLOW_unaryExpressionNotPlusMinus_in_unaryExpression4966);
                    unaryExpressionNotPlusMinus();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 121, unaryExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "unaryExpression"


    // $ANTLR start "unaryExpressionNotPlusMinus"
    // Java.g:825:1: unaryExpressionNotPlusMinus : ( '~' unaryExpression | '!' unaryExpression | castExpression | primary ( selector )* ( '++' | '--' )? );
    public final void unaryExpressionNotPlusMinus() throws RecognitionException {
        int unaryExpressionNotPlusMinus_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 122) ) { return ; }
            // Java.g:826:5: ( '~' unaryExpression | '!' unaryExpression | castExpression | primary ( selector )* ( '++' | '--' )? )
            int alt145=4;
            alt145 = dfa145.predict(input);
            switch (alt145) {
                case 1 :
                    // Java.g:826:9: '~' unaryExpression
                    {
                    match(input,111,FOLLOW_111_in_unaryExpressionNotPlusMinus4985); if (state.failed) return ;
                    pushFollow(FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus4987);
                    unaryExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:827:9: '!' unaryExpression
                    {
                    match(input,112,FOLLOW_112_in_unaryExpressionNotPlusMinus4997); if (state.failed) return ;
                    pushFollow(FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus4999);
                    unaryExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // Java.g:828:9: castExpression
                    {
                    pushFollow(FOLLOW_castExpression_in_unaryExpressionNotPlusMinus5009);
                    castExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // Java.g:829:9: primary ( selector )* ( '++' | '--' )?
                    {
                    pushFollow(FOLLOW_primary_in_unaryExpressionNotPlusMinus5019);
                    primary();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:829:17: ( selector )*
                    loop143:
                    do {
                        int alt143=2;
                        int LA143_0 = input.LA(1);

                        if ( (LA143_0==29||LA143_0==48) ) {
                            alt143=1;
                        }


                        switch (alt143) {
                    	case 1 :
                    	    // Java.g:0:0: selector
                    	    {
                    	    pushFollow(FOLLOW_selector_in_unaryExpressionNotPlusMinus5021);
                    	    selector();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop143;
                        }
                    } while (true);

                    // Java.g:829:27: ( '++' | '--' )?
                    int alt144=2;
                    int LA144_0 = input.LA(1);

                    if ( ((LA144_0>=109 && LA144_0<=110)) ) {
                        alt144=1;
                    }
                    switch (alt144) {
                        case 1 :
                            // Java.g:
                            {
                            if ( (input.LA(1)>=109 && input.LA(1)<=110) ) {
                                input.consume();
                                state.errorRecovery=false;state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                throw mse;
                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 122, unaryExpressionNotPlusMinus_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "unaryExpressionNotPlusMinus"


    // $ANTLR start "castExpression"
    // Java.g:832:1: castExpression : ( '(' primitiveType ')' unaryExpression | '(' ( type | expression ) ')' unaryExpressionNotPlusMinus );
    public final void castExpression() throws RecognitionException {
        int castExpression_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 123) ) { return ; }
            // Java.g:833:5: ( '(' primitiveType ')' unaryExpression | '(' ( type | expression ) ')' unaryExpressionNotPlusMinus )
            int alt147=2;
            int LA147_0 = input.LA(1);

            if ( (LA147_0==66) ) {
                int LA147_1 = input.LA(2);

                if ( (synpred233_Java()) ) {
                    alt147=1;
                }
                else if ( (true) ) {
                    alt147=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 147, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 147, 0, input);

                throw nvae;
            }
            switch (alt147) {
                case 1 :
                    // Java.g:833:8: '(' primitiveType ')' unaryExpression
                    {
                    match(input,66,FOLLOW_66_in_castExpression5047); if (state.failed) return ;
                    pushFollow(FOLLOW_primitiveType_in_castExpression5049);
                    primitiveType();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,67,FOLLOW_67_in_castExpression5051); if (state.failed) return ;
                    pushFollow(FOLLOW_unaryExpression_in_castExpression5053);
                    unaryExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:834:8: '(' ( type | expression ) ')' unaryExpressionNotPlusMinus
                    {
                    match(input,66,FOLLOW_66_in_castExpression5062); if (state.failed) return ;
                    // Java.g:834:12: ( type | expression )
                    int alt146=2;
                    alt146 = dfa146.predict(input);
                    switch (alt146) {
                        case 1 :
                            // Java.g:834:13: type
                            {
                            pushFollow(FOLLOW_type_in_castExpression5065);
                            type();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // Java.g:834:20: expression
                            {
                            pushFollow(FOLLOW_expression_in_castExpression5069);
                            expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }

                    match(input,67,FOLLOW_67_in_castExpression5072); if (state.failed) return ;
                    pushFollow(FOLLOW_unaryExpressionNotPlusMinus_in_castExpression5074);
                    unaryExpressionNotPlusMinus();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 123, castExpression_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "castExpression"


    // $ANTLR start "primary"
    // Java.g:837:1: primary : ( parExpression | 'this' ( '.' Identifier )* ( identifierSuffix )? | 'super' superSuffix | literal | 'new' creator | Identifier ( '.' Identifier )* ( identifierSuffix )? | primitiveType ( '[' ']' )* '.' 'class' | 'void' '.' 'class' );
    public final void primary() throws RecognitionException {
        int primary_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 124) ) { return ; }
            // Java.g:838:5: ( parExpression | 'this' ( '.' Identifier )* ( identifierSuffix )? | 'super' superSuffix | literal | 'new' creator | Identifier ( '.' Identifier )* ( identifierSuffix )? | primitiveType ( '[' ']' )* '.' 'class' | 'void' '.' 'class' )
            int alt153=8;
            switch ( input.LA(1) ) {
            case 66:
                {
                alt153=1;
                }
                break;
            case 69:
                {
                alt153=2;
                }
                break;
            case 65:
                {
                alt153=3;
                }
                break;
            case FloatingPointLiteral:
            case CharacterLiteral:
            case StringLiteral:
            case HexLiteral:
            case OctalLiteral:
            case DecimalLiteral:
            case 70:
            case 71:
            case 72:
                {
                alt153=4;
                }
                break;
            case 113:
                {
                alt153=5;
                }
                break;
            case Identifier:
                {
                alt153=6;
                }
                break;
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
                {
                alt153=7;
                }
                break;
            case 47:
                {
                alt153=8;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 153, 0, input);

                throw nvae;
            }

            switch (alt153) {
                case 1 :
                    // Java.g:838:9: parExpression
                    {
                    pushFollow(FOLLOW_parExpression_in_primary5093);
                    parExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:839:9: 'this' ( '.' Identifier )* ( identifierSuffix )?
                    {
                    match(input,69,FOLLOW_69_in_primary5103); if (state.failed) return ;
                    // Java.g:839:16: ( '.' Identifier )*
                    loop148:
                    do {
                        int alt148=2;
                        int LA148_0 = input.LA(1);

                        if ( (LA148_0==29) ) {
                            int LA148_2 = input.LA(2);

                            if ( (LA148_2==Identifier) ) {
                                int LA148_3 = input.LA(3);

                                if ( (synpred236_Java()) ) {
                                    alt148=1;
                                }


                            }


                        }


                        switch (alt148) {
                    	case 1 :
                    	    // Java.g:839:17: '.' Identifier
                    	    {
                    	    match(input,29,FOLLOW_29_in_primary5106); if (state.failed) return ;
                    	    match(input,Identifier,FOLLOW_Identifier_in_primary5108); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop148;
                        }
                    } while (true);

                    // Java.g:839:34: ( identifierSuffix )?
                    int alt149=2;
                    alt149 = dfa149.predict(input);
                    switch (alt149) {
                        case 1 :
                            // Java.g:0:0: identifierSuffix
                            {
                            pushFollow(FOLLOW_identifierSuffix_in_primary5112);
                            identifierSuffix();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // Java.g:840:9: 'super' superSuffix
                    {
                    match(input,65,FOLLOW_65_in_primary5123); if (state.failed) return ;
                    pushFollow(FOLLOW_superSuffix_in_primary5125);
                    superSuffix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // Java.g:841:9: literal
                    {
                    pushFollow(FOLLOW_literal_in_primary5135);
                    literal();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // Java.g:842:9: 'new' creator
                    {
                    match(input,113,FOLLOW_113_in_primary5145); if (state.failed) return ;
                    pushFollow(FOLLOW_creator_in_primary5147);
                    creator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // Java.g:843:9: Identifier ( '.' Identifier )* ( identifierSuffix )?
                    {
                    match(input,Identifier,FOLLOW_Identifier_in_primary5157); if (state.failed) return ;
                    // Java.g:843:20: ( '.' Identifier )*
                    loop150:
                    do {
                        int alt150=2;
                        int LA150_0 = input.LA(1);

                        if ( (LA150_0==29) ) {
                            int LA150_2 = input.LA(2);

                            if ( (LA150_2==Identifier) ) {
                                int LA150_3 = input.LA(3);

                                if ( (synpred242_Java()) ) {
                                    alt150=1;
                                }


                            }


                        }


                        switch (alt150) {
                    	case 1 :
                    	    // Java.g:843:21: '.' Identifier
                    	    {
                    	    match(input,29,FOLLOW_29_in_primary5160); if (state.failed) return ;
                    	    match(input,Identifier,FOLLOW_Identifier_in_primary5162); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop150;
                        }
                    } while (true);

                    // Java.g:843:38: ( identifierSuffix )?
                    int alt151=2;
                    alt151 = dfa151.predict(input);
                    switch (alt151) {
                        case 1 :
                            // Java.g:0:0: identifierSuffix
                            {
                            pushFollow(FOLLOW_identifierSuffix_in_primary5166);
                            identifierSuffix();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 7 :
                    // Java.g:844:9: primitiveType ( '[' ']' )* '.' 'class'
                    {
                    pushFollow(FOLLOW_primitiveType_in_primary5177);
                    primitiveType();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:844:23: ( '[' ']' )*
                    loop152:
                    do {
                        int alt152=2;
                        int LA152_0 = input.LA(1);

                        if ( (LA152_0==48) ) {
                            alt152=1;
                        }


                        switch (alt152) {
                    	case 1 :
                    	    // Java.g:844:24: '[' ']'
                    	    {
                    	    match(input,48,FOLLOW_48_in_primary5180); if (state.failed) return ;
                    	    match(input,49,FOLLOW_49_in_primary5182); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop152;
                        }
                    } while (true);

                    match(input,29,FOLLOW_29_in_primary5186); if (state.failed) return ;
                    match(input,37,FOLLOW_37_in_primary5188); if (state.failed) return ;

                    }
                    break;
                case 8 :
                    // Java.g:845:9: 'void' '.' 'class'
                    {
                    match(input,47,FOLLOW_47_in_primary5198); if (state.failed) return ;
                    match(input,29,FOLLOW_29_in_primary5200); if (state.failed) return ;
                    match(input,37,FOLLOW_37_in_primary5202); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 124, primary_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "primary"


    // $ANTLR start "identifierSuffix"
    // Java.g:848:1: identifierSuffix : ( ( '[' ']' )+ '.' 'class' | ( '[' expression ']' )+ | arguments | '.' 'class' | '.' explicitGenericInvocation | '.' 'this' | '.' 'super' arguments | '.' 'new' innerCreator );
    public final void identifierSuffix() throws RecognitionException {
        int identifierSuffix_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 125) ) { return ; }
            // Java.g:849:5: ( ( '[' ']' )+ '.' 'class' | ( '[' expression ']' )+ | arguments | '.' 'class' | '.' explicitGenericInvocation | '.' 'this' | '.' 'super' arguments | '.' 'new' innerCreator )
            int alt156=8;
            alt156 = dfa156.predict(input);
            switch (alt156) {
                case 1 :
                    // Java.g:849:9: ( '[' ']' )+ '.' 'class'
                    {
                    // Java.g:849:9: ( '[' ']' )+
                    int cnt154=0;
                    loop154:
                    do {
                        int alt154=2;
                        int LA154_0 = input.LA(1);

                        if ( (LA154_0==48) ) {
                            alt154=1;
                        }


                        switch (alt154) {
                    	case 1 :
                    	    // Java.g:849:10: '[' ']'
                    	    {
                    	    match(input,48,FOLLOW_48_in_identifierSuffix5222); if (state.failed) return ;
                    	    match(input,49,FOLLOW_49_in_identifierSuffix5224); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt154 >= 1 ) break loop154;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(154, input);
                                throw eee;
                        }
                        cnt154++;
                    } while (true);

                    match(input,29,FOLLOW_29_in_identifierSuffix5228); if (state.failed) return ;
                    match(input,37,FOLLOW_37_in_identifierSuffix5230); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:850:9: ( '[' expression ']' )+
                    {
                    // Java.g:850:9: ( '[' expression ']' )+
                    int cnt155=0;
                    loop155:
                    do {
                        int alt155=2;
                        alt155 = dfa155.predict(input);
                        switch (alt155) {
                    	case 1 :
                    	    // Java.g:850:10: '[' expression ']'
                    	    {
                    	    match(input,48,FOLLOW_48_in_identifierSuffix5241); if (state.failed) return ;
                    	    pushFollow(FOLLOW_expression_in_identifierSuffix5243);
                    	    expression();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    match(input,49,FOLLOW_49_in_identifierSuffix5245); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt155 >= 1 ) break loop155;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(155, input);
                                throw eee;
                        }
                        cnt155++;
                    } while (true);


                    }
                    break;
                case 3 :
                    // Java.g:851:9: arguments
                    {
                    pushFollow(FOLLOW_arguments_in_identifierSuffix5258);
                    arguments();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // Java.g:852:9: '.' 'class'
                    {
                    match(input,29,FOLLOW_29_in_identifierSuffix5268); if (state.failed) return ;
                    match(input,37,FOLLOW_37_in_identifierSuffix5270); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // Java.g:853:9: '.' explicitGenericInvocation
                    {
                    match(input,29,FOLLOW_29_in_identifierSuffix5280); if (state.failed) return ;
                    pushFollow(FOLLOW_explicitGenericInvocation_in_identifierSuffix5282);
                    explicitGenericInvocation();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // Java.g:854:9: '.' 'this'
                    {
                    match(input,29,FOLLOW_29_in_identifierSuffix5292); if (state.failed) return ;
                    match(input,69,FOLLOW_69_in_identifierSuffix5294); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    // Java.g:855:9: '.' 'super' arguments
                    {
                    match(input,29,FOLLOW_29_in_identifierSuffix5304); if (state.failed) return ;
                    match(input,65,FOLLOW_65_in_identifierSuffix5306); if (state.failed) return ;
                    pushFollow(FOLLOW_arguments_in_identifierSuffix5308);
                    arguments();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    // Java.g:856:9: '.' 'new' innerCreator
                    {
                    match(input,29,FOLLOW_29_in_identifierSuffix5318); if (state.failed) return ;
                    match(input,113,FOLLOW_113_in_identifierSuffix5320); if (state.failed) return ;
                    pushFollow(FOLLOW_innerCreator_in_identifierSuffix5322);
                    innerCreator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 125, identifierSuffix_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "identifierSuffix"


    // $ANTLR start "creator"
    // Java.g:859:1: creator : ( nonWildcardTypeArguments createdName classCreatorRest | createdName ( arrayCreatorRest | classCreatorRest ) );
    public final void creator() throws RecognitionException {
        int creator_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 126) ) { return ; }
            // Java.g:860:5: ( nonWildcardTypeArguments createdName classCreatorRest | createdName ( arrayCreatorRest | classCreatorRest ) )
            int alt158=2;
            int LA158_0 = input.LA(1);

            if ( (LA158_0==40) ) {
                alt158=1;
            }
            else if ( (LA158_0==Identifier||(LA158_0>=56 && LA158_0<=63)) ) {
                alt158=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 158, 0, input);

                throw nvae;
            }
            switch (alt158) {
                case 1 :
                    // Java.g:860:9: nonWildcardTypeArguments createdName classCreatorRest
                    {
                    pushFollow(FOLLOW_nonWildcardTypeArguments_in_creator5341);
                    nonWildcardTypeArguments();

                    state._fsp--;
                    if (state.failed) return ;
                    pushFollow(FOLLOW_createdName_in_creator5343);
                    createdName();

                    state._fsp--;
                    if (state.failed) return ;
                    pushFollow(FOLLOW_classCreatorRest_in_creator5345);
                    classCreatorRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:861:9: createdName ( arrayCreatorRest | classCreatorRest )
                    {
                    pushFollow(FOLLOW_createdName_in_creator5355);
                    createdName();

                    state._fsp--;
                    if (state.failed) return ;
                    // Java.g:861:21: ( arrayCreatorRest | classCreatorRest )
                    int alt157=2;
                    int LA157_0 = input.LA(1);

                    if ( (LA157_0==48) ) {
                        alt157=1;
                    }
                    else if ( (LA157_0==66) ) {
                        alt157=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 157, 0, input);

                        throw nvae;
                    }
                    switch (alt157) {
                        case 1 :
                            // Java.g:861:22: arrayCreatorRest
                            {
                            pushFollow(FOLLOW_arrayCreatorRest_in_creator5358);
                            arrayCreatorRest();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // Java.g:861:41: classCreatorRest
                            {
                            pushFollow(FOLLOW_classCreatorRest_in_creator5362);
                            classCreatorRest();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 126, creator_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "creator"


    // $ANTLR start "createdName"
    // Java.g:864:1: createdName : ( classOrInterfaceType | primitiveType );
    public final void createdName() throws RecognitionException {
        int createdName_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 127) ) { return ; }
            // Java.g:865:5: ( classOrInterfaceType | primitiveType )
            int alt159=2;
            int LA159_0 = input.LA(1);

            if ( (LA159_0==Identifier) ) {
                alt159=1;
            }
            else if ( ((LA159_0>=56 && LA159_0<=63)) ) {
                alt159=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 159, 0, input);

                throw nvae;
            }
            switch (alt159) {
                case 1 :
                    // Java.g:865:9: classOrInterfaceType
                    {
                    pushFollow(FOLLOW_classOrInterfaceType_in_createdName5382);
                    classOrInterfaceType();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:866:9: primitiveType
                    {
                    pushFollow(FOLLOW_primitiveType_in_createdName5392);
                    primitiveType();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 127, createdName_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "createdName"


    // $ANTLR start "innerCreator"
    // Java.g:869:1: innerCreator : ( nonWildcardTypeArguments )? Identifier classCreatorRest ;
    public final void innerCreator() throws RecognitionException {
        int innerCreator_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 128) ) { return ; }
            // Java.g:870:5: ( ( nonWildcardTypeArguments )? Identifier classCreatorRest )
            // Java.g:870:9: ( nonWildcardTypeArguments )? Identifier classCreatorRest
            {
            // Java.g:870:9: ( nonWildcardTypeArguments )?
            int alt160=2;
            int LA160_0 = input.LA(1);

            if ( (LA160_0==40) ) {
                alt160=1;
            }
            switch (alt160) {
                case 1 :
                    // Java.g:0:0: nonWildcardTypeArguments
                    {
                    pushFollow(FOLLOW_nonWildcardTypeArguments_in_innerCreator5415);
                    nonWildcardTypeArguments();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            match(input,Identifier,FOLLOW_Identifier_in_innerCreator5418); if (state.failed) return ;
            pushFollow(FOLLOW_classCreatorRest_in_innerCreator5420);
            classCreatorRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 128, innerCreator_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "innerCreator"


    // $ANTLR start "arrayCreatorRest"
    // Java.g:873:1: arrayCreatorRest : '[' ( ']' ( '[' ']' )* arrayInitializer | expression ']' ( '[' expression ']' )* ( '[' ']' )* ) ;
    public final void arrayCreatorRest() throws RecognitionException {
        int arrayCreatorRest_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 129) ) { return ; }
            // Java.g:874:5: ( '[' ( ']' ( '[' ']' )* arrayInitializer | expression ']' ( '[' expression ']' )* ( '[' ']' )* ) )
            // Java.g:874:9: '[' ( ']' ( '[' ']' )* arrayInitializer | expression ']' ( '[' expression ']' )* ( '[' ']' )* )
            {
            match(input,48,FOLLOW_48_in_arrayCreatorRest5439); if (state.failed) return ;
            // Java.g:875:9: ( ']' ( '[' ']' )* arrayInitializer | expression ']' ( '[' expression ']' )* ( '[' ']' )* )
            int alt164=2;
            int LA164_0 = input.LA(1);

            if ( (LA164_0==49) ) {
                alt164=1;
            }
            else if ( (LA164_0==Identifier||(LA164_0>=FloatingPointLiteral && LA164_0<=DecimalLiteral)||LA164_0==47||(LA164_0>=56 && LA164_0<=63)||(LA164_0>=65 && LA164_0<=66)||(LA164_0>=69 && LA164_0<=72)||(LA164_0>=105 && LA164_0<=106)||(LA164_0>=109 && LA164_0<=113)) ) {
                alt164=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 164, 0, input);

                throw nvae;
            }
            switch (alt164) {
                case 1 :
                    // Java.g:875:13: ']' ( '[' ']' )* arrayInitializer
                    {
                    match(input,49,FOLLOW_49_in_arrayCreatorRest5453); if (state.failed) return ;
                    // Java.g:875:17: ( '[' ']' )*
                    loop161:
                    do {
                        int alt161=2;
                        int LA161_0 = input.LA(1);

                        if ( (LA161_0==48) ) {
                            alt161=1;
                        }


                        switch (alt161) {
                    	case 1 :
                    	    // Java.g:875:18: '[' ']'
                    	    {
                    	    match(input,48,FOLLOW_48_in_arrayCreatorRest5456); if (state.failed) return ;
                    	    match(input,49,FOLLOW_49_in_arrayCreatorRest5458); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop161;
                        }
                    } while (true);

                    pushFollow(FOLLOW_arrayInitializer_in_arrayCreatorRest5462);
                    arrayInitializer();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:876:13: expression ']' ( '[' expression ']' )* ( '[' ']' )*
                    {
                    pushFollow(FOLLOW_expression_in_arrayCreatorRest5476);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,49,FOLLOW_49_in_arrayCreatorRest5478); if (state.failed) return ;
                    // Java.g:876:28: ( '[' expression ']' )*
                    loop162:
                    do {
                        int alt162=2;
                        alt162 = dfa162.predict(input);
                        switch (alt162) {
                    	case 1 :
                    	    // Java.g:876:29: '[' expression ']'
                    	    {
                    	    match(input,48,FOLLOW_48_in_arrayCreatorRest5481); if (state.failed) return ;
                    	    pushFollow(FOLLOW_expression_in_arrayCreatorRest5483);
                    	    expression();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    match(input,49,FOLLOW_49_in_arrayCreatorRest5485); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop162;
                        }
                    } while (true);

                    // Java.g:876:50: ( '[' ']' )*
                    loop163:
                    do {
                        int alt163=2;
                        int LA163_0 = input.LA(1);

                        if ( (LA163_0==48) ) {
                            int LA163_2 = input.LA(2);

                            if ( (LA163_2==49) ) {
                                alt163=1;
                            }


                        }


                        switch (alt163) {
                    	case 1 :
                    	    // Java.g:876:51: '[' ']'
                    	    {
                    	    match(input,48,FOLLOW_48_in_arrayCreatorRest5490); if (state.failed) return ;
                    	    match(input,49,FOLLOW_49_in_arrayCreatorRest5492); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop163;
                        }
                    } while (true);


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 129, arrayCreatorRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "arrayCreatorRest"


    // $ANTLR start "classCreatorRest"
    // Java.g:880:1: classCreatorRest : arguments ( classBody )? ;
    public final void classCreatorRest() throws RecognitionException {
        int classCreatorRest_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 130) ) { return ; }
            // Java.g:881:5: ( arguments ( classBody )? )
            // Java.g:881:9: arguments ( classBody )?
            {
            pushFollow(FOLLOW_arguments_in_classCreatorRest5523);
            arguments();

            state._fsp--;
            if (state.failed) return ;
            // Java.g:881:19: ( classBody )?
            int alt165=2;
            int LA165_0 = input.LA(1);

            if ( (LA165_0==44) ) {
                alt165=1;
            }
            switch (alt165) {
                case 1 :
                    // Java.g:0:0: classBody
                    {
                    pushFollow(FOLLOW_classBody_in_classCreatorRest5525);
                    classBody();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 130, classCreatorRest_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "classCreatorRest"


    // $ANTLR start "explicitGenericInvocation"
    // Java.g:884:1: explicitGenericInvocation : nonWildcardTypeArguments Identifier arguments ;
    public final void explicitGenericInvocation() throws RecognitionException {
        int explicitGenericInvocation_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 131) ) { return ; }
            // Java.g:885:5: ( nonWildcardTypeArguments Identifier arguments )
            // Java.g:885:9: nonWildcardTypeArguments Identifier arguments
            {
            pushFollow(FOLLOW_nonWildcardTypeArguments_in_explicitGenericInvocation5549);
            nonWildcardTypeArguments();

            state._fsp--;
            if (state.failed) return ;
            match(input,Identifier,FOLLOW_Identifier_in_explicitGenericInvocation5551); if (state.failed) return ;
            pushFollow(FOLLOW_arguments_in_explicitGenericInvocation5553);
            arguments();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 131, explicitGenericInvocation_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "explicitGenericInvocation"


    // $ANTLR start "nonWildcardTypeArguments"
    // Java.g:888:1: nonWildcardTypeArguments : '<' typeList '>' ;
    public final void nonWildcardTypeArguments() throws RecognitionException {
        int nonWildcardTypeArguments_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 132) ) { return ; }
            // Java.g:889:5: ( '<' typeList '>' )
            // Java.g:889:9: '<' typeList '>'
            {
            match(input,40,FOLLOW_40_in_nonWildcardTypeArguments5576); if (state.failed) return ;
            pushFollow(FOLLOW_typeList_in_nonWildcardTypeArguments5578);
            typeList();

            state._fsp--;
            if (state.failed) return ;
            match(input,42,FOLLOW_42_in_nonWildcardTypeArguments5580); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 132, nonWildcardTypeArguments_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "nonWildcardTypeArguments"


    // $ANTLR start "selector"
    // Java.g:892:1: selector : ( '.' Identifier ( arguments )? | '.' 'this' | '.' 'super' superSuffix | '.' 'new' innerCreator | '[' expression ']' );
    public final void selector() throws RecognitionException {
        int selector_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 133) ) { return ; }
            // Java.g:893:5: ( '.' Identifier ( arguments )? | '.' 'this' | '.' 'super' superSuffix | '.' 'new' innerCreator | '[' expression ']' )
            int alt167=5;
            int LA167_0 = input.LA(1);

            if ( (LA167_0==29) ) {
                switch ( input.LA(2) ) {
                case Identifier:
                    {
                    alt167=1;
                    }
                    break;
                case 69:
                    {
                    alt167=2;
                    }
                    break;
                case 65:
                    {
                    alt167=3;
                    }
                    break;
                case 113:
                    {
                    alt167=4;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 167, 1, input);

                    throw nvae;
                }

            }
            else if ( (LA167_0==48) ) {
                alt167=5;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 167, 0, input);

                throw nvae;
            }
            switch (alt167) {
                case 1 :
                    // Java.g:893:9: '.' Identifier ( arguments )?
                    {
                    match(input,29,FOLLOW_29_in_selector5603); if (state.failed) return ;
                    match(input,Identifier,FOLLOW_Identifier_in_selector5605); if (state.failed) return ;
                    // Java.g:893:24: ( arguments )?
                    int alt166=2;
                    int LA166_0 = input.LA(1);

                    if ( (LA166_0==66) ) {
                        alt166=1;
                    }
                    switch (alt166) {
                        case 1 :
                            // Java.g:0:0: arguments
                            {
                            pushFollow(FOLLOW_arguments_in_selector5607);
                            arguments();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // Java.g:894:9: '.' 'this'
                    {
                    match(input,29,FOLLOW_29_in_selector5618); if (state.failed) return ;
                    match(input,69,FOLLOW_69_in_selector5620); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // Java.g:895:9: '.' 'super' superSuffix
                    {
                    match(input,29,FOLLOW_29_in_selector5630); if (state.failed) return ;
                    match(input,65,FOLLOW_65_in_selector5632); if (state.failed) return ;
                    pushFollow(FOLLOW_superSuffix_in_selector5634);
                    superSuffix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // Java.g:896:9: '.' 'new' innerCreator
                    {
                    match(input,29,FOLLOW_29_in_selector5644); if (state.failed) return ;
                    match(input,113,FOLLOW_113_in_selector5646); if (state.failed) return ;
                    pushFollow(FOLLOW_innerCreator_in_selector5648);
                    innerCreator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // Java.g:897:9: '[' expression ']'
                    {
                    match(input,48,FOLLOW_48_in_selector5658); if (state.failed) return ;
                    pushFollow(FOLLOW_expression_in_selector5660);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,49,FOLLOW_49_in_selector5662); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 133, selector_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "selector"


    // $ANTLR start "superSuffix"
    // Java.g:900:1: superSuffix : ( arguments | '.' Identifier ( arguments )? );
    public final void superSuffix() throws RecognitionException {
        int superSuffix_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 134) ) { return ; }
            // Java.g:901:5: ( arguments | '.' Identifier ( arguments )? )
            int alt169=2;
            int LA169_0 = input.LA(1);

            if ( (LA169_0==66) ) {
                alt169=1;
            }
            else if ( (LA169_0==29) ) {
                alt169=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 169, 0, input);

                throw nvae;
            }
            switch (alt169) {
                case 1 :
                    // Java.g:901:9: arguments
                    {
                    pushFollow(FOLLOW_arguments_in_superSuffix5685);
                    arguments();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // Java.g:902:9: '.' Identifier ( arguments )?
                    {
                    match(input,29,FOLLOW_29_in_superSuffix5695); if (state.failed) return ;
                    match(input,Identifier,FOLLOW_Identifier_in_superSuffix5697); if (state.failed) return ;
                    // Java.g:902:24: ( arguments )?
                    int alt168=2;
                    int LA168_0 = input.LA(1);

                    if ( (LA168_0==66) ) {
                        alt168=1;
                    }
                    switch (alt168) {
                        case 1 :
                            // Java.g:0:0: arguments
                            {
                            pushFollow(FOLLOW_arguments_in_superSuffix5699);
                            arguments();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 134, superSuffix_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "superSuffix"


    // $ANTLR start "arguments"
    // Java.g:905:1: arguments : '(' ( expressionList )? ')' ;
    public final void arguments() throws RecognitionException {
        int arguments_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 135) ) { return ; }
            // Java.g:906:5: ( '(' ( expressionList )? ')' )
            // Java.g:906:9: '(' ( expressionList )? ')'
            {
            match(input,66,FOLLOW_66_in_arguments5719); if (state.failed) return ;
            // Java.g:906:13: ( expressionList )?
            int alt170=2;
            int LA170_0 = input.LA(1);

            if ( (LA170_0==Identifier||(LA170_0>=FloatingPointLiteral && LA170_0<=DecimalLiteral)||LA170_0==47||(LA170_0>=56 && LA170_0<=63)||(LA170_0>=65 && LA170_0<=66)||(LA170_0>=69 && LA170_0<=72)||(LA170_0>=105 && LA170_0<=106)||(LA170_0>=109 && LA170_0<=113)) ) {
                alt170=1;
            }
            switch (alt170) {
                case 1 :
                    // Java.g:0:0: expressionList
                    {
                    pushFollow(FOLLOW_expressionList_in_arguments5721);
                    expressionList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            match(input,67,FOLLOW_67_in_arguments5724); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 135, arguments_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "arguments"

    // $ANTLR start synpred5_Java
    public final void synpred5_Java_fragment() throws RecognitionException {   
        // Java.g:180:9: ( annotations ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* ) )
        // Java.g:180:9: annotations ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* )
        {
        pushFollow(FOLLOW_annotations_in_synpred5_Java44);
        annotations();

        state._fsp--;
        if (state.failed) return ;
        // Java.g:181:9: ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* )
        int alt176=2;
        int LA176_0 = input.LA(1);

        if ( (LA176_0==25) ) {
            alt176=1;
        }
        else if ( (LA176_0==ENUM||LA176_0==28||(LA176_0>=31 && LA176_0<=37)||LA176_0==46||LA176_0==73) ) {
            alt176=2;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 176, 0, input);

            throw nvae;
        }
        switch (alt176) {
            case 1 :
                // Java.g:181:13: packageDeclaration ( importDeclaration )* ( typeDeclaration )*
                {
                pushFollow(FOLLOW_packageDeclaration_in_synpred5_Java58);
                packageDeclaration();

                state._fsp--;
                if (state.failed) return ;
                // Java.g:181:32: ( importDeclaration )*
                loop173:
                do {
                    int alt173=2;
                    int LA173_0 = input.LA(1);

                    if ( (LA173_0==27) ) {
                        alt173=1;
                    }


                    switch (alt173) {
                	case 1 :
                	    // Java.g:0:0: importDeclaration
                	    {
                	    pushFollow(FOLLOW_importDeclaration_in_synpred5_Java60);
                	    importDeclaration();

                	    state._fsp--;
                	    if (state.failed) return ;

                	    }
                	    break;

                	default :
                	    break loop173;
                    }
                } while (true);

                // Java.g:181:51: ( typeDeclaration )*
                loop174:
                do {
                    int alt174=2;
                    int LA174_0 = input.LA(1);

                    if ( (LA174_0==ENUM||LA174_0==26||LA174_0==28||(LA174_0>=31 && LA174_0<=37)||LA174_0==46||LA174_0==73) ) {
                        alt174=1;
                    }


                    switch (alt174) {
                	case 1 :
                	    // Java.g:0:0: typeDeclaration
                	    {
                	    pushFollow(FOLLOW_typeDeclaration_in_synpred5_Java63);
                	    typeDeclaration();

                	    state._fsp--;
                	    if (state.failed) return ;

                	    }
                	    break;

                	default :
                	    break loop174;
                    }
                } while (true);


                }
                break;
            case 2 :
                // Java.g:182:13: classOrInterfaceDeclaration ( typeDeclaration )*
                {
                pushFollow(FOLLOW_classOrInterfaceDeclaration_in_synpred5_Java78);
                classOrInterfaceDeclaration();

                state._fsp--;
                if (state.failed) return ;
                // Java.g:182:41: ( typeDeclaration )*
                loop175:
                do {
                    int alt175=2;
                    int LA175_0 = input.LA(1);

                    if ( (LA175_0==ENUM||LA175_0==26||LA175_0==28||(LA175_0>=31 && LA175_0<=37)||LA175_0==46||LA175_0==73) ) {
                        alt175=1;
                    }


                    switch (alt175) {
                	case 1 :
                	    // Java.g:0:0: typeDeclaration
                	    {
                	    pushFollow(FOLLOW_typeDeclaration_in_synpred5_Java80);
                	    typeDeclaration();

                	    state._fsp--;
                	    if (state.failed) return ;

                	    }
                	    break;

                	default :
                	    break loop175;
                    }
                } while (true);


                }
                break;

        }


        }
    }
    // $ANTLR end synpred5_Java

    // $ANTLR start synpred113_Java
    public final void synpred113_Java_fragment() throws RecognitionException {   
        // Java.g:494:13: ( explicitConstructorInvocation )
        // Java.g:494:13: explicitConstructorInvocation
        {
        pushFollow(FOLLOW_explicitConstructorInvocation_in_synpred113_Java2455);
        explicitConstructorInvocation();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred113_Java

    // $ANTLR start synpred117_Java
    public final void synpred117_Java_fragment() throws RecognitionException {   
        // Java.g:498:9: ( ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';' )
        // Java.g:498:9: ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';'
        {
        // Java.g:498:9: ( nonWildcardTypeArguments )?
        int alt184=2;
        int LA184_0 = input.LA(1);

        if ( (LA184_0==40) ) {
            alt184=1;
        }
        switch (alt184) {
            case 1 :
                // Java.g:0:0: nonWildcardTypeArguments
                {
                pushFollow(FOLLOW_nonWildcardTypeArguments_in_synpred117_Java2480);
                nonWildcardTypeArguments();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }

        if ( input.LA(1)==65||input.LA(1)==69 ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }

        pushFollow(FOLLOW_arguments_in_synpred117_Java2491);
        arguments();

        state._fsp--;
        if (state.failed) return ;
        match(input,26,FOLLOW_26_in_synpred117_Java2493); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred117_Java

    // $ANTLR start synpred128_Java
    public final void synpred128_Java_fragment() throws RecognitionException {   
        // Java.g:530:9: ( annotation )
        // Java.g:530:9: annotation
        {
        pushFollow(FOLLOW_annotation_in_synpred128_Java2704);
        annotation();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred128_Java

    // $ANTLR start synpred151_Java
    public final void synpred151_Java_fragment() throws RecognitionException {   
        // Java.g:603:9: ( localVariableDeclarationStatement )
        // Java.g:603:9: localVariableDeclarationStatement
        {
        pushFollow(FOLLOW_localVariableDeclarationStatement_in_synpred151_Java3231);
        localVariableDeclarationStatement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred151_Java

    // $ANTLR start synpred152_Java
    public final void synpred152_Java_fragment() throws RecognitionException {   
        // Java.g:604:9: ( classOrInterfaceDeclaration )
        // Java.g:604:9: classOrInterfaceDeclaration
        {
        pushFollow(FOLLOW_classOrInterfaceDeclaration_in_synpred152_Java3241);
        classOrInterfaceDeclaration();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred152_Java

    // $ANTLR start synpred157_Java
    public final void synpred157_Java_fragment() throws RecognitionException {   
        // Java.g:623:54: ( 'else' statement )
        // Java.g:623:54: 'else' statement
        {
        match(input,77,FOLLOW_77_in_synpred157_Java3386); if (state.failed) return ;
        pushFollow(FOLLOW_statement_in_synpred157_Java3388);
        statement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred157_Java

    // $ANTLR start synpred162_Java
    public final void synpred162_Java_fragment() throws RecognitionException {   
        // Java.g:628:11: ( catches 'finally' block )
        // Java.g:628:11: catches 'finally' block
        {
        pushFollow(FOLLOW_catches_in_synpred162_Java3464);
        catches();

        state._fsp--;
        if (state.failed) return ;
        match(input,82,FOLLOW_82_in_synpred162_Java3466); if (state.failed) return ;
        pushFollow(FOLLOW_block_in_synpred162_Java3468);
        block();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred162_Java

    // $ANTLR start synpred163_Java
    public final void synpred163_Java_fragment() throws RecognitionException {   
        // Java.g:629:11: ( catches )
        // Java.g:629:11: catches
        {
        pushFollow(FOLLOW_catches_in_synpred163_Java3480);
        catches();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred163_Java

    // $ANTLR start synpred178_Java
    public final void synpred178_Java_fragment() throws RecognitionException {   
        // Java.g:664:9: ( switchLabel )
        // Java.g:664:9: switchLabel
        {
        pushFollow(FOLLOW_switchLabel_in_synpred178_Java3771);
        switchLabel();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred178_Java

    // $ANTLR start synpred180_Java
    public final void synpred180_Java_fragment() throws RecognitionException {   
        // Java.g:668:9: ( 'case' constantExpression ':' )
        // Java.g:668:9: 'case' constantExpression ':'
        {
        match(input,89,FOLLOW_89_in_synpred180_Java3798); if (state.failed) return ;
        pushFollow(FOLLOW_constantExpression_in_synpred180_Java3800);
        constantExpression();

        state._fsp--;
        if (state.failed) return ;
        match(input,75,FOLLOW_75_in_synpred180_Java3802); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred180_Java

    // $ANTLR start synpred181_Java
    public final void synpred181_Java_fragment() throws RecognitionException {   
        // Java.g:669:9: ( 'case' enumConstantName ':' )
        // Java.g:669:9: 'case' enumConstantName ':'
        {
        match(input,89,FOLLOW_89_in_synpred181_Java3812); if (state.failed) return ;
        pushFollow(FOLLOW_enumConstantName_in_synpred181_Java3814);
        enumConstantName();

        state._fsp--;
        if (state.failed) return ;
        match(input,75,FOLLOW_75_in_synpred181_Java3816); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred181_Java

    // $ANTLR start synpred182_Java
    public final void synpred182_Java_fragment() throws RecognitionException {   
        // Java.g:675:9: ( enhancedForControl )
        // Java.g:675:9: enhancedForControl
        {
        pushFollow(FOLLOW_enhancedForControl_in_synpred182_Java3859);
        enhancedForControl();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred182_Java

    // $ANTLR start synpred186_Java
    public final void synpred186_Java_fragment() throws RecognitionException {   
        // Java.g:680:9: ( localVariableDeclaration )
        // Java.g:680:9: localVariableDeclaration
        {
        pushFollow(FOLLOW_localVariableDeclaration_in_synpred186_Java3899);
        localVariableDeclaration();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred186_Java

    // $ANTLR start synpred198_Java
    public final void synpred198_Java_fragment() throws RecognitionException {   
        // Java.g:724:9: ( '<' '<' '=' )
        // Java.g:724:10: '<' '<' '='
        {
        match(input,40,FOLLOW_40_in_synpred198_Java4200); if (state.failed) return ;
        match(input,40,FOLLOW_40_in_synpred198_Java4202); if (state.failed) return ;
        match(input,51,FOLLOW_51_in_synpred198_Java4204); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred198_Java

    // $ANTLR start synpred199_Java
    public final void synpred199_Java_fragment() throws RecognitionException {   
        // Java.g:729:9: ( '>' '>' '>' '=' )
        // Java.g:729:10: '>' '>' '>' '='
        {
        match(input,42,FOLLOW_42_in_synpred199_Java4240); if (state.failed) return ;
        match(input,42,FOLLOW_42_in_synpred199_Java4242); if (state.failed) return ;
        match(input,42,FOLLOW_42_in_synpred199_Java4244); if (state.failed) return ;
        match(input,51,FOLLOW_51_in_synpred199_Java4246); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred199_Java

    // $ANTLR start synpred200_Java
    public final void synpred200_Java_fragment() throws RecognitionException {   
        // Java.g:736:9: ( '>' '>' '=' )
        // Java.g:736:10: '>' '>' '='
        {
        match(input,42,FOLLOW_42_in_synpred200_Java4285); if (state.failed) return ;
        match(input,42,FOLLOW_42_in_synpred200_Java4287); if (state.failed) return ;
        match(input,51,FOLLOW_51_in_synpred200_Java4289); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred200_Java

    // $ANTLR start synpred211_Java
    public final void synpred211_Java_fragment() throws RecognitionException {   
        // Java.g:780:9: ( '<' '=' )
        // Java.g:780:10: '<' '='
        {
        match(input,40,FOLLOW_40_in_synpred211_Java4597); if (state.failed) return ;
        match(input,51,FOLLOW_51_in_synpred211_Java4599); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred211_Java

    // $ANTLR start synpred212_Java
    public final void synpred212_Java_fragment() throws RecognitionException {   
        // Java.g:783:9: ( '>' '=' )
        // Java.g:783:10: '>' '='
        {
        match(input,42,FOLLOW_42_in_synpred212_Java4631); if (state.failed) return ;
        match(input,51,FOLLOW_51_in_synpred212_Java4633); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred212_Java

    // $ANTLR start synpred215_Java
    public final void synpred215_Java_fragment() throws RecognitionException {   
        // Java.g:795:9: ( '<' '<' )
        // Java.g:795:10: '<' '<'
        {
        match(input,40,FOLLOW_40_in_synpred215_Java4724); if (state.failed) return ;
        match(input,40,FOLLOW_40_in_synpred215_Java4726); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred215_Java

    // $ANTLR start synpred216_Java
    public final void synpred216_Java_fragment() throws RecognitionException {   
        // Java.g:798:9: ( '>' '>' '>' )
        // Java.g:798:10: '>' '>' '>'
        {
        match(input,42,FOLLOW_42_in_synpred216_Java4758); if (state.failed) return ;
        match(input,42,FOLLOW_42_in_synpred216_Java4760); if (state.failed) return ;
        match(input,42,FOLLOW_42_in_synpred216_Java4762); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred216_Java

    // $ANTLR start synpred217_Java
    public final void synpred217_Java_fragment() throws RecognitionException {   
        // Java.g:803:9: ( '>' '>' )
        // Java.g:803:10: '>' '>'
        {
        match(input,42,FOLLOW_42_in_synpred217_Java4798); if (state.failed) return ;
        match(input,42,FOLLOW_42_in_synpred217_Java4800); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred217_Java

    // $ANTLR start synpred229_Java
    public final void synpred229_Java_fragment() throws RecognitionException {   
        // Java.g:828:9: ( castExpression )
        // Java.g:828:9: castExpression
        {
        pushFollow(FOLLOW_castExpression_in_synpred229_Java5009);
        castExpression();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred229_Java

    // $ANTLR start synpred233_Java
    public final void synpred233_Java_fragment() throws RecognitionException {   
        // Java.g:833:8: ( '(' primitiveType ')' unaryExpression )
        // Java.g:833:8: '(' primitiveType ')' unaryExpression
        {
        match(input,66,FOLLOW_66_in_synpred233_Java5047); if (state.failed) return ;
        pushFollow(FOLLOW_primitiveType_in_synpred233_Java5049);
        primitiveType();

        state._fsp--;
        if (state.failed) return ;
        match(input,67,FOLLOW_67_in_synpred233_Java5051); if (state.failed) return ;
        pushFollow(FOLLOW_unaryExpression_in_synpred233_Java5053);
        unaryExpression();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred233_Java

    // $ANTLR start synpred234_Java
    public final void synpred234_Java_fragment() throws RecognitionException {   
        // Java.g:834:13: ( type )
        // Java.g:834:13: type
        {
        pushFollow(FOLLOW_type_in_synpred234_Java5065);
        type();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred234_Java

    // $ANTLR start synpred236_Java
    public final void synpred236_Java_fragment() throws RecognitionException {   
        // Java.g:839:17: ( '.' Identifier )
        // Java.g:839:17: '.' Identifier
        {
        match(input,29,FOLLOW_29_in_synpred236_Java5106); if (state.failed) return ;
        match(input,Identifier,FOLLOW_Identifier_in_synpred236_Java5108); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred236_Java

    // $ANTLR start synpred237_Java
    public final void synpred237_Java_fragment() throws RecognitionException {   
        // Java.g:839:34: ( identifierSuffix )
        // Java.g:839:34: identifierSuffix
        {
        pushFollow(FOLLOW_identifierSuffix_in_synpred237_Java5112);
        identifierSuffix();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred237_Java

    // $ANTLR start synpred242_Java
    public final void synpred242_Java_fragment() throws RecognitionException {   
        // Java.g:843:21: ( '.' Identifier )
        // Java.g:843:21: '.' Identifier
        {
        match(input,29,FOLLOW_29_in_synpred242_Java5160); if (state.failed) return ;
        match(input,Identifier,FOLLOW_Identifier_in_synpred242_Java5162); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred242_Java

    // $ANTLR start synpred243_Java
    public final void synpred243_Java_fragment() throws RecognitionException {   
        // Java.g:843:38: ( identifierSuffix )
        // Java.g:843:38: identifierSuffix
        {
        pushFollow(FOLLOW_identifierSuffix_in_synpred243_Java5166);
        identifierSuffix();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred243_Java

    // $ANTLR start synpred249_Java
    public final void synpred249_Java_fragment() throws RecognitionException {   
        // Java.g:850:10: ( '[' expression ']' )
        // Java.g:850:10: '[' expression ']'
        {
        match(input,48,FOLLOW_48_in_synpred249_Java5241); if (state.failed) return ;
        pushFollow(FOLLOW_expression_in_synpred249_Java5243);
        expression();

        state._fsp--;
        if (state.failed) return ;
        match(input,49,FOLLOW_49_in_synpred249_Java5245); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred249_Java

    // $ANTLR start synpred262_Java
    public final void synpred262_Java_fragment() throws RecognitionException {   
        // Java.g:876:29: ( '[' expression ']' )
        // Java.g:876:29: '[' expression ']'
        {
        match(input,48,FOLLOW_48_in_synpred262_Java5481); if (state.failed) return ;
        pushFollow(FOLLOW_expression_in_synpred262_Java5483);
        expression();

        state._fsp--;
        if (state.failed) return ;
        match(input,49,FOLLOW_49_in_synpred262_Java5485); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred262_Java

    // Delegated rules

    public final boolean synpred199_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred199_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred128_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred128_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred182_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred182_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred249_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred249_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred181_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred181_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred152_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred152_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred5_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred217_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred217_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred178_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred178_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred262_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred262_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred162_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred162_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred236_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred236_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred186_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred186_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred198_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred198_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred216_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred216_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred151_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred151_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred229_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred229_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred215_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred215_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred234_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred234_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred117_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred117_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred180_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred180_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred211_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred211_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred113_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred113_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred212_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred212_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred243_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred243_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred157_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred157_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred242_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred242_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred237_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred237_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred200_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred200_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred233_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred233_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred163_Java() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred163_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA8 dfa8 = new DFA8(this);
    protected DFA81 dfa81 = new DFA81(this);
    protected DFA85 dfa85 = new DFA85(this);
    protected DFA106 dfa106 = new DFA106(this);
    protected DFA114 dfa114 = new DFA114(this);
    protected DFA123 dfa123 = new DFA123(this);
    protected DFA124 dfa124 = new DFA124(this);
    protected DFA127 dfa127 = new DFA127(this);
    protected DFA139 dfa139 = new DFA139(this);
    protected DFA145 dfa145 = new DFA145(this);
    protected DFA146 dfa146 = new DFA146(this);
    protected DFA149 dfa149 = new DFA149(this);
    protected DFA151 dfa151 = new DFA151(this);
    protected DFA156 dfa156 = new DFA156(this);
    protected DFA155 dfa155 = new DFA155(this);
    protected DFA162 dfa162 = new DFA162(this);
    static final String DFA8_eotS =
        "\21\uffff";
    static final String DFA8_eofS =
        "\1\2\20\uffff";
    static final String DFA8_minS =
        "\1\5\1\0\17\uffff";
    static final String DFA8_maxS =
        "\1\111\1\0\17\uffff";
    static final String DFA8_acceptS =
        "\2\uffff\1\2\15\uffff\1\1";
    static final String DFA8_specialS =
        "\1\uffff\1\0\17\uffff}>";
    static final String[] DFA8_transitionS = {
            "\1\2\23\uffff\4\2\2\uffff\7\2\10\uffff\1\2\32\uffff\1\1",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
    static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
    static final char[] DFA8_min = DFA.unpackEncodedStringToUnsignedChars(DFA8_minS);
    static final char[] DFA8_max = DFA.unpackEncodedStringToUnsignedChars(DFA8_maxS);
    static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
    static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
    static final short[][] DFA8_transition;

    static {
        int numStates = DFA8_transitionS.length;
        DFA8_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
        }
    }

    class DFA8 extends DFA {

        public DFA8(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 8;
            this.eot = DFA8_eot;
            this.eof = DFA8_eof;
            this.min = DFA8_min;
            this.max = DFA8_max;
            this.accept = DFA8_accept;
            this.special = DFA8_special;
            this.transition = DFA8_transition;
        }
        public String getDescription() {
            return "179:1: compilationUnit : ( annotations ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* ) | ( packageDeclaration )? ( importDeclaration )* ( typeDeclaration )* );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA8_1 = input.LA(1);

                         
                        int index8_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Java()) ) {s = 16;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index8_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 8, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA81_eotS =
        "\57\uffff";
    static final String DFA81_eofS =
        "\57\uffff";
    static final String DFA81_minS =
        "\1\4\1\uffff\15\0\40\uffff";
    static final String DFA81_maxS =
        "\1\161\1\uffff\15\0\40\uffff";
    static final String DFA81_acceptS =
        "\1\uffff\1\1\15\uffff\1\2\37\uffff";
    static final String DFA81_specialS =
        "\2\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\40\uffff}>";
    static final String[] DFA81_transitionS = {
            "\1\14\1\17\1\6\1\7\1\10\3\5\1\17\15\uffff\1\17\1\uffff\1\17"+
            "\2\uffff\7\17\2\uffff\1\1\3\uffff\3\17\1\16\5\uffff\1\17\2\uffff"+
            "\10\15\1\uffff\1\4\1\3\2\uffff\1\2\1\12\2\11\1\17\2\uffff\1"+
            "\17\1\uffff\4\17\1\uffff\5\17\21\uffff\2\17\2\uffff\4\17\1\13",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA81_eot = DFA.unpackEncodedString(DFA81_eotS);
    static final short[] DFA81_eof = DFA.unpackEncodedString(DFA81_eofS);
    static final char[] DFA81_min = DFA.unpackEncodedStringToUnsignedChars(DFA81_minS);
    static final char[] DFA81_max = DFA.unpackEncodedStringToUnsignedChars(DFA81_maxS);
    static final short[] DFA81_accept = DFA.unpackEncodedString(DFA81_acceptS);
    static final short[] DFA81_special = DFA.unpackEncodedString(DFA81_specialS);
    static final short[][] DFA81_transition;

    static {
        int numStates = DFA81_transitionS.length;
        DFA81_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA81_transition[i] = DFA.unpackEncodedString(DFA81_transitionS[i]);
        }
    }

    class DFA81 extends DFA {

        public DFA81(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 81;
            this.eot = DFA81_eot;
            this.eof = DFA81_eof;
            this.min = DFA81_min;
            this.max = DFA81_max;
            this.accept = DFA81_accept;
            this.special = DFA81_special;
            this.transition = DFA81_transition;
        }
        public String getDescription() {
            return "494:13: ( explicitConstructorInvocation )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA81_2 = input.LA(1);

                         
                        int index81_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_2);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA81_3 = input.LA(1);

                         
                        int index81_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_3);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA81_4 = input.LA(1);

                         
                        int index81_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_4);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA81_5 = input.LA(1);

                         
                        int index81_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_5);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA81_6 = input.LA(1);

                         
                        int index81_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_6);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA81_7 = input.LA(1);

                         
                        int index81_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_7);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA81_8 = input.LA(1);

                         
                        int index81_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_8);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA81_9 = input.LA(1);

                         
                        int index81_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_9);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA81_10 = input.LA(1);

                         
                        int index81_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_10);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA81_11 = input.LA(1);

                         
                        int index81_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_11);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA81_12 = input.LA(1);

                         
                        int index81_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_12);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA81_13 = input.LA(1);

                         
                        int index81_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_13);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA81_14 = input.LA(1);

                         
                        int index81_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_14);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 81, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA85_eotS =
        "\17\uffff";
    static final String DFA85_eofS =
        "\17\uffff";
    static final String DFA85_minS =
        "\1\4\1\uffff\1\0\1\uffff\1\0\12\uffff";
    static final String DFA85_maxS =
        "\1\161\1\uffff\1\0\1\uffff\1\0\12\uffff";
    static final String DFA85_acceptS =
        "\1\uffff\1\1\1\uffff\1\2\13\uffff";
    static final String DFA85_specialS =
        "\2\uffff\1\0\1\uffff\1\1\12\uffff}>";
    static final String[] DFA85_transitionS = {
            "\1\3\1\uffff\6\3\34\uffff\1\1\6\uffff\1\3\10\uffff\10\3\1\uffff"+
            "\1\4\1\3\2\uffff\1\2\3\3\50\uffff\1\3",
            "",
            "\1\uffff",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA85_eot = DFA.unpackEncodedString(DFA85_eotS);
    static final short[] DFA85_eof = DFA.unpackEncodedString(DFA85_eofS);
    static final char[] DFA85_min = DFA.unpackEncodedStringToUnsignedChars(DFA85_minS);
    static final char[] DFA85_max = DFA.unpackEncodedStringToUnsignedChars(DFA85_maxS);
    static final short[] DFA85_accept = DFA.unpackEncodedString(DFA85_acceptS);
    static final short[] DFA85_special = DFA.unpackEncodedString(DFA85_specialS);
    static final short[][] DFA85_transition;

    static {
        int numStates = DFA85_transitionS.length;
        DFA85_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA85_transition[i] = DFA.unpackEncodedString(DFA85_transitionS[i]);
        }
    }

    class DFA85 extends DFA {

        public DFA85(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 85;
            this.eot = DFA85_eot;
            this.eof = DFA85_eof;
            this.min = DFA85_min;
            this.max = DFA85_max;
            this.accept = DFA85_accept;
            this.special = DFA85_special;
            this.transition = DFA85_transition;
        }
        public String getDescription() {
            return "497:1: explicitConstructorInvocation : ( ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';' | primary '.' ( nonWildcardTypeArguments )? 'super' arguments ';' );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA85_2 = input.LA(1);

                         
                        int index85_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred117_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 3;}

                         
                        input.seek(index85_2);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA85_4 = input.LA(1);

                         
                        int index85_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred117_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 3;}

                         
                        input.seek(index85_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 85, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA106_eotS =
        "\56\uffff";
    static final String DFA106_eofS =
        "\56\uffff";
    static final String DFA106_minS =
        "\1\4\4\0\51\uffff";
    static final String DFA106_maxS =
        "\1\161\4\0\51\uffff";
    static final String DFA106_acceptS =
        "\5\uffff\1\2\10\uffff\1\3\36\uffff\1\1";
    static final String DFA106_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\51\uffff}>";
    static final String[] DFA106_transitionS = {
            "\1\3\1\5\7\16\15\uffff\1\16\1\uffff\1\5\2\uffff\4\5\1\1\2\5"+
            "\6\uffff\1\16\1\uffff\1\5\1\16\5\uffff\1\16\2\uffff\10\4\1\uffff"+
            "\2\16\2\uffff\4\16\1\2\2\uffff\1\16\1\uffff\4\16\1\uffff\5\16"+
            "\21\uffff\2\16\2\uffff\5\16",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA106_eot = DFA.unpackEncodedString(DFA106_eotS);
    static final short[] DFA106_eof = DFA.unpackEncodedString(DFA106_eofS);
    static final char[] DFA106_min = DFA.unpackEncodedStringToUnsignedChars(DFA106_minS);
    static final char[] DFA106_max = DFA.unpackEncodedStringToUnsignedChars(DFA106_maxS);
    static final short[] DFA106_accept = DFA.unpackEncodedString(DFA106_acceptS);
    static final short[] DFA106_special = DFA.unpackEncodedString(DFA106_specialS);
    static final short[][] DFA106_transition;

    static {
        int numStates = DFA106_transitionS.length;
        DFA106_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA106_transition[i] = DFA.unpackEncodedString(DFA106_transitionS[i]);
        }
    }

    class DFA106 extends DFA {

        public DFA106(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 106;
            this.eot = DFA106_eot;
            this.eof = DFA106_eof;
            this.min = DFA106_min;
            this.max = DFA106_max;
            this.accept = DFA106_accept;
            this.special = DFA106_special;
            this.transition = DFA106_transition;
        }
        public String getDescription() {
            return "602:1: blockStatement : ( localVariableDeclarationStatement | classOrInterfaceDeclaration | statement );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA106_1 = input.LA(1);

                         
                        int index106_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred151_Java()) ) {s = 45;}

                        else if ( (synpred152_Java()) ) {s = 5;}

                         
                        input.seek(index106_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA106_2 = input.LA(1);

                         
                        int index106_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred151_Java()) ) {s = 45;}

                        else if ( (synpred152_Java()) ) {s = 5;}

                         
                        input.seek(index106_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA106_3 = input.LA(1);

                         
                        int index106_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred151_Java()) ) {s = 45;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index106_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA106_4 = input.LA(1);

                         
                        int index106_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred151_Java()) ) {s = 45;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index106_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 106, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA114_eotS =
        "\22\uffff";
    static final String DFA114_eofS =
        "\22\uffff";
    static final String DFA114_minS =
        "\1\4\17\uffff\1\32\1\uffff";
    static final String DFA114_maxS =
        "\1\161\17\uffff\1\156\1\uffff";
    static final String DFA114_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1"+
        "\15\1\16\1\17\1\uffff\1\20";
    static final String DFA114_specialS =
        "\22\uffff}>";
    static final String[] DFA114_transitionS = {
            "\1\20\1\uffff\6\17\1\2\15\uffff\1\16\21\uffff\1\1\2\uffff\1"+
            "\17\5\uffff\1\11\2\uffff\10\17\1\uffff\2\17\2\uffff\4\17\3\uffff"+
            "\1\3\1\uffff\1\4\1\5\1\6\1\7\1\uffff\1\10\1\12\1\13\1\14\1\15"+
            "\21\uffff\2\17\2\uffff\5\17",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\17\2\uffff\2\17\11\uffff\1\17\1\uffff\2\17\4\uffff\1\17"+
            "\2\uffff\1\17\14\uffff\1\17\1\uffff\1\17\10\uffff\1\21\16\uffff"+
            "\25\17",
            ""
    };

    static final short[] DFA114_eot = DFA.unpackEncodedString(DFA114_eotS);
    static final short[] DFA114_eof = DFA.unpackEncodedString(DFA114_eofS);
    static final char[] DFA114_min = DFA.unpackEncodedStringToUnsignedChars(DFA114_minS);
    static final char[] DFA114_max = DFA.unpackEncodedStringToUnsignedChars(DFA114_maxS);
    static final short[] DFA114_accept = DFA.unpackEncodedString(DFA114_acceptS);
    static final short[] DFA114_special = DFA.unpackEncodedString(DFA114_specialS);
    static final short[][] DFA114_transition;

    static {
        int numStates = DFA114_transitionS.length;
        DFA114_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA114_transition[i] = DFA.unpackEncodedString(DFA114_transitionS[i]);
        }
    }

    class DFA114 extends DFA {

        public DFA114(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 114;
            this.eot = DFA114_eot;
            this.eof = DFA114_eof;
            this.min = DFA114_min;
            this.max = DFA114_max;
            this.accept = DFA114_accept;
            this.special = DFA114_special;
            this.transition = DFA114_transition;
        }
        public String getDescription() {
            return "620:1: statement : ( block | ASSERT expression ( ':' expression )? ';' | 'if' parExpression statement ( options {k=1; } : 'else' statement )? | 'for' '(' forControl ')' statement | 'while' parExpression statement | 'do' statement 'while' parExpression ';' | 'try' block ( catches 'finally' block | catches | 'finally' block ) | 'switch' parExpression '{' switchBlockStatementGroups '}' | 'synchronized' parExpression block | 'return' ( expression )? ';' | 'throw' expression ';' | 'break' ( Identifier )? ';' | 'continue' ( Identifier )? ';' | ';' | statementExpression ';' | Identifier ':' statement );";
        }
    }
    static final String DFA123_eotS =
        "\u0087\uffff";
    static final String DFA123_eofS =
        "\u0087\uffff";
    static final String DFA123_minS =
        "\5\4\22\uffff\7\4\4\uffff\1\4\24\uffff\1\32\1\61\1\uffff\1\32\22"+
        "\0\5\uffff\1\0\45\uffff\2\0\1\uffff\1\0\5\uffff\1\0\5\uffff";
    static final String DFA123_maxS =
        "\1\161\1\111\1\4\1\156\1\60\22\uffff\2\60\1\111\1\4\1\111\2\161"+
        "\4\uffff\1\161\24\uffff\1\113\1\61\1\uffff\1\113\22\0\5\uffff\1"+
        "\0\45\uffff\2\0\1\uffff\1\0\5\uffff\1\0\5\uffff";
    static final String DFA123_acceptS =
        "\5\uffff\1\2\166\uffff\1\1\12\uffff";
    static final String DFA123_specialS =
        "\73\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\1\15\1\16\1\17\1\20\1\21\5\uffff\1\22\45\uffff\1\23\1\24\1\uffff"+
        "\1\25\5\uffff\1\26\5\uffff}>";
    static final String[] DFA123_transitionS = {
            "\1\3\1\uffff\6\5\16\uffff\1\5\10\uffff\1\1\13\uffff\1\5\10\uffff"+
            "\10\4\1\uffff\2\5\2\uffff\4\5\1\2\37\uffff\2\5\2\uffff\5\5",
            "\1\27\36\uffff\1\31\24\uffff\10\30\11\uffff\1\32",
            "\1\33",
            "\1\67\25\uffff\1\5\2\uffff\1\34\1\5\11\uffff\1\42\3\5\4\uffff"+
            "\1\35\2\uffff\1\5\14\uffff\1\5\1\uffff\1\5\27\uffff\25\5",
            "\1\72\30\uffff\1\5\22\uffff\1\70",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\76\30\uffff\1\74\12\uffff\1\73\7\uffff\1\75",
            "\1\100\53\uffff\1\77",
            "\1\101\36\uffff\1\103\24\uffff\10\102\11\uffff\1\104",
            "\1\105",
            "\1\110\30\uffff\1\106\5\uffff\1\112\24\uffff\10\111\2\uffff"+
            "\1\107\6\uffff\1\113",
            "\1\114\40\uffff\1\5\2\uffff\1\5\30\uffff\1\5\3\uffff\1\5\53"+
            "\uffff\1\5",
            "\1\5\1\uffff\6\5\43\uffff\1\5\1\uffff\1\122\6\uffff\10\5\1"+
            "\uffff\2\5\2\uffff\4\5\40\uffff\2\5\2\uffff\5\5",
            "",
            "",
            "",
            "",
            "\1\170\1\uffff\6\5\34\uffff\1\5\6\uffff\1\5\3\uffff\1\5\4\uffff"+
            "\10\171\1\173\2\5\2\uffff\4\5\40\uffff\2\5\2\uffff\5\5",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\5\16\uffff\1\5\6\uffff\1\5\2\uffff\1\5\27\uffff\1\174",
            "\1\u0081",
            "",
            "\1\5\16\uffff\1\5\6\uffff\1\5\2\uffff\1\5\27\uffff\1\174",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA123_eot = DFA.unpackEncodedString(DFA123_eotS);
    static final short[] DFA123_eof = DFA.unpackEncodedString(DFA123_eofS);
    static final char[] DFA123_min = DFA.unpackEncodedStringToUnsignedChars(DFA123_minS);
    static final char[] DFA123_max = DFA.unpackEncodedStringToUnsignedChars(DFA123_maxS);
    static final short[] DFA123_accept = DFA.unpackEncodedString(DFA123_acceptS);
    static final short[] DFA123_special = DFA.unpackEncodedString(DFA123_specialS);
    static final short[][] DFA123_transition;

    static {
        int numStates = DFA123_transitionS.length;
        DFA123_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA123_transition[i] = DFA.unpackEncodedString(DFA123_transitionS[i]);
        }
    }

    class DFA123 extends DFA {

        public DFA123(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 123;
            this.eot = DFA123_eot;
            this.eof = DFA123_eof;
            this.min = DFA123_min;
            this.max = DFA123_max;
            this.accept = DFA123_accept;
            this.special = DFA123_special;
            this.transition = DFA123_transition;
        }
        public String getDescription() {
            return "673:1: forControl options {k=3; } : ( enhancedForControl | ( forInit )? ';' ( expression )? ';' ( forUpdate )? );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA123_59 = input.LA(1);

                         
                        int index123_59 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_59);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA123_60 = input.LA(1);

                         
                        int index123_60 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_60);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA123_61 = input.LA(1);

                         
                        int index123_61 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_61);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA123_62 = input.LA(1);

                         
                        int index123_62 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_62);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA123_63 = input.LA(1);

                         
                        int index123_63 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_63);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA123_64 = input.LA(1);

                         
                        int index123_64 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_64);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA123_65 = input.LA(1);

                         
                        int index123_65 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_65);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA123_66 = input.LA(1);

                         
                        int index123_66 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_66);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA123_67 = input.LA(1);

                         
                        int index123_67 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_67);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA123_68 = input.LA(1);

                         
                        int index123_68 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_68);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA123_69 = input.LA(1);

                         
                        int index123_69 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_69);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA123_70 = input.LA(1);

                         
                        int index123_70 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_70);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA123_71 = input.LA(1);

                         
                        int index123_71 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_71);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA123_72 = input.LA(1);

                         
                        int index123_72 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_72);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA123_73 = input.LA(1);

                         
                        int index123_73 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_73);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA123_74 = input.LA(1);

                         
                        int index123_74 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_74);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA123_75 = input.LA(1);

                         
                        int index123_75 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_75);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA123_76 = input.LA(1);

                         
                        int index123_76 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_76);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA123_82 = input.LA(1);

                         
                        int index123_82 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_82);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA123_120 = input.LA(1);

                         
                        int index123_120 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_120);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA123_121 = input.LA(1);

                         
                        int index123_121 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_121);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA123_123 = input.LA(1);

                         
                        int index123_123 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_123);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA123_129 = input.LA(1);

                         
                        int index123_129 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_129);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 123, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA124_eotS =
        "\26\uffff";
    static final String DFA124_eofS =
        "\26\uffff";
    static final String DFA124_minS =
        "\1\4\2\uffff\2\0\21\uffff";
    static final String DFA124_maxS =
        "\1\161\2\uffff\2\0\21\uffff";
    static final String DFA124_acceptS =
        "\1\uffff\1\1\3\uffff\1\2\20\uffff";
    static final String DFA124_specialS =
        "\3\uffff\1\0\1\1\21\uffff}>";
    static final String[] DFA124_transitionS = {
            "\1\3\1\uffff\6\5\27\uffff\1\1\13\uffff\1\5\10\uffff\10\4\1\uffff"+
            "\2\5\2\uffff\4\5\1\1\37\uffff\2\5\2\uffff\5\5",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA124_eot = DFA.unpackEncodedString(DFA124_eotS);
    static final short[] DFA124_eof = DFA.unpackEncodedString(DFA124_eofS);
    static final char[] DFA124_min = DFA.unpackEncodedStringToUnsignedChars(DFA124_minS);
    static final char[] DFA124_max = DFA.unpackEncodedStringToUnsignedChars(DFA124_maxS);
    static final short[] DFA124_accept = DFA.unpackEncodedString(DFA124_acceptS);
    static final short[] DFA124_special = DFA.unpackEncodedString(DFA124_specialS);
    static final short[][] DFA124_transition;

    static {
        int numStates = DFA124_transitionS.length;
        DFA124_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA124_transition[i] = DFA.unpackEncodedString(DFA124_transitionS[i]);
        }
    }

    class DFA124 extends DFA {

        public DFA124(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 124;
            this.eot = DFA124_eot;
            this.eof = DFA124_eof;
            this.min = DFA124_min;
            this.max = DFA124_max;
            this.accept = DFA124_accept;
            this.special = DFA124_special;
            this.transition = DFA124_transition;
        }
        public String getDescription() {
            return "679:1: forInit : ( localVariableDeclaration | expressionList );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA124_3 = input.LA(1);

                         
                        int index124_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred186_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index124_3);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA124_4 = input.LA(1);

                         
                        int index124_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred186_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index124_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 124, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA127_eotS =
        "\17\uffff";
    static final String DFA127_eofS =
        "\17\uffff";
    static final String DFA127_minS =
        "\1\50\12\uffff\2\52\2\uffff";
    static final String DFA127_maxS =
        "\1\141\12\uffff\1\52\1\63\2\uffff";
    static final String DFA127_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\2\uffff\1\13"+
        "\1\14";
    static final String DFA127_specialS =
        "\1\1\13\uffff\1\0\2\uffff}>";
    static final String[] DFA127_transitionS = {
            "\1\12\1\uffff\1\13\10\uffff\1\1\46\uffff\1\2\1\3\1\4\1\5\1\6"+
            "\1\7\1\10\1\11",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\14",
            "\1\15\10\uffff\1\16",
            "",
            ""
    };

    static final short[] DFA127_eot = DFA.unpackEncodedString(DFA127_eotS);
    static final short[] DFA127_eof = DFA.unpackEncodedString(DFA127_eofS);
    static final char[] DFA127_min = DFA.unpackEncodedStringToUnsignedChars(DFA127_minS);
    static final char[] DFA127_max = DFA.unpackEncodedStringToUnsignedChars(DFA127_maxS);
    static final short[] DFA127_accept = DFA.unpackEncodedString(DFA127_acceptS);
    static final short[] DFA127_special = DFA.unpackEncodedString(DFA127_specialS);
    static final short[][] DFA127_transition;

    static {
        int numStates = DFA127_transitionS.length;
        DFA127_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA127_transition[i] = DFA.unpackEncodedString(DFA127_transitionS[i]);
        }
    }

    class DFA127 extends DFA {

        public DFA127(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 127;
            this.eot = DFA127_eot;
            this.eof = DFA127_eof;
            this.min = DFA127_min;
            this.max = DFA127_max;
            this.accept = DFA127_accept;
            this.special = DFA127_special;
            this.transition = DFA127_transition;
        }
        public String getDescription() {
            return "714:1: assignmentOperator : ( '=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '%=' | ( '<' '<' '=' )=>t1= '<' t2= '<' t3= '=' {...}? | ( '>' '>' '>' '=' )=>t1= '>' t2= '>' t3= '>' t4= '=' {...}? | ( '>' '>' '=' )=>t1= '>' t2= '>' t3= '=' {...}?);";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA127_12 = input.LA(1);

                         
                        int index127_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA127_12==42) && (synpred199_Java())) {s = 13;}

                        else if ( (LA127_12==51) && (synpred200_Java())) {s = 14;}

                         
                        input.seek(index127_12);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA127_0 = input.LA(1);

                         
                        int index127_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA127_0==51) ) {s = 1;}

                        else if ( (LA127_0==90) ) {s = 2;}

                        else if ( (LA127_0==91) ) {s = 3;}

                        else if ( (LA127_0==92) ) {s = 4;}

                        else if ( (LA127_0==93) ) {s = 5;}

                        else if ( (LA127_0==94) ) {s = 6;}

                        else if ( (LA127_0==95) ) {s = 7;}

                        else if ( (LA127_0==96) ) {s = 8;}

                        else if ( (LA127_0==97) ) {s = 9;}

                        else if ( (LA127_0==40) && (synpred198_Java())) {s = 10;}

                        else if ( (LA127_0==42) ) {s = 11;}

                         
                        input.seek(index127_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 127, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA139_eotS =
        "\30\uffff";
    static final String DFA139_eofS =
        "\30\uffff";
    static final String DFA139_minS =
        "\1\50\1\uffff\1\52\1\4\24\uffff";
    static final String DFA139_maxS =
        "\1\52\1\uffff\1\52\1\161\24\uffff";
    static final String DFA139_acceptS =
        "\1\uffff\1\1\2\uffff\1\2\23\3";
    static final String DFA139_specialS =
        "\1\0\2\uffff\1\1\24\uffff}>";
    static final String[] DFA139_transitionS = {
            "\1\1\1\uffff\1\2",
            "",
            "\1\3",
            "\1\25\1\uffff\1\17\1\20\1\21\3\16\36\uffff\1\4\4\uffff\1\27"+
            "\10\uffff\10\26\1\uffff\1\15\1\13\2\uffff\1\14\1\23\2\22\40"+
            "\uffff\1\5\1\6\2\uffff\1\7\1\10\1\11\1\12\1\24",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA139_eot = DFA.unpackEncodedString(DFA139_eotS);
    static final short[] DFA139_eof = DFA.unpackEncodedString(DFA139_eofS);
    static final char[] DFA139_min = DFA.unpackEncodedStringToUnsignedChars(DFA139_minS);
    static final char[] DFA139_max = DFA.unpackEncodedStringToUnsignedChars(DFA139_maxS);
    static final short[] DFA139_accept = DFA.unpackEncodedString(DFA139_acceptS);
    static final short[] DFA139_special = DFA.unpackEncodedString(DFA139_specialS);
    static final short[][] DFA139_transition;

    static {
        int numStates = DFA139_transitionS.length;
        DFA139_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA139_transition[i] = DFA.unpackEncodedString(DFA139_transitionS[i]);
        }
    }

    class DFA139 extends DFA {

        public DFA139(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 139;
            this.eot = DFA139_eot;
            this.eof = DFA139_eof;
            this.min = DFA139_min;
            this.max = DFA139_max;
            this.accept = DFA139_accept;
            this.special = DFA139_special;
            this.transition = DFA139_transition;
        }
        public String getDescription() {
            return "794:1: shiftOp : ( ( '<' '<' )=>t1= '<' t2= '<' {...}? | ( '>' '>' '>' )=>t1= '>' t2= '>' t3= '>' {...}? | ( '>' '>' )=>t1= '>' t2= '>' {...}?);";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA139_0 = input.LA(1);

                         
                        int index139_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA139_0==40) && (synpred215_Java())) {s = 1;}

                        else if ( (LA139_0==42) ) {s = 2;}

                         
                        input.seek(index139_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA139_3 = input.LA(1);

                         
                        int index139_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA139_3==42) && (synpred216_Java())) {s = 4;}

                        else if ( (LA139_3==105) && (synpred217_Java())) {s = 5;}

                        else if ( (LA139_3==106) && (synpred217_Java())) {s = 6;}

                        else if ( (LA139_3==109) && (synpred217_Java())) {s = 7;}

                        else if ( (LA139_3==110) && (synpred217_Java())) {s = 8;}

                        else if ( (LA139_3==111) && (synpred217_Java())) {s = 9;}

                        else if ( (LA139_3==112) && (synpred217_Java())) {s = 10;}

                        else if ( (LA139_3==66) && (synpred217_Java())) {s = 11;}

                        else if ( (LA139_3==69) && (synpred217_Java())) {s = 12;}

                        else if ( (LA139_3==65) && (synpred217_Java())) {s = 13;}

                        else if ( ((LA139_3>=HexLiteral && LA139_3<=DecimalLiteral)) && (synpred217_Java())) {s = 14;}

                        else if ( (LA139_3==FloatingPointLiteral) && (synpred217_Java())) {s = 15;}

                        else if ( (LA139_3==CharacterLiteral) && (synpred217_Java())) {s = 16;}

                        else if ( (LA139_3==StringLiteral) && (synpred217_Java())) {s = 17;}

                        else if ( ((LA139_3>=71 && LA139_3<=72)) && (synpred217_Java())) {s = 18;}

                        else if ( (LA139_3==70) && (synpred217_Java())) {s = 19;}

                        else if ( (LA139_3==113) && (synpred217_Java())) {s = 20;}

                        else if ( (LA139_3==Identifier) && (synpred217_Java())) {s = 21;}

                        else if ( ((LA139_3>=56 && LA139_3<=63)) && (synpred217_Java())) {s = 22;}

                        else if ( (LA139_3==47) && (synpred217_Java())) {s = 23;}

                         
                        input.seek(index139_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 139, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA145_eotS =
        "\21\uffff";
    static final String DFA145_eofS =
        "\21\uffff";
    static final String DFA145_minS =
        "\1\4\2\uffff\1\0\15\uffff";
    static final String DFA145_maxS =
        "\1\161\2\uffff\1\0\15\uffff";
    static final String DFA145_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\13\uffff\1\3";
    static final String DFA145_specialS =
        "\3\uffff\1\0\15\uffff}>";
    static final String[] DFA145_transitionS = {
            "\1\4\1\uffff\6\4\43\uffff\1\4\10\uffff\10\4\1\uffff\1\4\1\3"+
            "\2\uffff\4\4\46\uffff\1\1\1\2\1\4",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA145_eot = DFA.unpackEncodedString(DFA145_eotS);
    static final short[] DFA145_eof = DFA.unpackEncodedString(DFA145_eofS);
    static final char[] DFA145_min = DFA.unpackEncodedStringToUnsignedChars(DFA145_minS);
    static final char[] DFA145_max = DFA.unpackEncodedStringToUnsignedChars(DFA145_maxS);
    static final short[] DFA145_accept = DFA.unpackEncodedString(DFA145_acceptS);
    static final short[] DFA145_special = DFA.unpackEncodedString(DFA145_specialS);
    static final short[][] DFA145_transition;

    static {
        int numStates = DFA145_transitionS.length;
        DFA145_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA145_transition[i] = DFA.unpackEncodedString(DFA145_transitionS[i]);
        }
    }

    class DFA145 extends DFA {

        public DFA145(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 145;
            this.eot = DFA145_eot;
            this.eof = DFA145_eof;
            this.min = DFA145_min;
            this.max = DFA145_max;
            this.accept = DFA145_accept;
            this.special = DFA145_special;
            this.transition = DFA145_transition;
        }
        public String getDescription() {
            return "825:1: unaryExpressionNotPlusMinus : ( '~' unaryExpression | '!' unaryExpression | castExpression | primary ( selector )* ( '++' | '--' )? );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA145_3 = input.LA(1);

                         
                        int index145_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred229_Java()) ) {s = 16;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index145_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 145, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA146_eotS =
        "\7\uffff";
    static final String DFA146_eofS =
        "\7\uffff";
    static final String DFA146_minS =
        "\1\4\1\0\1\35\2\uffff\1\61\1\35";
    static final String DFA146_maxS =
        "\1\161\1\0\1\103\2\uffff\1\61\1\103";
    static final String DFA146_acceptS =
        "\3\uffff\1\2\1\1\2\uffff";
    static final String DFA146_specialS =
        "\1\uffff\1\0\5\uffff}>";
    static final String[] DFA146_transitionS = {
            "\1\1\1\uffff\6\3\43\uffff\1\3\10\uffff\10\2\1\uffff\2\3\2\uffff"+
            "\4\3\40\uffff\2\3\2\uffff\5\3",
            "\1\uffff",
            "\1\3\22\uffff\1\5\22\uffff\1\4",
            "",
            "",
            "\1\6",
            "\1\3\22\uffff\1\5\22\uffff\1\4"
    };

    static final short[] DFA146_eot = DFA.unpackEncodedString(DFA146_eotS);
    static final short[] DFA146_eof = DFA.unpackEncodedString(DFA146_eofS);
    static final char[] DFA146_min = DFA.unpackEncodedStringToUnsignedChars(DFA146_minS);
    static final char[] DFA146_max = DFA.unpackEncodedStringToUnsignedChars(DFA146_maxS);
    static final short[] DFA146_accept = DFA.unpackEncodedString(DFA146_acceptS);
    static final short[] DFA146_special = DFA.unpackEncodedString(DFA146_specialS);
    static final short[][] DFA146_transition;

    static {
        int numStates = DFA146_transitionS.length;
        DFA146_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA146_transition[i] = DFA.unpackEncodedString(DFA146_transitionS[i]);
        }
    }

    class DFA146 extends DFA {

        public DFA146(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 146;
            this.eot = DFA146_eot;
            this.eof = DFA146_eof;
            this.min = DFA146_min;
            this.max = DFA146_max;
            this.accept = DFA146_accept;
            this.special = DFA146_special;
            this.transition = DFA146_transition;
        }
        public String getDescription() {
            return "834:12: ( type | expression )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA146_1 = input.LA(1);

                         
                        int index146_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred234_Java()) ) {s = 4;}

                        else if ( (true) ) {s = 3;}

                         
                        input.seek(index146_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 146, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA149_eotS =
        "\41\uffff";
    static final String DFA149_eofS =
        "\1\4\40\uffff";
    static final String DFA149_minS =
        "\1\32\1\0\1\uffff\1\0\35\uffff";
    static final String DFA149_maxS =
        "\1\156\1\0\1\uffff\1\0\35\uffff";
    static final String DFA149_acceptS =
        "\2\uffff\1\1\1\uffff\1\2\34\uffff";
    static final String DFA149_specialS =
        "\1\uffff\1\0\1\uffff\1\1\35\uffff}>";
    static final String[] DFA149_transitionS = {
            "\1\4\2\uffff\1\3\1\4\11\uffff\4\4\1\uffff\1\4\2\uffff\1\1\1"+
            "\4\1\uffff\1\4\14\uffff\1\4\1\uffff\1\2\1\4\7\uffff\1\4\16\uffff"+
            "\25\4",
            "\1\uffff",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA149_eot = DFA.unpackEncodedString(DFA149_eotS);
    static final short[] DFA149_eof = DFA.unpackEncodedString(DFA149_eofS);
    static final char[] DFA149_min = DFA.unpackEncodedStringToUnsignedChars(DFA149_minS);
    static final char[] DFA149_max = DFA.unpackEncodedStringToUnsignedChars(DFA149_maxS);
    static final short[] DFA149_accept = DFA.unpackEncodedString(DFA149_acceptS);
    static final short[] DFA149_special = DFA.unpackEncodedString(DFA149_specialS);
    static final short[][] DFA149_transition;

    static {
        int numStates = DFA149_transitionS.length;
        DFA149_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA149_transition[i] = DFA.unpackEncodedString(DFA149_transitionS[i]);
        }
    }

    class DFA149 extends DFA {

        public DFA149(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 149;
            this.eot = DFA149_eot;
            this.eof = DFA149_eof;
            this.min = DFA149_min;
            this.max = DFA149_max;
            this.accept = DFA149_accept;
            this.special = DFA149_special;
            this.transition = DFA149_transition;
        }
        public String getDescription() {
            return "839:34: ( identifierSuffix )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA149_1 = input.LA(1);

                         
                        int index149_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred237_Java()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index149_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA149_3 = input.LA(1);

                         
                        int index149_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred237_Java()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index149_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 149, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA151_eotS =
        "\41\uffff";
    static final String DFA151_eofS =
        "\1\4\40\uffff";
    static final String DFA151_minS =
        "\1\32\1\0\1\uffff\1\0\35\uffff";
    static final String DFA151_maxS =
        "\1\156\1\0\1\uffff\1\0\35\uffff";
    static final String DFA151_acceptS =
        "\2\uffff\1\1\1\uffff\1\2\34\uffff";
    static final String DFA151_specialS =
        "\1\uffff\1\0\1\uffff\1\1\35\uffff}>";
    static final String[] DFA151_transitionS = {
            "\1\4\2\uffff\1\3\1\4\11\uffff\4\4\1\uffff\1\4\2\uffff\1\1\1"+
            "\4\1\uffff\1\4\14\uffff\1\4\1\uffff\1\2\1\4\7\uffff\1\4\16\uffff"+
            "\25\4",
            "\1\uffff",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA151_eot = DFA.unpackEncodedString(DFA151_eotS);
    static final short[] DFA151_eof = DFA.unpackEncodedString(DFA151_eofS);
    static final char[] DFA151_min = DFA.unpackEncodedStringToUnsignedChars(DFA151_minS);
    static final char[] DFA151_max = DFA.unpackEncodedStringToUnsignedChars(DFA151_maxS);
    static final short[] DFA151_accept = DFA.unpackEncodedString(DFA151_acceptS);
    static final short[] DFA151_special = DFA.unpackEncodedString(DFA151_specialS);
    static final short[][] DFA151_transition;

    static {
        int numStates = DFA151_transitionS.length;
        DFA151_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA151_transition[i] = DFA.unpackEncodedString(DFA151_transitionS[i]);
        }
    }

    class DFA151 extends DFA {

        public DFA151(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 151;
            this.eot = DFA151_eot;
            this.eof = DFA151_eof;
            this.min = DFA151_min;
            this.max = DFA151_max;
            this.accept = DFA151_accept;
            this.special = DFA151_special;
            this.transition = DFA151_transition;
        }
        public String getDescription() {
            return "843:38: ( identifierSuffix )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA151_1 = input.LA(1);

                         
                        int index151_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred243_Java()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index151_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA151_3 = input.LA(1);

                         
                        int index151_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred243_Java()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index151_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 151, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA156_eotS =
        "\13\uffff";
    static final String DFA156_eofS =
        "\13\uffff";
    static final String DFA156_minS =
        "\1\35\1\4\1\uffff\1\45\7\uffff";
    static final String DFA156_maxS =
        "\1\102\1\161\1\uffff\1\161\7\uffff";
    static final String DFA156_acceptS =
        "\2\uffff\1\3\1\uffff\1\1\1\2\1\4\1\6\1\7\1\10\1\5";
    static final String DFA156_specialS =
        "\13\uffff}>";
    static final String[] DFA156_transitionS = {
            "\1\3\22\uffff\1\1\21\uffff\1\2",
            "\1\5\1\uffff\6\5\43\uffff\1\5\1\uffff\1\4\6\uffff\10\5\1\uffff"+
            "\2\5\2\uffff\4\5\40\uffff\2\5\2\uffff\5\5",
            "",
            "\1\6\2\uffff\1\12\30\uffff\1\10\3\uffff\1\7\53\uffff\1\11",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA156_eot = DFA.unpackEncodedString(DFA156_eotS);
    static final short[] DFA156_eof = DFA.unpackEncodedString(DFA156_eofS);
    static final char[] DFA156_min = DFA.unpackEncodedStringToUnsignedChars(DFA156_minS);
    static final char[] DFA156_max = DFA.unpackEncodedStringToUnsignedChars(DFA156_maxS);
    static final short[] DFA156_accept = DFA.unpackEncodedString(DFA156_acceptS);
    static final short[] DFA156_special = DFA.unpackEncodedString(DFA156_specialS);
    static final short[][] DFA156_transition;

    static {
        int numStates = DFA156_transitionS.length;
        DFA156_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA156_transition[i] = DFA.unpackEncodedString(DFA156_transitionS[i]);
        }
    }

    class DFA156 extends DFA {

        public DFA156(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 156;
            this.eot = DFA156_eot;
            this.eof = DFA156_eof;
            this.min = DFA156_min;
            this.max = DFA156_max;
            this.accept = DFA156_accept;
            this.special = DFA156_special;
            this.transition = DFA156_transition;
        }
        public String getDescription() {
            return "848:1: identifierSuffix : ( ( '[' ']' )+ '.' 'class' | ( '[' expression ']' )+ | arguments | '.' 'class' | '.' explicitGenericInvocation | '.' 'this' | '.' 'super' arguments | '.' 'new' innerCreator );";
        }
    }
    static final String DFA155_eotS =
        "\41\uffff";
    static final String DFA155_eofS =
        "\1\1\40\uffff";
    static final String DFA155_minS =
        "\1\32\1\uffff\1\0\36\uffff";
    static final String DFA155_maxS =
        "\1\156\1\uffff\1\0\36\uffff";
    static final String DFA155_acceptS =
        "\1\uffff\1\2\36\uffff\1\1";
    static final String DFA155_specialS =
        "\2\uffff\1\0\36\uffff}>";
    static final String[] DFA155_transitionS = {
            "\1\1\2\uffff\2\1\11\uffff\4\1\1\uffff\1\1\2\uffff\1\2\1\1\1"+
            "\uffff\1\1\14\uffff\1\1\2\uffff\1\1\7\uffff\1\1\16\uffff\25"+
            "\1",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA155_eot = DFA.unpackEncodedString(DFA155_eotS);
    static final short[] DFA155_eof = DFA.unpackEncodedString(DFA155_eofS);
    static final char[] DFA155_min = DFA.unpackEncodedStringToUnsignedChars(DFA155_minS);
    static final char[] DFA155_max = DFA.unpackEncodedStringToUnsignedChars(DFA155_maxS);
    static final short[] DFA155_accept = DFA.unpackEncodedString(DFA155_acceptS);
    static final short[] DFA155_special = DFA.unpackEncodedString(DFA155_specialS);
    static final short[][] DFA155_transition;

    static {
        int numStates = DFA155_transitionS.length;
        DFA155_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA155_transition[i] = DFA.unpackEncodedString(DFA155_transitionS[i]);
        }
    }

    class DFA155 extends DFA {

        public DFA155(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 155;
            this.eot = DFA155_eot;
            this.eof = DFA155_eof;
            this.min = DFA155_min;
            this.max = DFA155_max;
            this.accept = DFA155_accept;
            this.special = DFA155_special;
            this.transition = DFA155_transition;
        }
        public String getDescription() {
            return "()+ loopback of 850:9: ( '[' expression ']' )+";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA155_2 = input.LA(1);

                         
                        int index155_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred249_Java()) ) {s = 32;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index155_2);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 155, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA162_eotS =
        "\41\uffff";
    static final String DFA162_eofS =
        "\1\2\40\uffff";
    static final String DFA162_minS =
        "\1\32\1\0\37\uffff";
    static final String DFA162_maxS =
        "\1\156\1\0\37\uffff";
    static final String DFA162_acceptS =
        "\2\uffff\1\2\35\uffff\1\1";
    static final String DFA162_specialS =
        "\1\uffff\1\0\37\uffff}>";
    static final String[] DFA162_transitionS = {
            "\1\2\2\uffff\2\2\11\uffff\4\2\1\uffff\1\2\2\uffff\1\1\1\2\1"+
            "\uffff\1\2\14\uffff\1\2\2\uffff\1\2\7\uffff\1\2\16\uffff\25"+
            "\2",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA162_eot = DFA.unpackEncodedString(DFA162_eotS);
    static final short[] DFA162_eof = DFA.unpackEncodedString(DFA162_eofS);
    static final char[] DFA162_min = DFA.unpackEncodedStringToUnsignedChars(DFA162_minS);
    static final char[] DFA162_max = DFA.unpackEncodedStringToUnsignedChars(DFA162_maxS);
    static final short[] DFA162_accept = DFA.unpackEncodedString(DFA162_acceptS);
    static final short[] DFA162_special = DFA.unpackEncodedString(DFA162_specialS);
    static final short[][] DFA162_transition;

    static {
        int numStates = DFA162_transitionS.length;
        DFA162_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA162_transition[i] = DFA.unpackEncodedString(DFA162_transitionS[i]);
        }
    }

    class DFA162 extends DFA {

        public DFA162(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 162;
            this.eot = DFA162_eot;
            this.eof = DFA162_eof;
            this.min = DFA162_min;
            this.max = DFA162_max;
            this.accept = DFA162_accept;
            this.special = DFA162_special;
            this.transition = DFA162_transition;
        }
        public String getDescription() {
            return "()* loopback of 876:28: ( '[' expression ']' )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA162_1 = input.LA(1);

                         
                        int index162_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred262_Java()) ) {s = 32;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index162_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 162, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_annotations_in_compilationUnit44 = new BitSet(new long[]{0x0000403F92000020L,0x0000000000000200L});
    public static final BitSet FOLLOW_packageDeclaration_in_compilationUnit58 = new BitSet(new long[]{0x0000403F9E000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_importDeclaration_in_compilationUnit60 = new BitSet(new long[]{0x0000403F9E000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_typeDeclaration_in_compilationUnit63 = new BitSet(new long[]{0x0000403F96000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_compilationUnit78 = new BitSet(new long[]{0x0000403F96000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_typeDeclaration_in_compilationUnit80 = new BitSet(new long[]{0x0000403F96000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_packageDeclaration_in_compilationUnit101 = new BitSet(new long[]{0x0000403F9E000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_importDeclaration_in_compilationUnit104 = new BitSet(new long[]{0x0000403F9E000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_typeDeclaration_in_compilationUnit107 = new BitSet(new long[]{0x0000403F96000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_25_in_packageDeclaration127 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedName_in_packageDeclaration129 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_packageDeclaration131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_importDeclaration154 = new BitSet(new long[]{0x0000000010000010L});
    public static final BitSet FOLLOW_28_in_importDeclaration156 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedName_in_importDeclaration159 = new BitSet(new long[]{0x0000000024000000L});
    public static final BitSet FOLLOW_29_in_importDeclaration162 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_importDeclaration164 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_importDeclaration168 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_typeDeclaration191 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_typeDeclaration201 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classOrInterfaceModifiers_in_classOrInterfaceDeclaration224 = new BitSet(new long[]{0x0000403F92000020L,0x0000000000000200L});
    public static final BitSet FOLLOW_classDeclaration_in_classOrInterfaceDeclaration227 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_interfaceDeclaration_in_classOrInterfaceDeclaration231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classOrInterfaceModifier_in_classOrInterfaceModifiers255 = new BitSet(new long[]{0x0000001F90000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_annotation_in_classOrInterfaceModifier275 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_classOrInterfaceModifier288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_classOrInterfaceModifier303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_33_in_classOrInterfaceModifier315 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_34_in_classOrInterfaceModifier329 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_classOrInterfaceModifier342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_35_in_classOrInterfaceModifier357 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_36_in_classOrInterfaceModifier373 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_modifier_in_modifiers395 = new BitSet(new long[]{0x00F0001F90000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_normalClassDeclaration_in_classDeclaration415 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_enumDeclaration_in_classDeclaration425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_37_in_normalClassDeclaration448 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_normalClassDeclaration450 = new BitSet(new long[]{0x000011C000000000L});
    public static final BitSet FOLLOW_typeParameters_in_normalClassDeclaration452 = new BitSet(new long[]{0x000011C000000000L});
    public static final BitSet FOLLOW_38_in_normalClassDeclaration464 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_normalClassDeclaration466 = new BitSet(new long[]{0x000011C000000000L});
    public static final BitSet FOLLOW_39_in_normalClassDeclaration479 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_typeList_in_normalClassDeclaration481 = new BitSet(new long[]{0x000011C000000000L});
    public static final BitSet FOLLOW_classBody_in_normalClassDeclaration493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_typeParameters516 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_typeParameter_in_typeParameters518 = new BitSet(new long[]{0x0000060000000000L});
    public static final BitSet FOLLOW_41_in_typeParameters521 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_typeParameter_in_typeParameters523 = new BitSet(new long[]{0x0000060000000000L});
    public static final BitSet FOLLOW_42_in_typeParameters527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_typeParameter546 = new BitSet(new long[]{0x0000004000000002L});
    public static final BitSet FOLLOW_38_in_typeParameter549 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_typeBound_in_typeParameter551 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_typeBound580 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_43_in_typeBound583 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_typeBound585 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_ENUM_in_enumDeclaration606 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_enumDeclaration608 = new BitSet(new long[]{0x0000108000000000L});
    public static final BitSet FOLLOW_39_in_enumDeclaration611 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_typeList_in_enumDeclaration613 = new BitSet(new long[]{0x0000108000000000L});
    public static final BitSet FOLLOW_enumBody_in_enumDeclaration617 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_enumBody636 = new BitSet(new long[]{0x0000220004000010L,0x0000000000000200L});
    public static final BitSet FOLLOW_enumConstants_in_enumBody638 = new BitSet(new long[]{0x0000220004000000L});
    public static final BitSet FOLLOW_41_in_enumBody641 = new BitSet(new long[]{0x0000200004000000L});
    public static final BitSet FOLLOW_enumBodyDeclarations_in_enumBody644 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_45_in_enumBody647 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_enumConstant_in_enumConstants666 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_enumConstants669 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000200L});
    public static final BitSet FOLLOW_enumConstant_in_enumConstants671 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_annotations_in_enumConstant696 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_enumConstant699 = new BitSet(new long[]{0x000011C000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_enumConstant701 = new BitSet(new long[]{0x000011C000000002L});
    public static final BitSet FOLLOW_classBody_in_enumConstant704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_enumBodyDeclarations728 = new BitSet(new long[]{0x00F0101F94000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_classBodyDeclaration_in_enumBodyDeclarations731 = new BitSet(new long[]{0x00F0101F94000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_normalInterfaceDeclaration_in_interfaceDeclaration756 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotationTypeDeclaration_in_interfaceDeclaration766 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_46_in_normalInterfaceDeclaration789 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_normalInterfaceDeclaration791 = new BitSet(new long[]{0x0000114000000000L});
    public static final BitSet FOLLOW_typeParameters_in_normalInterfaceDeclaration793 = new BitSet(new long[]{0x0000114000000000L});
    public static final BitSet FOLLOW_38_in_normalInterfaceDeclaration797 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_typeList_in_normalInterfaceDeclaration799 = new BitSet(new long[]{0x0000114000000000L});
    public static final BitSet FOLLOW_interfaceBody_in_normalInterfaceDeclaration803 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_typeList826 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_typeList829 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_typeList831 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_44_in_classBody856 = new BitSet(new long[]{0x00F0301F94000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_classBodyDeclaration_in_classBody858 = new BitSet(new long[]{0x00F0301F94000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_45_in_classBody861 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_interfaceBody884 = new BitSet(new long[]{0x00F0301F94000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_interfaceBodyDeclaration_in_interfaceBody886 = new BitSet(new long[]{0x00F0301F94000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_45_in_interfaceBody889 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_classBodyDeclaration908 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_classBodyDeclaration918 = new BitSet(new long[]{0x0000100010000000L});
    public static final BitSet FOLLOW_block_in_classBodyDeclaration921 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_modifiers_in_classBodyDeclaration931 = new BitSet(new long[]{0xFF00C13F92000030L,0x0000000000000200L});
    public static final BitSet FOLLOW_memberDecl_in_classBodyDeclaration933 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_genericMethodOrConstructorDecl_in_memberDecl956 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_memberDeclaration_in_memberDecl966 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_47_in_memberDecl976 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_memberDecl978 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_voidMethodDeclaratorRest_in_memberDecl980 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_memberDecl990 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_constructorDeclaratorRest_in_memberDecl992 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_interfaceDeclaration_in_memberDecl1002 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classDeclaration_in_memberDecl1012 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_memberDeclaration1035 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_methodDeclaration_in_memberDeclaration1038 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldDeclaration_in_memberDeclaration1042 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_typeParameters_in_genericMethodOrConstructorDecl1062 = new BitSet(new long[]{0xFF00800000000010L});
    public static final BitSet FOLLOW_genericMethodOrConstructorRest_in_genericMethodOrConstructorDecl1064 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_genericMethodOrConstructorRest1088 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_47_in_genericMethodOrConstructorRest1092 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_genericMethodOrConstructorRest1095 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_methodDeclaratorRest_in_genericMethodOrConstructorRest1097 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_genericMethodOrConstructorRest1107 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_constructorDeclaratorRest_in_genericMethodOrConstructorRest1109 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_methodDeclaration1128 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_methodDeclaratorRest_in_methodDeclaration1130 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableDeclarators_in_fieldDeclaration1149 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_fieldDeclaration1151 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_modifiers_in_interfaceBodyDeclaration1178 = new BitSet(new long[]{0xFF00C13F92000030L,0x0000000000000200L});
    public static final BitSet FOLLOW_interfaceMemberDecl_in_interfaceBodyDeclaration1180 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_interfaceBodyDeclaration1190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_interfaceMethodOrFieldDecl_in_interfaceMemberDecl1209 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_interfaceGenericMethodDecl_in_interfaceMemberDecl1219 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_47_in_interfaceMemberDecl1229 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_interfaceMemberDecl1231 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_voidInterfaceMethodDeclaratorRest_in_interfaceMemberDecl1233 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_interfaceDeclaration_in_interfaceMemberDecl1243 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classDeclaration_in_interfaceMemberDecl1253 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_interfaceMethodOrFieldDecl1276 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_interfaceMethodOrFieldDecl1278 = new BitSet(new long[]{0x0009000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_interfaceMethodOrFieldRest_in_interfaceMethodOrFieldDecl1280 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constantDeclaratorsRest_in_interfaceMethodOrFieldRest1303 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_interfaceMethodOrFieldRest1305 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_interfaceMethodDeclaratorRest_in_interfaceMethodOrFieldRest1315 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_formalParameters_in_methodDeclaratorRest1338 = new BitSet(new long[]{0x0005100014000000L});
    public static final BitSet FOLLOW_48_in_methodDeclaratorRest1341 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_methodDeclaratorRest1343 = new BitSet(new long[]{0x0005100014000000L});
    public static final BitSet FOLLOW_50_in_methodDeclaratorRest1356 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedNameList_in_methodDeclaratorRest1358 = new BitSet(new long[]{0x0000100014000000L});
    public static final BitSet FOLLOW_methodBody_in_methodDeclaratorRest1374 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_methodDeclaratorRest1388 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_formalParameters_in_voidMethodDeclaratorRest1421 = new BitSet(new long[]{0x0004100014000000L});
    public static final BitSet FOLLOW_50_in_voidMethodDeclaratorRest1424 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedNameList_in_voidMethodDeclaratorRest1426 = new BitSet(new long[]{0x0000100014000000L});
    public static final BitSet FOLLOW_methodBody_in_voidMethodDeclaratorRest1442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_voidMethodDeclaratorRest1456 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_formalParameters_in_interfaceMethodDeclaratorRest1489 = new BitSet(new long[]{0x0005000004000000L});
    public static final BitSet FOLLOW_48_in_interfaceMethodDeclaratorRest1492 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_interfaceMethodDeclaratorRest1494 = new BitSet(new long[]{0x0005000004000000L});
    public static final BitSet FOLLOW_50_in_interfaceMethodDeclaratorRest1499 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedNameList_in_interfaceMethodDeclaratorRest1501 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_interfaceMethodDeclaratorRest1505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_typeParameters_in_interfaceGenericMethodDecl1528 = new BitSet(new long[]{0xFF00800000000010L});
    public static final BitSet FOLLOW_type_in_interfaceGenericMethodDecl1531 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_47_in_interfaceGenericMethodDecl1535 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_interfaceGenericMethodDecl1538 = new BitSet(new long[]{0x0009000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_interfaceMethodDeclaratorRest_in_interfaceGenericMethodDecl1548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_formalParameters_in_voidInterfaceMethodDeclaratorRest1571 = new BitSet(new long[]{0x0004000004000000L});
    public static final BitSet FOLLOW_50_in_voidInterfaceMethodDeclaratorRest1574 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedNameList_in_voidInterfaceMethodDeclaratorRest1576 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_voidInterfaceMethodDeclaratorRest1580 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_formalParameters_in_constructorDeclaratorRest1603 = new BitSet(new long[]{0x0004100000000000L});
    public static final BitSet FOLLOW_50_in_constructorDeclaratorRest1606 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedNameList_in_constructorDeclaratorRest1608 = new BitSet(new long[]{0x0004100000000000L});
    public static final BitSet FOLLOW_constructorBody_in_constructorDeclaratorRest1612 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_constantDeclarator1631 = new BitSet(new long[]{0x0009000000000000L});
    public static final BitSet FOLLOW_constantDeclaratorRest_in_constantDeclarator1633 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableDeclarator_in_variableDeclarators1656 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_variableDeclarators1659 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_variableDeclarator_in_variableDeclarators1661 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_variableDeclaratorId_in_variableDeclarator1682 = new BitSet(new long[]{0x0008000000000002L});
    public static final BitSet FOLLOW_51_in_variableDeclarator1685 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_variableInitializer_in_variableDeclarator1687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constantDeclaratorRest_in_constantDeclaratorsRest1712 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_constantDeclaratorsRest1715 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_constantDeclarator_in_constantDeclaratorsRest1717 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_48_in_constantDeclaratorRest1739 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_constantDeclaratorRest1741 = new BitSet(new long[]{0x0009000000000000L});
    public static final BitSet FOLLOW_51_in_constantDeclaratorRest1745 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_variableInitializer_in_constantDeclaratorRest1747 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_variableDeclaratorId1770 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_48_in_variableDeclaratorId1773 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_variableDeclaratorId1775 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_arrayInitializer_in_variableInitializer1796 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_variableInitializer1806 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_arrayInitializer1833 = new BitSet(new long[]{0xFF00B00000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_variableInitializer_in_arrayInitializer1836 = new BitSet(new long[]{0x0000220000000000L});
    public static final BitSet FOLLOW_41_in_arrayInitializer1839 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_variableInitializer_in_arrayInitializer1841 = new BitSet(new long[]{0x0000220000000000L});
    public static final BitSet FOLLOW_41_in_arrayInitializer1846 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_45_in_arrayInitializer1853 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotation_in_modifier1872 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_modifier1882 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_modifier1892 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_33_in_modifier1902 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_modifier1912 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_34_in_modifier1922 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_35_in_modifier1932 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_52_in_modifier1942 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_53_in_modifier1952 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_54_in_modifier1962 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_55_in_modifier1972 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_36_in_modifier1982 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifiedName_in_packageOrTypeName2001 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_enumConstantName2020 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifiedName_in_typeName2039 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classOrInterfaceType_in_type2053 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_48_in_type2056 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_type2058 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_primitiveType_in_type2065 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_48_in_type2068 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_type2070 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_Identifier_in_classOrInterfaceType2083 = new BitSet(new long[]{0x0000010020000002L});
    public static final BitSet FOLLOW_typeArguments_in_classOrInterfaceType2085 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_29_in_classOrInterfaceType2089 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_classOrInterfaceType2091 = new BitSet(new long[]{0x0000010020000002L});
    public static final BitSet FOLLOW_typeArguments_in_classOrInterfaceType2093 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_set_in_primitiveType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_35_in_variableModifier2202 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotation_in_variableModifier2212 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_typeArguments2231 = new BitSet(new long[]{0xFF00000000000010L,0x0000000000000001L});
    public static final BitSet FOLLOW_typeArgument_in_typeArguments2233 = new BitSet(new long[]{0x0000060000000000L});
    public static final BitSet FOLLOW_41_in_typeArguments2236 = new BitSet(new long[]{0xFF00000000000010L,0x0000000000000001L});
    public static final BitSet FOLLOW_typeArgument_in_typeArguments2238 = new BitSet(new long[]{0x0000060000000000L});
    public static final BitSet FOLLOW_42_in_typeArguments2242 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_typeArgument2265 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_64_in_typeArgument2275 = new BitSet(new long[]{0x0000004000000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_typeArgument2278 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_typeArgument2286 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifiedName_in_qualifiedNameList2311 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_qualifiedNameList2314 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedName_in_qualifiedNameList2316 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_66_in_formalParameters2337 = new BitSet(new long[]{0xFF00000800000010L,0x0000000000000208L});
    public static final BitSet FOLLOW_formalParameterDecls_in_formalParameters2339 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_formalParameters2342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableModifiers_in_formalParameterDecls2365 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_formalParameterDecls2367 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000010L});
    public static final BitSet FOLLOW_formalParameterDeclsRest_in_formalParameterDecls2369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableDeclaratorId_in_formalParameterDeclsRest2392 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_formalParameterDeclsRest2395 = new BitSet(new long[]{0xFF00000800000010L,0x0000000000000200L});
    public static final BitSet FOLLOW_formalParameterDecls_in_formalParameterDeclsRest2397 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_68_in_formalParameterDeclsRest2409 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_variableDeclaratorId_in_formalParameterDeclsRest2411 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_block_in_methodBody2434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_constructorBody2453 = new BitSet(new long[]{0xFF20F13F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_explicitConstructorInvocation_in_constructorBody2455 = new BitSet(new long[]{0xFF20F03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_blockStatement_in_constructorBody2458 = new BitSet(new long[]{0xFF20F03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_45_in_constructorBody2461 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation2480 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000022L});
    public static final BitSet FOLLOW_set_in_explicitConstructorInvocation2483 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_explicitConstructorInvocation2491 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_explicitConstructorInvocation2493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_in_explicitConstructorInvocation2503 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_explicitConstructorInvocation2505 = new BitSet(new long[]{0x0000010000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation2507 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_explicitConstructorInvocation2510 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_explicitConstructorInvocation2512 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_explicitConstructorInvocation2514 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_qualifiedName2534 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_29_in_qualifiedName2537 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_qualifiedName2539 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_integerLiteral_in_literal2565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FloatingPointLiteral_in_literal2575 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CharacterLiteral_in_literal2585 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StringLiteral_in_literal2595 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanLiteral_in_literal2605 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_70_in_literal2615 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_integerLiteral0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_booleanLiteral0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotation_in_annotations2704 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_73_in_annotation2724 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_annotationName_in_annotation2726 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_annotation2730 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000003EEL});
    public static final BitSet FOLLOW_elementValuePairs_in_annotation2734 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_elementValue_in_annotation2738 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_annotation2743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_annotationName2767 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_29_in_annotationName2770 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_annotationName2772 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_elementValuePair_in_elementValuePairs2793 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_elementValuePairs2796 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_elementValuePair_in_elementValuePairs2798 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_Identifier_in_elementValuePair2819 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_elementValuePair2821 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000003E6L});
    public static final BitSet FOLLOW_elementValue_in_elementValuePair2823 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalExpression_in_elementValue2846 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotation_in_elementValue2856 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_elementValueArrayInitializer_in_elementValue2866 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_elementValueArrayInitializer2889 = new BitSet(new long[]{0xFF00B20000000FD0L,0x0003E600000003E6L});
    public static final BitSet FOLLOW_elementValue_in_elementValueArrayInitializer2892 = new BitSet(new long[]{0x0000220000000000L});
    public static final BitSet FOLLOW_41_in_elementValueArrayInitializer2895 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000003E6L});
    public static final BitSet FOLLOW_elementValue_in_elementValueArrayInitializer2897 = new BitSet(new long[]{0x0000220000000000L});
    public static final BitSet FOLLOW_41_in_elementValueArrayInitializer2904 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_45_in_elementValueArrayInitializer2908 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_73_in_annotationTypeDeclaration2931 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_46_in_annotationTypeDeclaration2933 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_annotationTypeDeclaration2935 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_annotationTypeBody_in_annotationTypeDeclaration2937 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_annotationTypeBody2960 = new BitSet(new long[]{0x00F0301F94000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_annotationTypeElementDeclaration_in_annotationTypeBody2963 = new BitSet(new long[]{0x00F0301F94000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_45_in_annotationTypeBody2967 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_modifiers_in_annotationTypeElementDeclaration2990 = new BitSet(new long[]{0xFF00403F92000030L,0x0000000000000200L});
    public static final BitSet FOLLOW_annotationTypeElementRest_in_annotationTypeElementDeclaration2992 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_annotationTypeElementRest3015 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_annotationMethodOrConstantRest_in_annotationTypeElementRest3017 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_annotationTypeElementRest3019 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_normalClassDeclaration_in_annotationTypeElementRest3029 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_26_in_annotationTypeElementRest3031 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_normalInterfaceDeclaration_in_annotationTypeElementRest3042 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_26_in_annotationTypeElementRest3044 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_enumDeclaration_in_annotationTypeElementRest3055 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_26_in_annotationTypeElementRest3057 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotationTypeDeclaration_in_annotationTypeElementRest3068 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_26_in_annotationTypeElementRest3070 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotationMethodRest_in_annotationMethodOrConstantRest3094 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotationConstantRest_in_annotationMethodOrConstantRest3104 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_annotationMethodRest3127 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_annotationMethodRest3129 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_annotationMethodRest3131 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_defaultValue_in_annotationMethodRest3133 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableDeclarators_in_annotationConstantRest3157 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_74_in_defaultValue3180 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000003E6L});
    public static final BitSet FOLLOW_elementValue_in_defaultValue3182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_block3203 = new BitSet(new long[]{0xFF20F03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_blockStatement_in_block3205 = new BitSet(new long[]{0xFF20F03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_45_in_block3208 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_localVariableDeclarationStatement_in_blockStatement3231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_blockStatement3241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statement_in_blockStatement3251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_localVariableDeclaration_in_localVariableDeclarationStatement3275 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_localVariableDeclarationStatement3277 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableModifiers_in_localVariableDeclaration3296 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_localVariableDeclaration3298 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_variableDeclarators_in_localVariableDeclaration3300 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableModifier_in_variableModifiers3323 = new BitSet(new long[]{0x0000000800000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_block_in_statement3341 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASSERT_in_statement3351 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_statement3353 = new BitSet(new long[]{0x0000000004000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_statement3356 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_statement3358 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_statement3362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_76_in_statement3372 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_parExpression_in_statement3374 = new BitSet(new long[]{0xFF20D03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_statement_in_statement3376 = new BitSet(new long[]{0x0000000000000002L,0x0000000000002000L});
    public static final BitSet FOLLOW_77_in_statement3386 = new BitSet(new long[]{0xFF20D03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_statement_in_statement3388 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_78_in_statement3400 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_statement3402 = new BitSet(new long[]{0xFF00900804000FD0L,0x0003E600000003E6L});
    public static final BitSet FOLLOW_forControl_in_statement3404 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_statement3406 = new BitSet(new long[]{0xFF20D03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_statement_in_statement3408 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_79_in_statement3418 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_parExpression_in_statement3420 = new BitSet(new long[]{0xFF20D03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_statement_in_statement3422 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_80_in_statement3432 = new BitSet(new long[]{0xFF20D03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_statement_in_statement3434 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_79_in_statement3436 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_parExpression_in_statement3438 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_statement3440 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_81_in_statement3450 = new BitSet(new long[]{0x0000100010000000L});
    public static final BitSet FOLLOW_block_in_statement3452 = new BitSet(new long[]{0x0000000000000000L,0x0000000001040000L});
    public static final BitSet FOLLOW_catches_in_statement3464 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_82_in_statement3466 = new BitSet(new long[]{0x0000100010000000L});
    public static final BitSet FOLLOW_block_in_statement3468 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_catches_in_statement3480 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_82_in_statement3494 = new BitSet(new long[]{0x0000100010000000L});
    public static final BitSet FOLLOW_block_in_statement3496 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_83_in_statement3516 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_parExpression_in_statement3518 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_44_in_statement3520 = new BitSet(new long[]{0x0000200000000000L,0x0000000002000400L});
    public static final BitSet FOLLOW_switchBlockStatementGroups_in_statement3522 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_45_in_statement3524 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_53_in_statement3534 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_parExpression_in_statement3536 = new BitSet(new long[]{0x0000100010000000L});
    public static final BitSet FOLLOW_block_in_statement3538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_84_in_statement3548 = new BitSet(new long[]{0xFF00900004000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_statement3550 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_statement3553 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_85_in_statement3563 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_statement3565 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_statement3567 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_86_in_statement3577 = new BitSet(new long[]{0x0000000004000010L});
    public static final BitSet FOLLOW_Identifier_in_statement3579 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_statement3582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_87_in_statement3592 = new BitSet(new long[]{0x0000000004000010L});
    public static final BitSet FOLLOW_Identifier_in_statement3594 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_statement3597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_statement3607 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statementExpression_in_statement3618 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_statement3620 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_statement3630 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_statement3632 = new BitSet(new long[]{0xFF20D03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_statement_in_statement3634 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_catchClause_in_catches3657 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_catchClause_in_catches3660 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_88_in_catchClause3685 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_catchClause3687 = new BitSet(new long[]{0xFF00000800000010L,0x0000000000000200L});
    public static final BitSet FOLLOW_formalParameter_in_catchClause3689 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_catchClause3691 = new BitSet(new long[]{0x0000100010000000L});
    public static final BitSet FOLLOW_block_in_catchClause3693 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableModifiers_in_formalParameter3712 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_formalParameter3714 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_variableDeclaratorId_in_formalParameter3716 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_switchBlockStatementGroup_in_switchBlockStatementGroups3744 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000400L});
    public static final BitSet FOLLOW_switchLabel_in_switchBlockStatementGroup3771 = new BitSet(new long[]{0xFF20D03F96001FF2L,0x0003E60002FBD7E6L});
    public static final BitSet FOLLOW_blockStatement_in_switchBlockStatementGroup3774 = new BitSet(new long[]{0xFF20D03F96001FF2L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_89_in_switchLabel3798 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_constantExpression_in_switchLabel3800 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_switchLabel3802 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_89_in_switchLabel3812 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_enumConstantName_in_switchLabel3814 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_switchLabel3816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_74_in_switchLabel3826 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_switchLabel3828 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_enhancedForControl_in_forControl3859 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forInit_in_forControl3869 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_forControl3872 = new BitSet(new long[]{0xFF00900004000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_forControl3874 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_forControl3877 = new BitSet(new long[]{0xFF00900800000FD2L,0x0003E600000003E6L});
    public static final BitSet FOLLOW_forUpdate_in_forControl3879 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_localVariableDeclaration_in_forInit3899 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionList_in_forInit3909 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableModifiers_in_enhancedForControl3932 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_enhancedForControl3934 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_enhancedForControl3936 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_enhancedForControl3938 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_enhancedForControl3940 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionList_in_forUpdate3959 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_66_in_parExpression3980 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_parExpression3982 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_parExpression3984 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_expressionList4007 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_expressionList4010 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_expressionList4012 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_expression_in_statementExpression4033 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_constantExpression4056 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalExpression_in_expression4079 = new BitSet(new long[]{0x0008050000000002L,0x00000003FC000000L});
    public static final BitSet FOLLOW_assignmentOperator_in_expression4082 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_expression4084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_51_in_assignmentOperator4109 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_90_in_assignmentOperator4119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_91_in_assignmentOperator4129 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_92_in_assignmentOperator4139 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_93_in_assignmentOperator4149 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_94_in_assignmentOperator4159 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_95_in_assignmentOperator4169 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_96_in_assignmentOperator4179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_97_in_assignmentOperator4189 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_assignmentOperator4210 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_40_in_assignmentOperator4214 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_assignmentOperator4218 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_assignmentOperator4252 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_assignmentOperator4256 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_assignmentOperator4260 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_assignmentOperator4264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_assignmentOperator4295 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_assignmentOperator4299 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_assignmentOperator4303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalOrExpression_in_conditionalExpression4332 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_64_in_conditionalExpression4336 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_conditionalExpression_in_conditionalExpression4338 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_conditionalExpression4340 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_conditionalExpression_in_conditionalExpression4342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalAndExpression_in_conditionalOrExpression4364 = new BitSet(new long[]{0x0000000000000002L,0x0000000400000000L});
    public static final BitSet FOLLOW_98_in_conditionalOrExpression4368 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_conditionalAndExpression_in_conditionalOrExpression4370 = new BitSet(new long[]{0x0000000000000002L,0x0000000400000000L});
    public static final BitSet FOLLOW_inclusiveOrExpression_in_conditionalAndExpression4392 = new BitSet(new long[]{0x0000000000000002L,0x0000000800000000L});
    public static final BitSet FOLLOW_99_in_conditionalAndExpression4396 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_inclusiveOrExpression_in_conditionalAndExpression4398 = new BitSet(new long[]{0x0000000000000002L,0x0000000800000000L});
    public static final BitSet FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression4420 = new BitSet(new long[]{0x0000000000000002L,0x0000001000000000L});
    public static final BitSet FOLLOW_100_in_inclusiveOrExpression4424 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression4426 = new BitSet(new long[]{0x0000000000000002L,0x0000001000000000L});
    public static final BitSet FOLLOW_andExpression_in_exclusiveOrExpression4448 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
    public static final BitSet FOLLOW_101_in_exclusiveOrExpression4452 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_andExpression_in_exclusiveOrExpression4454 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
    public static final BitSet FOLLOW_equalityExpression_in_andExpression4476 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_43_in_andExpression4480 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_equalityExpression_in_andExpression4482 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_instanceOfExpression_in_equalityExpression4504 = new BitSet(new long[]{0x0000000000000002L,0x000000C000000000L});
    public static final BitSet FOLLOW_set_in_equalityExpression4508 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_instanceOfExpression_in_equalityExpression4516 = new BitSet(new long[]{0x0000000000000002L,0x000000C000000000L});
    public static final BitSet FOLLOW_relationalExpression_in_instanceOfExpression4538 = new BitSet(new long[]{0x0000000000000002L,0x0000010000000000L});
    public static final BitSet FOLLOW_104_in_instanceOfExpression4541 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_instanceOfExpression4543 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_shiftExpression_in_relationalExpression4564 = new BitSet(new long[]{0x0000050000000002L});
    public static final BitSet FOLLOW_relationalOp_in_relationalExpression4568 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_shiftExpression_in_relationalExpression4570 = new BitSet(new long[]{0x0000050000000002L});
    public static final BitSet FOLLOW_40_in_relationalOp4605 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_relationalOp4609 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_relationalOp4639 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_relationalOp4643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_relationalOp4664 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_relationalOp4675 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_additiveExpression_in_shiftExpression4695 = new BitSet(new long[]{0x0000050000000002L});
    public static final BitSet FOLLOW_shiftOp_in_shiftExpression4699 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_additiveExpression_in_shiftExpression4701 = new BitSet(new long[]{0x0000050000000002L});
    public static final BitSet FOLLOW_40_in_shiftOp4732 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_40_in_shiftOp4736 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_shiftOp4768 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_shiftOp4772 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_shiftOp4776 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_shiftOp4806 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_shiftOp4810 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression4840 = new BitSet(new long[]{0x0000000000000002L,0x0000060000000000L});
    public static final BitSet FOLLOW_set_in_additiveExpression4844 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression4852 = new BitSet(new long[]{0x0000000000000002L,0x0000060000000000L});
    public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression4874 = new BitSet(new long[]{0x0000000040000002L,0x0000180000000000L});
    public static final BitSet FOLLOW_set_in_multiplicativeExpression4878 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression4892 = new BitSet(new long[]{0x0000000040000002L,0x0000180000000000L});
    public static final BitSet FOLLOW_105_in_unaryExpression4918 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression4920 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_106_in_unaryExpression4930 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression4932 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_109_in_unaryExpression4942 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression4944 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_110_in_unaryExpression4954 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression4956 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryExpressionNotPlusMinus_in_unaryExpression4966 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_111_in_unaryExpressionNotPlusMinus4985 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus4987 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_112_in_unaryExpressionNotPlusMinus4997 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus4999 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_castExpression_in_unaryExpressionNotPlusMinus5009 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_in_unaryExpressionNotPlusMinus5019 = new BitSet(new long[]{0x0001000020000002L,0x0000600000000000L});
    public static final BitSet FOLLOW_selector_in_unaryExpressionNotPlusMinus5021 = new BitSet(new long[]{0x0001000020000002L,0x0000600000000000L});
    public static final BitSet FOLLOW_set_in_unaryExpressionNotPlusMinus5024 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_66_in_castExpression5047 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_primitiveType_in_castExpression5049 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_castExpression5051 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_castExpression5053 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_66_in_castExpression5062 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_type_in_castExpression5065 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_expression_in_castExpression5069 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_castExpression5072 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpressionNotPlusMinus_in_castExpression5074 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parExpression_in_primary5093 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_69_in_primary5103 = new BitSet(new long[]{0x0001000020000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_29_in_primary5106 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_primary5108 = new BitSet(new long[]{0x0001000020000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_identifierSuffix_in_primary5112 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_primary5123 = new BitSet(new long[]{0x0000000020000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_superSuffix_in_primary5125 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_primary5135 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_113_in_primary5145 = new BitSet(new long[]{0xFF00010000000010L});
    public static final BitSet FOLLOW_creator_in_primary5147 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_primary5157 = new BitSet(new long[]{0x0001000020000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_29_in_primary5160 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_primary5162 = new BitSet(new long[]{0x0001000020000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_identifierSuffix_in_primary5166 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primitiveType_in_primary5177 = new BitSet(new long[]{0x0001000020000000L});
    public static final BitSet FOLLOW_48_in_primary5180 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_primary5182 = new BitSet(new long[]{0x0001000020000000L});
    public static final BitSet FOLLOW_29_in_primary5186 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_37_in_primary5188 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_47_in_primary5198 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_primary5200 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_37_in_primary5202 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_48_in_identifierSuffix5222 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_identifierSuffix5224 = new BitSet(new long[]{0x0001000020000000L});
    public static final BitSet FOLLOW_29_in_identifierSuffix5228 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_37_in_identifierSuffix5230 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_48_in_identifierSuffix5241 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_identifierSuffix5243 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_identifierSuffix5245 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_arguments_in_identifierSuffix5258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_identifierSuffix5268 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_37_in_identifierSuffix5270 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_identifierSuffix5280 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_explicitGenericInvocation_in_identifierSuffix5282 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_identifierSuffix5292 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_69_in_identifierSuffix5294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_identifierSuffix5304 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_identifierSuffix5306 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_identifierSuffix5308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_identifierSuffix5318 = new BitSet(new long[]{0x0000000000000000L,0x0002000000000000L});
    public static final BitSet FOLLOW_113_in_identifierSuffix5320 = new BitSet(new long[]{0x0000010000000010L});
    public static final BitSet FOLLOW_innerCreator_in_identifierSuffix5322 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonWildcardTypeArguments_in_creator5341 = new BitSet(new long[]{0xFF00010000000010L});
    public static final BitSet FOLLOW_createdName_in_creator5343 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_classCreatorRest_in_creator5345 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createdName_in_creator5355 = new BitSet(new long[]{0x0001000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_arrayCreatorRest_in_creator5358 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classCreatorRest_in_creator5362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classOrInterfaceType_in_createdName5382 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primitiveType_in_createdName5392 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonWildcardTypeArguments_in_innerCreator5415 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_innerCreator5418 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_classCreatorRest_in_innerCreator5420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_48_in_arrayCreatorRest5439 = new BitSet(new long[]{0xFF02900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_49_in_arrayCreatorRest5453 = new BitSet(new long[]{0x0001100000000000L});
    public static final BitSet FOLLOW_48_in_arrayCreatorRest5456 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_arrayCreatorRest5458 = new BitSet(new long[]{0x0001100000000000L});
    public static final BitSet FOLLOW_arrayInitializer_in_arrayCreatorRest5462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_arrayCreatorRest5476 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_arrayCreatorRest5478 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_48_in_arrayCreatorRest5481 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_arrayCreatorRest5483 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_arrayCreatorRest5485 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_48_in_arrayCreatorRest5490 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_arrayCreatorRest5492 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_arguments_in_classCreatorRest5523 = new BitSet(new long[]{0x000011C000000002L});
    public static final BitSet FOLLOW_classBody_in_classCreatorRest5525 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonWildcardTypeArguments_in_explicitGenericInvocation5549 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_explicitGenericInvocation5551 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_explicitGenericInvocation5553 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_nonWildcardTypeArguments5576 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_typeList_in_nonWildcardTypeArguments5578 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_nonWildcardTypeArguments5580 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_selector5603 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_selector5605 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_selector5607 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_selector5618 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_69_in_selector5620 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_selector5630 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_selector5632 = new BitSet(new long[]{0x0000000020000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_superSuffix_in_selector5634 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_selector5644 = new BitSet(new long[]{0x0000000000000000L,0x0002000000000000L});
    public static final BitSet FOLLOW_113_in_selector5646 = new BitSet(new long[]{0x0000010000000010L});
    public static final BitSet FOLLOW_innerCreator_in_selector5648 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_48_in_selector5658 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_selector5660 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_selector5662 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arguments_in_superSuffix5685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_superSuffix5695 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_superSuffix5697 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_superSuffix5699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_66_in_arguments5719 = new BitSet(new long[]{0xFF00900800000FD0L,0x0003E600000003EEL});
    public static final BitSet FOLLOW_expressionList_in_arguments5721 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_arguments5724 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotations_in_synpred5_Java44 = new BitSet(new long[]{0x0000403F92000020L,0x0000000000000200L});
    public static final BitSet FOLLOW_packageDeclaration_in_synpred5_Java58 = new BitSet(new long[]{0x0000403F9E000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_importDeclaration_in_synpred5_Java60 = new BitSet(new long[]{0x0000403F9E000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_typeDeclaration_in_synpred5_Java63 = new BitSet(new long[]{0x0000403F96000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_synpred5_Java78 = new BitSet(new long[]{0x0000403F96000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_typeDeclaration_in_synpred5_Java80 = new BitSet(new long[]{0x0000403F96000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_explicitConstructorInvocation_in_synpred113_Java2455 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonWildcardTypeArguments_in_synpred117_Java2480 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000022L});
    public static final BitSet FOLLOW_set_in_synpred117_Java2483 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_synpred117_Java2491 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_synpred117_Java2493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotation_in_synpred128_Java2704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_localVariableDeclarationStatement_in_synpred151_Java3231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_synpred152_Java3241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_77_in_synpred157_Java3386 = new BitSet(new long[]{0xFF20D03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_statement_in_synpred157_Java3388 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_catches_in_synpred162_Java3464 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_82_in_synpred162_Java3466 = new BitSet(new long[]{0x0000100010000000L});
    public static final BitSet FOLLOW_block_in_synpred162_Java3468 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_catches_in_synpred163_Java3480 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_switchLabel_in_synpred178_Java3771 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_89_in_synpred180_Java3798 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_constantExpression_in_synpred180_Java3800 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_synpred180_Java3802 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_89_in_synpred181_Java3812 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_enumConstantName_in_synpred181_Java3814 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_synpred181_Java3816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_enhancedForControl_in_synpred182_Java3859 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_localVariableDeclaration_in_synpred186_Java3899 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_synpred198_Java4200 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_40_in_synpred198_Java4202 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_synpred198_Java4204 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_synpred199_Java4240 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_synpred199_Java4242 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_synpred199_Java4244 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_synpred199_Java4246 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_synpred200_Java4285 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_synpred200_Java4287 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_synpred200_Java4289 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_synpred211_Java4597 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_synpred211_Java4599 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_synpred212_Java4631 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_synpred212_Java4633 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_synpred215_Java4724 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_40_in_synpred215_Java4726 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_synpred216_Java4758 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_synpred216_Java4760 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_synpred216_Java4762 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_synpred217_Java4798 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_synpred217_Java4800 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_castExpression_in_synpred229_Java5009 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_66_in_synpred233_Java5047 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_primitiveType_in_synpred233_Java5049 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_synpred233_Java5051 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_synpred233_Java5053 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_synpred234_Java5065 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_synpred236_Java5106 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_synpred236_Java5108 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifierSuffix_in_synpred237_Java5112 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_synpred242_Java5160 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_synpred242_Java5162 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifierSuffix_in_synpred243_Java5166 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_48_in_synpred249_Java5241 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_synpred249_Java5243 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_synpred249_Java5245 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_48_in_synpred262_Java5481 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_synpred262_Java5483 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_synpred262_Java5485 = new BitSet(new long[]{0x0000000000000002L});


    private List<MethodInfo> currentInfos = new ArrayList<MethodInfo>();
}


