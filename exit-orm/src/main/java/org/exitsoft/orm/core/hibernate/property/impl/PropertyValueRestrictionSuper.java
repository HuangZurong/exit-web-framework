package org.exitsoft.orm.core.hibernate.property.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.exitsoft.common.utils.ConvertUtils;
import org.exitsoft.orm.core.MatchValue;
import org.exitsoft.orm.core.PropertyFilter;
import org.exitsoft.orm.core.hibernate.property.PropertyCriterionBuilder;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;

/**
 * 处理{@link PropertyFilter#getMatchValue()}的基类，本类对3种值做处理
 * <p>
 * 1.值等于正常值的，如："amdin"，会产生的squall为:property = 'admin'
 * </p>
 * <p>
 * 2.值等于或值的，如："admin_OR_vincent"，会产生的sql为:property = 'admin' or property = 'vincent'
 * </p>
 * <p>
 * 3.值等于与值的,如:"admin_AND_vincent"，会产生的sql为:property = 'admin' and property = 'vincent'
 * </p>
 * 
 * @author vincent
 *
 */
public abstract class PropertyValueRestrictionSuper implements PropertyCriterionBuilder{
	
	//or值分隔符
	private String orValueSeparator = "|";
	//and值分隔符
	private String andValueSeparator = ",";
	
	public PropertyValueRestrictionSuper() {
		
	}
	
	
	public Criterion build(PropertyFilter filter) {
		String matchValue = filter.getMatchValue();
		Class<?> propertyType = filter.getPropertyType();
		
		MatchValue matchValueModel = createMatchValueModel(matchValue, propertyType);
		
		Junction junction = null;
		
		if (matchValueModel.hasOrOperate()) {
			junction = Restrictions.disjunction();
		} else {
			junction = Restrictions.conjunction();
		}
		
		for (Object value : matchValueModel.getValues()) {
			
			if (filter.hasMultiplePropertyNames()) {
				Disjunction disjunction = Restrictions.disjunction();
				for (String propertyName:filter.getPropertyNames()) {
					disjunction.add(build(propertyName,value));
				}
				junction.add(disjunction);
			} else {
				junction.add(build(filter.getSinglePropertyName(),value));
			}
			
		}
		
		return junction;
	}
	
	/**
	 * 创建对比值模型，如果多值将以逗号","或者竖杠"|"分割
	 * 
	 * @param matchValue
	 * @param type
	 * 
	 * @return
	 */
	public MatchValue createMatchValueModel(String matchValue,Class<?> type) {
		
		List<Object> values = new ArrayList<Object>();
		
		if (StringUtils.contains(matchValue, andValueSeparator)) {
			String[] siplit = StringUtils.splitByWholeSeparator(matchValue, andValueSeparator);
			CollectionUtils.addAll(values, (Object[])ConvertUtils.convertToObject(siplit, type));
			return new MatchValue(false, values);
		} else if (StringUtils.contains(matchValue, orValueSeparator)){
			String[] siplit = StringUtils.splitByWholeSeparator(matchValue, orValueSeparator);
			CollectionUtils.addAll(values, (Object[])ConvertUtils.convertToObject(siplit, type));
			return new MatchValue(true, values);
		} else {
			values.add(ConvertUtils.convertToObject(matchValue, type));
			return new MatchValue(false, values);
		}
		
	}

	/**
	 * 获取or值分隔符
	 * 
	 * @return String
	 */
	public String getOrValueSeparator() {
		return orValueSeparator;
	}

	/**
	 * 设置or值分隔符
	 * @param orValueSeparator or值分隔符
	 */
	public void setOrValueSeparator(String orValueSeparator) {
		this.orValueSeparator = orValueSeparator;
	}

	/**
	 * 获取and值分隔符
	 * 
	 * @return String
	 */
	public String getAndValueSeparator() {
		return andValueSeparator;
	}

	/**
	 * 设置and值分隔符
	 * @param andValueSeparator and值分隔符
	 */
	public void setAndValueSeparator(String andValueSeparator) {
		this.andValueSeparator = andValueSeparator;
	}
	
	
	
}
