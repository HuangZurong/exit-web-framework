package org.exitsoft.orm.core.hibernate.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.exitsoft.common.utils.CollectionUtils;
import org.exitsoft.common.utils.ReflectionUtils;
import org.exitsoft.orm.core.Page;
import org.exitsoft.orm.core.PageRequest;
import org.exitsoft.orm.core.PageRequest.Sort;
import org.exitsoft.orm.core.PropertyFilter;
import org.exitsoft.orm.core.PropertyFilterConstructors;
import org.exitsoft.orm.core.RestrictionNames;
import org.exitsoft.orm.core.hibernate.CriterionBuilder;
import org.exitsoft.orm.core.hibernate.HibernateRestrictionBuilder;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.transform.ResultTransformer;
import org.springframework.util.Assert;

/**
 * {@link BasicHibernateDao}基础扩展类。包含对{@link PropertyFilter}的支持。或其他查询的支持
 * 
 * @author vincent
 *
 * @param <T> ORM对象
 * @param <PK> 主键Id类型
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class HibernateSupportDao<T,PK extends Serializable> extends BasicHibernateDao<T, PK>{
	
	public HibernateSupportDao(){
		
	}
	
	public HibernateSupportDao(Class entityClass){
		super(entityClass);
	}
	
	/**
	 * 执行count查询获得本次Criteria查询所能获得的对象总数.
	 * 
	 * @param c Criteria对象
	 * 
	 * @return long
	 */
	protected long countCriteriaResult( Criteria c) {
		CriteriaImpl impl = (CriteriaImpl) c;

		// 先把Projection、ResultTransformer、OrderBy取出来,清空三者后再执行Count操作
		Projection projection = impl.getProjection();
		ResultTransformer transformer = impl.getResultTransformer();

		List<CriteriaImpl.OrderEntry> orderEntries = null;
		try {
			orderEntries = (List) ReflectionUtils.getFieldValue(impl,"orderEntries");
			ReflectionUtils.setFieldValue(impl, "orderEntries", new ArrayList());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 执行Count查询
		Long totalCountObject = (Long) c.setProjection(Projections.rowCount()).uniqueResult();
		long totalCount = (totalCountObject != null) ? totalCountObject : 0;

		// 将之前的Projection,ResultTransformer和OrderBy条件重新设回去
		c.setProjection(projection);

		if (projection == null) {
			c.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);
		}
		if (transformer != null) {
			c.setResultTransformer(transformer);
		}
		
		try {
			ReflectionUtils.setFieldValue(impl, "orderEntries", orderEntries);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return totalCount;
	}
	
	/**
	 * 通过表达式和对比值创建Criteria,要求表达式与值必须相等
	 * <pre>
	 * 	如：
	 * 	createCriteria(new String[]{"EQS_propertyName1","NEI_propertyName2"},new String[]{"vincent","vincent|admin"})
	 * 	对比值长度与表达式长度必须相等
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * 
	 * @return {@link Criteria}
	 */
	protected Criteria createCriteria(String[] expressions,String[] matchValues) {
		return createCriteria(expressions, matchValues, StringUtils.EMPTY);
	}
	
	/**
	 * 通过表达式和对比值创建Criteria,要求表达式与值必须相等
	 * <pre>
	 * 	如：
	 * 	createCriteria(new String[]{"EQS_propertyName1","NEI_propertyName2"},new String[]{"vincent","vincent|admin"})
	 * 	对比值长度与表达式长度必须相等
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * 
	 * @return {@link Criteria}
	 */
	protected Criteria createCriteria(String[] expressions,String[] matchValues,String orderBy) {
		return createCriteria(expressions, matchValues, orderBy,this.entityClass);
	}
	
	/**
	 * 通过表达式和对比值创建Criteria,要求表达式与值必须相等
	 * <pre>
	 * 	如：
	 * 	createCriteria(new String[]{"EQS_propertyName1","NEI_propertyName2"},new String[]{"vincent","vincent|admin"})
	 * 	对比值长度与表达式长度必须相等
	 * <pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * @param persistentClass orm实体Class
	 * 
	 * @return {@link Criteria}
	 */
	protected Criteria createCriteria(String[] expressions,String[] matchValues,Class<?> persistentClass) {
		return createCriteria(expressions,matchValues,StringUtils.EMPTY,persistentClass);
	}
	
	/**
	 * 通过表达式和对比值创建Criteria,要求表达式与值必须相等
	 * <pre>
	 * 	如：
	 * 	createCriteria(new String[]{"EQS_propertyName1","NEI_propertyName2"},new String[]{"vincent","vincent|admin"})
	 * 	对比值长度与表达式长度必须相等
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * @param persistentClass orm实体Class
	 * 
	 * @return {@link Criteria}
	 */
	protected Criteria createCriteria(String[] expressions,String[] matchValues,String orderBy,Class<?> persistentClass) {
		List<PropertyFilter> filters = PropertyFilterConstructors.createPropertyFilters(expressions, matchValues);
		return createCriteria(filters,orderBy,persistentClass);
	}
	
	/**
	 * 根据{@link PropertyFilter}创建Criteria
	 * 
	 * @param filters 属性过滤器
	 * 
	 * @return {@link Criteria}
	 */
	protected Criteria createCriteria(List<PropertyFilter> filters) {
		return createCriteria(filters,StringUtils.EMPTY);
	}
	
	/**
	 * 根据{@link PropertyFilter}创建Criteria
	 * 
	 * @param filters 属性过滤器
	 * @param persistentClass orm实体Class
	 * 
	 * @return {@link Criteria}
	 */
	protected Criteria createCriteria(List<PropertyFilter> filters,Class<?> persistentClass) {
		return createCriteria(filters,StringUtils.EMPTY,persistentClass);
	}
	
	/**
	 * 根据{@link PropertyFilter}创建Criteria
	 * 
	 * @param filters 属性过滤器
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * 
	 * @return {@link Criteria}
	 */
	protected Criteria createCriteria(List<PropertyFilter> filters,String orderBy) {
		return createCriteria(filters,orderBy, this.entityClass);
	}
	
	/**
	 * 根据{@link PropertyFilter}创建Criteria
	 * 
	 * @param filters 属性过滤器
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * @param persistentClass orm实体Class
	 * 
	 * @return {@link Criteria}
	 */
	protected Criteria createCriteria(List<PropertyFilter> filters,String orderBy,Class<?> persistentClass) {
		
		if(persistentClass == null) {
			persistentClass = this.entityClass;
		}
		
		Criteria criteria = createCriteria(persistentClass,orderBy);
		
		if (CollectionUtils.isEmpty(filters)) {
			return criteria;
		}
		for (PropertyFilter filter : filters) {
			criteria.add(createCriterion(filter));
		}
		return criteria;
	}
	
	/**
	 * 通过表达式和对比值创建Criterion
	 * <pre>
	 * 	如：
	 * 	createCriterion("EQS_propertyName","vincent")
	 * 	对比值长度与表达式长度必须相等
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * 
	 * @return {@link Criterion}
	 */
	protected Criterion createCriterion(String expression,String matchValue) {
		PropertyFilter filter = PropertyFilterConstructors.createPropertyFilter(expression, matchValue);
		return createCriterion(filter);
	}
	
	/**
	 * 通过{@link PropertyFilter} 创建 Criterion
	 * 
	 * @param filter 属性过滤器
	 * 
	 * @return {@link Criterion}
	 */
	protected Criterion createCriterion(PropertyFilter filter) {
		if (filter == null) {
			return null;
		}
		return HibernateRestrictionBuilder.getRestriction(filter);
	}
	
	/**
	 * 根据detachedCriteria查询全部
	 * 
	 * @param detachedCriteria detachedCriteria
	 * 
	 * @return List
	 */
	public <X> List<X> findByDetachedCriteria(DetachedCriteria detachedCriteria) {
		return createCriteria(detachedCriteria).list();
	}

	/**
	 * 根据{@link PropertyFilter} 查询全部
	 * 
	 * @param filters 属性过滤器
	 * 
	 * @return List
	 */
	public List<T> findByPropertyFilters(List<PropertyFilter> filters) {
		return findByPropertyFilters(filters,this.entityClass);
	}
	
	/**
	 * 根据{@link PropertyFilter} 查询全部
	 * 
	 * @param filters 属性过滤器
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * 
	 * @return List
	 */
	public List<T> findByPropertyFilters(List<PropertyFilter> filters,String orderBy) {
		return findByPropertyFilters(filters,orderBy,this.entityClass);
	}
	
	/**
	 * 根据{@link PropertyFilter} 查询全部
	 * 
	 * @param filters 属性过滤器
	 * @param persistentClass orm实体Class
	 * 
	 * @return List
	 */
	public <X> List<X> findByPropertyFilters(List<PropertyFilter> filters,Class<?> persistentClass) {
		return findByPropertyFilters(filters,StringUtils.EMPTY,persistentClass);
	}
	
	/**
	 * 根据{@link PropertyFilter} 查询全部
	 * 
	 * @param filters 属性过滤器
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * @param persistentClass orm实体Class
	 * 
	 * @return List
	 */
	public <X> List<X> findByPropertyFilters(List<PropertyFilter> filters,String orderBy,Class<?> persistentClass) {
		return createCriteria(filters, orderBy,persistentClass).list();
	}
	
	/**
	 * 通过表达式和对比值查询全部
	 * <pre>
	 * 如：
	 * findByExpressions(new String[]{"EQS_propertyName1","NEI_propertyName2"},new String[]{"vincent","vincent|admin"})
	 * 对比值长度与表达式长度必须相等
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * 
	 * @return List
	 */
	public List<T> findByExpressions(String[] expressions,String[] matchValues) {
		return findByExpressions(expressions,matchValues,this.entityClass);
	}
	
	/**
	 * 通过表达式和对比值查询全部
	 * <pre>
	 * 如：
	 * findByExpressions(new String[]{"EQS_propertyName1","NEI_propertyName2"},new String[]{"vincent","vincent|admin"},"propertyName_asc")
	 * 对比值长度与表达式长度必须相等
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * 
	 * @return List
	 */
	public List<T> findByExpressions(String[] expressions,String[] matchValues,String orderBy) {
		return findByExpressions(expressions,matchValues,orderBy,this.entityClass);
	}
	
	/**
	 * 通过表达式和对比值查询全部
	 * <pre>
	 * 如：
	 * findByExpressions(new String[]{"EQS_propertyName1","NEI_propertyName2"},new String[]{"vincent","vincent|admin"},OtherOrm.class)
	 * 对比值长度与表达式长度必须相等
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * @param persistentClass orm实体Class
	 * 
	 * @return List
	 */
	public <X> List<X> findByExpressions(String[] expressions,String[] matchValues,Class<?> persistentClass) {
		return findByExpressions(expressions,matchValues,StringUtils.EMPTY,persistentClass);
	}
	
	/**
	 * 通过表达式和对比值查询全部
	 * <pre>
	 * 如：
	 * findByExpressions(new String[]{"EQS_propertyName1","NEI_propertyName2"},new String[]{"vincent","vincent|admin"},"propertyName_asc",OtherOrm.class)
	 * 对比值长度与表达式长度必须相等
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * @param persistentClass orm实体Class
	 * 
	 * @return List
	 */
	public <X> List<X> findByExpressions(String[] expressions,String[] matchValues,String orderBy,Class<?> persistentClass) {
		return createCriteria(expressions, matchValues, orderBy, persistentClass).list();
	}
	
	/**
	 * 通过表达式和对比值查询全部
	 * <pre>
	 * 如：
	 * findByExpression("EQS_propertyName","vincent")
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * @param persistentClass orm实体Class
	 * 
	 * @return List
	 */
	public List<T> findByExpression(String expression,String matchValue) {
		return findByExpression(expression,matchValue,this.entityClass);
	}
	
	/**
	 * 通过表达式和对比值查询全部
	 * <pre>
	 * 如：
	 * findByExpression("EQS_propertyName","vincent","propertyName_asc")
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * 
	 * @return List
	 */
	public List<T> findByExpression(String expression,String matchValue,String orderBy) {
		return findByExpression(expression,matchValue,orderBy,this.entityClass);
	}
	
	/**
	 * 通过表达式和对比值查询全部
	 * 
	 * <pre>
	 * 如：
	 * findByExpression("EQS_propertyName","vincent",OtherOrm.class)
	 * </p>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * @param persistentClass orm实体Class
	 * 
	 * @return List
	 */
	public <X> List<X> findByExpression(String expression,String matchValue,Class<?> persistentClass) {
		return findByExpression(expression,matchValue,StringUtils.EMPTY,persistentClass);
	}
	
	/**
	 * 通过表达式和对比值查询全部
	 * <pre>
	 * 如：
	 * findByExpression("EQS_propertyName","vincent",OtherOrm.class)
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * @param persistentClass orm实体Class
	 * 
	 * @return List
	 */
	public <X> List<X> findByExpression(String expression,String matchValue,String orderBy,Class<?> persistentClass) {
		return createCriteria(persistentClass, orderBy, createCriterion(expression, matchValue)).list();
	}
	

	/**
	 * 根据Criterion查询全部
	 * 
	 * @param criterions 可变长度的Criterion数组
	 * 
	 * @return List
	 */
	public List<T> findByCriterion(Criterion...criterions) {
		return findByCriterion(this.entityClass,criterions);
	}

	/**
	 * 根据Criterion查询全部
	 * 
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * @param criterions 可变长度的Criterion数组
	 * 
	 * @return List
	 */
	public List<T> findByCriterion(String orderBy,Criterion...criterions) {
		return findByCriterion(this.entityClass,orderBy,criterions);
	}
	
	/**
	 * 根据Criterion查询全部
	 * 
	 * @param persistentClass orm实体Class
	 * @param criterions 可变长度的Criterion数组
	 * 
	 * @return List
	 */
	public <X> List<X> findByCriterion(Class<?> persistentClass,Criterion...criterions) {
		return findByCriterion(persistentClass,StringUtils.EMPTY,criterions);
	}
	
	/**
	 * 根据Criterion查询全部
	 * 
	 * @param persistentClass orm实体Class
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * @param criterions 可变长度的Criterion数组
	 * 
	 * @return List
	 */
	public <X> List<X> findByCriterion(Class<?> persistentClass,String orderBy,Criterion...criterions) {
		return createCriteria(persistentClass, orderBy, criterions).list();
	}
	
	/**
	 * 通过orm实体属性名称查询全部
	 * 
	 * @param propertyName orm实体属性名称
	 * @param value 值
	 * 
	 * @return List
	 */
	public List<T> findByProperty(String propertyName,Object value) {
		return findByProperty(propertyName, value, RestrictionNames.EQ);
	}
	
	/**
	 * 通过orm实体属性名称查询全部
	 * 
	 * @param propertyName orm实体属性名称
	 * @param value 值
	 * @param restrictionName 约束名称,参考{@link CriterionBuilder}的实现类
	 * 
	 * @return List
	 */
	public List<T> findByProperty(String propertyName,Object value,String restrictionName) {
		return findByPropertyWithOrderBy(propertyName, value, StringUtils.EMPTY,restrictionName);
	}
	
	/**
	 * 通过orm实体属性名称查询全部
	 * 
	 * @param propertyName orm实体属性名称
	 * @param value 值
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * 
	 * @return List
	 */
	public List<T> findByPropertyWithOrderBy(String propertyName,Object value,String orderBy) {
		return findByPropertyWithOrderBy(propertyName, value, orderBy, RestrictionNames.EQ);
	}
	
	/**
	 * 通过orm实体属性名称查询全部
	 * 
	 * @param propertyName orm实体属性名称
	 * @param value 值
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * @param restrictionName 约束名称,参考{@link CriterionBuilder}的实现类
	 * 
	 * @return List
	 */
	public List<T> findByPropertyWithOrderBy(String propertyName,Object value,String orderBy,String restrictionName) {
		return findByProperty(propertyName, value, restrictionName, this.entityClass,orderBy);
	}
	
	/**
	 * 通过orm实体属性名称查询全部
	 * 
	 * @param propertyName orm实体属性名称
	 * @param value 值
	 * @param persistentClass orm实体Class
	 * 
	 * @return List
	 */
	public <X> List<X> findByProperty(String propertyName,Object value,Class<?> persistentClass) {
		return findByProperty(propertyName, value, RestrictionNames.EQ, persistentClass);
	}
	
	/**
	 * 通过orm实体属性名称查询全部
	 * 
	 * @param propertyName orm实体属性名称
	 * @param value 值
	 * @param persistentClass orm实体Class
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * 
	 * @return List
	 */
	public <X> List<X> findByPropertyWithOrderBy(String propertyName,Object value,Class<?> persistentClass,String orderBy) {
		return findByProperty(propertyName, value, RestrictionNames.EQ, persistentClass,orderBy);
	}
	
	/**
	 * 通过orm实体属性名称查询全部
	 * 
	 * @param propertyName orm实体属性名称
	 * @param value 值
	 * @param restrictionName 约束名称,参考{@link CriterionBuilder}的实现类
	 * @param persistentClass orm实体Class
	 * 
	 * @return List
	 */
	public <X> List<X> findByProperty(String propertyName,Object value,String restrictionName,Class<?> persistentClass) {
		return findByProperty(propertyName, value, restrictionName, persistentClass, StringUtils.EMPTY);
	}
	
	/**
	 * 通过orm实体属性名称查询全部
	 * 
	 * @param propertyName orm实体属性名称
	 * @param value 值
	 * @param restrictionName 约束名称,参考{@link CriterionBuilder}的实现类
	 * @param persistentClass orm实体Class
	 * @param orderBy 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 * 
	 * @return List
	 */
	public <X> List<X> findByProperty(String propertyName,Object value,String restrictionName,Class<?> persistentClass,String orderBy) {
		Criterion criterion = HibernateRestrictionBuilder.getRestriction(propertyName, value, restrictionName);
		return createCriteria(persistentClass, orderBy, criterion).list();
	}
	
	/**
	 * 通过detachedCriteria查询单个orm实体
	 * 
	 * @param detachedCriteria hibernate detachedCriteria
	 * 
	 * @return Object
	 */
	public <X> X findUniqueByDetachedCriteria(DetachedCriteria detachedCriteria) {
		return (X) createCriteria(detachedCriteria).uniqueResult();
	}
	
	/**
	 * 通过{@link PropertyFilter} 查询单个orm实体
	 * 
	 * @param filters 属性过滤器
	 * 
	 * @return Object
	 * 
	 */
	public T findUniqueByPropertyFilters(List<PropertyFilter> filters) {
		return (T)findUniqueByPropertyFilters(filters, this.entityClass);
	}
	
	/**
	 * 通过{@link PropertyFilter} 查询单个orm实体
	 * 
	 * @param filters 属性过滤器
	 * @param persistentClass orm 实体Class
	 * 
	 * @return Object
	 * 
	 */
	public <X> X findUniqueByPropertyFilters(List<PropertyFilter> filters,Class<?> persistentClass) {
		return (X) createCriteria(filters, persistentClass).uniqueResult();
	}

	/**
	 * 通过表达式和对比值查询单个orm实体
	 * <pre>
	 * 如：
	 * findUniqueByExpression("EQS_propertyName","vincent")
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * 
	 * @return Object
	 */
	public T findUniqueByExpression(String expression,String matchValue) {
		return (T)findUniqueByExpression(expression, matchValue,this.entityClass);
	}
	
	/**
	 * 通过表达式和对比值查询单个orm实体
	 * <pre>
	 * 如：
	 * findUniqueByExpressions(new String[]{"EQS_propertyName1","NEI_propertyName2"},new String[]{"vincent","vincent|admin"})
	 * 对比值长度与表达式长度必须相等
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * 
	 * @return Object
	 */
	public T findUniqueByExpressions(String[] expressions,String[] matchValues) {
		return (T)findUniqueByExpressions(expressions,matchValues,this.entityClass);
	}
	
	/**
	 * 通过criterion数组查询单个orm实体
	 * 
	 * @param criterions criterion数组
	 * 
	 * @return Object
	 */
	public T findUniqueByCriterions(Criterion[] criterions){
		return (T)findUniqueByCriterions(criterions,this.entityClass);
	}
	
	/**
	 * 通过criterion数组查询单个orm实体
	 * 
	 * @param criterions criterion数组
	 * @param persistentClass orm实体Class
	 * 
	 * @return Object
	 */
	public <X> X findUniqueByCriterions(Criterion[] criterions,Class<?> persistentClass){
		return (X)createCriteria(persistentClass,criterions).uniqueResult();
	}

	/**
	 * 通过表达式和对比值查询单个orm实体
	 * <pre>
	 * 如：
	 * findUniqueByExpression("EQS_propertyName","vincent",OtherOrm.class)
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * 
	 * @return Object
	 */
	public <X> X findUniqueByExpression(String expression,String matchValue,Class<?> persistentClass) {
		Criterion criterion = createCriterion(expression, matchValue);
		return (X)findUniqueByCriterions(new Criterion[]{criterion}, persistentClass);
	}
	
	/**
	 * 通过表达式和对比值查询单个orm实体
	 * <pre>
	 * 如：
	 * findUniqueByExpressions(new String[]{"EQS_propertyName1","NEI_propertyName2"},new String[]{"vincent","vincent|admin"},OtherOrm.class)
	 * 对比值长度与表达式长度必须相等
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * @param persistentClass orm实体Class
	 * 
	 * @return Object
	 */
	public <X> X findUniqueByExpressions(String[] expressions,String[] matchValues,Class<?> persistentClass) {
		return (X)createCriteria(expressions, matchValues, StringUtils.EMPTY, persistentClass).uniqueResult();
	}

	/**
	 * 通过orm实体的属性名称查询单个orm实体
	 * 
	 * @param propertyName 属性名称
	 * @param value 值
	 * 
	 * @return Object
	 */
	public T findUniqueByProperty(String propertyName,Object value) {
		return (T)findUniqueByProperty(propertyName,value,RestrictionNames.EQ);
	}
	
	/**
	 * 通过orm实体的属性名称查询单个orm实体
	 * 
	 * @param propertyName 属性名称
	 * @param value 值
	 * @param restrictionName 约束名称 参考{@link CriterionBuilder}的所有实现类
	 * 
	 * @return Object
	 */
	public T findUniqueByProperty(String propertyName,Object value,String restrictionName) {
		return (T)findUniqueByProperty(propertyName,value,restrictionName,this.entityClass);
	}
	
	/**
	 * 
	 * 通过orm实体的属性名称查询单个orm实体
	 * 
	 * @param propertyName 属性名称
	 * @param value 值
	 * @param persistentClass ORM对象类型Class
	 * 
	 * @return Object
	 */
	public <X> X findUniqueByProperty(String propertyName,Object value,Class<?> persistentClass) {
		return (X)findUniqueByProperty(propertyName,value,RestrictionNames.EQ, persistentClass);
	}
	
	/**
	 * 通过orm实体的属性名称查询单个orm实体
	 * 
	 * @param propertyName 属性名称
	 * @param value 值
	 * @param restrictionName 约束名称 参考{@link CriterionBuilder}的所有实现类
	 * @param persistentClass orm实体Class
	 * 
	 * @return Object
	 */
	public <X> X findUniqueByProperty(String propertyName,Object value,String restrictionName,Class<?> persistentClass) {
		Criterion criterion = HibernateRestrictionBuilder.getRestriction(propertyName, value, restrictionName);
		return (X) createCriteria(persistentClass, criterion).uniqueResult();
	}
	
	/**
	 * 通过DetachedCriteria和分页请求参数获取分页对象
	 * 
	 * @param request 分页请求参数
	 * @param detachedCriteria Hiberante DetachedCriteria
	 * 
	 * @return {@link Page}
	 */
	public <X> Page<X> findPage(PageRequest request,DetachedCriteria detachedCriteria) {
		Criteria criteria = createCriteria(detachedCriteria);
		return findPage(request,criteria);
	}
	
	/**
	 * 通过Criterion和分页请求参数获取分页对象
	 * 
	 * @param request 分页请求参数
	 * @param criterions 可变的Criterion对象
	 * 
	 * @return {@link Page}
	 */
	public Page<T> findPage(PageRequest request, Criterion... criterions) {
		return findPage(request,this.entityClass,criterions);
	}
	
	/**
	 * 通过Criterion和分页请求参数获取分页对象
	 * 
	 * @param request 分页请求参数
	 * @param persistentClass orm实体Class
	 * @param criterions 可变长度的Criterion数组
	 * 
	 * 
	 * @return {@link Page}
	 */
	public <X> Page<X> findPage(PageRequest request,Class<?> persistentClass, Criterion... criterions) {
		Criteria c = createCriteria(persistentClass,criterions);
		return findPage(request,c);
	}
	
	/**
	 * 通过分页参数，和属性过滤器查询分页
	 * 
	 * @param request
	 * @param filters
	 * 
	 * @return {@link Page}
	 */
	public Page<T> findPage(PageRequest request,List<PropertyFilter> filters) {
		return findPage(request,filters,this.entityClass);
	}
	
	/**
	 * 通过{@link PropertyFilter}和分页请求参数获取分页对象
	 * 
	 * @param request 分页请求参数
	 * @param filters 属性过滤器集合
	 * @param persistentClass orm实体Class
	 * 
	 * 
	 * @return {@link Page}
	 */
	public <X> Page<X> findPage(PageRequest request,List<PropertyFilter> filters,Class<?> persistentClass) {
		Criteria c = createCriteria(filters, persistentClass);
		return findPage(request,c);
	}
	
	/**
	 * 通过分页请求参数和表达式与对比值获取分页对象
	 * 
	 * <pre>
	 * 如：
	 * findPage(request,"EQS_propertyName","vincent")
	 * </pre>
	 * 
	 * @param request 分页请求参数
	 * @param expression 表达式
	 * @param matchValue 对比值
	 * 
	 * @return {@link Page}
	 */
	public Page<T> findPage(PageRequest request,String expression,String matchValue) {
		
		return findPage(request, expression,matchValue,this.entityClass);
	}
	
	/**
	 * 通过分页请求参数和表达式与对比值获取分页对象
	 * <pre>
	 * 如：
	 * findPage(request,"EQS_propertyName","vincent",OtherOrm.class)
	 * </pre>
	 * 
	 * @param request 分页请求参数
	 * @param expression 表达式
	 * @param matchValue 对比值
	 * @param persistentClass orm实体Class
	 * 
	 * @return {@link Page}
	 */
	public <X> Page<X> findPage(PageRequest request,String expression,String matchValue,Class<?> persistentClass) {
		Criterion criterion = createCriterion(expression, matchValue);
		Criteria criteria = createCriteria(persistentClass,criterion);
		return findPage(request, criteria);
	}
	
	/**
	 * 通过表达式和对比值获取分页对象
	 * <pre>
	 * 如：
	 * findPage(new String[]{"EQS_propertyName1","NEI_propertyName2"},new String[]{"vincent","vincent|admin"})
	 * 对比值长度与表达式长度必须相等
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * 
	 * @return Object
	 */
	public Page<T> findPage(PageRequest request,String[] expressions,String[] matchValues) {
		return findPage(request, expressions,matchValues,this.entityClass);
	}
	
	/**
	 * 通过表达式和对比值获取分页对象
	 * <pre>
	 * 如：
	 * findPage(new String[]{"EQS_propertyName1","NEI_propertyName2"},new String[]{"vincent","vincent|admin"},OtherOrm.class)
	 * 对比值长度与表达式长度必须相等
	 * </pre>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * @param persistentClass orm实体Class
	 * 
	 * @return Object
	 */
	public <X> Page<X> findPage(PageRequest request,String[] expressions,String[] matchValues,Class<?> persistentClass) {
		Criteria criteria = createCriteria(expressions, matchValues, persistentClass);
		return findPage(request, criteria);
	}
	
	/**
	 * 根据分页参数与Criteria获取分页对象
	 * 
	 * @param request 分页请求参数
	 * @param c Criteria对象
	 * 
	 * @return {@link Page}
	 */
	public <X> Page<X> findPage(PageRequest request, Criteria c) {

		Page<X> page = new Page<X>(request);
		
		if (request == null) {
			return page;
		}
		
		if (request.isCountTotal()) {
			long totalCount = countCriteriaResult(c);
			page.setTotalItems(totalCount);
		}
		
		setPageRequestToCriteria(c, request);
		
		List result = c.list();
		page.setResult(result); 
		
		return page;
	}
	
	/**
	 * 根据分页请求参数与Query获取分页请求对象
	 * 
	 * @param request 分页请求参数对象
	 * @param query Hibernate Query
	 * 
	 * @return {@link Page}
	 */
	public <X> Page<X> findPage(PageRequest request, Query query) {
		Page<X> page = new Page<X>(request);
		
		if (request == null) {
			return page;
		}
		
		if (request.isCountTotal()) {
			List<Object> values = ReflectionUtils.invokeGetterMethod(query, "values");
			long totalCount = countHqlResult(query.getQueryString(), values.toArray());
			page.setTotalItems(totalCount);
		}
		
		setPageRequestToQuery(query, request);
		
		List result = query.list();
		page.setResult(result);
		
		return page;
	}
	
	/**
	 * 通过分页参数与HQL语句获取分页对象
	 * 
	 * @param request 分页请求参数
	 * @param queryString HQL语句
	 * @param values 值
	 * 
	 * @return {@link Page}
	 */
	public <X> Page<X> findPage(PageRequest request,String queryString,Object... values) {
		
		Page<X> page = createQueryPage(request, queryString, values);
		Query q = createQuery(queryString, values);

		setPageRequestToQuery(q, request);

		List result = q.list();
		page.setResult(result);
		return page;
	}
	
	/**
	 * 根据NamedQuery获取分页对象
	 * 
	 * @param request 分页请求参数
	 * @param namedQuery hibernate named query
	 * @param values 值
	 * 
	 * @return Page
	 */
	public <X> Page<X> findPageByNamedQuery(PageRequest request, String namedQuery,Object... values) {
		Query query = createQueryByNamedQuery(namedQuery, values);
		Page<X> page = new Page<X>(request);
		
		if (request == null) {
			return page;
		}
		
		if (request.isCountTotal()) {
			
			long totalCount = countHqlResult(query.getQueryString(), values);
			page.setTotalItems(totalCount);
		}
		
		setPageRequestToQuery(query, request);
		
		List result = query.list();
		page.setResult(result);
		
		return page;
	}
	
	/**
	 * 根据NamedQuery获取分页对象
	 * 
	 * @param request 分页请求参数
	 * @param namedQuery hibernate named query
	 * @param values 值
	 * 
	 * @return Page
	 */
	public <X> Page<X> findPageByNamedQuery(PageRequest request, String namedQuery,Map<String, Object> values) {
		Query query = createQueryByNamedQuery(namedQuery, values);
		return findPage(request,query);
	}
	
	/**
	 * 根据NamedQuery获取分页对象（使用jpa风格参数方式）
	 * 
	 * @param request 分页请求参数
	 * @param namedQuery hibernate named query
	 * @param values 值
	 * 
	 * @return Page
	 */
	public <X> Page<X> findPageByNamedQueryUseJpaStyle(PageRequest request, String namedQuery,Object... values) {
		Query query = createQueryByNamedQueryUseJpaStyle(namedQuery, values);
		Page<X> page = new Page<X>(request);
		
		if (request == null) {
			return page;
		}
		
		if (request.isCountTotal()) {
			
			long totalCount = countHqlResultUseJpaStyle(query.getQueryString(), values);
			page.setTotalItems(totalCount);
		}
		
		setPageRequestToQuery(query, request);
		
		List result = query.list();
		page.setResult(result);
		return page;
	}
	
	/**
	 * 通过分页参数与HQL语句获取分页对象
	 * 
	 * @param request 分页请求参数
	 * @param queryString HQL语句
	 * @param values 值
	 * 
	 * @return {@link Page}
	 */
	public <X> Page<X> findPage(PageRequest request, String queryString,Map<String,Object> values) {
		
		Page<X> page = createQueryPage(request, queryString, values);
		Query q = createQuery(queryString, values);

		setPageRequestToQuery(q, request);

		List result = q.list();
		page.setResult(result);
		return page;
	}
	
	/**
	 * 通过分页请求参数和HQL创建分页对象
	 * 
	 * @param pageRequest 分页请求参数
	 * @param queryString HQL
	 * @param values 值
	 * 
	 * @return {@link Page}
	 */
	protected <X> Page<X> createQueryPage(PageRequest pageRequest, String queryString, Object... values) {

		Page<X> page = new Page<X>(pageRequest);
		
		if (pageRequest == null) {
			return page;
		}
		
		if (pageRequest.isCountTotal()) {
			long totalCount = countHqlResult(queryString, values);
			page.setTotalItems(totalCount);
		}

		if (pageRequest.isOrderBySetted()) {
			queryString = setPageRequestToHql(queryString, pageRequest);
		}
		
		return page;
	}
	
	/**
	 * 在HQL的后面添加分页参数定义的orderBy, 辅助函数.
	 */
	protected String setPageRequestToHql( String hql, PageRequest pageRequest) {
		StringBuilder builder = new StringBuilder(hql);
		builder.append(" order by");

		for (Sort orderBy : pageRequest.getSort()) {
			builder.append(String.format(" %s.%s %s,", DEFAULT_ALIAS,orderBy.getProperty(), orderBy.getDir()));
		}

		builder.deleteCharAt(builder.length() - 1);

		return builder.toString();
	}
	
	/**
	 * 设置分页参数到Query对象,辅助函数.
	 */
	protected Query setPageRequestToQuery( Query q, PageRequest pageRequest) {
		q.setFirstResult(pageRequest.getOffset());
		q.setMaxResults(pageRequest.getPageSize());
		return q;
	}
	
	/**
	 * 设置分页参数到Criteria对象,辅助函数.
	 * 
	 * @param c Hibernate Criteria
	 * @param pageRequest 分页请求参数
	 * 
	 * @return {@link Criteria}
	 */
	protected Criteria setPageRequestToCriteria( Criteria c,  PageRequest pageRequest) {
		Assert.isTrue(pageRequest.getPageSize() > 0, "分页大小必须大于0");

		c.setFirstResult(pageRequest.getOffset());
		c.setMaxResults(pageRequest.getPageSize());

		if (pageRequest.isOrderBySetted()) {
			for (Sort sort : pageRequest.getSort()) {
				setOrderToCriteria(c,sort.getProperty(),sort.getDir());
			}
		}
		return c;
	}
	
}
