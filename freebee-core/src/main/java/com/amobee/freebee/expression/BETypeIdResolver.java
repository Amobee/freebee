package com.amobee.freebee.expression;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

/**
 * @author Michael Bond
 */
public class BETypeIdResolver extends TypeIdResolverBase
{
    private JavaType baseType;

    public BETypeIdResolver()
    {
    }

    @Override
    public void init(final JavaType baseType)
    {
        this.baseType = baseType;
    }

    @Override
    public String idFromValue(final Object value)
    {
        return ((BENode) value).getType();
    }

    @Override
    public String idFromValueAndType(final Object value, final Class<?> suggestedType)
    {
        return idFromValue(value);
    }

    @Override
    public String idFromBaseType()
    {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("checkstyle:returncount")
    @Override
    public JavaType typeFromId(final DatabindContext context, final String id)
    {
        if (BooleanExpression.class.isAssignableFrom(this.baseType.getRawClass()))
        {
            return context.constructSpecializedType(this.baseType, this.baseType.getRawClass());
        }
        else
        {
            this.baseType.getRawClass();
            switch (id.toUpperCase())
            {
            case BEConstants.NODE_TYPE_AND:
            case BEConstants.NODE_TYPE_OR:
                return context.constructSpecializedType(this.baseType, BEConjunctionNode.class);
            case BEConstants.NODE_TYPE_REFERENCE:
                return context.constructSpecializedType(this.baseType, BEReferenceNode.class);
            default:
                return context.constructSpecializedType(this.baseType, BEPredicateNode.class);
            }
        }
    }

    @Override
    public JsonTypeInfo.Id getMechanism()
    {
        return JsonTypeInfo.Id.CUSTOM;
    }
}
