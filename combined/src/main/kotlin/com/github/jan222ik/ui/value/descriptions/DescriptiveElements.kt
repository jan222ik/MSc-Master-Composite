package com.github.jan222ik.ui.value.descriptions

object DescriptiveElements {
    val isAbstract = PropertyViewDemoElement(
        title = "Is abstract:",
        tooltip = "If true, the Classifier does not provide a complete declaration and cna typically not be instantiated. " +
                "An abstract classifier is intended to be used by other classifiers e.g. as the target of a general metarelationships or generalization relationships."
    )
    val isActive = PropertyViewDemoElement(
        title = "Is active:",
        tooltip = "isActive: Determines whether an object specified by this class is active or not. If true, then the owning class is referred to as an active class." +
                " If false, then suh a class is referred to as a passive class."
    )
    val qualifiedName= PropertyViewDemoElement(
        title = "Qualified Name:",
        tooltip = """
            |Qualified Name: A name which allows the NamedElement to be identified within a hierarchy of nested Namespaces.
            |It is constructed from the containing namespaces starting at the root of the hierarchy and ending with the name of the NamedElement itself.
        """.trimMargin()
    )
    val label = PropertyViewDemoElement(
        title = "Label:",
        tooltip = "" // TODO
    )
    val uri = PropertyViewDemoElement(
        title = "URI:",
        tooltip = "Universal unique resource identifier" // TODO
    )
    val location = PropertyViewDemoElement(
        title = "Location:",
        tooltip = "Location of the imported package" // TODO
    )
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
    val diagramType = PropertyViewDemoElement(
        title = "Type:",
        tooltip = "type: Type of the diagram."
    )
    val diagramName = PropertyViewDemoElement(title = "Name:", tooltip = "name: The name of diagram")
}