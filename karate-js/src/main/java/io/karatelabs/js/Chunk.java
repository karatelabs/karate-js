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

public class Chunk {

    static final Chunk _NODE = new Chunk(Source.of(""), Token.EOF, 0, 0, 0, "");

    Source source;
    public final long pos;
    public final int line;
    public final int col;
    public final Token token;
    public final String text;
    Chunk prev;
    Chunk next;

    public Chunk(Source source, Token token, long pos, int line, int col, String text) {
        this.source = source;
        this.token = token;
        this.pos = pos;
        this.line = line;
        this.col = col;
        this.text = text;
    }

    public String getLineText() {
        return source.getLine(line);
    }

    public String getPositionDisplay() {
        return "[" + (line + 1) + ":" + (col + 1) + "]";
    }

    @Override
    public String toString() {
        switch (token) {
            case WS:
                return "_";
            case WS_LF:
                return "_\\n_";
        }
        return text;
    }

}
