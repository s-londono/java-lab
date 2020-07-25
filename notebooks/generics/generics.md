Wrapper to capture key and value types of consumed records. Why is this necessary? Refer to:

https://docs.oracle.com/javase/tutorial/java/generics/capture.html

```java
class ConsumeRecordHelper {
  <K> void fnConsumeRecordHelper(ConsumerRecord<K,C> record) {
    logger.debug("Consuming record: {}", record);
    consumerFunction.accept(record.value());
  }
}

ConsumeRecordHelper helper = new ConsumeRecordHelper();

lstListenerThreads.add(
    listenerThreadFactory.newThread(() -> { 
      consumer.start(lstTopics, helper::fnConsumeRecordHelper); 
    })
);
```
Be aware of generics and subtyping. Parameterized data structures containing a supertype do not automatically extend the same  data structure containing a subtype. In such cases, use wildcards and extends. E.g.:
```java
List<? extends Serializable> lst = new List<Date>();
```
Refer to: 
https://docs.oracle.com/javase/tutorial/java/generics/subtyping.html

