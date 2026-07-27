package com.lox;

class Return extends RuntimeException {
    final Object value;

    // we’re using our exception class for control flow and not actual error handling
    Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}