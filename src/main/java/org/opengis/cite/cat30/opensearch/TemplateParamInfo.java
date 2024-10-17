package org.opengis.cite.cat30.opensearch;

import java.util.Objects;

import javax.xml.namespace.QName;

/**
 * Provides information about a URL template parameter appearing in an OpenSearch
 * description document.
 *
 * @see <a target="_blank" href=
 * "http://www.opensearch.org/Specifications/OpenSearch/1.1#OpenSearch_URL_template_syntax">OpenSearch
 * URL template syntax</a>
 */
public class TemplateParamInfo {

	private QName name;

	private boolean isRequired;

	private Class type;

	private Object defaultValue;

	/**
	 * Default constructor sets the parameter type to be String with a zero-length string
	 * as the default value.
	 */
	public TemplateParamInfo() {
		this.type = String.class;
		this.defaultValue = new String();
	}

	/**
	 * Constructs a new TemplateParamInfo with the given qualified name.
	 * @param name A QName representing the name of the parameter.
	 * @param isRequired A boolean value indicating whether or not the parameter is
	 * required.
	 */
	public TemplateParamInfo(QName name, boolean isRequired) {
		this();
		this.name = name;
		this.isRequired = isRequired;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + Objects.hashCode(this.name);
		hash = 67 * hash + (this.isRequired ? 1 : 0);
		return hash;
	}

	@Override
	public boolean equals(Object that) {
		boolean isEqual;
		if (that == null || this.getClass() != that.getClass()) {
			isEqual = false;
		}
		else {
			final TemplateParamInfo other = (TemplateParamInfo) that;
			isEqual = this.name.equals(other.name) && this.isRequired == other.isRequired;
		}
		return isEqual;
	}

	/**
	 * Get the default value of the parameter.
	 * @return A instance of the parameter's type, or a zero-length string if it has not
	 * been set.
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Set the default value of the parameter.
	 * @param defaultValue A type-compatible value.
	 */
	public void setDefaultValue(Object defaultValue) {
		if (!this.type.isInstance(defaultValue)) {
			throw new IllegalArgumentException("Default value must be an instance of " + this.type.getName());
		}
		this.defaultValue = defaultValue;
	}

	/**
	 * Get the data type of the parameter.
	 * @return The parameter type.
	 */
	public Class getType() {
		return type;
	}

	/**
	 * Set the data type of the parameter (java.lang.String by default).
	 * @param type The new data type.
	 *
	 */
	public void setType(Class type) {
		this.type = type;
	}

	/**
	 * Get the value of isRequired
	 * @return the value of isRequired
	 */
	public boolean isRequired() {
		return isRequired;
	}

	/**
	 * Set the value of isRequired
	 * @param isRequired new value of isRequired
	 */
	public void setIsRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}

	/**
	 * Get the qualified name of the parameter.
	 * @return A QName object specifying a name.
	 */
	public QName getName() {
		return name;
	}

	/**
	 * Set the (qualified) name of the parameter.
	 * @param name A QName object.
	 */
	public void setName(QName name) {
		this.name = name;
	}

}
