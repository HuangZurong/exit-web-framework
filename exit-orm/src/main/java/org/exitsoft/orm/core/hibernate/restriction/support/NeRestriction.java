package org.exitsoft.orm.core.hibernate.restriction.support;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

/**
 * 不等于约束 ( from object o where o.value <> ?) RestrictionName:NE
 * <p>
 * 表达式:NE属性类型_属性名称[_OR_属性名称...]
 * </p>
 * 
 * @author vincent
 *
 */
public class NeRestriction extends EqRestriction{
	
	public final static String RestrictionName = "NE";


	
	public String getRestrictionName() {
		
		return RestrictionName;
	}


	
	public Criterion build(String propertyName, Object value) {
		
		return value == null ? Restrictions.isNotNull(propertyName) : Restrictions.ne(propertyName, value);
		
	}

	
}
