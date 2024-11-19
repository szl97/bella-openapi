package com.ke.bella.openapi;

import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class TypeSchema implements Serializable {
    private static final long serialVersionUID = 1L;
    private String code;
    private String name;
    private String valueType;
    private JsonSchema child;

    @Override
    public boolean equals(Object o) {
        if(o instanceof TypeSchema) {
            return this.code.equals(((TypeSchema) o).code);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.code.hashCode();
    }

    public static TypeSchema toSchema(Field field, Map<String, String> desc) {
        if(!desc.isEmpty() && !desc.containsKey(field.getName())) {
            return null;
        }
        TypeSchema schema = new TypeSchema();
        schema.setCode(field.getName());
        schema.setName(desc.isEmpty() ? field.getName() : desc.get(field.getName()));
        Class<?> fieldType = field.getType();
        if(isPrimitiveOrWrapper(fieldType) || fieldType == String.class || fieldType == BigDecimal.class || fieldType.isEnum()) {
            // 简单类型： 基本类型、String、BigDecimal、枚举
            schema.setValueType(fieldType == boolean.class || fieldType == Boolean.class ? "bool" :
                    fieldType == char.class || fieldType == String.class || fieldType == Character.class || fieldType.isEnum() ? "string" : "number");
        } else if(fieldType.isArray()) {
            // 数组类型
            schema.setValueType("array");
            if(fieldType.getComponentType().isAssignableFrom(IDescription.class)) {
                schema.setChild(JsonSchema.toSchema(fieldType.getComponentType()));
            }
        }  else if(Map.class.isAssignableFrom(fieldType)) {
            // Map类型
            schema.setValueType("map");
        } else {
            // 其他复杂类型，递归创建JsonSchema
            schema.setValueType("object");
            schema.setChild(JsonSchema.toSchema(fieldType));

        }
        return schema;
    }

    private static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() ||
                type == Boolean.class ||
                type == Character.class ||
                type == Byte.class ||
                type == Short.class ||
                type == Integer.class ||
                type == Long.class ||
                type == Float.class ||
                type == Double.class;
    }
}
