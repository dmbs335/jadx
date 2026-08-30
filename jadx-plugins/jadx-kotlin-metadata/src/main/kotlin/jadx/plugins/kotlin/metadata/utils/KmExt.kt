package jadx.plugins.kotlin.metadata.utils

import kotlin.metadata.KmFunction
import kotlin.metadata.KmProperty
import kotlin.metadata.jvm.fieldSignature
import kotlin.metadata.jvm.getterSignature
import kotlin.metadata.jvm.signature

inline val KmFunction.shortId: String? get() = signature?.toString()

inline val KmProperty.shortId: String? get() = fieldSignature?.toString()

inline val KmProperty.getterShortId: String? get() = getterSignature?.toString()
