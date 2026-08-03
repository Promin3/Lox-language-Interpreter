package com.lox;

import java.util.HashMap;
import java.util.Map;

class LoxInstance {
    private LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    // 可以查询实例的字段，或者没找到字段的情况下等于查询实例对应类的方法
    Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        LoxFunction method = klass.findMethod(name.lexeme);
        if (method != null) return method.bind(this);


        throw new RuntimeError(name,
                "Undefined property '" + name.lexeme + "'.");
    }

    // Lox allows freely creating new fields on instances, there’s no need to see if the key is already present.
    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}