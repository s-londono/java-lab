package edu.slz.javalab.jmx.monitor.metric;

/**
 * Defines a metric that is read from an MBean Attribute
 */
class AttributeMetric<T> {

  private final String name;

  private final String attributeName;

  private final Class<T> attributeClass;

  private T value;

  public AttributeMetric(String name, String attributeName, Class<T> attributeClass) {
    this.name = name;
    this.attributeName = attributeName;
    this.attributeClass = attributeClass;
  }

  public String getName() {
    return name;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public Class<T> getAttributeClass() {
    return attributeClass;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  public void castAndSetValue(Object value) {
    this.value = attributeClass.cast(value);
  }

  @Override
  public String toString() {
    return "AttributeMetric{" +
        "name='" + name + '\'' +
        ", attributeName='" + attributeName + '\'' +
        ", value=" + value +
        '}';
  }
}
