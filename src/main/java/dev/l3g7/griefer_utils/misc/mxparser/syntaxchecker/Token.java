package dev.l3g7.griefer_utils.misc.mxparser.syntaxchecker;


public class Token implements java.io.Serializable {

  private static final long serialVersionUID = 1L;

  public int kind;

  public int beginLine;
  public int beginColumn;
  public int endLine;
  public int endColumn;

  public String image;

  public Token next;

  public Object getValue() {
    return null;
  }

  public Token() {}

  public Token(int kind, String image)
  {
    this.kind = kind;
    this.image = image;
  }

  public String toString()
  {
    return image;
  }

  public static Token newToken(int ofKind, String image)
  {
    return new Token(ofKind, image);
  }

}
