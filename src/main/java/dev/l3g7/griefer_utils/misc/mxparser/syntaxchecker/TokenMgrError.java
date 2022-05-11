package dev.l3g7.griefer_utils.misc.mxparser.syntaxchecker;

public class TokenMgrError extends Error
{

  private static final long serialVersionUID = 1L;


  static final int LEXICAL_ERROR = 0;

  static final int INVALID_LEXICAL_STATE = 2;

  int errorCode;

  protected static String addEscapes(String str) {
    StringBuilder retval = new StringBuilder();
    char ch;
    for (int i = 0; i < str.length(); i++) {
      switch (str.charAt(i))
      {
        case 0 :
          continue;
        case '\b':
          retval.append("\\b");
          continue;
        case '\t':
          retval.append("\\t");
          continue;
        case '\n':
          retval.append("\\n");
          continue;
        case '\f':
          retval.append("\\f");
          continue;
        case '\r':
          retval.append("\\r");
          continue;
        case '\"':
          retval.append("\\\"");
          continue;
        case '\'':
          retval.append("\\'");
          continue;
        case '\\':
          retval.append("\\\\");
          continue;
        default:
          if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
            String s = "0000" + Integer.toString(ch, 16);
            retval.append("\\u").append(s.substring(s.length() - 4));
          } else {
            retval.append(ch);
          }
      }
    }
    return retval.toString();
  }

  protected static String LexicalError(boolean EOFSeen, int errorLine, int errorColumn, String errorAfter, char curChar) {
    return("Lexical error at line " +
          errorLine + ", column " +
          errorColumn + ".  Encountered: " +
          (EOFSeen ? "<EOF> " : ("\"" + addEscapes(String.valueOf(curChar)) + "\"") + " (" + (int)curChar + "), ") +
          "after : \"" + addEscapes(errorAfter) + "\"");
  }

  public String getMessage() {
    return super.getMessage();
  }

  public TokenMgrError(String message, int reason) {
    super(message);
    errorCode = reason;
  }

  public TokenMgrError(boolean EOFSeen, int errorLine, int errorColumn, String errorAfter, char curChar, int reason) {
    this(LexicalError(EOFSeen, errorLine, errorColumn, errorAfter, curChar), reason);
  }
}
