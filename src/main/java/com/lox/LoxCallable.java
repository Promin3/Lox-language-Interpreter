package com.lox;

import java.util.List;

interface LoxCallable {
    // num of arguments
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}