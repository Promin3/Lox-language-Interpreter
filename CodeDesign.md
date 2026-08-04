# CodeDesign：访问者模式

## 为什么需要访问者模式

Lox 解释器有 **21 种 AST 节点**（12 种表达式 + 9 种语句），需要对它们执行多种操作：

| 操作 | 职责 | 阶段 |
|------|------|------|
| 解析（Parsing） | 源码 → AST | 构建 |
| 解析（Resolving） | 变量绑定、作用域分析 | 遍历 |
| 解释执行（Interpreting） | 运行时求值 | 遍历 |
| 静态分析（未来） | 类型检查、死代码检测等 | 遍历 |

如果把每个操作写成 AST 节点类里的方法（`integerate()` 面向对象风格），每增加一个新操作就要改动 21 个类。访问者模式将所有操作集中到独立的 Visitor 类中，增加新操作时只需新增一个类，无需改动任何 AST 节点。

```
               不加模式                              访问者模式
        ┌──────┬──────┬──────┐              ┌──────┬──────┬──────┐
        │Binary│Unary │Lit...│              │Binary│Unary │Lit.. │
        ├──────┼──────┼──────┤              │.left │.op   │.val  │
        │exec()│exec()│exec()│              │.right│.right│      │
        │reslv()│reslv()│reslv()│           │accept()│accept()│accept()│
        │print()│print()│print()│           └──┬───┴──┬───┴──┬───┘
        └──────┴──────┴──────┘                 │      │      │
         每个类都要改                       ┌──▼──────▼──────▼───┐
                                            │       Visitor      │
         新增操作 = 改 21 个文件            │    visitBinary()   │
         新增节点 = 改 1 个文件             │    visitUnary()    │
                                            │    visitLiteral()  │
                                            └────────────────────┘

                                             新增操作 = 1 个文件
                                             新增节点 = 改 Visitor + 所有实现
```

这个取舍叫做 **表达式问题（Expression Problem）**。Lox 选择访问者模式是因为：节点类型固定（由语言规范定义），而操作会不断增长（解释、解析、类型检查、格式化、优化……）。

---

## 核心组件

### 1. Visitor 接口

定义在 `Expr` 和 `Stmt` 的抽象类内部：

```java
// Expr.java
abstract class Expr {
    interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        // ... 共 12 个表达式
    }
    abstract <R> R accept(Visitor<R> visitor);
}

// Stmt.java
abstract class Stmt {
    interface Visitor<R> {
        R visitBlockStmt(Block stmt);
        R visitClassStmt(Class stmt);
        // ... 共 9 个语句
    }
    abstract <R> R accept(Visitor<R> visitor);
}
```

泛型 `<R>` 让每个访问者自由选择返回值类型：

| 实现者 | 接口 | 返回值 | 含义 |
|--------|------|--------|------|
| `Interpreter` | `Expr.Visitor<Object>` | `Object` | 运行时计算结果 |
| `Interpreter` | `Stmt.Visitor<Void>` | `null` | 语句只有副作用 |
| `Resolver` | `Expr.Visitor<Void>` | `null` | 仅做静态分析 |
| `Resolver` | `Stmt.Visitor<Void>` | `null` | 仅做静态分析 |

### 2. 具体 AST 节点

每个节点类都有两个职责：(1) 存储数据，(2) 通过 `accept()` 调度访问者。

```java
// Expr.java - 以 Binary 为例
static class Binary extends Expr {
    final Expr left;        // 数据
    final Token operator;   // 数据
    final Expr right;       // 数据

    Binary(Expr left, Token operator, Expr right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitBinaryExpr(this);  // 调度
    }
}
```

节点本身不包含任何业务逻辑，纯粹的"被操作的数据结构"。

### 3. 访问者实现

`Interpreter.java` 和 `Resolver.java` 各自实现了 `Expr.Visitor` 和 `Stmt.Visitor`。以 `visitBinaryExpr` 为例对比：

```java
// Interpreter.java - 执行计算
@Override
public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);
    switch (expr.operator.type) {
        case PLUS: return (double) left + (double) right;
        // ...
    }
}

// Resolver.java - 静态分析
@Override
public Void visitBinaryExpr(Expr.Binary expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
}
```

同一个 `accept()` 调用，根据 visitor 的不同走完全不同的逻辑。

---

## 双重分派（Double Dispatch）

这是访问者模式的核心机制，一次方法调用完成两次多态分发：

```
    caller                node                  visitor
  ──────▶ evaluate(expr) ──────▶ expr.accept(this) ──────▶ this.visitBinaryExpr(expr)
    调用者                  第一次分派（节点多态）         第二次分派（访问者多态）
                           运行时根据节点的实际类型          运行时根据 this 的实际类型
                           调用正确的 accept() 重写          调用正确的 visit*() 实现
```
accept 方法的实现是固定的：

