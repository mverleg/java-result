package nl.markv.result;

/**
 * Thrown for branches that cannot be reached, but the compiler does not know cannot be reached.
 */
class Unreachable extends RuntimeException {}
