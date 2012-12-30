package org.exitsoft.orm.core.hibernate.property.impl.restriction;

import org.exitsoft.orm.core.hibernate.property.impl.PropertyValueRestrictionSupport;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

/**
 * 小于等于约束 ( from object o where o.value <= ?) RestrictionName:LR
 * <p>
 * 表达式:LE属性类型_属性名称[_OR_属性名称...]
 * </p>
 * 
 * @author vincent
 *
 */
public class LeRestriction extends PropertyValueRestrictionSupport{

	public final static String RestrictionName = "LE";
	
	
	public String getRestrictionName() {
		
		return RestrictionName;
	}

	
	public Criterion build(String propertyName, Object value) {
		return Restrictions.le(propertyName, value);
	}

}