```
<R> R accept(Visitor<R> visitor) {
    return visitor.visitBinaryExpr(this);
}
```

它只做一件事：调用 visitor.visitBinaryExpr(this)。

但 visitor 是一个参数，方法的执行结果完全取决于传进来的是什么：
```
expr.accept(interpreter)
→ visitor = interpreter
→ visitor.visitBinaryExpr(this)
→ Interpreter.visitBinaryExpr()  ← 执行求值逻辑

expr.accept(resolver)
→ visitor = resolver
→ visitor.visitBinaryExpr(this)
→ Resolver.visitBinaryExpr()     ← 执行作用域分析逻辑
```

具体执行过程：

```
1. interpreter.evaluate(binaryExpr)
2.   → binaryExpr.accept(interpreter)        // 第一步：Java 发现 binaryExpr 是 Binary 类型
3.     → interpreter.visitBinaryExpr(this)   // 第二步：Java 发现 interpreter 是 Interpreter 类型
4.       → 执行 Interpreter 中的 visitBinaryExpr 逻辑
```

普通多态只能根据一个对象分派（比如 `obj.doWork()` 根据 `obj` 的类型），访问者模式通过 `accept → visit` 的两次调用实现了根据两个对象分派（节点类型 + 操作类型）。

---

## 完整执行流程

### 管道编排（Lox.java）

```
源码字符串
    │
    ▼
Scanner.scanTokens()          ──→  List<Token>          词法分析
    │
    ▼
Parser.parse()                ──→  List<Stmt>           语法分析，构建 AST
    │
    ▼
Resolver.resolve(statements)  ──→  locals 映射           静态分析，变量绑定
    │   ├── stmt.accept(resolver)
    │   └── expr.accept(resolver)
    │
    ▼
Interpreter.interpret(stmts)  ──→  程序输出               运行时解释
        ├── stmt.accept(interpreter)
        └── expr.accept(interpreter)
```

### 递归遍历

访问者模式自身不提供遍历能力，遍历由访问者实现内部完成：

```java
// Interpreter.java
private Object evaluate(Expr expr) {
    return expr.accept(this);     // 访问者入口
}

@Override
public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);   // 递归遍历左子树
    Object right = evaluate(expr.right); // 递归遍历右子树
    return (double) left + (double) right;  // 对当前节点执行操作
}
```

`Resolver` 的遍历结构完全一致，区别仅在于对每个节点执行的操作不同（变量绑定 vs 运行时计算）。

---

## 代码生成：GenerateAst.java

21 种 AST 节点的样板代码（`accept()` 方法、构造函数、字段定义）全部由 `tool/GenerateAst.java` 自动生成，输入是简短的声明式描述：

```java
defineAst(outputDir, "Expr", Arrays.asList(
    "Binary   : Expr left, Token operator, Expr right",
    "Unary    : Token operator, Expr right",
    "Literal  : Object value",
    // ... 共 12 行
));
```

生成规则：

```
输入: "Binary : Expr left, Token operator, Expr right"

输出:
  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) { ... }
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }
    final Expr left;
    final Token operator;
    final Expr right;
  }

同时在 Visitor 接口中生成:
  R visitBinaryExpr(Binary expr);
```

这样新增一个 AST 节点只需在 `GenerateAst` 中添加一行，然后重新运行即可生成所有样板代码。

---

## 扩展指南

### 添加新操作（容易）

以添加 AST 格式化器为例，只需新建一个类：

```java
class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
    String print(Expr expr) { return expr.accept(this); }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return "(" + expr.operator.lexeme
             + " " + print(expr.left)
             + " " + print(expr.right) + ")";
    }
    // ... 实现其余 20 个 visit* 方法
}
```

无需改动任何现有代码。

### 添加新节点类型（困难）

需要改动：

1. `GenerateAst.java` — 添加一行声明
2. 重新运行 `GenerateAst` — 生成新节点的 `accept()` 和 Visitor 接口方法
3. `Interpreter.java` — 添加 `visit*` 实现
4. `Resolver.java` — 添加 `visit*` 实现
5. `Parser.java` — 添加语法规则和节点构造逻辑

由于节点类型由语言规范定义，这种修改在实际开发中很少发生。

---

## 项目文件索引

| 文件 | 角色 |
|------|------|
| `Expr.java` | 表达式节点定义 + `Visitor<R>` 接口 |
| `Stmt.java` | 语句节点定义 + `Visitor<R>` 接口 |
| `Interpreter.java` | 运行时解释（`Expr.Visitor<Object>` + `Stmt.Visitor<Void>`） |
| `Resolver.java` | 静态分析（`Expr.Visitor<Void>` + `Stmt.Visitor<Void>`） |
| `Parser.java` | AST 构造者（使用节点，不实现 Visitor） |
| `Lox.java` | 管道编排（Scanner → Parser → Resolver → Interpreter） |
| `tool/GenerateAst.java` | AST 代码生成器 |
