# Lox AST 节点组成

## 概述

Lox 解释器的 AST 分为两大类：

- **Expr（表达式）** — 求值后产生值的节点（12 种）
- **Stmt（语句）** — 产生副作用但不产生值的节点（9 种）

所有节点都遵循 **Visitor 设计模式**，代码由 `tool/GenerateAst.java` 自动生成到 `Expr.java` 和 `Stmt.java`。

---

## Token — 叶子节点

AST 的叶子节点统一使用 `Token` 类型。

| 字段 | 类型 | 说明 |
|------|------|------|
| `type` | `TokenType` | Token 类型（如 `PLUS`、`IDENTIFIER`、`NUMBER` 等） |
| `lexeme` | `String` | 原始词素文本（如 `"+"`、`"foo"`、`"42"`） |
| `literal` | `Object` | 字面量值（如 `42.0`、`"hello"`、`true`、`null`） |
| `line` | `int` | 所在行号，用于错误报告 |

---

## Expr — 表达式节点（12 种）

### 1. Assign — 赋值表达式

```
Expr.Assign
├── name : Token         ← 变量名（IDENTIFIER）
└── value : Expr         ← 赋的值（任意表达式）
```

对应语法：`IDENTIFIER "=" expression`

### 2. Binary — 二元运算表达式

```
Expr.Binary
├── left     : Expr      ← 左操作数
├── operator : Token     ← 运算符（+ - * / > < >= <= == !=）
└── right    : Expr      ← 右操作数
```

对应语法：`expression operator expression`

### 3. Call — 函数/方法调用表达式

```
Expr.Call
├── callee    : Expr           ← 被调用者（如 Variable、Get 等）
├── paren     : Token          ← 右括号 Token（用于错误定位）
└── arguments : List<Expr>     ← 实参列表
```

对应语法：`primary "(" arguments? ")"`

### 4. Get — 属性访问表达式

```
Expr.Get
├── object : Expr      ← 对象表达式
└── name   : Token     ← 属性名（IDENTIFIER）
```

对应语法：`primary "." IDENTIFIER`

### 5. Grouping — 分组表达式

```
Expr.Grouping
└── expression : Expr      ← 括号内的表达式
```

对应语法：`"(" expression ")"`

### 6. Literal — 字面量表达式

```
Expr.Literal
└── value : Object      ← 字面量值（Double, String, Boolean, null）
```

对应语法：`NUMBER | STRING | "true" | "false" | "nil"`

### 7. Logical — 逻辑表达式

```
Expr.Logical
├── left     : Expr      ← 左操作数
├── operator : Token     ← 运算符（"and" 或 "or"）
└── right    : Expr      ← 右操作数
```

对应语法：`expression ("and" | "or") expression`

短路求值：`and` 在 left 为 falsy 时短路；`or` 在 left 为 truthy 时短路。

### 8. Set — 属性赋值表达式

```
Expr.Set
├── object : Expr      ← 对象表达式
├── name   : Token     ← 属性名（IDENTIFIER）
└── value  : Expr      ← 赋的值
```

对应语法：`call "." IDENTIFIER "=" assignment`

### 9. Super — 父类方法访问表达式

```
Expr.Super
├── keyword : Token     ← "super" 关键字 Token
└── method  : Token     ← 父类方法名（IDENTIFIER）
```

对应语法：`"super" "." IDENTIFIER`

### 10. This — this 引用表达式

```
Expr.This
└── keyword : Token     ← "this" 关键字 Token
```

对应语法：`"this"`

### 11. Unary — 一元运算表达式

```
Expr.Unary
├── operator : Token     ← 运算符（"!" 或 "-"）
└── right    : Expr      ← 操作数
```

对应语法：`("!" | "-") unary`

### 12. Variable — 变量引用表达式

```
Expr.Variable
└── name : Token     ← 变量名（IDENTIFIER）
```

对应语法：`IDENTIFIER`

---

## Stmt — 语句节点（9 种）

### 1. Block — 块语句

```
Stmt.Block
└── statements : List<Stmt>     ← 语句列表
```

对应语法：`"{" declaration* "}"`

Block 会创建一个新的作用域环境。

### 2. Class — 类声明语句

```
Stmt.Class
├── name       : Token              ← 类名（IDENTIFIER）
├── superclass : Expr.Variable      ← 父类引用（可为 null）
└── methods    : List<Stmt.Function> ← 方法列表
```

对应语法：`"class" IDENTIFIER ("<" IDENTIFIER)? "{" function* "}"`

### 3. Expression — 表达式语句

```
Stmt.Expression
└── expression : Expr      ← 被求值的表达式
```

对应语法：`expression ";"`

### 4. Function — 函数/方法声明语句

```
Stmt.Function
├── name   : Token          ← 函数名（IDENTIFIER）
├── params : List<Token>    ← 形式参数列表
└── body   : List<Stmt>     ← 函数体语句列表
```

对应语法：`IDENTIFIER "(" parameters? ")" block`

### 5. If — 条件语句

```
Stmt.If
├── condition  : Expr     ← 条件表达式
├── thenBranch : Stmt     ← then 分支语句
└── elseBranch : Stmt     ← else 分支语句（可为 null）
```

