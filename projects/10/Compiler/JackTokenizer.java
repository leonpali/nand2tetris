import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class JackTokenizer {

    private File input;
    private Scanner sc;
    private LinkedList<Token> tokens;
    private LinkedList<String> withoutComments;
    private String currentToken;
    private static final Set<String> KEYWORDS = Arrays.stream(KeyWord.values())
                                                    .map(Enum::name)
                                                    .collect(Collectors.toSet());
    private static final Set<Character> SYMBOLS = Set.of(
                                                    '{', '}', '(', ')', '[', ']', '.', ',', ';',
                                                    '+', '-', '*', '/', '&', '|', '<', '>', '=', '~'
                                                    ); 

    public JackTokenizer(File input) throws IOException {
        this.input = input;
        this.sc = new Scanner(input);
        this.tokens = new LinkedList<Token>();
        this.withoutComments = new LinkedList<String>();
        this.currentToken = "";
    }

    public LinkedList<Token> tokenize() throws IOException {
        String content = new String(java.nio.file.Files.readAllBytes(input.toPath()));

        Pattern multiLineComment = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
        Matcher commentMatcher = multiLineComment.matcher(content);
        content = commentMatcher.replaceAll("");
        content = content.replaceAll("//.*", "");

        Matcher matcher = Pattern.compile(
            "\"[^\"]*\"" +                // String constants
            "|\\d+" +                     // Integer constants
            "|[a-zA-Z_][\\w_]*" +         // Identifiers and keywords
            "|[{}()\\[\\].,;\\+\\-\\*/&|<>=~]" // Symbols
        ).matcher(content);

        while (matcher.find()) {
            currentToken = matcher.group();
            TokenType type = tokenType();
            if(type != null) {
                tokens.add(new Token(type, currentToken));
            }
        }


        return tokens;
    }

    private TokenType tokenType() {
        if (isKeyword(currentToken)) {
            return TokenType.KEYWORD;
        } else if (isSymbol(currentToken)) {
            return TokenType.SYMBOL; 
        } else if (isIntegerConstant(currentToken)) {
            return TokenType.INT_CONST;
        } else if (isStringConstant(currentToken)) {
            return TokenType.STRING_CONST;
        } else if (isIdentifier(currentToken)) {
            return TokenType.IDENTIFIER;
        } else {
            return null;
        }
    }

    private boolean isKeyword(String token) {
        return KEYWORDS.contains(token.toUpperCase());
    }

    private boolean isSymbol(String token) {
        return token.length() == 1 && SYMBOLS.contains(token.charAt(0));
    }

    private boolean isIntegerConstant(String token) {
        try {
            int num = Integer.parseInt(token);
            if (num >= 0 && num < 32768) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isStringConstant(String token) {
        return token.matches("\".*\"");
    }

    private boolean isIdentifier(String token) {
        return token.matches("[\\w_][\\w\\d_]*");
    }
}