# Lox Interpreter (Java)

基于 *Crafting Interpreters* 的 Lox 语言 Java 实现。

## 构建

```bash
mvn compile
```

## 使用

**运行脚本文件：**
```bash
java -cp target/classes com.lox.Lox <path/to/script.lox>
```

**交互式 REPL：**
```bash
java -cp target/classes com.lox.Lox
```

## 测试

测试文件位于 `src/test/resources/test.lox`：

```bash
java -cp target/classes com.lox.Lox src/test/resources/test.lox
```

## 项目结构

```
src/main/java/com/lox/
├── Lox.java           # 入口，负责编排整个流程
├── Scanner.java       # 词法分析：源码 → Token 列表
├── Parser.java        # 语法分析：Token 列表 → AST
├── Interpreter.java   # 解释执行：遍历 AST 求值
├── Expr.java          # 表达式 AST 节点定义
├── Stmt.java          # 语句 AST 节点定义
├── Token.java         # Token 数据结构
├── TokenType.java     # Token 类型枚举
├── RuntimeError.java  # 运行时异常
├── Environment.java   # 变量作用域环境
└── tool/
    └── GenerateAst.java  # AST 节点代码生成器
```
