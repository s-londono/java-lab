# Spring SpEL Expressions

* Experiment:

```java
Merchant merc = new Merchant(137, "B2C Dev");

Bot botProcI = new Bot("BotStandardServices_DevLab_PI", "", merc);
botProcI.setBizLogicProvider("BotStandardServiceSessionLogic");
botProcI.setAgentChatConfig(null);
botProcI.setId(187);

BusinessProcessNode bizProcessNode = new BusinessProcessNode(1, botProcI, 1, "SALES",
    "Sales department.", "Sales");

StandardEvaluationContext ctx = new StandardEvaluationContext(this);
ctx.setVariable("bizProcessNode", bizProcessNode);
ExpressionParser parser = new SpelExpressionParser();

Expression exp1 = parser.parseExpression(
    "new String('hello world').toUpperCase()");

Expression exp2 = parser.parseExpression(
    "#bizProcessNode != null && #bizProcessNode.bot != null ? T(String).valueOf(#bizProcessNode.bot.id) : ''");

System.out.println("Expression 1: " + exp2.getValue(ctx));

ctx.setVariable("bizProcessNode", null);

System.out.println("Expression 2: " + exp2.getValue(ctx));

bizProcessNode = new BusinessProcessNode(1, botProcI, 1, "SALES",
    "Sales department.", "Sales");

ctx.setVariable("bizProcessNode", bizProcessNode);

System.out.println("Expression 3: " + exp2.getValue(ctx));
```