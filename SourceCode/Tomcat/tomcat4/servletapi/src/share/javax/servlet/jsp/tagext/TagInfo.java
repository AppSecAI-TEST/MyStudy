/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
 
package javax.servlet.jsp.tagext;

/**
 * Tag information for a tag in a Tag Library;
 * This class is instantiated from the Tag Library Descriptor file (TLD)
 * and is available only at translation time.
 *
 * 
*/

public class TagInfo {

    /**
     * static constant for getBodyContent() when it is JSP
     */

    public static final String BODY_CONTENT_JSP = "JSP";

    /**
     * static constant for getBodyContent() when it is Tag dependent
     */

    public static final String BODY_CONTENT_TAG_DEPENDENT = "TAGDEPENDENT";


    /**
     * static constant for getBodyContent() when it is empty
     */

    public static final String BODY_CONTENT_EMPTY = "EMPTY";

    /**
     * Constructor for TagInfo from data in the JSP 1.1 format for TLD.
     * This class is to be instantiated only from the TagLibrary code
     * under request from some JSP code that is parsing a
     * TLD (Tag Library Descriptor).
     *
     * Note that, since TagLibibraryInfo reflects both TLD information
     * and taglib directive information, a TagInfo instance is
     * dependent on a taglib directive.  This is probably a
     * design error, which may be fixed in the future.
     *
     * @param tagName The name of this tag
     * @param tagClassName The name of the tag handler class
     * @param bodycontent Information on the body content of these tags
     * @param infoString The (optional) string information for this tag
     * @param taglib The instance of the tag library that contains us.
     * @param tagExtraInfo The instance providing extra Tag info.  May be null
     * @param attributeInfo An array of AttributeInfo data from descriptor.
     * May be null;
     *
     */
    public TagInfo(String tagName,
	    String tagClassName,
	    String bodycontent,
	    String infoString,
	    TagLibraryInfo taglib,
	    TagExtraInfo tagExtraInfo,
	    TagAttributeInfo[] attributeInfo) {
	this.tagName       = tagName;
	this.tagClassName  = tagClassName;
	this.bodyContent   = bodycontent;
	this.infoString    = infoString;
	this.tagLibrary    = taglib;
	this.tagExtraInfo  = tagExtraInfo;
	this.attributeInfo = attributeInfo;

	if (tagExtraInfo != null)
            tagExtraInfo.setTagInfo(this);
    }
			 
    /**
     * Constructor for TagInfo from data in the JSP 1.2 format for TLD.
     * This class is to be instantiated only from the TagLibrary code
     * under request from some JSP code that is parsing a
     * TLD (Tag Library Descriptor).
     *
     * Note that, since TagLibibraryInfo reflects both TLD information
     * and taglib directive information, a TagInfo instance is
     * dependent on a taglib directive.  This is probably a
     * design error, which may be fixed in the future.
     *
     * @param tagName The name of this tag
     * @param tagClassName The name of the tag handler class
     * @param bodycontent Information on the body content of these tags
     * @param infoString The (optional) string information for this tag
     * @param taglib The instance of the tag library that contains us.
     * @param tagExtraInfo The instance providing extra Tag info.  May be null
     * @param attributeInfo An array of AttributeInfo data from descriptor.
     * May be null;
     * @param displayName A short name to be displayed by tools
     * @param smallIcon Path to a small icon to be displayed by tools
     * @param largeIcon Path to a large icon to be displayed by tools
     * @param tagVariableInfo An array of a TagVariableInfo (or null)
     */
    public TagInfo(String tagName,
	    String tagClassName,
	    String bodycontent,
	    String infoString,
	    TagLibraryInfo taglib,
	    TagExtraInfo tagExtraInfo,
	    TagAttributeInfo[] attributeInfo,
	    String displayName,
	    String smallIcon,
	    String largeIcon,
	    TagVariableInfo[] tvi) {
	this.tagName       = tagName;
	this.tagClassName  = tagClassName;
	this.bodyContent   = bodycontent;
	this.infoString    = infoString;
	this.tagLibrary    = taglib;
	this.tagExtraInfo  = tagExtraInfo;
	this.attributeInfo = attributeInfo;
	this.displayName = displayName;
	this.smallIcon = smallIcon;
	this.largeIcon = largeIcon;
	this.tagVariableInfo = tvi;

	if (tagExtraInfo != null)
            tagExtraInfo.setTagInfo(this);
    }
			 
    /**
     * The name of the Tag.
     *
     * @return The (short) name of the tag.
     */

    public String getTagName() {
	return tagName;
    }