对应语法：`"if" "(" expression ")" statement ("else" statement)?`

### 6. Print — 打印语句

```
Stmt.Print
└── expression : Expr      ← 要打印的表达式
```

对应语法：`"print" expression ";"`

### 7. Return — 返回语句

```
Stmt.Return
├── keyword : Token     ← "return" 关键字 Token
└── value   : Expr      ← 返回值表达式（可为 null）
```

对应语法：`"return" expression? ";"`

### 8. Var — 变量声明语句

```
Stmt.Var
├── name        : Token     ← 变量名（IDENTIFIER）
└── initializer : Expr      ← 初始值表达式（可为 null）
```

对应语法：`"var" IDENTIFIER ("=" expression)? ";"`

### 9. While — 循环语句

```
Stmt.While
├── condition : Expr      ← 条件表达式
└── body      : Stmt      ← 循环体语句
```

对应语法：`"while" "(" expression ")" statement`

---

## 实例 1：基础语法

源码：

```lox
var x = 2 + 3;
if (x > 3) {
    print x + 1;
}
```

解析后的 AST：

```
List<Stmt>

Stmt.Var "var x = 2 + 3;"
├── name: Token(IDENTIFIER, "x")
└── initializer: Expr.Binary
    ├── left: Expr.Literal(2.0)
    ├── operator: Token(PLUS, "+")
    └── right: Expr.Literal(3.0)

Stmt.If "if (x > 3) { print x + 1; }"
├── condition: Expr.Binary
│   ├── left: Expr.Variable
│   │   └── name: Token(IDENTIFIER, "x")
│   ├── operator: Token(GREATER, ">")
│   └── right: Expr.Literal(3.0)
├── thenBranch: Stmt.Block
│   └── statements:
│       Stmt.Print "print x + 1;"
│       └── expression: Expr.Binary
│           ├── left: Expr.Variable
│           │   └── name: Token(IDENTIFIER, "x")
│           ├── operator: Token(PLUS, "+")
│           └── right: Expr.Literal(1.0)
└── elseBranch: null
```

---

## 实例 2：闭包、类与继承

源码：

```lox
fun makeAdder(base) {
    fun add(n) {
        return base + n;      // add 捕获了外层参数 base（闭包）
    }
    return add;
}

class Counter {
    init(start) {
        this.val = start;     // 属性赋值 (Set)
    }
}

class FastCounter < Counter {
    inc() {
        this.val = this.val + 1; // 属性读取 (Get) + 属性赋值 (Set)
    }
}
```

解析后的 AST：

```
List<Stmt>

Stmt.Function "fun makeAdder(base) { ... }"
├── name: Token(IDENTIFIER, "makeAdder")
├── params: [Token(IDENTIFIER, "base")]
└── body:
    │
    Stmt.Function "fun add(n) { ... }"          ← 嵌套函数（闭包）
    ├── name: Token(IDENTIFIER, "add")
    ├── params: [Token(IDENTIFIER, "n")]
    └── body:
        Stmt.Return "return base + n;"
        ├── keyword: Token(RETURN, "return")
        └── value: Expr.Binary
            ├── left: Expr.Variable             ← base 来自外层作用域
            │   └── name: Token(IDENTIFIER, "base")
            ├── operator: Token(PLUS, "+")
            └── right: Expr.Variable
                └── name: Token(IDENTIFIER, "n")
    │
    Stmt.Return "return add;"
    ├── keyword: Token(RETURN, "return")
    └── value: Expr.Variable
        └── name: Token(IDENTIFIER, "add")

Stmt.Class "class Counter { ... }"
├── name: Token(IDENTIFIER, "Counter")
├── superclass: null
└── methods:
    Stmt.Function "init(start)"
    ├── name: Token(IDENTIFIER, "init")
    ├── params: [Token(IDENTIFIER, "start")]
    └── body:
        Stmt.Expression
        └── expression: Expr.Set                 ← this.val = start
            ├── object: Expr.This
            │   └── keyword: Token(THIS, "this")
            ├── name: Token(IDENTIFIER, "val")
            └── value: Expr.Variable
                └── name: Token(IDENTIFIER, "start")

Stmt.Class "class FastCounter < Counter { ... }"
├── name: Token(IDENTIFIER, "FastCounter")
├── superclass: Expr.Variable                   ← 继承 Counter
│   └── name: Token(IDENTIFIER, "Counter")
└── methods:
    Stmt.Function "inc()"
    ├── name: Token(IDENTIFIER, "inc")
    ├── params: []
    └── body:
        Stmt.Expression
        └── expression: Expr.Set                 ← this.val = this.val + 1
            ├── object: Expr.This
            │   └── keyword: Token(THIS, "this")
            ├── name: Token(IDENTIFIER, "val")
            └── value: Expr.Binary
                ├── left: Expr.Get               ← this.val（读）
                │   ├── object: Expr.This
                │   │   └── keyword: Token(THIS, "this")
                │   └── name: Token(IDENTIFIER, "val")
                ├── operator: Token(PLUS, "+")
                └── right: Expr.Literal(1.0)
```
