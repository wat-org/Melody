package com.wat.melody.api;

import org.w3c.dom.Element;

import com.wat.melody.common.properties.PropertySet;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ICondition {

	public boolean isEligible(Element elmt, PropertySet ps);

}