    /**
     * Attribute information (in the TLD) on this tag.
     * The return is an array describing the attributes of this tag, as
     * indicated in the TLD.
     * A null return means no attributes.
     *
     * @return The array of TagAttributeInfo for this tag.
     */

   public TagAttributeInfo[] getAttributes() {
       return attributeInfo;
   }

    /**
     * Information on the scripting objects created by this tag at runtime.
     * This is a convenience method on the associated TagExtraInfo class.
     * <p>
     * Default is null if the tag has no "id" attribute,
     * otherwise, {"id", Object}
     *
     * @param data TagData describing this action.
     * @return Array of VariableInfo elements.
     */

   public VariableInfo[] getVariableInfo(TagData data) {
       TagExtraInfo tei = getTagExtraInfo();
       if (tei == null) {
	   return null;
       }
       return tei.getVariableInfo(data);
   }

    /**
     * Translation-time validation of the attributes. 
     * This is a convenience method on the associated TagExtraInfo class.
     *
     * @param data The translation-time TagData instance.
     * @return Whether the data is valid.
     */


   public boolean isValid(TagData data) {
       TagExtraInfo tei = getTagExtraInfo();
       if (tei == null) {
	   return true;
       }
       return tei.isValid(data);
   }


    /**
     * Set the instance for extra tag information
     * 
     * @param tei the TagExtraInfo instance
     */
    public void setTagExtraInfo(TagExtraInfo tei) {
	tagExtraInfo = tei;
    }


    /**
     * The instance (if any) for extra tag information
     * 
     * @return The TagExtraInfo instance, if any.
     */
    public TagExtraInfo getTagExtraInfo() {
	return tagExtraInfo;
    }


    /**
     * Name of the class that provides the handler for this tag.
     *
     * @return The name of the tag handler class.
     */
    
    public String getTagClassName() {
	return tagClassName;
    }


    /**
     * The bodycontent information for this tag.
     *
     * @return the body content string.
     */

    public String getBodyContent() {
	return bodyContent;
    }


    /**
     * The information string for the tag.
     *
     * @return the info string
     */

    public String getInfoString() {
	return infoString;
    }


    /**
     * Set the TagLibraryInfo property.
     *
     * Note that a TagLibraryInfo element is dependent
     * not just on the TLD information but also on the
     * specific taglib instance used.  This means that
     * a fair amount of work needs to be done to construct
     * and initialize TagLib objects.
     *
     * If used carefully, this setter can be used to avoid having to
     * create new TagInfo elements for each taglib directive.
     *
     * @param tl the TagLibraryInfo to assign
     */

    public void setTagLibrary(TagLibraryInfo tl) {
	tagLibrary = tl;
    }

    /**
     * The instance of TabLibraryInfo we belong to.
     *
     * @return the tab library instance we belong to.
     */

    public TagLibraryInfo getTagLibrary() {
	return tagLibrary;
    }


    // ============== JSP 1.2 TLD Information ========


    /**
     * Get the displayName
     *
     * @return A short name to be displayed by tools
     */

    public String getDisplayName() {
	return displayName;
    }

    /**
     * Get the path to the small icon
     *
     * @return Path to a small icon to be displayed by tools
     */

    public String getSmallIcon() {
	return smallIcon;
    }

    /**
     * Get the path to the large icon
     *
     * @return Path to a large icon to be displayed by tools
     */

    public String getLargeIcon() {
	return largeIcon;
    }

    /**
     * Get TagVariableInfo objects associated with this TagInfo
     *
     * @return A TagVariableInfo object associated with this 
     */

    public TagVariableInfo[] getTagVariableInfos() {
	return tagVariableInfo;
    }

    // ============== Probably does not belong here =======

    /**
     * Stringify for debug purposes...
     */
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("name = "+tagName+" ");
        b.append("class = "+tagClassName+" ");
        b.append("body = "+bodyContent+" ");
        b.append("info = "+infoString+" ");
        b.append("attributes = {\n");
        for(int i = 0; i < attributeInfo.length; i++)
            b.append("\t"+attributeInfo[i].toString());
        b.append("\n}\n");
        return b.toString();
    }

    /*
     * private fields for 1.1 info
     */

    private String             tagName; // the name of the tag
    private String             tagClassName;
    private String             bodyContent;
    private String             infoString;
    private TagLibraryInfo     tagLibrary;
    private TagExtraInfo       tagExtraInfo; // instance of TagExtraInfo
    private TagAttributeInfo[] attributeInfo;

    /*
     * private fields for 1.2 info
     */
    private String             displayName;
    private String             smallIcon;
    private String             largeIcon;
    private TagVariableInfo[]    tagVariableInfo;
}
