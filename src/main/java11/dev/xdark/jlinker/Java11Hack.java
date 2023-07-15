package dev.xdark.jlinker;

// TODO: java compiler refuses to generate module-info if there is nothing in the package
// Get rid of this somehow some day
final class Java11Hack {
    private Java11Hack() {
        throw new RuntimeException("For javac");
    }
}
