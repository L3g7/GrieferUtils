package dev.l3g7.griefer_utils.misc;

import java.util.Objects;

public class Pair<A, B> {

    public A a;
    public B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

    @Override
    public String toString() {
        return "(" + a.toString() + ", " + b.toString() + ")";
    }

    @Override
    public int hashCode() {
        return a.hashCode() ^ b.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Pair))
            return false;

        Pair<?, ?> pair = (Pair<?, ?>) obj;
        return Objects.equals(a, pair.a) && Objects.equals(b, pair.b);
    }

}
