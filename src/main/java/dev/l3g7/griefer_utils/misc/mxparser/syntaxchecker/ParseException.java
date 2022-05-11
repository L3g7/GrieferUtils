package dev.l3g7.griefer_utils.misc.mxparser.syntaxchecker;

public class ParseException extends Exception {

  private static final long serialVersionUID = 1L;

  public ParseException(Token currentTokenVal,
                        int[][] expectedTokenSequencesVal,
                        String[] tokenImageVal
                       )
  {
    super(initialise(currentTokenVal, expectedTokenSequencesVal, tokenImageVal));
    currentToken = currentTokenVal;
    expectedTokenSequences = expectedTokenSequencesVal;
    tokenImage = tokenImageVal;
  }


  public ParseException() {
    super();
  }

  public Token currentToken;

  public int[][] expectedTokenSequences;

  public String[] tokenImage;

  private static String initialise(Token currentToken,
                           int[][] expectedTokenSequences,
                           String[] tokenImage) {
    String eol = System.getProperty("line.separator", "\n");
    int maxSize = 0;
      for (int[] expectedTokenSequence : expectedTokenSequences) {
          if (maxSize < expectedTokenSequence.length) {
              maxSize = expectedTokenSequence.length;
          }
      }
    String retval = "";
    Token tok = currentToken.next;
    for (int i = 0; i < maxSize; i++) {
      if (i != 0) retval += " ";
      if (tok.kind == 0) {
        retval += tokenImage[0];
        break;
      }
      retval += "" + tokenImage[tok.kind];
      retval += " \"";
      retval += add_escapes(tok.image);
      retval += "\"";
      tok = tok.next;
    }
    retval += " bei #" + currentToken.next.beginColumn;
    retval += "." + eol;

    return retval;
  }

  static String add_escapes(String str) {
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

}
