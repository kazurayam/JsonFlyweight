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

/**
 * This utility class pretty-prints a JSON.
 * It inserts NewLine characters, inserts line indentations,
 * strips redundant white spaces, so that the JSON becomes better readable for human.
 * This method reads a character stream from the InputStream as the 1st argument,
 * which is supposed to be a large JSON string,
 * pretty-print it,
 * and write the character stream immediately into the OutputStream as the 2nd argument.
 * This class does pretty-printing without buffering the input character stream
 * into a variable of type java.lang.String; therefore it is flyweight.
 * This method requires minimum memory to run regardless how large the input JSON is.
 * This method is useful to perform "pretty-print" on a super large JSON file.
 *
 */
public class JsonFlyweightPrettyPrinter {

    private static final int BUFFER_CAPACITY = 8192;

    static void prettyPrint(InputStream uglyJSON, OutputStream prettyPrintedJSON) throws IOException {
        Reader reader = new InputStreamReader(uglyJSON, StandardCharsets.UTF_8);
        Writer writer = new OutputStreamWriter(prettyPrintedJSON, StandardCharsets.UTF_8);
        prettyPrint(reader, writer);
        reader.close();
        writer.close();
    }

    static void prettyPrint(Reader uglyJSON, Writer prettyPrintedJSON) throws IOException {
        BufferedReader br = new BufferedReader(uglyJSON);
        PrintWriter pw = new PrintWriter(new BufferedWriter(prettyPrintedJSON));
        //
        StringBuilder sb = new StringBuilder();
        int indentLevel = 0;
        boolean inQuote = false;
        String line;
        // loop over all input lines,
        while ((line = br.readLine()) != null) {
            // loop over all characters in a line
            char prevChar = ' ';
            for (char ch : line.toCharArray()) {
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
                        }
                        break;
                    case '}':
                    case ']':
                        // Ending a new block; decrease the indent level
                        if (!inQuote) {
                            indentLevel--;
                            newLineAndIndent(indentLevel, sb);
                        }
                        sb.append(ch);
                        break;
                    case ',':
                        sb.append(ch);
                        // Ending a JSON item; create a new line after
                        if (!inQuote) {
                            newLineAndIndent(indentLevel, sb);
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
        // Assuming indentation using 4 spaces per level
        stringBuilder.append("    ".repeat(Math.max(0, indentLevel)));
    }
}
