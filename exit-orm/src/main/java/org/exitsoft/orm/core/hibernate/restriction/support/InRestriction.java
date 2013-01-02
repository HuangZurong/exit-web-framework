package org.exitsoft.orm.core.hibernate.restriction.support;

import org.exitsoft.orm.core.hibernate.restriction.CriterionMultipleValueSupport;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

/**
 * 包含约束 (from object o where o.value in (?,?,?,?,?))RestrictionName:IN
 * <p>
 * 表达式:IN属性类型_属性名称[_OR_属性名称...]
 * </p>
 * 
 * @author vincent
 *
 */
public class InRestriction extends CriterionMultipleValueSupport{

	public final static String RestrictionName = "IN";
	
	
	public String getRestrictionName() {
		return RestrictionName;
	}
	
	
	public Criterion buildRestriction(String propertyName, Object[] values) {
		return Restrictions.in(propertyName,values);
	}

}

