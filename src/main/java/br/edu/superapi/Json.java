package br.edu.superapi;

import java.util.LinkedHashMap;
import java.util.Map;

/** Parser propositalmente limitado ao contrato didático: objeto de strings. */
public final class Json {
    private final String source;
    private int pos;
    private Json(String source) { this.source = source; }
    public static Map<String, String> parse(String source) {
        return new Json(source).object();
    }
    private Map<String, String> object() {
        Map<String, String> result = new LinkedHashMap<>();
        expect('{');
        if (!take('}')) {
            do {
                String key = string();
                expect(':');
                if (result.putIfAbsent(key, string()) != null) fail();
            } while (take(','));
            expect('}');
        }
        whitespace();
        if (pos != source.length()) fail();
        return result;
    }
    private String string() {
        expect('"');
        StringBuilder value = new StringBuilder();
        while (pos < source.length()) {
            char c = source.charAt(pos++);
            if (c == '"') return value.toString();
            if (c < 32) fail();
            if (c == '\\') {
                if (pos >= source.length()) fail();
                c = source.charAt(pos++);
                switch (c) {
                    case '"', '\\', '/' -> value.append(c);
                    case 'n' -> value.append('\n');
                    case 'r' -> value.append('\r');
                    case 't' -> value.append('\t');
                    case 'b' -> value.append('\b');
                    case 'f' -> value.append('\f');
                    case 'u' -> {
                        if (pos + 4 > source.length()) fail();
                        try { value.append((char) Integer.parseInt(source.substring(pos, pos + 4), 16)); }
                        catch (NumberFormatException ex) { fail(); }
                        pos += 4;
                    }
                    default -> fail();
                }
            } else value.append(c);
        }
        throw new IllegalArgumentException("String JSON incompleta");
    }
    private void whitespace() {
        while (pos < source.length() && " \r\n\t".indexOf(source.charAt(pos)) >= 0) pos++;
    }
    private boolean take(char c) {
        whitespace();
        if (pos < source.length() && source.charAt(pos) == c) { pos++; return true; }
        return false;
    }
    private void expect(char c) { if (!take(c)) fail(); }
    private void fail() { throw new IllegalArgumentException("JSON inválido: use um objeto com valores entre aspas (posição " + pos + ")"); }
}
