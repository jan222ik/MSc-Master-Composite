package com.github.jan222ik.ui.value.descriptions

object DescriptiveElements {
    val multiplicity = PropertyViewDemoElement(
        title = "Multiplicity",
        tooltip = "Multiplicity: is the active logical association when the cardinality of a class in relation to another is being depicted."
    )
    val name = PropertyViewDemoElement(title = "Name:", tooltip = "name: The name of the NamedElement")
    val isDerived = PropertyViewDemoElement(
        title = "Is derived:",
        tooltip = "isDerived: If isDerived is true, the value of the attribute is derived from information elsewhere. Specifies whether the property is derived, i.e., whether its values can be computed from other information."
    )
    val isReadOnly = PropertyViewDemoElement(
        title = "Is read only:",
        tooltip = "isReadOnly: States whether the feature's value may be modified by a client."
    )
    val isUnique = PropertyViewDemoElement(
        title = "Is unique:",
        tooltip = "isUnique: For a multivalued multiplicity, this attributes specifies whether the values in an instantiation of this element are unique."
    )
    val isOrdered = PropertyViewDemoElement(
        title = "Is ordered:",
        tooltip = "isOrdered: For a multivalued multiplicity, this attribute specifies whether the values in an instantiation of this element are sequentially ordered."
    )
    val isStatic = PropertyViewDemoElement(
        title = "Is static:",
        tooltip = "isStatic: Specifies whether this feature characterises individual instances classified by the classifier (false) or the classifier itself (true)"
    )
    val visibility = PropertyViewDemoElement(
        title = "Visibility:",
        tooltip = "visibility: Determines where the NamedElement appears within different Namespaces within the overall model, and its accessibility."
    )
    val type = PropertyViewDemoElement(
        title = "Type:",
        tooltip = "type: This information is derived from the result for this Operation. The type of the TypedElement."
    )
    val defaultValue = PropertyViewDemoElement(
        title = "Default Value:",
        tooltip = "defaultValue: A ValueSpecification that is evaluated to give a default value for the Property when an object of the owning Classifier is instantiated."
    )
    val aggregation = PropertyViewDemoElement(
        title = "Aggregation",
        tooltip = "aggregation: Specifies the kind of aggregation that applies to the Property"
    )
}