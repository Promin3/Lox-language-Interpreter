# Lox Interpreter (Java)

Lox 语言 Java 实现

## 环境要求

- Java 21+

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
├── Resolver.java      # 语义分析：静态作用域解析 + 变量绑定
├── Interpreter.java   # 解释执行：遍历 AST 求值
├── Expr.java          # 表达式 AST 节点定义（Visitor 生成）
├── Stmt.java          # 语句 AST 节点定义（Visitor 生成）
├── Token.java         # Token 数据结构
├── TokenType.java     # Token 类型枚举
├── RuntimeError.java  # 运行时异常
├── Environment.java   # 变量作用域环境（支持嵌套与 distance 查找）
├── LoxCallable.java   # 可调用对象接口
├── LoxFunction.java   # Lox 函数运行时表示（闭包、bind、init 返回处理）
├── LoxClass.java      # Lox 类运行时表示（方法存储、实例构造）
├── LoxInstance.java   # Lox 实例运行时表示（字段存储、方法绑定）
├── Return.java        # return 异常（控制流跳出）
└── tool/
    └── GenerateAst.java  # AST 节点代码生成器
```

## 已实现特性

- 字面量、算术与比较运算
- 变量声明、赋值与块作用域
- `if` / `else` 控制流
- `while` 循环
- 短路逻辑（`and` / `or`）
- 函数定义、调用、递归与闭包
- 高阶函数
- 类定义、实例化、属性访问与赋值
- 方法定义与调用（含 `this`）
- `init` 初始化方法
- 类继承（`<` 语法）
