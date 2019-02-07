package org.oscii.lex;

/**
 * A written form of a lexeme in a language.
 */
public class Expression {
    public final String text;
    public final String degraded_text; // Lowercased, etc.
    public final String language; // ISO-639-1 code (2-letter), e.g., "zh"
    // Tag inventory: http://www.iana.org/assignments/language-subtag-registry/language-subtag-registry
    public String languageTag; // RFC5654 language tag, e.g., "zh-cmn-Hans-CN"
    public final String source;

    public Expression(String text, String degraded_text, String languageTag) {
        this(text, degraded_text, languageTag, "");
    }

    public Expression(String text, String degraded_text, String languageTag, String source) {
        this.text = text;
        this.degraded_text = degraded_text == null ? Lexicon.degrade(text) : degraded_text;
        this.languageTag = languageTag;
        // TODO(denero) Split language tag to open language
        this.language = languageTag;
        this.source = source;
    }

    public Expression(String text, String languageTag) {
        this(text, Lexicon.degrade(text), languageTag);
    }

    @Override
    public String toString() {
        return "Expression{" +
                "text='" + text + '\'' +
                "language='" + language + '\'' +
                "source='" + source + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Expression that = (Expression) o;

        if (!language.equals(that.language)) return false;
        if (!text.equals(that.text)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + language.hashCode();
        return result;
    }
}