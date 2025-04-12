/*
 * The MIT License
 *
 * Copyright 2024 Karate Labs Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.karatelabs.js;

public enum Token {

    WS_LF(false),
    WS(false),
    BACKTICK,
    L_CURLY,
    R_CURLY,
    L_BRACKET,
    R_BRACKET,
    L_PAREN,
    R_PAREN,
    COMMA,
    COLON,
    SEMI,
    DOT_DOT_DOT,
    DOT,
    //====
    NULL(true, true),
    TRUE(true, true),
    FALSE(true, true),
    FUNCTION(true, true),
    RETURN(true, true),
    TRY(true, true),
    CATCH(true, true),
    FINALLY(true, true),
    THROW(true, true),
    NEW(true, true),
    VAR(true, true),
    LET(true, true),
    CONST(true, true),
    IF(true, true),
    ELSE(true, true),
    TYPEOF(true, true),
    INSTANCEOF(true, true),
    DELETE(true, true),
    FOR(true, true),
    IN(true, true),
    OF(true, true),
    DO(true, true),
    WHILE(true, true),
    SWITCH(true, true),
    CASE(true, true),
    DEFAULT(true, true),
    BREAK(true, true),
    THIS(true, true),
    VOID(true, true),
    //====
    EQ_EQ_EQ,
    EQ_EQ,
    EQ,
    EQ_GT, // arrow
    LT_LT_EQ,
    LT_LT,
    LT_EQ,
    LT,
    GT_GT_GT_EQ,
    GT_GT_GT,
    GT_GT_EQ,
    GT_GT,
    GT_EQ,
    GT,
    //====
    NOT_EQ_EQ,
    NOT_EQ,
    NOT,
    PIPE_PIPE_EQ,
    PIPE_PIPE,
    PIPE_EQ,
    PIPE,
    AMP_AMP_EQ,
    AMP_AMP,
    AMP_EQ,
    AMP,
    CARET_EQ,
    CARET,
    QUES_QUES,
    QUES,
    //====
    PLUS_PLUS,
    PLUS_EQ,
    PLUS,
    MINUS_MINUS,
    MINUS_EQ,
    MINUS,
    STAR_STAR_EQ,
    STAR_STAR,
    STAR_EQ,
    STAR,
    SLASH_EQ,
    SLASH,
    PERCENT_EQ,
    PERCENT,
    TILDE,
    //====
    L_COMMENT(false),
    B_COMMENT(false),
    S_STRING,
    D_STRING,
    NUMBER,
    IDENT,
    //====
    REGEX,
    DOLLAR_L_CURLY,
    T_STRING,
    //====
    EOF(false);

    public final boolean primary;
    public final boolean keyword;

    Token() {
        primary = true;
        keyword = false;
    }

    Token(boolean primary, boolean keyword) {
        this.primary = primary;
        this.keyword = keyword;
    }

    Token(boolean primary) {
        this.primary = primary;
        this.keyword = false;
    }

}
