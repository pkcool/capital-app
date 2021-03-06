package au.com.normist.capital.repository.cap.sqlwriter.mapping;

import javax.persistence.JoinColumn;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

class PropertyDescriptorWrapper extends PropertyDescriptor {

  private static final PropertyDescriptorWrapper[] EMPTY_PROPERTY_DESCRIPTOR_ARRAY = new PropertyDescriptorWrapper[0];

  public static PropertyDescriptorWrapper of(Class<?> c, String property) throws IntrospectionException, NoSuchFieldException {
    AccessibleObject idAccessor = Entities.getIdAccessorOrNull(c);
    if (idAccessor instanceof Field) {
      return new PropertyDescriptorWrapper(property, c.getDeclaredField(property));
    } else if (idAccessor instanceof Method) {
      BeanInfo beanInfo = Introspector.getBeanInfo(c);

      for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
        if (propertyDescriptor.getName().equals(property)) {
          return new PropertyDescriptorWrapper(propertyDescriptor);
        }
      }

      return null;
    } else {
      return null;
    }
  }

  public static PropertyDescriptorWrapper[] of(Class<?> c) {
    AccessibleObject idAccessor = Entities.getIdAccessorOrNull(c);
    if (idAccessor instanceof Method) {
      return getPropertyDescriptorsFromMethods(c);
    }
    return getPropertyDescriptorsFromFields(c);
  }

  public static PropertyDescriptorWrapper forId(Class<?> c) {
    AccessibleObject idAccessor = Entities.getIdAccessorOrNull(c);
    try {
      if (idAccessor instanceof Field) {
        return of(c, ((Field) idAccessor).getName());
      } else if (idAccessor instanceof Method) {
        return of(c, Entities.getName(idAccessor));
      } else {
        return null;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static PropertyDescriptorWrapper[] getPropertyDescriptorsFromMethods(Class<?> c) {
    BeanInfo beanInfo = null;
    try {
      beanInfo = Introspector.getBeanInfo(c);

      List<PropertyDescriptorWrapper> propertyDescriptors = new ArrayList<PropertyDescriptorWrapper>();

      for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
        propertyDescriptors.add(new PropertyDescriptorWrapper(propertyDescriptor));
      }

      return propertyDescriptors.toArray(EMPTY_PROPERTY_DESCRIPTOR_ARRAY);
    } catch (IntrospectionException e) {
      throw new RuntimeException(e);
    }
  }

  private static PropertyDescriptorWrapper[] getPropertyDescriptorsFromFields(Class<?> c) {
    List<PropertyDescriptorWrapper> propertyDescriptors = new ArrayList<PropertyDescriptorWrapper>();

    for (Field field : c.getDeclaredFields()) {

      String propertyName = Entities.getName(field);

      try {
        propertyDescriptors.add(new PropertyDescriptorWrapper(propertyName, field));
      } catch (IntrospectionException e) {
        throw new RuntimeException(e);
      }
    }

    return propertyDescriptors.toArray(EMPTY_PROPERTY_DESCRIPTOR_ARRAY);
  }


  private final Field field;
  private final PropertyDescriptor propertyDescriptor;

  private PropertyDescriptorWrapper(String propertyName, Field field) throws IntrospectionException {
    super(propertyName, null, null);
    this.field = field;
    field.setAccessible(true);
    this.propertyDescriptor = null;
  }

  private PropertyDescriptorWrapper(PropertyDescriptor propertyDescriptor) throws IntrospectionException {
    super(propertyDescriptor.getName(), propertyDescriptor.getReadMethod(), propertyDescriptor.getWriteMethod());
    this.propertyDescriptor = propertyDescriptor;
    this.field = null;
  }

  @Override
  public synchronized Class<?> getPropertyType() {
    return field != null ? field.getType() : propertyDescriptor.getPropertyType();
  }

  public Object get(Object target) {
    try {
      if (field != null) {
        return field.get(target);
      } else {
        return propertyDescriptor.getReadMethod().invoke(target);
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    }
  }

  public void set(Object target, Object value) {
    try {
      if (field != null) {
        field.set(target, value);
      } else {
        propertyDescriptor.getWriteMethod().invoke(target, value);
      }
    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public AccessibleObject getAccessibleObject() {
    return field != null ? field : propertyDescriptor.getReadMethod();
  }

  public Member getMember() {
    return field != null ? field : propertyDescriptor.getReadMethod();
  }

  public String getColumnName(String defaultForeignKeySuffix) {
    AccessibleObject accessibleObject = getAccessibleObject();
    String columnName = getReadMethod() != null || field != null ? Entities.getName(accessibleObject) : getName();

    if (Entities.isToOneRelation(accessibleObject)) {
      if (accessibleObject.isAnnotationPresent(JoinColumn.class)) {
        return accessibleObject.getAnnotation(JoinColumn.class).name();
      }
      return columnName + defaultForeignKeySuffix;
    }

    return columnName;
  }

  public ParameterizedType getGenericPropertyType() {
    return (ParameterizedType) (field != null ? field.getGenericType() : propertyDescriptor.getReadMethod().getGenericReturnType());
  }
}
