package com.kazurayam.jsonflyweight;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * This utility class performs pretty-printing a JSON.
 * This class runs very fast.
 * This class does NOT load whole JSON text into a String in memory.
 * The methods translate characters to characters while performing pretty-printing JSON.
 * Therefore, the methods requires very small size of memory runtime
 * regardless how large the input JSON is (such as 300 MB).
 * It used a buffer of 32 KB and no more.
 */
public class JsonFlyweight {

    private static final int BUFFER_CAPACITY = 32768;

    /**
     * This method will pretty-print a JSON as an InputStream,
     * write the result into an OutputStream.
     *
     * @param uglyJSON ugly JSON. The source could be a large file of 2 megabytes or more
     * @param prettyPrintedJSON pretty printed JSON
     * @return number of lines in the pretty printed JSON
     * @throws IOException anything may happen
     */
    public static int prettyPrint(InputStream uglyJSON, OutputStream prettyPrintedJSON) throws IOException {
        Reader reader = new InputStreamReader(uglyJSON, StandardCharsets.UTF_8);
        Writer writer = new OutputStreamWriter(prettyPrintedJSON, StandardCharsets.UTF_8);
        int numLines = prettyPrint(reader, writer);
        return numLines;
    }

    /**
     * This method will pretty-print a JSON as a Reader, write the result into a Writer.
     * This method runs very fast. This method requires minimum size of runtime memory.
     *
     * @param uglyJSON ugly JSON. The source could be a large file of 2 megabytes or more
     * @param prettyPrintedJSON pretty printed JSON
     * @return number of lines in the pretty printed JSON
     * @throws IOException anything may happen
     */
    public static int prettyPrint(Reader uglyJSON, Writer prettyPrintedJSON) throws IOException {
        Objects.requireNonNull(uglyJSON);
        Objects.requireNonNull(prettyPrintedJSON);
        BufferedReader br = new BufferedReader(uglyJSON);
        PrintWriter pw = new PrintWriter(new BufferedWriter(prettyPrintedJSON));
        int numLines = 0;
        //
        StringBuilder sb = new StringBuilder();
        int indentLevel = 0;
        boolean inQuote = false;

        char[] charBuffer = new char[BUFFER_CAPACITY];
        int numCharsRead;
        // consume all characters from the input
        while ((numCharsRead = br.read(charBuffer, 0, charBuffer.length)) != -1) {
            // loop over all characters buffered
            char prevChar = ' ';
            for (int i = 0; i < numCharsRead; i++) {
                char ch = charBuffer[i];
                // translate characters
                switch (ch) {
                    case '"':
                        sb.append(ch);
                        if (prevChar != '\\') {
                            // switch the quoting status
                            inQuote = !inQuote;
                        }
                        break;
                    case ' ':
                    case '\t':
                        // For space and tab: ignore the space if it is not being quoted.
                        if (inQuote) {
                            sb.append(ch);
                        }
                        break;
                    case '{':
                    case '[':
                        // Starting a new block: increase the indent level
                        sb.append(ch);
                        if (!inQuote) {
                            indentLevel++;
                            newLineAndIndent(indentLevel, sb);
                            numLines++;
                        }
                        break;
                    case '}':
                    case ']':
                        // Ending a new block; decrease the indent level
                        if (!inQuote) {
                            indentLevel--;
                            newLineAndIndent(indentLevel, sb);
                            numLines++;
                        }
                        sb.append(ch);
                        break;
                    case ',':
                        sb.append(ch);
                        // Ending a JSON item; create a new line after
                        if (!inQuote) {
                            newLineAndIndent(indentLevel, sb);
                            numLines++;
                        }
                        break;
                    case ':':
                        sb.append(ch);
                        if (!inQuote) {
                            sb.append(" ");
                            // "key": "value" --- insert a space after colon :
                        }
                        break;
                    default:
                        sb.append(ch);
                }
                prevChar = ch;   // to distinguish " and \"
            }
            // we will flush the buffer before it gets too large
            if (sb.length() >  BUFFER_CAPACITY * 0.9) {
                pw.print(sb);
                pw.flush();
                sb.setLength(0);
            }
        }
        pw.print(sb);
        pw.flush();
        pw.close();
        br.close();
        return numLines;
    }


    /**
     * Print a new line with indentation at the beginning of the new line.
     * Append a NewLine char at the end.
     *
     * @param indentLevel 0,1,2,3,...
     * @param stringBuilder buffer where the output JSON string is constructed
     */
    private static void newLineAndIndent(int indentLevel, StringBuilder stringBuilder) {
        stringBuilder.append(System.lineSeparator());
        // Assuming indentation using 2 spaces per level
        stringBuilder.append("  ".repeat(Math.max(0, indentLevel)));
    }
}
